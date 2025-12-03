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
                            apellidos = registroData.apellidos,
                            correo = registroData.username,
                            contrasena = registroData.password,
                            direccion = _estado.value.direccion,
                            fechaNacimiento = _estado.value.fechaNacimiento,
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


                                val direccionFinal = if (direccionAPI.isBlank() || direccionAPI == "string") {
                                    usuarioExistente.direccion
                                } else {
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


    fun updateProfilePicture(imageUri: Uri, onUserUpdated: (Usuario) -> Unit) {
        viewModelScope.launch {
            val email = _estado.value.correo
            val usuarioActual = usuarioRepository.findUserByEmail(email)

            if (usuarioActual != null) {
                val updatedUser = usuarioActual.copy(profilePictureUri = imageUri.toString())

                usuarioRepository.updateUser(updatedUser)

                onUserLoaded(updatedUser)

                onUserUpdated(updatedUser)
            }
        }
    }

    fun saveChanges(onUserUpdated: (Usuario) -> Unit) {
        Log.d("FLOW_DEBUG", "Punto A: saveChanges llamado. Validando perfil...")

        if (estaValidadoElPerfil()) {
            Log.d("FLOW_DEBUG", "Punto B: Validación OK. Iniciando viewModelScope.launch.")

            viewModelScope.launch {
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

                val updateData = UpdateData(
                    nombre = estadoActual.nombre,
                    apellidos = estadoActual.apellidos,
                    fechaNac = convertirParaApi(estadoActual.fechaNacimiento),
                    direccion = estadoActual.direccion,
                    profilePictureUri = usuarioLocal.profilePictureUri,
                    contrasena = newPasswordToSend,
                    correo = estadoActual.correo
                )

                try {
                    Log.d("FLOW_DEBUG", "Punto D: Llamando a actualizarUsuarioRemoto con DTO: $updateData")

                    val resultadoRemoto = usuarioRepository.actualizarUsuarioRemoto(updateData)

                    if (resultadoRemoto.isSuccess) {
                        Log.d("FLOW_DEBUG", "Punto E: Actualización remota exitosa.")

                        val usuarioActualizadoLocal = usuarioLocal.copy(
                            nombre = estadoActual.nombre,
                            apellidos = estadoActual.apellidos,
                            direccion = estadoActual.direccion,
                            fechaNacimiento = estadoActual.fechaNacimiento,
                            contrasena = newPasswordToSend ?: usuarioLocal.contrasena,
                            correo = estadoActual.correo
                        )
                        usuarioRepository.updateUser(usuarioActualizadoLocal)

                        _estado.update { it.copy(contrasena = "") }

                        onUserUpdated(usuarioActualizadoLocal)
                    } else {
                        val mensajeError = resultadoRemoto.exceptionOrNull()?.message ?: "Error desconocido al actualizar (Fallo de API)."
                        Log.e("FLOW_DEBUG", "Punto E/Fallo: Fallo en la respuesta de API: $mensajeError")
                        _estado.update { it.copy(errores = it.errores.copy(correo = mensajeError)) }
                    }
                } catch (e: Exception) {
                    val mensajeError = "Error de conexión o serialización al guardar cambios: ${e.message}"
                    Log.e("FLOW_DEBUG", "Punto F: EXCEPCIÓN FATAL: $mensajeError", e)
                    _estado.update { it.copy(errores = it.errores.copy(correo = mensajeError)) }
                }
            }
        } else {
            Log.w("FLOW_DEBUG", "Punto B/Fallo: Validación del perfil fallida. No se llama a la API.")
        }
    }

    fun deleteCurrentUser(onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val userEmail = _estado.value.correo

            if (userEmail.isBlank()) {
                onResult(false, "El correo del usuario no está cargado. Inicia sesión primero.")
                return@launch
            }

            try {
                val resultadoRemoto = usuarioRepository.deleteUserRemoto()

                if (resultadoRemoto.isSuccess) {
                    usuarioRepository.deleteUserByEmail(userEmail)

                    // Limpiar la sesión
                    authTokenManager.clearAuthData()
                    _estado.update { UsuarioUiState() }

                    _navigationEvents.send(
                        NavigationEvent.NavigateTo(
                            route = AppRoute.Home.route,
                            popUpTo = AppRoute.Home.route,
                            inclusive = true
                        )
                    )

                    onResult(true, "Tu cuenta ha sido eliminada exitosamente.")
                } else {
                    val mensajeError = resultadoRemoto.exceptionOrNull()?.message ?: "Fallo desconocido al eliminar la cuenta."
                    _estado.update { it.copy(errores = it.errores.copy(correo = mensajeError)) }
                    onResult(false, mensajeError)
                }
            } catch (e: Exception) {
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

            _navigationEvents.send(
                NavigationEvent.NavigateTo(
                    route = AppRoute.Home.route,
                    popUpTo = AppRoute.Home.route,
                    inclusive = true
                )
            )
        }
    }



    fun estaValidadoElPerfil(): Boolean {
        val formularioActual = _estado.value

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

            contrasena = contrasenaError,
            terminos = null
        )

        val hayErrores = listOfNotNull(
            errores.nombre,
            errores.apellidos,
            errores.correo,
            errores.fechaNacimiento,
            errores.direccion,
            errores.contrasena
        ).isNotEmpty()

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

    private fun convertirParaApi(fechaLocal: String): String {
        if (fechaLocal.length == 10 && fechaLocal[4] == '-') {
            return fechaLocal
        }
        if (fechaLocal.length == 10 && fechaLocal[2] == '-') {
            val partes = fechaLocal.split("-")
            return "${partes[2]}-${partes[1]}-${partes[0]}"
        }

        return fechaLocal
    }
}