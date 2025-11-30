package com.example.pasteleriaapp.data

import ApiService
import com.example.pasteleriaapp.data.dao.UpdateData
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


    suspend fun deleteUserRemoto(): Result<com.example.pasteleriaapp.model.MensajeRespuesta> {
        return try {
            val response = apiService.deleteUser()

            if (response.isSuccessful) {
                // ⭐ LÓGICA CORREGIDA PARA MANEJAR RESPUESTAS 2XX ⭐

                if (response.code() == 204 || response.body() == null) {
                    // 1. Éxito forzado: Si es 204 (No Content) o 200 con cuerpo nulo,
                    // asumimos que la operación fue un éxito total. Esto evita la excepción de deserialización.
                    Result.success(com.example.pasteleriaapp.model.MensajeRespuesta("Cuenta eliminada exitosamente."))
                } else {
                    // 2. Éxito con cuerpo: El servidor devolvió 200 con un cuerpo JSON válido (MensajeRespuesta)
                    // (Ej: {"message": "Usuario eliminado..."})
                    Result.success(response.body()!!)
                }
            } else {
                // 3. Manejo de errores HTTP (4xx o 5xx)
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Error ${response.code()}: ${errorBody ?: "Fallo al eliminar"}"))
            }
        } catch (e: Exception) {
            // ⭐ LÓGICA LIMPIA: Solo capturamos errores de red o excepciones inesperadas ⭐

            // Si es IOException, es un error de red real.
            if (e is IOException) {
                Result.failure(Exception("Error de red: No se pudo contactar al servidor."))
            } else {
                // Para cualquier otra excepción (como la de deserialización si el servidor mintió
                // sobre el 204 y devolvió 200 con cuerpo vacío), devolvemos un fallo general
                // para que el ViewModel decida si continuar o no.
                // Ya que confirmaste que la cuenta se borra, el punto 1 ya resolvió este caso.
                Result.failure(Exception("Error inesperado en el procesamiento de la respuesta: ${e.message}"))
            }
        }
    }

    suspend fun deleteUserByEmail(email: String) {
        usuarioDao.deleteByEmail(email)
    }

    suspend fun updateUserRemoto(usuario: Usuario): Result<Unit> {
        // 1. Mapear el objeto Usuario a UpdateData DTO
        val updateData = UpdateData(
            nombre = usuario.nombre,
            apellidos = usuario.apellidos
                ?: "", // Usar operador Elvis si el campo es nullable en Usuario
            correo = usuario.correo,
            contrasena = usuario.contrasena, // Enviar la contraseña actual (o un campo vacío si no se cambió)
            fechaNac = usuario.fechaNacimiento ?: "",
            direccion = usuario.direccion ?: "",
            profilePictureUri = usuario.profilePictureUri
        )

        return try {
            // 2. Llama al método updateProfile del ApiService
            val response = apiService.updateProfile(updateData)

            if (response.isSuccessful) {
                // Éxito (código 2xx): El servidor ha actualizado los datos
                Result.success(Unit)
            } else {
                // Error HTTP (4xx o 5xx)
                val errorBody = response.errorBody()?.string()

                // Intentamos obtener un mensaje de error claro
                val mensajeError = try {
                    val errorMap = response.body() // El body es Map<String, Any>
                    errorMap?.get("message") as? String ?: errorBody ?: "Fallo desconocido al actualizar."
                } catch (e: Exception) {
                    errorBody ?: "Fallo desconocido al actualizar."
                }

                Result.failure(Exception("Error ${response.code()}: $mensajeError"))
            }
        } catch (e: IOException) {
            // Error de Conexión (Network Error)
            Result.failure(Exception("Error de red: No se pudo conectar con el servidor para actualizar el perfil."))
        } catch (e: Exception) {
            // Error inesperado
            Result.failure(e)
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

}