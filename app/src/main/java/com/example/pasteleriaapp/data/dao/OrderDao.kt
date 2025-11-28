package com.example.pasteleriaapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pasteleriaapp.model.Order
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY date DESC")
    fun getOrdersForUser(userId: Long): Flow<List<Order>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertOrder(order: Order)
}