package com.example.pasteleriaapp.data.dao

import com.example.pasteleriaapp.model.Product
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("products")
    suspend fun getProducts(): List<Product>


    @GET("/products/{id}")
    suspend fun getProduct(@Path("id") id: Long): Product
}