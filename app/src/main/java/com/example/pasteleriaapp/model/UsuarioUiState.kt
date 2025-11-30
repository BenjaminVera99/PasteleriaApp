package com.example.pasteleriaapp.model

data class UsuarioUiState (
    val nombre: String = "",
    val apellidos: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val fechaNacimiento: String = "",
    val direccion: String = "",
    val profilePictureUri: String? = null,
    val isPasswordVisible: Boolean = false,
    val aceptaTerminos: Boolean = false,
    val errores: UsuarioErrores = UsuarioErrores()
)