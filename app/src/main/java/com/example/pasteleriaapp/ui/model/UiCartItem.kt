package com.example.pasteleriaapp.ui.model

import com.example.pasteleriaapp.model.Product

/**
 * Clase de datos que representa un item del carrito para la UI.
 * Contiene el objeto Product completo para un acceso fácil a sus detalles.
 */
data class UiCartItem(
    val product: Product,
    val quantity: Int
)