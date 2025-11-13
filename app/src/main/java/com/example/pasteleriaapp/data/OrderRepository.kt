package com.example.pasteleriaapp.data

import com.example.pasteleriaapp.data.network.OrderDao
import com.example.pasteleriaapp.model.Order
import kotlinx.coroutines.flow.Flow

class OrderRepository(private val orderDao: OrderDao) {

    suspend fun placeOrder(order: Order) {
        orderDao.insertOrder(order)
    }

    fun getOrdersForUser(userId: Int): Flow<List<Order>> {
        return orderDao.getOrdersForUser(userId)
    }
}