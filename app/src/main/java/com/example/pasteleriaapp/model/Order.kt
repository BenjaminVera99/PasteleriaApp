package com.example.pasteleriaapp.model

data class Order(
    val id: Long,
    val items: List<CartItem>,
    val totalPrice: Double,
    val date: String,
    val shippingAddress: String
)