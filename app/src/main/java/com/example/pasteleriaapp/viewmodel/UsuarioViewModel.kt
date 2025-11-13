package com.example.pasteleriaapp.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pasteleriaapp.data.AppDatabase
import com.example.pasteleriaapp.data.UsuarioRepository
import com.example.pasteleriaapp.model.Usuario
import com.example.pasteleriaapp.model.UsuarioErrores
import com.example.pasteleriaapp.model.UsuarioUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UsuarioViewModel(application: Application): AndroidViewModel(application) {

    private val usuarioRepository: UsuarioRepository

    // Estado del formulario de usuario, tanto para registro como para login.
    private val _estado= MutableStateFlow(UsuarioUiState())
    val estado: StateFlow<UsuarioUiState> = _estado

    init {
        val usuarioDao = AppDatabase.getDatabase(application).usuarioDao()
        usuarioRepository = UsuarioRepository(usuarioDao)
    }

    // --- Actualizadores de estado para los campos del formulario ---
    fun onNombreChange(nuevoNombre:String){
        _estado.update { it.copy(nombre = nuevoNombre, errores = it.errores.copy(nombre=null)) }
    }
    fun onCorreoChange(nuevoCorreo:String){
        _estado.update { it.copy(correo = nuevoCorreo, errores = it.errores.copy(correo =null)) }
    }

    fun onContrasenaChange(nuevaContrasena:String){
        _estado.update { it.copy(contrasena = nuevaContrasena, errores = it.errores.copy(contrasena =null)) }
    }

    fun onDireccionChange(nuevaDireccion:String){
        _estado.update { it.copy(direccion = nuevaDireccion, errores = it.errores.copy(direccion =null)) }
    }

    fun onAceptarTerminosChange(nuevoAceptarTerminos: Boolean){
        _estado.update { it.copy(aceptaTerminos = nuevoAceptarTerminos) }
    }

    // --- Lógica de negocio ---

    // Guarda el estado actual como un nuevo usuario registrado en la base de datos.
    fun registrarUsuario() {
        if (estaValidadoElFormulario()) {
            viewModelScope.launch {
                val nuevoUsuario = Usuario(
                    nombre = _estado.value.nombre,
                    correo = _estado.value.correo,
                    contrasena = _estado.value.contrasena,
                    direccion = _estado.value.direccion
                )
                usuarioRepository.registrarUsuario(nuevoUsuario)
            }
        }
    }

    // Comprueba si el correo y contraseña actuales coinciden con algún usuario en la base de datos.
    fun authenticateUser(onResult: (Usuario?) -> Unit) {
        viewModelScope.launch {
            val loginAttempt = _estado.value
            val usuarioAutenticado = usuarioRepository.autenticarUsuario(loginAttempt.correo, loginAttempt.contrasena)
            onResult(usuarioAutenticado)
        }
    }

    // --- Validaciones ---

    // Validación completa para el formulario de registro.
    fun estaValidadoElFormulario(): Boolean{
        val formularioActual=_estado.value
        val errores= UsuarioErrores(
            nombre = if(formularioActual.nombre.isBlank()) "El campo es obligatorio" else null,
            correo = if(!Patterns.EMAIL_ADDRESS.matcher(formularioActual.correo).matches()) "El correo debe ser valido" else null,
            contrasena= if(formularioActual.contrasena.length <6)"La contraseña debe tener al menos 6 caracteres" else null,
            direccion = if(formularioActual.direccion.isBlank()) "El campo es obligatorio" else null,
        )

        val hayErrores=listOfNotNull(
            errores.nombre,
            errores.correo,
            errores.contrasena,
            errores.direccion
        ).isNotEmpty()

        _estado.update { it.copy(errores=errores) }

        return !hayErrores
    }

    // Validación específica para el formulario de login.
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
}