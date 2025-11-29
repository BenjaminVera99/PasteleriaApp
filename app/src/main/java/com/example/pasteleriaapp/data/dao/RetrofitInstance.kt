package com.example.pasteleriaapp.data.dao

import ApiService
import android.content.Context
import com.example.pasteleriaapp.data.preferences.AuthTokenManager
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object RetrofitInstance {

    // 🔑 Verifica tu URL. Si el endpoint es /auth/login, la BASE_URL debe terminar en /
    private const val BASE_URL = "http://192.168.0.10:9090/"

    // 🔑 Propiedades lateinit para inicialización dinámica (Necesario para el Context)
    private lateinit var applicationContext: Context
    private lateinit var retrofitInstance: Retrofit

    // 🔑 Instancia perezosa (lazy) de tu ApiService
    val api: ApiService by lazy {
        if (!::retrofitInstance.isInitialized) {
            throw IllegalStateException("RetrofitInstance no ha sido inicializado. Llama a initialize(context) primero.")
        }
        retrofitInstance.create(ApiService::class.java)
    }

    /**
     * Inicializa Retrofit y configura el OkHttpClient con el Interceptor de Autenticación.
     */
    fun initialize(context: Context) {
        if (::retrofitInstance.isInitialized) return // Evitar doble inicialización

        applicationContext = context.applicationContext

        // 1. Instancia del Token Manager
        val authTokenManager = AuthTokenManager(applicationContext)

        // 2. Interceptor para inyectar el token (Authorization: Bearer <token>)
        val authInterceptor = AuthInterceptor(authTokenManager)

        // 3. Interceptor de Logging (Para ver peticiones y respuestas en Logcat)
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Nivel recomendado para debug
        }

        // 4. Crear el Cliente OkHttp con los Interceptores
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)  // 🛡️ Agrega el token de autenticación
            .addInterceptor(loggingInterceptor) // 🐛 Agrega el logging
            .build()

        // 5. Configuración del serializador JSON (Kotlinx Serialization)
        val json = Json {
            ignoreUnknownKeys = true // Ignora campos que no estén en tus modelos de datos (recomendado)
        }
        val contentType = "application/json".toMediaType()

        // 6. Construir Retrofit
        retrofitInstance = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // 🔑 Usar el cliente OkHttp con los interceptores
            // 🔑 USAMOS Kotlinx Serialization
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }
}