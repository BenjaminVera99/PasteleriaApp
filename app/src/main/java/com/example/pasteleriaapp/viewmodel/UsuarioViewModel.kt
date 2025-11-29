package com.example.pasteleriaapp.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pasteleriaapp.data.AppDatabase
import com.example.pasteleriaapp.data.UsuarioRepository
import com.example.pasteleriaapp.data.dao.RetrofitInstance // 👈 IMPORTACIÓN NECESARIA
import com.example.pasteleriaapp.model.InicioSesion
import com.example.pasteleriaapp.model.RegistroData // 👈 IMPORTACIÓN NECESARIA
import com.example.pasteleriaapp.model.Usuario
import com.example.pasteleriaapp.model.UsuarioErrores
import com.example.pasteleriaapp.model.UsuarioUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.HttpURLConnection
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class UsuarioViewModel(application: Application): AndroidViewModel(application) {

    private val usuarioRepository: UsuarioRepository

    private val _estado= MutableStateFlow(UsuarioUiState())
    val estado: StateFlow<UsuarioUiState> = _estado

    init {
        val usuarioDao = AppDatabase.getDatabase(application).usuarioDao()
        val apiService = RetrofitInstance.api // Obtener la instancia de Retrofit
        // 🔑 INYECCIÓN DE DEPENDENCIAS: Ahora pasamos DAO local y el Servicio API remoto.
        usuarioRepository = UsuarioRepository(usuarioDao, apiService)
    }

    // --- Actualizadores de estado para los campos del formulario ---
    fun onNombreChange(nuevoNombre:String){
        _estado.update { it.copy(nombre = nuevoNombre, errores = it.errores.copy(nombre=null)) }
    }
    fun onApellidosChange(nuevosApellidos:String){ // 👈 NUEVO MANEJADOR
        _estado.update { it.copy(apellidos = nuevosApellidos, errores = it.errores.copy(apellidos=null)) }
    }
    fun onCorreoChange(nuevoCorreo:String){
        _estado.update { it.copy(correo = nuevoCorreo, errores = it.errores.copy(correo =null)) }
    }

    fun onContrasenaChange(nuevaContrasena:String){
        _estado.update { it.copy(contrasena = nuevaContrasena, errores = it.errores.copy(contrasena =null)) }
    }

    fun onFechaNacimientoChange(nuevaFechaNac:String){ // 👈 NUEVO MANEJADOR
        _estado.update { it.copy(fechaNacimiento = nuevaFechaNac, errores = it.errores.copy(fechaNacimiento =null)) }
    }

    fun onDireccionChange(nuevaDireccion:String){
        _estado.update { it.copy(direccion = nuevaDireccion, errores = it.errores.copy(direccion =null)) }
    }

    fun onAceptarTerminosChange(nuevoAceptarTerminos: Boolean){
        _estado.update { it.copy(aceptaTerminos = nuevoAceptarTerminos, errores = it.errores.copy(terminos = null)) }
    }

    // ... (onUserLoaded function remains the same)

    // --- Lógica de negocio de Registro (MODIFICADA) ---

    fun registrarUsuario(onResult: (Usuario?) -> Unit) {
        if (estaValidadoElFormulario()) {
            viewModelScope.launch {

                // 1. Mapear el estado a los datos requeridos por la API (RegistroData)
                val registroData = RegistroData(
                    username = _estado.value.correo,
                    password = _estado.value.contrasena,
                    nombres = _estado.value.nombre,
                    apellidos = _estado.value.apellidos,
                    // 🔑 Mapear a formato ISO para Spring Boot
                    fechaNac = convertirAFormatoISO(_estado.value.fechaNacimiento)
                )

                try {
                    // 2. Llamar a la función de registro REMOTO (API de Spring Boot)
                    val resultadoRemoto = usuarioRepository.registrarUsuarioRemoto(registroData)

                    if (resultadoRemoto.isSuccess) {
                        // 3. ÉXITO: Guardar en la base de datos LOCAL (Room)
                        val nuevoUsuarioLocal = Usuario(
                            // ⚠️ ID se genera automáticamente en el modelo Usuario (Room)
                            nombre = registroData.nombres,
                            correo = registroData.username,
                            contrasena = registroData.password,
                            direccion = _estado.value.direccion,
                            profilePictureUri = null
                        )

                        usuarioRepository.registrarUsuario(nuevoUsuarioLocal) // Guarda en Room

                        val usuarioRegistrado = usuarioRepository.findUserByEmail(nuevoUsuarioLocal.correo)
                        onResult(usuarioRegistrado) // Notificar éxito

                    } else {
                        // 4. ERROR de API: Mostrar mensaje de error (400, 500, etc.)
                        val mensajeError = resultadoRemoto.exceptionOrNull()?.message ?: "Error desconocido al registrar."
                        _estado.update { it.copy(errores = it.errores.copy(correo = mensajeError)) }
                        onResult(null)
                    }
                } catch (e: Exception) {
                    // Error de red (Network Error)
                    _estado.update { it.copy(errores = it.errores.copy(correo = "Error de conexión: ${e.message}")) }
                    onResult(null)
                }
            }
        } else {
            onResult(null)
        }
    }

    // --- Lógica de Autenticación y Perfil (Sin Cambios) ---
    fun authenticateUser(onResult: (Usuario?, String?) -> Unit) {
        if (estaValidadoElLogin()) { // Reutiliza la validación de login existente
            viewModelScope.launch {
                val estadoActual = _estado.value
                val email = estadoActual.correo
                val contrasena = estadoActual.contrasena

                // 1. Intentar Autenticación Remota (API de Spring Boot)
                val credencialesRemotas = InicioSesion(username = email, password = contrasena)

                try {
                    val resultadoRemoto = usuarioRepository.iniciarSesionRemoto(credencialesRemotas)

                    if (resultadoRemoto.isSuccess) {
                        val loginResponse = resultadoRemoto.getOrNull()!!
                        val token = loginResponse.token

                        // 2. Éxito Remoto: Gestionar el usuario local y el token

                        // Buscar si el usuario ya existe en la BD LOCAL (Room)
                        var usuarioAutenticado = usuarioRepository.findUserByEmail(email)

                        if (usuarioAutenticado == null) {
                            // Si el usuario no existe localmente, lo creamos y lo guardamos en Room.
                            // NOTA: Idealmente, usarías otro endpoint (`/auth/me`) para obtener los datos completos.
                            usuarioAutenticado = Usuario(
                                // Generamos un modelo local usando la información disponible
                                nombre = "Usuario Registrado", // Placeholder
                                correo = email,
                                contrasena = contrasena, // Guardamos la contraseña (aunque no se recomienda)
                                direccion = estadoActual.direccion,
                                profilePictureUri = null
                            )
                            usuarioRepository.registrarUsuario(usuarioAutenticado) // Guarda en Room
                        }

                        // 3. Cargar la información y notificar el éxito con el token
                        onUserLoaded(usuarioAutenticado)
                        onResult(usuarioAutenticado, token)

                        // ⚠️ Aquí es donde DEBES guardar el 'token' y 'role' en DataStore/SharedPreferences
                        // ... guardarToken(token, loginResponse.role) ...

                    } else {
                        // 4. Fallo Remoto: Mostrar el error de credenciales (401, 403, etc.)
                        val mensajeError = resultadoRemoto.exceptionOrNull()?.message ?: "Fallo al iniciar sesión."
                        _estado.update { it.copy(errores = it.errores.copy(correo = mensajeError)) }
                        onResult(null, null)
                    }
                } catch (e: Exception) {
                    // 5. Error de red/conexión (no se pudo contactar al servidor)
                    val mensajeError = e.message ?: "Error de conexión."
                    _estado.update { it.copy(errores = it.errores.copy(correo = mensajeError)) }
                    onResult(null, null)
                }
            }
        } else {
            onResult(null, null)
        }
    }
    fun updateProfilePicture(imageUri: Uri, onUserUpdated: (Usuario) -> Unit) { /* ... */ }
    fun saveChanges(onUserUpdated: (Usuario) -> Unit) { /* ... */ }
    fun deleteCurrentUser(onResult: (Boolean) -> Unit) { /* ... */ }

    // --- Validaciones (MODIFICADAS) ---
    fun estaValidadoElFormulario(): Boolean{
        val formularioActual=_estado.value
        val errores= UsuarioErrores(
            nombre = if(formularioActual.nombre.isBlank()) "El campo es obligatorio" else null,
            apellidos = if(formularioActual.apellidos.isBlank()) "El campo es obligatorio" else null, // 👈 VALIDACIÓN AÑADIDA
            correo = if(!Patterns.EMAIL_ADDRESS.matcher(formularioActual.correo).matches()) "El correo debe ser valido" else null,
            contrasena= if(formularioActual.contrasena.length <6)"La contraseña debe tener al menos 6 caracteres" else null,
            fechaNacimiento = if(formularioActual.fechaNacimiento.isBlank()) "El campo es obligatorio" else null, // 👈 VALIDACIÓN AÑADIDA
            direccion = if(formularioActual.direccion.isBlank()) "El campo es obligatorio" else null,
            terminos = if(!formularioActual.aceptaTerminos) "Debes aceptar los términos" else null
        )

        val hayErrores=listOfNotNull(
            errores.nombre,
            errores.apellidos, // Incluir error de apellidos
            errores.correo,
            errores.contrasena,
            errores.fechaNacimiento, // Incluir error de fecha
            errores.direccion,
            errores.terminos
        ).isNotEmpty()

        _estado.update { it.copy(errores=errores) }

        return !hayErrores
    }

    // ... (estaValidadoElLogin function remains the same)

    // --- FUNCIÓN DE UTILIDAD ---
    /**
     * Convierte la fecha de entrada (ej: dd-mm-aaaa) al formato ISO (yyyy-MM-dd)
     * requerido por el backend de Spring Boot.
     */
    private fun convertirAFormatoISO(fechaDeEntrada: String): String {
        // Asume que la entrada es dd-mm-aaaa (como sugiere la imagen del formulario web)
        val partes = fechaDeEntrada.split("-")
        return if (partes.size == 3 && partes[2].length == 4) {
            // Reordena a YYYY-MM-DD
            "${partes[2]}-${partes[1].padStart(2, '0')}-${partes[0].padStart(2, '0')}"
        } else {
            // Si no tiene el formato esperado, Spring lo manejará como un error 400
            fechaDeEntrada
        }
    }
}