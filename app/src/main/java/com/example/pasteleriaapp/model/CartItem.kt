package com.example.pasteleriaapp.model

data class CartItem(
    val product: Product,
    var quantity: Int = 1
)