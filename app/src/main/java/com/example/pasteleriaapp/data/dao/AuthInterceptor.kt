package com.example.pasteleriaapp.data.dao

import com.example.pasteleriaapp.data.preferences.AuthTokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val authTokenManager: AuthTokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // 1. Obtener el token de forma síncrona
        val token = runBlocking {
            authTokenManager.authToken.first()
        }

        // 2. Construir la solicitud original
        val originalRequest = chain.request()

        // 3. Modificar la solicitud solo si hay un token disponible
        val requestBuilder = originalRequest.newBuilder()

        if (!token.isNullOrEmpty()) {
            // Añadir el encabezado Authorization
            requestBuilder.header("Authorization", "Bearer $token")
        }

        // 4. Continuar con la solicitud modificada
        val newRequest = requestBuilder.build()
        return chain.proceed(newRequest)
    }
}