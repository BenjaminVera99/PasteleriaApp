package com.example.pasteleriaapp.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.pasteleriaapp.data.converters.OrderItemsConverter
import com.example.pasteleriaapp.ui.model.UiCartItem

@Entity(
    tableName = "orders",
    indices = [Index(value = ["userId"])],
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@TypeConverters(OrderItemsConverter::class)
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Int?, // Nulo para pedidos de invitados
    val items: List<UiCartItem>,
    val totalPrice: Double,
    val date: String,
    val shippingAddress: String,
    val buyerName: String,
    val buyerEmail: String
)