
package com.example.pasteleriaapp.data.dao

import kotlinx.serialization.Serializable

@Serializable
data class UpdateResponse(
    val message: String,
    val nombre: String,
    val apellidos: String,
    val correo: String,
    val fechaNac: String,
    val direccion: String,
    val profilePictureUri: String?,
    val role: String
)