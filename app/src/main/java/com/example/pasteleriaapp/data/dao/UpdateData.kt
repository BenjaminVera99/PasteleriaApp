package com.example.pasteleriaapp.data.dao

import kotlinx.serialization.Serializable

@Serializable
data class UpdateData(
    val nombre: String,
    val apellidos: String,
    val correo: String,
    val contrasena: String? = null,
    val fechaNac: String,
    val direccion: String,
    val profilePictureUri: String?
)