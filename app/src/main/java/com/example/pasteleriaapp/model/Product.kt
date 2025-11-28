package com.example.pasteleriaapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

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
            val baseUrl = "http://192.168.0.8:9090"

            return "$baseUrl/${img.trimStart('/')}"
        }
}