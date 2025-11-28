package com.example.pasteleriaapp.data.dao

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

object RetrofitInstance {
    // Apuntando al servidor local (tu PC) desde el emulador de Android
    private const val BASE_URL = "http://192.168.0.8:9090/api/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // Usamos el conversor de kotlinx.serialization
            .addConverterFactory(Json.Default.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)
    }
}