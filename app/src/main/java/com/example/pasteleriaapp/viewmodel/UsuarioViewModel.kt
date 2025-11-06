package com.example.pasteleriaapp.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.example.pasteleriaapp.model.UsuarioErrores
import com.example.pasteleriaapp.model.UsuarioUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class UsuarioViewModel: ViewModel() {

    private val _estado= MutableStateFlow(UsuarioUiState())
    val estado: StateFlow<UsuarioUiState> = _estado

    // In-memory list to store registered users
    private val _registeredUsers = mutableListOf<UsuarioUiState>()

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

    fun registrarUsuario() {
        if (estaValidadoElFormulario()) {
            _registeredUsers.add(_estado.value)
        }
    }

    fun authenticateUser(): Boolean {
        val loginAttempt = _estado.value
        val user = _registeredUsers.find { 
            it.correo == loginAttempt.correo && it.contrasena == loginAttempt.contrasena
        }
        return user != null
    }

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