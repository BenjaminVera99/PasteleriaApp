package com.example.pasteleriaapp.model

import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

private const val BASE_URL = "http://192.168.0.7:9090"

@Serializable
@Entity(tableName = "products")
data class Product(
    @PrimaryKey
    val id: Long,
    val code: String,
    val category: String,
    val name: String,
    val price: Double,
    val img: String,
    val onSale: Boolean
) {
    val fullImageUrl: String
        get() {
            val imagePath = img.trim()
            val finalUrl = "$BASE_URL$imagePath"

            Log.d("IMG_URL_DEBUG", "URL de imagen construida: $finalUrl")
            // ⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐

            return finalUrl
        }
}