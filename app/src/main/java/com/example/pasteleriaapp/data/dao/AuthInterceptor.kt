package com.example.pasteleriaapp.data.dao

import com.example.pasteleriaapp.data.preferences.AuthTokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val authTokenManager: AuthTokenManager) : Interceptor {

    private val publicApiPaths = listOf(
        "/auth/login",
        "/auth/register",
        "/products",
        "/api/products"
    )

    override fun intercept(chain: Interceptor.Chain): Response {

        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath
        val isPublicPath = publicApiPaths.any { path.startsWith(it) }

        if (isPublicPath) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking {
            authTokenManager.authToken.first()
        }

        android.util.Log.d("AUTH_INTERCEPTOR", "Intentando token para ruta: ${originalRequest.url.encodedPath}")
        android.util.Log.d("AUTH_INTERCEPTOR", "Token Obtenido: $token")

        if (!token.isNullOrEmpty()) {
            val requestBuilder = originalRequest.newBuilder()
            requestBuilder.header("Authorization", "Bearer $token")
            val newRequest = requestBuilder.build()
            return chain.proceed(newRequest)
        }

        android.util.Log.w("AUTH_INTERCEPTOR", "Petición a ruta privada sin token: CONTINÚA SIN AUTH HEADER")
        return chain.proceed(originalRequest)
    }
}