package com.example.pasteleriaapp.data.dao

import kotlinx.serialization.Serializable

@Serializable
data class UpdateData(
    val nombre: String,
    val apellidos: String,
    val correo: String, // Mapea a 'username' en el backend
    val contrasena: String, // Mapea a 'password' en el backend (se hashea si no está vacío)
    val fechaNac: String,
    val direccion: String,
    val profilePictureUri: String? // Puede ser nulo
)