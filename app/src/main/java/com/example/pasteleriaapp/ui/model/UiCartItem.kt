package com.example.pasteleriaapp.ui.model

import com.example.pasteleriaapp.model.Product
import kotlinx.serialization.Serializable

@Serializable
data class UiCartItem(
    val product: Product,
    val quantity: Int
)