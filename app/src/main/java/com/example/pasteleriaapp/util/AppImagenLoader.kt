package com.example.pasteleriaapp.util

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import okhttp3.OkHttpClient

class AppImageLoader(private val application: Application) : ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        val okHttpClient = OkHttpClient.Builder()
            .build()

        return ImageLoader.Builder(application.applicationContext)
            .okHttpClient(okHttpClient)
            .crossfade(true)
            .components {
            }
            .build()
    }
}