package com.example.pasteleriaapp.model

data class UsuarioErrores (

    val nombre: String?=null,
    val correo: String?=null,
    val contrasena: String?=null,
    val direccion: String? =null,
    val terminos: String? = null // Nuevo campo de error para los términos

)