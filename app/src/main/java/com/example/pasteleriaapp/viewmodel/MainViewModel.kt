package com.example.pasteleriaapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pasteleriaapp.data.AppDatabase
import com.example.pasteleriaapp.data.CartRepository
import com.example.pasteleriaapp.data.ProductoRepository
import com.example.pasteleriaapp.model.CartItem
import com.example.pasteleriaapp.model.Order
import com.example.pasteleriaapp.model.Product
import com.example.pasteleriaapp.model.Usuario
import com.example.pasteleriaapp.navigation.AppRoute
import com.example.pasteleriaapp.navigation.NavigationEvent
import com.example.pasteleriaapp.ui.model.UiCartItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    // --- Inicialización de Repositorios ---
    private val database = AppDatabase.getDatabase(application)
    private val productoRepository = ProductoRepository(application)
    private val cartRepository = CartRepository(database.cartDao())

    // --- Estados de la UI (Flujos de datos) ---

    // Estado para la lista de productos
    val products: StateFlow<List<Product>> = productoRepository.products.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Estado del carrito de la base de datos (privado)
    private val _dbCartItems = MutableStateFlow<List<CartItem>>(emptyList())

    // Estado del carrito para la UI (combina productos y datos del carrito)
    val uiCartItems: StateFlow<List<UiCartItem>> = products.combine(_dbCartItems) { productList, cartItems ->
        cartItems.mapNotNull { cartItem ->
            productList.find { it.id == cartItem.productId }?.let {
                UiCartItem(product = it, quantity = cartItem.quantity)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Estado para el total del carrito, derivado del carrito de la UI
    val cartTotal: StateFlow<Double> = uiCartItems.map { uiItems ->
        uiItems.sumOf { it.product.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Estado para el contador de items en el carrito
    val cartItemCount: StateFlow<Int> = _dbCartItems.map { items ->
        items.sumOf { it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- Otros Estados ---
    private val _currentUser = MutableStateFlow<Usuario?>(null)
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()
    private val _navEvents = MutableSharedFlow<NavigationEvent>()
    val navEvents = _navEvents.asSharedFlow()

    // --- Lógica de Sesión ---

    fun login(usuario: Usuario) {
        _currentUser.value = usuario
        _isLoggedIn.value = true
        loadCartForUser(usuario.id)
        navigateTo(AppRoute.Home, popUpRoute = AppRoute.Welcome, inclusive = true)
    }

    fun logout() {
        _currentUser.value = null
        _isLoggedIn.value = false
        _dbCartItems.value = emptyList() // Limpiar carrito al cerrar sesión.
        navigateTo(AppRoute.Welcome, popUpRoute = AppRoute.Home, inclusive = true)
    }

    private fun loadCartForUser(userId: Int) {
        cartRepository.getCartForUser(userId)
            .onEach { items -> _dbCartItems.value = items }
            .launchIn(viewModelScope)
    }

    // --- Lógica de Pedidos ---

    fun placeOrder(shippingAddress: String, buyerName: String, buyerEmail: String) {
        val currentCartItems = uiCartItems.value
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
            // Limpiar el carrito de la base de datos
            _currentUser.value?.id?.let {
                viewModelScope.launch {
                    cartRepository.clearCart(it)
                }
            }
            navigateTo(AppRoute.Home, popUpRoute = AppRoute.Cart, inclusive = true)
        }
    }

    // --- Lógica del Carrito (Ahora usa el Repositorio) ---

    fun addToCart(product: Product) {
        _currentUser.value?.id?.let { userId ->
            viewModelScope.launch {
                cartRepository.addToCart(userId, product.id)
            }
        }
    }

    fun removeFromCart(productId: Int) {
        _currentUser.value?.id?.let { userId ->
            viewModelScope.launch {
                cartRepository.removeFromCart(userId, productId)
            }
        }
    }

    fun increaseCartItem(productId: Int) {
        _currentUser.value?.id?.let { userId ->
            viewModelScope.launch {
                cartRepository.increaseQuantity(userId, productId)
            }
        }
    }

    fun decreaseCartItem(productId: Int) {
        _currentUser.value?.id?.let { userId ->
            viewModelScope.launch {
                cartRepository.decreaseQuantity(userId, productId)
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