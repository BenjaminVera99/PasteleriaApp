
package com.example.pasteleriaapp.data.dao

import kotlinx.serialization.Serializable

@Serializable
data class UpdateResponse(
    // Los nombres deben coincidir con lo que devuelve el servidor
    val message: String,
    val nombre: String,
    val apellidos: String,
    val correo: String,
    val fechaNac: String,
    val direccion: String,
    val profilePictureUri: String?, // Puede ser cadena vacía o nulo
    val role: String
)