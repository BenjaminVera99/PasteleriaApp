package com.example.pasteleriaapp.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["correo"], unique = true)] // Asegura que no haya correos repetidos
)
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val correo: String,
    val contrasena: String, // En una app real, esto debería estar encriptado
    val direccion: String,
    val profilePictureUri: String? = null
)