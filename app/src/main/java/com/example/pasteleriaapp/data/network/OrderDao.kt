package com.example.pasteleriaapp.data.network

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.pasteleriaapp.model.Order
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Insert
    suspend fun insertOrder(order: Order)

    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY id DESC")
    fun getOrdersForUser(userId: Int): Flow<List<Order>>
}