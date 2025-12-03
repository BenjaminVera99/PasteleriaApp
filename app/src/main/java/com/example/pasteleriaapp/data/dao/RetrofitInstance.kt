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
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private const val BASE_URL = "http://192.168.0.7:9090/api/"

    private lateinit var applicationContext: Context
    private lateinit var retrofitInstance: Retrofit

    val api: ApiService by lazy {
        if (!::retrofitInstance.isInitialized) {
            throw IllegalStateException("RetrofitInstance no ha sido inicializado. Llama a initialize(context) primero.")
        }
        retrofitInstance.create(ApiService::class.java)
    }

    fun initialize(context: Context) {
        if (::retrofitInstance.isInitialized) return

        applicationContext = context.applicationContext

        val authTokenManager = AuthTokenManager(applicationContext)
        val authInterceptor = AuthInterceptor(authTokenManager)
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
        // FIN MODIFICACIÓN

        val json = Json {
            ignoreUnknownKeys = true
        }
        val contentType = "application/json".toMediaType()

        retrofitInstance = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }
}