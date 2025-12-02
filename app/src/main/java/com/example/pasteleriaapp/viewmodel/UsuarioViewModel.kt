package com.example.pasteleriaapp.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pasteleriaapp.data.AppDatabase
import com.example.pasteleriaapp.data.UsuarioRepository
import com.example.pasteleriaapp.data.dao.RetrofitInstance
import com.example.pasteleriaapp.data.dao.UpdateData
import com.example.pasteleriaapp.data.preferences.AuthTokenManager
import com.example.pasteleriaapp.model.RegistroData
import com.example.pasteleriaapp.model.Usuario
import com.example.pasteleriaapp.model.UsuarioErrores
import com.example.pasteleriaapp.model.UsuarioUiState
import com.example.pasteleriaapp.model.InicioSesion
import com.example.pasteleriaapp.navigation.AppRoute
import com.example.pasteleriaapp.navigation.NavigationEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class UsuarioViewModel(application: Application): AndroidViewModel(application) {

    private val usuarioRepository: UsuarioRepository
    private val authTokenManager: AuthTokenManager

    private val _estado= MutableStateFlow(UsuarioUiState())
    val estado: StateFlow<UsuarioUiState> = _estado

    private val _navigationEvents = Channel<NavigationEvent>()
    val navigationEvents = _navigationEvents.receiveAsFlow()

    init {
        val usuarioDao = AppDatabase.getDatabase(application).usuarioDao()
        val apiService = RetrofitInstance.api

        authTokenManager = AuthTokenManager(application)
        usuarioRepository = UsuarioRepository(usuarioDao, apiService, authTokenManager)
    }

    // --- Actualizadores de estado para los campos del formulario ---
    fun onNombreChange(nuevoNombre:String){
        _estado.update { it.copy(nombre = nuevoNombre, errores = it.errores.copy(nombre=null)) }
    }
    fun onApellidosChange(nuevosApellidos:String){
        _estado.update { it.copy(apellidos = nuevosApellidos, errores = it.errores.copy(apellidos=null)) }
    }
    fun onCorreoChange(nuevoCorreo:String){
        _estado.update { it.copy(correo = nuevoCorreo, errores = it.errores.copy(correo =null)) }
    }

    fun onContrasenaChange(nuevaContrasena:String){
        _estado.update { it.copy(contrasena = nuevaContrasena, errores = it.errores.copy(contrasena =null)) }
    }

    fun onFechaNacimientoChange(nuevaFechaNac:String){
        _estado.update { it.copy(fechaNacimiento = nuevaFechaNac, errores = it.errores.copy(fechaNacimiento =null)) }
    }

    fun onDireccionChange(nuevaDireccion:String){
        _estado.update { it.copy(direccion = nuevaDireccion, errores = it.errores.copy(direccion =null)) }
    }

    fun onAceptarTerminosChange(nuevoAceptarTerminos: Boolean){
        _estado.update { it.copy(aceptaTerminos = nuevoAceptarTerminos, errores = it.errores.copy(terminos = null)) }
    }

    fun togglePasswordVisibility() {
        _estado.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onUserLoaded(usuario: Usuario) {
        _estado.update { it.copy(
            nombre = usuario.nombre,
            apellidos = usuario.apellidos,
            correo = usuario.correo,
            direccion = usuario.direccion,
            fechaNacimiento = usuario.fechaNacimiento,
            profilePictureUri = usuario.profilePictureUri
        ) }
    }

    // --- Lógica de negocio de Registro (API REMOTA) ---

    fun registrarUsuario(onResult: (Usuario?) -> Unit) {
        if (estaValidadoElFormulario()) {
            viewModelScope.launch {

                val registroData = RegistroData(
                    username = _estado.value.correo,
                    password = _estado.value.contrasena,
                    nombres = _estado.value.nombre,
                    apellidos = _estado.value.apellidos,
                    fechaNac = convertirParaApi(_estado.value.fechaNacimiento),
                    direccion = estado.value.direccion

                )

                try {
                    val resultadoRemoto = usuarioRepository.registrarUsuarioRemoto(registroData)

                    if (resultadoRemoto.isSuccess) {
                        val nuevoUsuarioLocal = Usuario(
                            nombre = registroData.nombres,
                            apellidos = registroData.apellidos, // Asegurar que apellidos se guarda
                            correo = registroData.username,
                            contrasena = registroData.password,
                            direccion = _estado.value.direccion,
                            fechaNacimiento = _estado.value.fechaNacimiento, // Asegurar que fechaNac se guarda
                            profilePictureUri = null
                        )

                        usuarioRepository.registrarUsuario(nuevoUsuarioLocal)

                        val usuarioRegistrado = usuarioRepository.findUserByEmail(nuevoUsuarioLocal.correo)
                        onResult(usuarioRegistrado)

                    } else {
                        val mensajeError = resultadoRemoto.exceptionOrNull()?.message ?: "Error desconocido al registrar."
                        _estado.update { it.copy(errores = it.errores.copy(correo = mensajeError)) }
                        onResult(null)
                    }
                } catch (e: Exception) {
                    _estado.update { it.copy(errores = it.errores.copy(correo = "Error de conexión: ${e.message}")) }
                    onResult(null)
                }
            }
        } else {
            onResult(null)
        }
    }

    // --- Lógica de Autenticación (API REMOTA) ---

    fun authenticateUser(onResult: (Usuario?, String?) -> Unit) {
        if (estaValidadoElLogin()) {
            viewModelScope.launch {
                val estadoActual = _estado.value
                val email = estadoActual.correo
                val contrasena = estadoActual.contrasena

                val credencialesRemotas = InicioSesion(username = email, password = contrasena)

                try {
                    val resultadoRemoto = usuarioRepository.iniciarSesionRemoto(credencialesRemotas)

                    if (resultadoRemoto.isSuccess) {
                        val loginResponse = resultadoRemoto.getOrNull()!!
                        val token = loginResponse.token

                        authTokenManager.saveAuthData(token, loginResponse.role, email)

                        val profileResult = usuarioRepository.fetchProfileRemoto()

                        if (profileResult.isSuccess) {
                            val profile = profileResult.getOrNull()!!

                            val direccionAPI = profile.direccion.trim()

                            // 1. Crear usuarioBase con datos de API (Apellidos y Fecha Nacimiento ahora funcionan)
                            val usuarioBase = Usuario(
                                nombre = profile.nombres.trim(),
                                apellidos = profile.apellidos.trim(),
                                correo = profile.username.trim(),
                                contrasena = contrasena,
                                direccion = direccionAPI,
                                fechaNacimiento = convertirParaApi(profile.fechaNac).trim(),
                                profilePictureUri = null
                            )

                            val usuarioExistente = usuarioRepository.findUserByEmail(email)

                            val usuarioFinal = if (usuarioExistente == null) {
                                // 2. Si NO EXISTE EN ROOM (Inserción Post-Desinstalación)

                                val direccionInicial = if (estadoActual.direccion.isNotBlank()) {
                                    estadoActual.direccion
                                } else {
                                    direccionAPI
                                }

                                val usuarioParaInsertar = usuarioBase.copy(
                                    direccion = direccionInicial
                                )

                                usuarioRepository.registrarUsuario(usuarioParaInsertar)
                                usuarioParaInsertar
                            } else {
                                // 3. Si EXISTE EN ROOM (Preservación)

                                // ⭐ NUEVA LÓGICA: Preservamos el valor de Room SOLO si la API devuelve algo que no es útil. ⭐
                                // Si la API devuelve un valor válido (e.g., "sucasa"), lo usamos para asegurar la actualización.

                                val direccionFinal = if (direccionAPI.isBlank() || direccionAPI == "string") {
                                    // La API falló o dio un valor no deseado, mantenemos el valor local (ej. "sucasa" si ya estaba).
                                    usuarioExistente.direccion
                                } else {
                                    // La API devolvió un valor válido (es la fuente de verdad del servidor), lo usamos.
                                    direccionAPI
                                }

                                val usuarioParaGuardar = usuarioBase.copy(
                                    id = usuarioExistente.id,
                                    direccion = direccionFinal, // Usamos la dirección determinada por la lógica
                                    profilePictureUri = usuarioExistente.profilePictureUri ?: usuarioBase.profilePictureUri
                                )

                                usuarioRepository.updateUser(usuarioParaGuardar)
                                usuarioParaGuardar
                            }

                            onUserLoaded(usuarioFinal)
                            onResult(usuarioFinal, token)

                        } else {
                            val mensajeErrorPerfil = profileResult.exceptionOrNull()?.message ?: "Error al obtener datos del perfil."
                            _estado.update { it.copy(errores = it.errores.copy(correo = "Login exitoso, pero error al cargar datos: $mensajeErrorPerfil")) }
                            onResult(null, null)
                        }

                    } else {
                        // ... (Manejo de errores del login)
                        val exception = resultadoRemoto.exceptionOrNull()
                        val mensajeOriginal = exception?.message ?: "Fallo al iniciar sesión."
                        val mensajeFinal = if (mensajeOriginal == "FALLO_CREDENCIALES_INVALIDAS") {
                            "Correo electrónico o contraseña incorrectos."
                        } else {
                            mensajeOriginal
                        }
                        _estado.update { it.copy(errores = it.errores.copy(correo = mensajeFinal)) }
                        onResult(null, null)
                    }
                } catch (e: Exception) {
                    val mensajeError = e.message ?: "Error de conexión."
                    _estado.update { it.copy(errores = it.errores.copy(correo = mensajeError)) }
                    onResult(null, null)
                }
            }
        } else {
            onResult(null, null)
        }
    }


    // 🔑 FUNCIÓN PARA VERIFICAR AUTENTICACIÓN AL INICIO
    suspend fun checkAuthStatus(): String? {
        return authTokenManager.authToken.first()
    }

    suspend fun getSavedEmail(): String? {
        return authTokenManager.userEmail.first()
    }


    fun loadCurrentUserProfile() {
        viewModelScope.launch {
            val email = _estado.value.correo.trim()

            if (email.isNotBlank()) {
                val usuario = usuarioRepository.findUserByEmail(email)
                usuario?.let { onUserLoaded(it) }
            } else {
                val savedEmail = authTokenManager.userEmail.first()
                if (!savedEmail.isNullOrBlank()) {
                    val usuario = usuarioRepository.findUserByEmail(savedEmail)
                    usuario?.let { onUserLoaded(it) }
                }
            }
        }
    }

    // --- Lógica de Perfil y Logout ---

    fun updateProfilePicture(imageUri: Uri, onUserUpdated: (Usuario) -> Unit) {
        viewModelScope.launch {
            val email = _estado.value.correo
            val usuarioActual = usuarioRepository.findUserByEmail(email)

            if (usuarioActual != null) {
                // Crear una copia del usuario con la nueva URI de imagen
                val updatedUser = usuarioActual.copy(profilePictureUri = imageUri.toString())

                // 1. Guardar en la base de datos local
                usuarioRepository.updateUser(updatedUser)

                // 2. Actualizar el estado de la UI (para que se vea inmediatamente)
                onUserLoaded(updatedUser)

                // 3. Notificar al MainViewModel para actualizar el estado global
                onUserUpdated(updatedUser)
            }
        }
    }

    /**
     * Función para guardar los cambios de edición del perfil local y remotamente.
     */
    fun saveChanges(onUserUpdated: (Usuario) -> Unit) {
        // ⭐ Punto A: Click capturado. Se inicia la validación.
        Log.d("FLOW_DEBUG", "Punto A: saveChanges llamado. Validando perfil...")

        if (estaValidadoElPerfil()) {
            // ⭐ Punto B: Validación OK. Se inicia la corrutina.
            Log.d("FLOW_DEBUG", "Punto B: Validación OK. Iniciando viewModelScope.launch.")

            viewModelScope.launch {
                // ⭐ Punto C: Corrutina de API (launch) iniciada.
                Log.d("FLOW_DEBUG", "Punto C: Corrutina iniciada. Preparando datos...")

                val estadoActual = _estado.value
                val usuarioLocal = usuarioRepository.findUserByEmail(estadoActual.correo)

                if (usuarioLocal == null) {
                    Log.e("FLOW_DEBUG", "Punto C/Error: Usuario local no encontrado. Abortando PUT.")
                    return@launch
                }

                val newPasswordToSend = if (estadoActual.contrasena.isNotEmpty()) {
                    estadoActual.contrasena
                } else {
                    null
                }

                // Creamos el objeto de datos para la API
                // NOTA: profilePictureUri es String? en el DTO de Kotlin
                val updateData = UpdateData(
                    nombre = estadoActual.nombre,
                    apellidos = estadoActual.apellidos,
                    fechaNac = convertirParaApi(estadoActual.fechaNacimiento), // Asegurar formato YYYY-MM-DD
                    direccion = estadoActual.direccion,
                    // Incluimos profilePictureUri ya que está en el DTO de Kotlin
                    profilePictureUri = usuarioLocal.profilePictureUri,
                    contrasena = newPasswordToSend, // Puede ser null
                    correo = estadoActual.correo
                )

                try {
                    // ⭐ Punto D: LLAMADA A LA API REMOTA (OkHttp log debería aparecer ahora) ⭐
                    Log.d("FLOW_DEBUG", "Punto D: Llamando a actualizarUsuarioRemoto con DTO: $updateData")

                    val resultadoRemoto = usuarioRepository.actualizarUsuarioRemoto(updateData)

                    if (resultadoRemoto.isSuccess) {
                        // ⭐ Punto E: ÉXITO REMOTO ⭐
                        Log.d("FLOW_DEBUG", "Punto E: Actualización remota exitosa.")

                        // 2. ÉXITO REMOTO: Actualizar el usuario LOCAL
                        val usuarioActualizadoLocal = usuarioLocal.copy(
                            nombre = estadoActual.nombre,
                            apellidos = estadoActual.apellidos,
                            direccion = estadoActual.direccion,
                            fechaNacimiento = estadoActual.fechaNacimiento,
                            contrasena = newPasswordToSend ?: usuarioLocal.contrasena,
                            correo = estadoActual.correo
                        )
                        usuarioRepository.updateUser(usuarioActualizadoLocal)

                        // Limpiamos el campo de contraseña después de guardar
                        _estado.update { it.copy(contrasena = "") }

                        onUserUpdated(usuarioActualizadoLocal)
                    } else {
                        // ⭐ Punto E/Fallo: FALLO DE API (400, 401, 500) ⭐
                        val mensajeError = resultadoRemoto.exceptionOrNull()?.message ?: "Error desconocido al actualizar (Fallo de API)."
                        Log.e("FLOW_DEBUG", "Punto E/Fallo: Fallo en la respuesta de API: $mensajeError")
                        _estado.update { it.copy(errores = it.errores.copy(correo = mensajeError)) }
                    }
                } catch (e: Exception) {
                    // ⭐ Punto F: EXCEPCIÓN FATAL (Error de Red o Serialización) ⭐
                    val mensajeError = "Error de conexión o serialización al guardar cambios: ${e.message}"
                    Log.e("FLOW_DEBUG", "Punto F: EXCEPCIÓN FATAL: $mensajeError", e)
                    _estado.update { it.copy(errores = it.errores.copy(correo = mensajeError)) }
                }
            }
        } else {
            // ⭐ Punto B/Fallo: Validación FALLIDA
            Log.w("FLOW_DEBUG", "Punto B/Fallo: Validación del perfil fallida. No se llama a la API.")
        }
    }

    fun deleteCurrentUser(onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val userEmail = _estado.value.correo

            // 1. Verificación básica (si el email es nulo, no se puede eliminar)
            if (userEmail.isBlank()) {
                onResult(false, "El correo del usuario no está cargado. Inicia sesión primero.")
                return@launch
            }

            try {
                // ⭐ 2. LLAMADA A LA API REMOTA PARA ELIMINAR CUENTA ⭐
                val resultadoRemoto = usuarioRepository.deleteUserRemoto()

                if (resultadoRemoto.isSuccess) {
                    // 3. ÉXITO: ELIMINAR DATOS LOCALES Y LIMPIAR SESIÓN
                    usuarioRepository.deleteUserByEmail(userEmail)

                    // Limpiar la sesión
                    authTokenManager.clearAuthData()
                    _estado.update { UsuarioUiState() }

                    // ⭐ CLAVE: EMITIR EL EVENTO DE NAVEGACIÓN A HOME (Modo Invitado) ⭐
                    _navigationEvents.send(
                        NavigationEvent.NavigateTo(
                            route = AppRoute.Home.route,
                            popUpTo = AppRoute.Home.route,
                            inclusive = true
                        )
                    )

                    onResult(true, "Tu cuenta ha sido eliminada exitosamente.")
                } else {
                    // 4. FALLO: MOSTRAR MENSAJE DE ERROR DEL SERVIDOR
                    val mensajeError = resultadoRemoto.exceptionOrNull()?.message ?: "Fallo desconocido al eliminar la cuenta."
                    _estado.update { it.copy(errores = it.errores.copy(correo = mensajeError)) }
                    onResult(false, mensajeError)
                }
            } catch (e: Exception) {
                // 5. ERROR DE RED
                val mensajeError = "Error de red: No se pudo contactar al servidor para eliminar la cuenta."
                _estado.update { it.copy(errores = it.errores.copy(correo = mensajeError)) }
                onResult(false, mensajeError)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authTokenManager.clearAuthData()
            _estado.update { UsuarioUiState() }

            // ⭐ OPCIONAL: Si el logout también debe llevar a Home:
            _navigationEvents.send(
                NavigationEvent.NavigateTo(
                    route = AppRoute.Home.route,
                    popUpTo = AppRoute.Home.route,
                    inclusive = true
                )
            )
        }
    }


    // --- Validaciones ---

    fun estaValidadoElPerfil(): Boolean {
        val formularioActual = _estado.value

        // ⭐ VALIDACIÓN DE CONTRASEÑA CONDICIONAL ⭐
        val contrasenaError = if (formularioActual.contrasena.isNotEmpty() && formularioActual.contrasena.length < 6) {
            "La contraseña debe tener al menos 6 caracteres"
        } else {
            null
        }

        val errores = UsuarioErrores(
            nombre = if (formularioActual.nombre.isBlank()) "El campo es obligatorio" else null,
            apellidos = if (formularioActual.apellidos.isBlank()) "El campo es obligatorio" else null,
            correo = if (!Patterns.EMAIL_ADDRESS.matcher(formularioActual.correo).matches()) "El correo debe ser válido" else null,
            fechaNacimiento = if (formularioActual.fechaNacimiento.isBlank()) "El campo es obligatorio" else null,
            direccion = if (formularioActual.direccion.isBlank()) "El campo es obligatorio" else null,

            // Usamos la validación condicional
            contrasena = contrasenaError,
            terminos = null // Ignoramos términos
        )

        val hayErrores = listOfNotNull(
            errores.nombre,
            errores.apellidos,
            errores.correo,
            errores.fechaNacimiento,
            errores.direccion,
            errores.contrasena
        ).isNotEmpty()

        // Actualizar solo los errores relevantes en el estado
        _estado.update {
            it.copy(
                errores = it.errores.copy(
                    nombre = errores.nombre,
                    apellidos = errores.apellidos,
                    correo = errores.correo,
                    fechaNacimiento = errores.fechaNacimiento,
                    direccion = errores.direccion,
                    contrasena = errores.contrasena,
                    terminos = null
                )
            )
        }

        return !hayErrores
    }

    fun estaValidadoElFormulario(): Boolean{
        val formularioActual=_estado.value
        val errores= UsuarioErrores(
            nombre = if(formularioActual.nombre.isBlank()) "El campo es obligatorio" else null,
            apellidos = if(formularioActual.apellidos.isBlank()) "El campo es obligatorio" else null,
            correo = if(!Patterns.EMAIL_ADDRESS.matcher(formularioActual.correo).matches()) "El correo debe contener @" else null,
            contrasena= if(formularioActual.contrasena.length <6)"La contraseña debe tener al menos 6 caracteres" else null,
            fechaNacimiento = if(formularioActual.fechaNacimiento.isBlank()) "El campo es obligatorio" else null,
            direccion = if(formularioActual.direccion.isBlank()) "El campo es obligatorio" else null,
            terminos = if(!formularioActual.aceptaTerminos) "Debes aceptar los términos" else null
        )

        val hayErrores=listOfNotNull(
            errores.nombre,
            errores.apellidos,
            errores.correo,
            errores.contrasena,
            errores.fechaNacimiento,
            errores.direccion,
            errores.terminos
        ).isNotEmpty()

        _estado.update { it.copy(errores=errores) }

        return !hayErrores
    }

    fun estaValidadoElLogin(): Boolean {
        val formularioActual = _estado.value
        val errores = UsuarioErrores(
            correo = if (!Patterns.EMAIL_ADDRESS.matcher(formularioActual.correo).matches()) "El correo debe ser válido" else null,
            contrasena = if (formularioActual.contrasena.isBlank()) "El campo es obligatorio" else null
        )

        val hayErrores = listOfNotNull(
            errores.correo,
            errores.contrasena
        ).isNotEmpty()

        val erroresActualizados = _estado.value.errores.copy(
            correo = errores.correo,
            contrasena = errores.contrasena
        )

        _estado.update { it.copy(errores = erroresActualizados) }

        return !hayErrores
    }

    // --- FUNCIÓN DE UTILIDAD ---
    private fun convertirParaApi(fechaLocal: String): String {
        // Si la fecha coincide con el patrón YYYY-MM-DD (4º carácter es guion), entonces la invertimos.
        if (fechaLocal.length == 10 && fechaLocal[4] == '-') {
            return fechaLocal // Retorna YYYY-MM-DD
        }
        if (fechaLocal.length == 10 && fechaLocal[2] == '-') {
            // Asume formato DD-MM-YYYY, lo convierte a YYYY-MM-DD
            val partes = fechaLocal.split("-")
            return "${partes[2]}-${partes[1]}-${partes[0]}"
        }

        // Si no es el formato ISO (YYYY-MM-DD), lo devolvemos sin cambios (ej: 05-05-2020)
        return fechaLocal
    }
}