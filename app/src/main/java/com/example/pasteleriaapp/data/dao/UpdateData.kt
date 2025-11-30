package com.example.pasteleriaapp.data.dao

import kotlinx.serialization.Serializable

@Serializable
data class UpdateData(
    val nombre: String,
    val apellidos: String,
    val fechaNac: String,
    val direccion: String,
    val profilePictureUri: String?, // Puede ser nulo
    val newPassword: String? = null
)