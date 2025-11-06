package com.example.pasteleriaapp.model

data class Order(
    val id: Long, // A unique identifier, like a timestamp
    val items: List<CartItem>,
    val totalPrice: Double,
    val date: String
)