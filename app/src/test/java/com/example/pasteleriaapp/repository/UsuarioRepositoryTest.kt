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

    val credenciales = InicioSesion("test@mail.com", "password123")
    val fakeLoginResponse = LoginResponse("test_jwt_token", "USER")

    val mockApi = mockk<ApiService>()
    val mockAuthManager = mockk<AuthTokenManager>(relaxed = true)
    val mockUsuarioDao = mockk<com.example.pasteleriaapp.data.dao.UsuarioDao>(relaxed = true)

    "iniciarSesionRemoto debe guardar el token en AuthTokenManager tras éxito" {
        coEvery { mockApi.login(credenciales) } returns Response.success(fakeLoginResponse)

        val repo = UsuarioRepository(
            usuarioDao = mockUsuarioDao,
            apiService = mockApi,
            authTokenManager = mockAuthManager
        )

        runTest {
            val result = repo.iniciarSesionRemoto(credenciales)

            coVerify(exactly = 1) {
                mockAuthManager.saveAuthData(
                    token = fakeLoginResponse.token,
                    role = fakeLoginResponse.role,
                    email = credenciales.username
                )
            }

            result.isSuccess shouldBe true
        }
    }
})