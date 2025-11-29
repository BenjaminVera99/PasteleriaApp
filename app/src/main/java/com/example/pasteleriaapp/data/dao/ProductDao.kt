package com.example.pasteleriaapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pasteleriaapp.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products")
    fun getProducts(): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAll(products: List<Product>)

    @Query("DELETE FROM products")
    suspend fun clearTable()

    @Query("SELECT COUNT(*) FROM products")
    suspend fun countProducts(): Int
}