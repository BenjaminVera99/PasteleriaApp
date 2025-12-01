package com.example.pasteleriaapp.repository

import ApiService
import com.example.pasteleriaapp.data.UsuarioRepository
import com.example.pasteleriaapp.data.preferences.AuthTokenManager
import com.example.pasteleriaapp.model.InicioSesion
import com.example.pasteleriaapp.model.LoginResponse
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import retrofit2.Response
import kotlinx.coroutines.test.runTest

class UsuarioRepositoryTest : StringSpec({

    // Datos simulados
    val credenciales = InicioSesion("test@mail.com", "password123")
    val fakeLoginResponse = LoginResponse("test_jwt_token", "USER")

    // Mocks de las dependencias
    val mockApi = mockk<ApiService>()
    val mockAuthManager = mockk<AuthTokenManager>(relaxed = true)
    // Mock del DAO (necesario para el constructor, no para la lógica de este test)
    val mockUsuarioDao = mockk<com.example.pasteleriaapp.data.dao.UsuarioDao>(relaxed = true)

    // 1. Test de Login Exitoso y Guardado de Token
    "iniciarSesionRemoto debe guardar el token en AuthTokenManager tras éxito" {
        // 1. Configurar la respuesta exitosa de la API
        coEvery { mockApi.login(credenciales) } returns Response.success(fakeLoginResponse)

        // 2. Crear la instancia del Repository
        val repo = UsuarioRepository(
            usuarioDao = mockUsuarioDao,
            apiService = mockApi,
            authTokenManager = mockAuthManager
        )

        // 3. Ejecutar la función
        runTest {
            val result = repo.iniciarSesionRemoto(credenciales)

            // 4. Verificar que la llamada al guardado de token se haya realizado
            coVerify(exactly = 1) {
                mockAuthManager.saveAuthData(
                    token = fakeLoginResponse.token,
                    role = fakeLoginResponse.role,
                    email = credenciales.username // O el campo de email/username que uses
                )
            }

            // 5. Verificar que el resultado de la función sea éxito
            result.isSuccess shouldBe true
        }
    }
})