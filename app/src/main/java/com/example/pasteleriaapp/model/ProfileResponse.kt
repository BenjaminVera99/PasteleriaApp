package com.example.pasteleriaapp.model

import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponse(
    val username: String,
    val nombres: String,
    val apellidos: String = "",
    val fechaNac: String,
    val direccion: String = "",
    val role: String,
)