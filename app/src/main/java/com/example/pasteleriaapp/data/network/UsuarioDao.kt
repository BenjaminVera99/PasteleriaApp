package com.example.pasteleriaapp.data.network

import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Insert
import com.example.pasteleriaapp.model.Usuario

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.FAIL) // Falla si el email ya existe
    suspend fun insert(usuario: Usuario)

    @Update
    suspend fun update(usuario: Usuario)

    @Query("SELECT * FROM users WHERE correo = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): Usuario?

    @Query("DELETE FROM users WHERE correo = :email")
    suspend fun deleteByEmail(email: String)
}