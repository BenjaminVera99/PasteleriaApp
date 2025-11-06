package com.example.pasteleriaapp.model 

data class Product(
    val id: Int,
    val code: String,
    val category: String,
    val name: String,
    val price: Int,
    val img: String,
    val onSale: Boolean
)