package com.example.pasteleriaapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pasteleriaapp.model.CartItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    fun getCartItemsForUser(userId: Long): Flow<List<CartItem>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertOrUpdateItem(item: CartItem)

    @Query("DELETE FROM cart_items WHERE userId = :userId AND productId = :productId")
    suspend fun deleteItem(userId: Long, productId: Long)

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearCart(userId: Long)

    @Query("SELECT * FROM cart_items WHERE userId = :userId AND productId = :productId LIMIT 1")
    suspend fun getCartItem(userId: Long, productId: Long): CartItem?
}