package com.example.pasteleriaapp.model

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey
    val id: Int,
    val name: String,
    val price: Double,
    val description: String,
    @DrawableRes val imageResId: Int? = null,
    val imageUrl: String? = null
)