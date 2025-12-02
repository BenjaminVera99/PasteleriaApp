package com.example.pasteleriaapp.data.dao

import com.example.pasteleriaapp.data.preferences.AuthTokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val authTokenManager: AuthTokenManager) : Interceptor {

    // 🛑 Rutas que deben ser públicas y NO deben llevar el header de autenticación
    private val publicApiPaths = listOf(
        "/auth/login",
        "/auth/register",
        "/products",
        "/api/products"
        // ¡Asegúrate de que esta lista contenga el prefijo exacto de tu API de productos!
    )

    override fun intercept(chain: Interceptor.Chain): Response {

        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath // Obtiene la parte de la ruta, ej: "/api/products"

        // 1. Determinar si la ruta es pública
        // Usamos startsWith para cubrir rutas con IDs (ej: /api/products/123)
        val isPublicPath = publicApiPaths.any { path.startsWith(it) }

        // 2. Si la ruta es pública, procedemos con la petición original *sin* añadir el token.
        // Esto previene el error 403 por token inválido/ausente en rutas que no lo necesitan.
        if (isPublicPath) {
            return chain.proceed(originalRequest)
        }

        // 3. Si la ruta NO es pública (es privada o de usuario), intentamos añadir el token.
        val token = runBlocking {
            authTokenManager.authToken.first()
        }

        android.util.Log.d("AUTH_INTERCEPTOR", "Intentando token para ruta: ${originalRequest.url.encodedPath}")
        android.util.Log.d("AUTH_INTERCEPTOR", "Token Obtenido: $token")

        if (!token.isNullOrEmpty()) {
            val requestBuilder = originalRequest.newBuilder()
            // Añadir el encabezado Authorization
            requestBuilder.header("Authorization", "Bearer $token")
            val newRequest = requestBuilder.build()
            return chain.proceed(newRequest)
        }

        android.util.Log.w("AUTH_INTERCEPTOR", "Petición a ruta privada sin token: CONTINÚA SIN AUTH HEADER")
        // Si la ruta no es pública, pero no hay token, la petición continuará,
        // y el servidor (Spring Security) le enviará un 401 Unauthorized o 403 Forbidden,
        // lo cual es correcto para una ruta privada.
        return chain.proceed(originalRequest)
    }
}