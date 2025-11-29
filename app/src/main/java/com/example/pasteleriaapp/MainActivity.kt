package com.example.pasteleriaapp.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pasteleriaapp.data.AppDatabase
import com.example.pasteleriaapp.data.UsuarioRepository
import com.example.pasteleriaapp.data.dao.RetrofitInstance
import com.example.pasteleriaapp.data.preferences.AuthTokenManager // 🔑 Importación del Token Manager
import com.example.pasteleriaapp.model.RegistroData
import com.example.pasteleriaapp.model.Usuario
import com.example.pasteleriaapp.model.UsuarioErrores
import com.example.pasteleriaapp.model.UsuarioUiState
import com.example.pasteleriaapp.model.InicioSesion // 🔑 Importación de credenciales de Login
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UsuarioViewModel(application: Application): AndroidViewModel(application) {

    private val usuarioRepository: UsuarioRepository
    private val authTokenManager: AuthTokenManager // Instancia del Token Manager

    private val _estado= MutableStateFlow(UsuarioUiState())
    val estado: StateFlow<UsuarioUiState> = _estado

    init {
        val usuarioDao = AppDatabase.getDatabase(application).usuarioDao()
        val apiService = RetrofitInstance.api // Obtener la instancia de Retrofit

        // 🔑 Inicialización del Repositorio (DAO local y Servicio API)
        usuarioRepository = UsuarioRepository(usuarioDao, apiService)

        // 🔑 Inicialización del Token Manager (DataStore)
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
            correo = usuario.correo,
            direccion = usuario.direccion,
            profilePictureUri = usuario.profilePictureUri
            // Nota: No cargamos apellidos ni fechaNacimiento aquí si no se guardan en Room
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
                    fechaNac = convertirAFormatoISO(_estado.value.fechaNacimiento)
                )

                try {
                    val resultadoRemoto = usuarioRepository.registrarUsuarioRemoto(registroData)

                    if (resultadoRemoto.isSuccess) {
                        // ÉXITO API: Guardar usuario en la BD LOCAL (Room)
                        val nuevoUsuarioLocal = Usuario(
                            nombre = registroData.nombres,
                            correo = registroData.username,
                            contrasena = registroData.password,
                            direccion = _estado.value.direccion,
                            profilePictureUri = null
                        )

                        usuarioRepository.registrarUsuario(nuevoUsuarioLocal) // Guarda en Room

                        val usuarioRegistrado = usuarioRepository.findUserByEmail(nuevoUsuarioLocal.correo)
                        onResult(usuarioRegistrado)

                    } else {
                        // ERROR API (400, 500, etc.)
                        val mensajeError = resultadoRemoto.exceptionOrNull()?.message ?: "Error desconocido al registrar."
                        _estado.update { it.copy(errores = it.errores.copy(correo = mensajeError)) }
                        onResult(null)
                    }
                } catch (e: Exception) {
                    // Error de red
                    _estado.update { it.copy(errores = it.errores.copy(correo = "Error de conexión: ${e.message}")) }
                    onResult(null)
                }
            }
        } else {
            onResult(null)
        }
    }

    // --- Lógica de Autenticación (API REMOTA) ---

    // ⚠️ CAMBIO en la firma: onResult retorna el Usuario y el Token (String?)
    fun authenticateUser(onResult: (Usuario?, String?) -> Unit) {
        if (estaValidadoElLogin()) {
            viewModelScope.launch {
                val estadoActual = _estado.value
                val email = estadoActual.correo
                val contrasena = estadoActual.contrasena

                // 1. Intentar Autenticación Remota (API)
                val credencialesRemotas = InicioSesion(username = email, password = contrasena)

                try {
                    val resultadoRemoto = usuarioRepository.iniciarSesionRemoto(credencialesRemotas)

                    if (resultadoRemoto.isSuccess) {
                        val loginResponse = resultadoRemoto.getOrNull()!!
                        val token = loginResponse.token

                        // 2. Éxito Remoto: GUARDAR EL TOKEN y el ROL
                        authTokenManager.saveAuthData(token, loginResponse.role)

                        // 3. Gestionar usuario local
                        var usuarioAutenticado = usuarioRepository.findUserByEmail(email)

                        if (usuarioAutenticado == null) {
                            // Si el usuario existe en el backend pero no en Room, lo guardamos
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
                        // 4. Fallo Remoto: Mostrar el error de credenciales
                        val mensajeError = resultadoRemoto.exceptionOrNull()?.message ?: "Fallo al iniciar sesión."
                        _estado.update { it.copy(errores = it.errores.copy(correo = mensajeError)) }
                        onResult(null, null)
                    }
                } catch (e: Exception) {
                    // 5. Error de red/conexión
                    val mensajeError = e.message ?: "Error de conexión."
                    _estado.update { it.copy(errores = it.errores.copy(correo = mensajeError)) }
                    onResult(null, null)
                }
            }
        } else {
            onResult(null, null)
        }
    }

    // --- Lógica de Perfil ---

    fun updateProfilePicture(imageUri: Uri, onUserUpdated: (Usuario) -> Unit) {
        viewModelScope.launch {
            val user = usuarioRepository.findUserByEmail(_estado.value.correo)
            if (user != null) {
                val updatedUser = user.copy(profilePictureUri = imageUri.toString())
                usuarioRepository.updateUser(updatedUser)
                _estado.update { it.copy(profilePictureUri = imageUri.toString()) }
                onUserUpdated(updatedUser)
            }
        }
    }

    fun saveChanges(onUserUpdated: (Usuario) -> Unit) {
        viewModelScope.launch {
            val user = usuarioRepository.findUserByEmail(_estado.value.correo)
            if (user != null) {
                val updatedUser = user.copy(
                    nombre = _estado.value.nombre,
                    correo = _estado.value.correo,
                    direccion = _estado.value.direccion
                )
                usuarioRepository.updateUser(updatedUser)
                onUserUpdated(updatedUser)
            }
        }
    }

    fun deleteCurrentUser(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val userEmail = _estado.value.correo
            if (userEmail.isNotBlank()) {
                usuarioRepository.deleteUserByEmail(userEmail)
                // 🔑 También borramos los datos de autenticación local
                authTokenManager.clearAuthData()
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            // Borramos el token y reseteamos el estado de la UI
            authTokenManager.clearAuthData()
            _estado.update { UsuarioUiState() }
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
    /**
     * Convierte la fecha de entrada (ej: dd-mm-aaaa) al formato ISO (yyyy-MM-dd)
     * requerido por el backend de Spring Boot.
     */
    private fun convertirAFormatoISO(fechaDeEntrada: String): String {
        val partes = fechaDeEntrada.split("-")
        return if (partes.size == 3 && partes[2].length == 4) {
            "${partes[2]}-${partes[1].padStart(2, '0')}-${partes[0].padStart(2, '0')}"
        } else {
            fechaDeEntrada
        }
    }
}