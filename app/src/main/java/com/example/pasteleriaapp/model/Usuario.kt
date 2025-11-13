package com.example.pasteleriaapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val correo: String,
    val contrasena: String, // En una app real, esto debería estar encriptado
    val direccion: String,
    val profilePictureUri: String? = null // Nuevo campo para la URI de la foto de perfil
)