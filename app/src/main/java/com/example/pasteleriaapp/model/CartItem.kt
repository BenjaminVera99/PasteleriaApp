package com.example.pasteleriaapp.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index


@Entity(
    tableName = "cart_items",
    primaryKeys = ["userId", "productId"],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["productId"])
    ],

    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CartItem(
    val userId: Long,
    val productId: Long,
    var quantity: Int
)