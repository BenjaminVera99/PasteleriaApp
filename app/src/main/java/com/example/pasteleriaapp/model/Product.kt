package com.example.pasteleriaapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

private const val BASE_URL = "http://192.168.0.10:9090"

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
    // La propiedad calculada que usa la constante BASE_URL
    val fullImageUrl: String
        get() {
            val imagePath = img.trim()
            return "$BASE_URL$imagePath"
        }
}