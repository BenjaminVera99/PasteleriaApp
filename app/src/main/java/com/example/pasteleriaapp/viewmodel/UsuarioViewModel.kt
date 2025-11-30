package com.example.pasteleriaapp.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pasteleriaapp.data.AppDatabase
import com.example.pasteleriaapp.data.UsuarioRepository
import com.example.pasteleriaapp.data.dao.RetrofitInstance
import com.example.pasteleriaapp.data.preferences.AuthTokenManager
import com.example.pasteleriaapp.model.RegistroData
import com.example.pasteleriaapp.model.Usuario
import com.example.pasteleriaapp.model.UsuarioErrores
import com.example.pasteleriaapp.model.UsuarioUiState
import com.example.pasteleriaapp.model.InicioSesion
import com.example.pasteleriaapp.navigation.AppRoute // ⭐ Importar AppRoute
import com.example.pasteleriaapp.navigation.NavigationEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class UsuarioViewModel(application: Application): AndroidViewModel(application) {
// ☝️ ESTA ES LA ÚNICA LÍNEA QUE DEBE CONTENER 'class UsuarioViewModel' EN TU PROYECTO

    private val usuarioRepository: UsuarioRepository
    private val authTokenManager: AuthTokenManager

    private val _estado= MutableStateFlow(UsuarioUiState())
    val estado: StateFlow<UsuarioUiState> = _estado

    private val _navigationEvents = Channel<NavigationEvent>()
    val navigationEvents = _navigationEvents.receiveAsFlow()

    init {
        val usuarioDao = AppDatabase.getDatabase(application).usuarioDao()
        val apiService = RetrofitInstance.api

        usuarioRepository = UsuarioRepository(usuarioDao, apiService)
        authTokenManager = AuthTokenManager(application)
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

    fun onUserLoaded(usuario: Usuario) {
        _estado.update { it.copy(
            nombre = usuario.nombre,
            apellidos = usuario.apellidos ?: "",
            correo = usuario.correo,
            direccion = usuario.direccion ?: "",
            fechaNacimiento = usuario.fechaNacimiento ?: "",
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
                    fechaNac = convertirAFormatoISO(_estado.value.fechaNacimiento),
                    direccion = estado.value.direccion

                )

                try {
                    val resultadoRemoto = usuarioRepository.registrarUsuarioRemoto(registroData)

                    if (resultadoRemoto.isSuccess) {
                        val nuevoUsuarioLocal = Usuario(
                            nombre = registroData.nombres,
                            correo = registroData.username,
                            contrasena = registroData.password,
                            direccion = _estado.value.direccion,
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
                        var usuarioAutenticado = usuarioRepository.findUserByEmail(email)

                        if (usuarioAutenticado == null) {
                            usuarioAutenticado = Usuario(
                                nombre = "Usuario Autenticado",
                                correo = email,
                                contrasena = contrasena,
                                direccion = estadoActual.direccion,
                                profilePictureUri = null
                            )
                            usuarioRepository.registrarUsuario(usuarioAutenticado)
                        }

                        onUserLoaded(usuarioAutenticado)
                        onResult(usuarioAutenticado, token)

                    } else {
                        val mensajeError = resultadoRemoto.exceptionOrNull()?.message ?: "Fallo al iniciar sesión."
                        _estado.update { it.copy(errores = it.errores.copy(correo = mensajeError)) }
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
        // Necesitas el flujo de userEmail definido en AuthTokenManager
        return authTokenManager.userEmail.first()
    }

    // --- Lógica de Perfil y Logout ---

    fun updateProfilePicture(imageUri: Uri, onUserUpdated: (Usuario) -> Unit) { /* ... */ }
    fun saveChanges(onUserUpdated: (Usuario) -> Unit) { /* ... */ }

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
                    _estado.update { UsuarioUiState() } // Resetear el estado de la UI

                    // ⭐ CLAVE: EMITIR EL EVENTO DE NAVEGACIÓN A HOME (Modo Invitado) ⭐
                    _navigationEvents.send(
                        NavigationEvent.NavigateTo(
                            route = AppRoute.Home.route,
                            popUpTo = AppRoute.Home.route,
                            inclusive = true // Elimina todo el historial y te deja en Home
                        )
                    )

                    onResult(true, "Tu cuenta ha sido eliminada exitosamente.")
                } else {
                    // 4. FALLO: MOSTRAR MENSAJE DE ERROR DEL SERVIDOR
                    val mensajeError = resultadoRemoto.exceptionOrNull()?.message ?: "Fallo desconocido al eliminar la cuenta."
                    onResult(false, mensajeError)
                }
            } catch (e: Exception) {
                // 5. ERROR DE RED
                val mensajeError = "Error de red: No se pudo contactar al servidor para eliminar la cuenta."
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
    fun estaValidadoElFormulario(): Boolean{
        val formularioActual=_estado.value
        val errores= UsuarioErrores(
            nombre = if(formularioActual.nombre.isBlank()) "El campo es obligatorio" else null,
            apellidos = if(formularioActual.apellidos.isBlank()) "El campo es obligatorio" else null,
            correo = if(!Patterns.EMAIL_ADDRESS.matcher(formularioActual.correo).matches()) "El correo debe ser valido" else null,
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
    private fun convertirAFormatoISO(fechaDeEntrada: String): String {
        val partes = fechaDeEntrada.split("-")
        return if (partes.size == 3 && partes[2].length == 4) {
            "${partes[2]}-${partes[1].padStart(2, '0')}-${partes[0].padStart(2, '0')}"
        } else {
            fechaDeEntrada
        }
    }
}