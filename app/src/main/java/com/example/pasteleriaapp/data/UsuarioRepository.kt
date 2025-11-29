package com.example.pasteleriaapp.data

import ApiService
import com.example.pasteleriaapp.data.dao.UsuarioDao
import com.example.pasteleriaapp.model.InicioSesion
import com.example.pasteleriaapp.model.LoginResponse
import com.example.pasteleriaapp.model.RegistroData
import com.example.pasteleriaapp.model.Usuario
import java.io.IOException

class UsuarioRepository(private val usuarioDao: UsuarioDao,
                        private val apiService: ApiService) {

    suspend fun registrarUsuario(usuario: Usuario) {
        usuarioDao.insert(usuario)
    }

    suspend fun registrarUsuarioRemoto(registroData: RegistroData): Result<String> {
        return try {
            val response = apiService.register(registroData)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                val errorBody = response.errorBody()?.string()
                // El error 400 es común para usuario ya existente o datos inválidos
                Result.failure(Exception("Error ${response.code()}: ${errorBody ?: "Datos inválidos"}"))
            }
        } catch (e: IOException) {
            // Error de Conexión (Network Error)
            Result.failure(Exception("Error de red: No se pudo conectar con el servidor."))
        } catch (e: Exception) {
            // Error de JSON o inesperado
            Result.failure(e)
        }
    }

    suspend fun iniciarSesionRemoto(credenciales: InicioSesion): Result<LoginResponse> {
        return try {
            // Llama a la función que añadimos en ApiService.kt
            val response = apiService.login(credenciales)

            if (response.isSuccessful && response.body() != null) {
                // Éxito: Retorna el token y el rol
                Result.success(response.body()!!)
            } else {
                // Error HTTP (Ej: 401 Unauthorized, 400 Bad Request)
                val errorBody = response.errorBody()?.string()

                // Lanza una excepción con un mensaje más claro
                val errorMessage = if (response.code() == 401) {
                    "Credenciales incorrectas o usuario no encontrado."
                } else {
                    "Error ${response.code()}: ${errorBody ?: "Error en el servidor de autenticación."}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            // Error de red/conexión
            Result.failure(Exception("Error de red: No se pudo conectar con la API de autenticación."))
        }
    }



    suspend fun autenticarUsuario(email: String, contrasena: String): Usuario? {
        val usuario = usuarioDao.getUserByEmail(email)
        if (usuario != null && usuario.contrasena == contrasena) {
            return usuario
        }
        return null
    }

    suspend fun findUserByEmail(email: String): Usuario? {
        return usuarioDao.getUserByEmail(email)
    }

    suspend fun updateUser(usuario: Usuario) {
        usuarioDao.update(usuario)
    }

    suspend fun deleteUserByEmail(email: String) {
        usuarioDao.deleteByEmail(email)
    }
}