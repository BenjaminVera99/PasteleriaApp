package com.example.pasteleriaapp.model

import kotlinx.serialization.Serializable

@Serializable
data class RegistroData(

    val username: String,

    val password: String,

    val nombres: String,

    val apellidos: String,

    val fechaNac: String,

    val direccion: String
)