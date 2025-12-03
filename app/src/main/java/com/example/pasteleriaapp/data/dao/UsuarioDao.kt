package com.example.pasteleriaapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pasteleriaapp.model.Usuario

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.Companion.FAIL)
    suspend fun insert(usuario: Usuario)

    @Update
    suspend fun update(usuario: Usuario)

    @Query("SELECT * FROM users WHERE correo = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): Usuario?

    @Query("DELETE FROM users WHERE correo = :email")
    suspend fun deleteByEmail(email: String)
}