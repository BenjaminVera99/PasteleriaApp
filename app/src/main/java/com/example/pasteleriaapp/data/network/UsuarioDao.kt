package com.example.pasteleriaapp.data.network

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.pasteleriaapp.model.Usuario

@Dao
interface UsuarioDao {
    @Insert
    suspend fun insert(usuario: Usuario)

    @Query("SELECT * FROM users WHERE correo = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): Usuario?
}