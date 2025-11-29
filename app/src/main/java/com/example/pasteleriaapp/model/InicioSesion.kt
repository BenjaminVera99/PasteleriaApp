package com.example.pasteleriaapp.model

import kotlinx.serialization.Serializable

@Serializable
data class InicioSesion(
    val username: String,
    val password: String
)