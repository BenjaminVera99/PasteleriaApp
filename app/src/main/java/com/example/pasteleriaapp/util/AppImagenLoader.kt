package com.example.pasteleriaapp.util

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.request.CachePolicy
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class AppImageLoader(private val application: Application) : ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {

        // ⭐⭐ CÓDIGO CLAVE PARA IGNORAR CERTIFICADOS (SSL) ⭐⭐
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val sslSocketFactory = sslContext.socketFactory
        // ⭐⭐ FIN DEL CÓDIGO CLAVE ⭐⭐

        // Construimos el cliente OkHttp con la configuración de seguridad ignorada
        val okHttpClient = OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true } // Acepta cualquier nombre de host
            .build()

        return ImageLoader.Builder(application.applicationContext)
            .okHttpClient(okHttpClient)
            .crossfade(true)
            .diskCachePolicy(CachePolicy.DISABLED) // Mantenemos la caché deshabilitada
            .memoryCachePolicy(CachePolicy.DISABLED)
            .build()
    }

    fun clearCache() {
        val imageLoader = newImageLoader()
        imageLoader.diskCache?.clear()
        imageLoader.memoryCache?.clear()
    }

}