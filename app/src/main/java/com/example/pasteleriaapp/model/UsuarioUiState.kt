package com.example.pasteleriaapp.model

data class UsuarioUiState (

    val nombre: String="",
    val correo: String="",
    val contrasena: String="",
    val direccion: String="",
    val aceptaTerminos: Boolean=false,
    val errores: UsuarioErrores= UsuarioErrores()

)