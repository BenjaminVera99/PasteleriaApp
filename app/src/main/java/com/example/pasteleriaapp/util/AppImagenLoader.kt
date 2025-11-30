package com.example.pasteleriaapp.util

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.request.CachePolicy
import okhttp3.OkHttpClient // <-- Importamos OkHttpClient
import java.util.concurrent.TimeUnit // <-- Importamos TimeUnit

class AppImageLoader(private val application: Application) : ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {

        // ⭐⭐ SOLUCIÓN: Usar un cliente OkHttp estándar o simple. ⭐⭐
        // Opcional: Aumentamos los timeouts si la conexión local es lenta (como hicimos con Retrofit)
        val okHttpClient = OkHttpClient.Builder()
            .callTimeout(30, TimeUnit.SECONDS) // Aumentamos el tiempo de espera
            .build()

        // Devolvemos el ImageLoader usando el cliente limpio
        return ImageLoader.Builder(application.applicationContext)
            .okHttpClient(okHttpClient) // Pasamos el cliente simple
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED) // Habilita el caché (mejora el rendimiento)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    fun clearCache() {
        // Obtenemos la instancia para borrar el caché
        val imageLoader = newImageLoader()
        imageLoader.diskCache?.clear()
        imageLoader.memoryCache?.clear()
    }
}