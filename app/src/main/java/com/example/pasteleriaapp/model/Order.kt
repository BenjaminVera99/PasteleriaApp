package com.example.pasteleriaapp.model

import com.example.pasteleriaapp.ui.model.UiCartItem

data class Order(
    val id: Long,
    val items: List<UiCartItem>,
    val totalPrice: Double,
    val date: String,
    val shippingAddress: String,
    val buyerName: String,
    val buyerEmail: String
)