package com.example.pasteleriaapp.data

import com.example.pasteleriaapp.data.network.CartDao
import com.example.pasteleriaapp.model.CartItem
import kotlinx.coroutines.flow.Flow

class CartRepository(private val cartDao: CartDao) {

    fun getCartForUser(userId: Int): Flow<List<CartItem>> {
        return cartDao.getCartItemsForUser(userId)
    }

    suspend fun addToCart(userId: Int, productId: Int) {
        val existingItem = cartDao.getCartItem(userId, productId)
        if (existingItem != null) {
            // Si el item ya existe, incrementa la cantidad
            val updatedItem = existingItem.copy(quantity = existingItem.quantity + 1)
            cartDao.insertOrUpdateItem(updatedItem)
        } else {
            // Si es un item nuevo, lo inserta con cantidad 1
            cartDao.insertOrUpdateItem(CartItem(userId = userId, productId = productId, quantity = 1))
        }
    }

    suspend fun removeFromCart(userId: Int, productId: Int) {
        cartDao.deleteItem(userId, productId)
    }

    suspend fun increaseQuantity(userId: Int, productId: Int) {
        val item = cartDao.getCartItem(userId, productId)
        if (item != null) {
            val updatedItem = item.copy(quantity = item.quantity + 1)
            cartDao.insertOrUpdateItem(updatedItem)
        }
    }

    suspend fun decreaseQuantity(userId: Int, productId: Int) {
        val item = cartDao.getCartItem(userId, productId)
        if (item != null) {
            if (item.quantity > 1) {
                // Si la cantidad es mayor a 1, la decrementa
                val updatedItem = item.copy(quantity = item.quantity - 1)
                cartDao.insertOrUpdateItem(updatedItem)
            } else {
                // Si la cantidad es 1, elimina el item del carrito
                cartDao.deleteItem(userId, productId)
            }
        }
    }

    suspend fun clearCart(userId: Int) {
        cartDao.clearCart(userId)
    }
}