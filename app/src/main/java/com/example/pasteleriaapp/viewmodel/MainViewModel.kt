package com.example.pasteleriaapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pasteleriaapp.data.AppDatabase
import com.example.pasteleriaapp.data.CartRepository
import com.example.pasteleriaapp.data.OrderRepository
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
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

    // --- Repositorios ---
    private val database = AppDatabase.getDatabase(application)
    private val productoRepository = ProductoRepository(application)
    private val cartRepository = CartRepository(database.cartDao())
    private val orderRepository = OrderRepository(database.orderDao())

    // --- Trabajos de Corutinas para gestionar suscripciones ---
    private var cartJob: Job? = null
    private var ordersJob: Job? = null

    // --- Estados de la UI ---
    val products: StateFlow<List<Product>> = productoRepository.products.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val isLoading: StateFlow<Boolean> = productoRepository.isLoading

    // --- Estados de Sesión, Carrito y Pedidos ---
    private val _currentUser = MutableStateFlow<Usuario?>(null)
    val currentUser: StateFlow<Usuario?> = _currentUser.asStateFlow()
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _dbCartItems = MutableStateFlow<List<CartItem>>(emptyList())
    private val _guestCartItems = MutableStateFlow<List<UiCartItem>>(emptyList())

    val uiCartItems: StateFlow<List<UiCartItem>> = _isLoggedIn.flatMapLatest { loggedIn ->
        if (loggedIn) {
            products.combine(_dbCartItems) { productList, dbItems ->
                dbItems.mapNotNull { dbItem ->
                    productList.find { it.id == dbItem.productId }?.let {
                        UiCartItem(product = it, quantity = dbItem.quantity)
                    }
                }
            }
        } else {
            _guestCartItems
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItemCount: StateFlow<Int> = uiCartItems.map { it.sumOf { item -> item.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val cartTotal: StateFlow<Double> = uiCartItems.map { items ->
        items.sumOf { it.product.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _navEvents = MutableSharedFlow<NavigationEvent>()
    val navEvents = _navEvents.asSharedFlow()

    // --- Lógica de Sesión ---
    fun login(usuario: Usuario) {
        _currentUser.value = usuario
        _isLoggedIn.value = true
        _guestCartItems.value = emptyList() // El carrito de invitado se descarta
        loadCartForUser(usuario.id)
        loadOrdersForUser(usuario.id)
        navigateTo(AppRoute.Home, popUpTo = AppRoute.Welcome, inclusive = true)
    }

    fun onUserUpdated(updatedUser: Usuario) {
        _currentUser.value = updatedUser
    }

    fun logout() {
        cartJob?.cancel()
        ordersJob?.cancel()
        _currentUser.value = null
        _isLoggedIn.value = false
        _dbCartItems.value = emptyList()
        _orders.value = emptyList()
        _guestCartItems.value = emptyList()
        navigateTo(AppRoute.Welcome, popUpTo = AppRoute.Home, inclusive = true)
    }

    private fun loadCartForUser(userId: Int) {
        cartJob?.cancel()
        cartJob = cartRepository.getCartForUser(userId)
            .onEach { _dbCartItems.value = it }
            .launchIn(viewModelScope)
    }

    private fun loadOrdersForUser(userId: Int) {
        ordersJob?.cancel()
        ordersJob = orderRepository.getOrdersForUser(userId)
            .onEach { _orders.value = it }
            .launchIn(viewModelScope)
    }

    // --- Lógica de Pedidos ---
    fun placeOrder(shippingAddress: String, buyerName: String, buyerEmail: String) {
        val currentCart = uiCartItems.value
        if (currentCart.isNotEmpty()) {
            val newOrder = Order(
                userId = _currentUser.value?.id,
                items = currentCart,
                totalPrice = cartTotal.value,
                date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
                shippingAddress = shippingAddress,
                buyerName = buyerName,
                buyerEmail = buyerEmail
            )
            viewModelScope.launch { orderRepository.placeOrder(newOrder) }
            
            if (_isLoggedIn.value) {
                _currentUser.value?.id?.let { viewModelScope.launch { cartRepository.clearCart(it) } }
            } else {
                _orders.update { currentOrders -> listOf(newOrder) + currentOrders }
                _guestCartItems.value = emptyList()
            }
            navigateTo(AppRoute.Home, popUpTo = AppRoute.Cart, inclusive = true)
        }
    }

    // --- Lógica del Carrito ---
    fun addToCart(product: Product) {
        if (_isLoggedIn.value) {
            _currentUser.value?.id?.let { viewModelScope.launch { cartRepository.addToCart(it, product.id) } }
        } else {
            _guestCartItems.update { cart ->
                val existing = cart.find { it.product.id == product.id }
                if (existing == null) cart + UiCartItem(product, 1) else cart.map { if (it.product.id == product.id) it.copy(quantity = it.quantity + 1) else it }
            }
        }
    }

    fun removeFromCart(productId: Int) {
        if (_isLoggedIn.value) {
            _currentUser.value?.id?.let { viewModelScope.launch { cartRepository.removeFromCart(it, productId) } }
        } else {
            _guestCartItems.update { cart -> cart.filterNot { it.product.id == productId } }
        }
    }

    fun increaseCartItem(productId: Int) {
        if (_isLoggedIn.value) {
            _currentUser.value?.id?.let { viewModelScope.launch { cartRepository.increaseQuantity(it, productId) } }
        } else {
            _guestCartItems.update { cart -> cart.map { if (it.product.id == productId) it.copy(quantity = it.quantity + 1) else it } }
        }
    }

    fun decreaseCartItem(productId: Int) {
        if (_isLoggedIn.value) {
            _currentUser.value?.id?.let { viewModelScope.launch { cartRepository.decreaseQuantity(it, productId) } }
        } else {
            _guestCartItems.update { cart ->
                val item = cart.find { it.product.id == productId }
                if (item != null && item.quantity > 1) cart.map { if (it.product.id == productId) it.copy(quantity = it.quantity - 1) else it } else cart
            }
        }
    }

    // --- Funciones de Navegación ---
    fun navigateTo(appRoute: AppRoute, popUpTo: AppRoute? = null, inclusive: Boolean = false, singleTop: Boolean = false) {
        CoroutineScope(Dispatchers.Main).launch { _navEvents.emit(NavigationEvent.NavigateTo(appRoute, popUpTo, inclusive, singleTop)) }
    }
    fun navigateBack() { CoroutineScope(Dispatchers.Main).launch { _navEvents.emit(NavigationEvent.PopBackStack) } }
    fun navigateUp() { CoroutineScope(Dispatchers.Main).launch { _navEvents.emit(NavigationEvent.NavigateUp) } }
}