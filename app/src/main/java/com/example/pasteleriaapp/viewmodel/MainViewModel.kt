package com.example.pasteleriaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pasteleriaapp.model.CartItem
import com.example.pasteleriaapp.model.Order
import com.example.pasteleriaapp.model.Product
import com.example.pasteleriaapp.navigation.AppRoute
import com.example.pasteleriaapp.navigation.NavigationEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel : ViewModel() {

    // --- Navegación ---
    // Canal para enviar eventos de navegación a la UI.
    private val _navEvents = MutableSharedFlow<NavigationEvent>()
    val navEvents = _navEvents.asSharedFlow()

    // --- Estados de la Aplicación ---

    // Estado del carrito de compras.
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    // Estado del historial de pedidos.
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    // Estado de la sesión del usuario.
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // --- Estados Derivados (calculados a partir de otros estados) ---

    // Calcula la cantidad total de artículos en el carrito.
    val cartItemCount: StateFlow<Int> = _cartItems.map { items ->
        items.sumOf { it.quantity }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0)

    // Calcula el precio total del carrito.
    val cartTotal: StateFlow<Double> = cartItems.map { items ->
        var total = 0.0
        for (item in items) {
            total += item.product.price * item.quantity
        }
        total
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0.0)

    // --- Lógica de Sesión ---

    fun login() {
        _isLoggedIn.value = true
        navigateTo(AppRoute.Home, popUpRoute = AppRoute.Welcome, inclusive = true)
    }

    fun logout() {
        _isLoggedIn.value = false
        _cartItems.value = emptyList() // Limpiar carrito al cerrar sesión.
        navigateTo(AppRoute.Welcome, popUpRoute = AppRoute.Home, inclusive = true)
    }

    // --- Lógica de Pedidos ---

    // Crea un nuevo pedido, lo guarda en el historial y limpia el carrito.
    fun placeOrder(shippingAddress: String, buyerName: String, buyerEmail: String) {
        val currentCartItems = _cartItems.value
        if (currentCartItems.isNotEmpty()) {
            val newOrder = Order(
                id = System.currentTimeMillis(),
                items = currentCartItems,
                totalPrice = cartTotal.value,
                date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
                shippingAddress = shippingAddress,
                buyerName = buyerName,
                buyerEmail = buyerEmail
            )
            _orders.update { currentOrders -> listOf(newOrder) + currentOrders }
            _cartItems.value = emptyList()
            navigateTo(AppRoute.Home, popUpRoute = AppRoute.Cart, inclusive = true)
        }
    }

    // --- Lógica del Carrito ---

    fun addToCart(product: Product) {
        _cartItems.update { currentCart ->
            val cartItem = currentCart.find { it.product.id == product.id }
            if (cartItem == null) {
                currentCart + CartItem(product)
            } else {
                currentCart.map { 
                    if (it.product.id == product.id) {
                        it.copy(quantity = it.quantity + 1)
                    } else {
                        it
                    }
                }
            }
        }
    }

    fun removeFromCart(productId: Int) {
        _cartItems.update { currentCart ->
            currentCart.filterNot { it.product.id == productId }
        }
    }

    fun increaseCartItem(productId: Int) {
        _cartItems.update { currentCart ->
            currentCart.map {
                if (it.product.id == productId) {
                    it.copy(quantity = it.quantity + 1)
                } else {
                    it
                }
            }
        }
    }

    fun decreaseCartItem(productId: Int) {
        _cartItems.update { currentCart ->
            val existingItem = currentCart.find { it.product.id == productId }
            if (existingItem != null && existingItem.quantity > 1) {
                currentCart.map {
                    if (it.product.id == productId) {
                        it.copy(quantity = it.quantity - 1)
                    } else {
                        it
                    }
                }
            } else {
                currentCart
            }
        }
    }

    // --- Funciones de Navegación ---

    fun navigateTo(
        appRoute: AppRoute,
        popUpRoute: AppRoute? = null,
        inclusive: Boolean = false,
        singleTop: Boolean = false
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            _navEvents.emit(NavigationEvent.NavigateTo(appRoute, popUpRoute, inclusive, singleTop))
        }
    }

    fun navigateBack() {
        CoroutineScope(Dispatchers.Main).launch {
            _navEvents.emit(NavigationEvent.PopBackStack)
        }
    }

    fun navigateUp() {
        CoroutineScope(Dispatchers.Main).launch {
            _navEvents.emit(NavigationEvent.NavigateUp)
        }
    }
}