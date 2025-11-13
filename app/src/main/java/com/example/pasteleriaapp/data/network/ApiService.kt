package com.example.pasteleriaapp.data.network

import com.example.pasteleriaapp.model.Product
import retrofit2.http.GET

interface ApiService {
    @GET("products")
    suspend fun getProducts(): List<Product>
}