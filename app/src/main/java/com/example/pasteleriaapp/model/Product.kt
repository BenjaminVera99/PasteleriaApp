package com.example.pasteleriaapp.model

import androidx.annotation.DrawableRes

data class Product(
    val id: Int,
    val name: String,
    val price: Int,
    @DrawableRes val imageResId: Int,
    val description: String
)