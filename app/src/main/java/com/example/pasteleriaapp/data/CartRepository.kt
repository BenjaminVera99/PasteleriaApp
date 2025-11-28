package com.example.pasteleriaapp.data

import com.example.pasteleriaapp.data.dao.CartDao
import com.example.pasteleriaapp.model.CartItem
import kotlinx.coroutines.flow.Flow

class CartRepository(private val cartDao: CartDao) {

    // Cambiar userId: Int a userId: Long
    fun getCartForUser(userId: Long): Flow<List<CartItem>> {
        return cartDao.getCartItemsForUser(userId)
    }

    // Cambiar a Long
    suspend fun addToCart(userId: Long, productId: Long) {
        val existingItem = cartDao.getCartItem(userId, productId)
        if (existingItem != null) {
            val updatedItem = existingItem.copy(quantity = existingItem.quantity + 1)
            cartDao.insertOrUpdateItem(updatedItem)
        } else {
            // Asegúrate de que CartItem usa Long para userId y productId
            cartDao.insertOrUpdateItem(CartItem(userId = userId, productId = productId, quantity = 1))
        }
    }

    // Cambiar a Long
    suspend fun removeFromCart(userId: Long, productId: Long) {
        cartDao.deleteItem(userId, productId)
    }

    // Cambiar a Long
    suspend fun increaseQuantity(userId: Long, productId: Long) {
        val item = cartDao.getCartItem(userId, productId)
        if (item != null) {
            val updatedItem = item.copy(quantity = item.quantity + 1)
            cartDao.insertOrUpdateItem(updatedItem)
        }
    }

    // Cambiar a Long
    suspend fun decreaseQuantity(userId: Long, productId: Long) {
        val item = cartDao.getCartItem(userId, productId)
        if (item != null) {
            if (item.quantity > 1) {
                val updatedItem = item.copy(quantity = item.quantity - 1)
                cartDao.insertOrUpdateItem(updatedItem)
            } else {
                cartDao.deleteItem(userId, productId)
            }
        }
    }

    // Cambiar a Long
    suspend fun clearCart(userId: Long) {
        cartDao.clearCart(userId)
    }
}