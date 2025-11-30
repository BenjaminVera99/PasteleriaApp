package com.example.pasteleriaapp.data

import ApiService
import com.example.pasteleriaapp.data.dao.UpdateData
import com.example.pasteleriaapp.data.dao.UsuarioDao
import com.example.pasteleriaapp.model.InicioSesion
import com.example.pasteleriaapp.model.LoginResponse
import com.example.pasteleriaapp.model.MensajeRespuesta // ⭐ Importe agregado
import com.example.pasteleriaapp.model.RegistroData
import com.example.pasteleriaapp.model.Usuario
import java.io.IOException

class UsuarioRepository(private val usuarioDao: UsuarioDao,
                        private val apiService: ApiService) {

    // --- Operaciones Locales ---

    suspend fun registrarUsuario(usuario: Usuario) {
        usuarioDao.insert(usuario)
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

    suspend fun autenticarUsuario(email: String, contrasena: String): Usuario? {
        val usuario = usuarioDao.getUserByEmail(email)
        if (usuario != null && usuario.contrasena == contrasena) {
            return usuario
        }
        return null
    }

    // --- Operaciones Remotas de Autenticación y Registro ---

    suspend fun registrarUsuarioRemoto(registroData: RegistroData): Result<String> {
        return try {
            val response = apiService.register(registroData)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Error ${response.code()}: ${errorBody ?: "Datos inválidos"}"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Error de red: No se pudo conectar con el servidor."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun iniciarSesionRemoto(credenciales: InicioSesion): Result<LoginResponse> {
        return try {
            val response = apiService.login(credenciales)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()

                val errorMessage = if (response.code() == 401) {
                    "Credenciales incorrectas o usuario no encontrado."
                } else {
                    "Error ${response.code()}: ${errorBody ?: "Error en el servidor de autenticación."}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red: No se pudo conectar con la API de autenticación."))
        }
    }

    // --- Operaciones Remotas de Perfil y Eliminación ---

    /**
     * ⭐ FUNCIÓN REQUERIDA POR EL VIEWMODEL: Actualiza el perfil y la contraseña condicionalmente.
     * Recibe el DTO UpdateData que incluye el campo newPassword.
     */
    suspend fun actualizarUsuarioRemoto(updateData: UpdateData): Result<Unit> {
        return try {
            val response = apiService.updateProfile(updateData)

            if (response.isSuccessful) {
                // Éxito (código 2xx): El servidor ha actualizado los datos.
                Result.success(Unit)
            } else {
                // Error HTTP (4xx o 5xx)
                val errorBody = response.errorBody()?.string()
                val mensajeError = "Error ${response.code()}: ${errorBody ?: "Fallo al actualizar el perfil"}"
                Result.failure(Exception(mensajeError))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Error de red: No se pudo conectar con el servidor para actualizar el perfil."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUserRemoto(): Result<MensajeRespuesta> {
        return try {
            val response = apiService.deleteUser()

            if (response.isSuccessful) {
                if (response.code() == 204 || response.body() == null) {
                    Result.success(MensajeRespuesta("Cuenta eliminada exitosamente."))
                } else {
                    Result.success(response.body()!!)
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Error ${response.code()}: ${errorBody ?: "Fallo al eliminar"}"))
            }
        } catch (e: Exception) {
            if (e is IOException) {
                Result.failure(Exception("Error de red: No se pudo contactar al servidor."))
            } else {
                Result.failure(Exception("Error inesperado en el procesamiento de la respuesta: ${e.message}"))
            }
        }
    }
}