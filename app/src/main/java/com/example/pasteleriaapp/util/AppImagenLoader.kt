package com.example.pasteleriaapp.util

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.request.CachePolicy
import okhttp3.OkHttpClient // <-- Importamos OkHttpClient
import java.util.concurrent.TimeUnit // <-- Importamos TimeUnit

class AppImageLoader(private val application: Application) : ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {

        val okHttpClient = OkHttpClient.Builder()
            .callTimeout(30, TimeUnit.SECONDS)
            .build()

        return ImageLoader.Builder(application.applicationContext)
            .okHttpClient(okHttpClient)
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    fun clearCache() {
        val imageLoader = newImageLoader()
        imageLoader.diskCache?.clear()
        imageLoader.memoryCache?.clear()
    }
}