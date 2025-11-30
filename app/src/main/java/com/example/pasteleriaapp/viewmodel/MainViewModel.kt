package com.example.pasteleriaapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pasteleriaapp.data.AppDatabase
import com.example.pasteleriaapp.data.CartRepository
import com.example.pasteleriaapp.data.OrderRepository
import com.example.pasteleriaapp.data.ProductoRepository
import com.example.pasteleriaapp.data.dao.RetrofitInstance
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

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    // --- Repositorios ---
    private val database = AppDatabase.getDatabase(application)

    private val apiService = RetrofitInstance.api
    private val productoDao = database.productDao()
    private val productoRepository = ProductoRepository(productoDao, apiService)

    private val cartRepository = CartRepository(database.cartDao())
    private val orderRepository = OrderRepository(database.orderDao())

    init {
        viewModelScope.launch {
            productoRepository.refreshProducts()
        }
    }

    // --- Trabajos de Corutinas ---
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

    // --- Eventos de Navegación y UI ---
    private val _navEvents = MutableSharedFlow<NavigationEvent>()
    val navEvents = _navEvents.asSharedFlow()
    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    // --- Lógica de Sesión ---
    fun login(usuario: Usuario) {
        _currentUser.value = usuario
        _isLoggedIn.value = true
        _guestCartItems.value = emptyList()
        loadCartForUser(usuario.id!!)
        loadOrdersForUser(usuario.id!!)
        navigateTo(AppRoute.Home.route, popUpTo = AppRoute.Welcome.route, inclusive = true)
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
        // Navegación que podría dejar historial indeseado
        navigateTo(AppRoute.Welcome.route, popUpTo = AppRoute.Home.route, inclusive = true)
    }

    private fun loadCartForUser(userId: Long) {
        cartJob?.cancel()
        cartJob = cartRepository.getCartForUser(userId)
            .onEach { _dbCartItems.value = it }
            .launchIn(viewModelScope)
    }

    private fun loadOrdersForUser(userId: Long) {
        ordersJob?.cancel()
        ordersJob = orderRepository.getOrdersForUser(userId)
            .onEach { _orders.value = it }
            .launchIn(viewModelScope)
    }

    // --- Lógica de Pedidos ---
    fun placeOrder(shippingAddress: String, buyerName: String, buyerEmail: String): Boolean {
        if (shippingAddress.isBlank()) {
            return false
        }

        val userIdLong: Long? = _currentUser.value?.id?.toLong()

        val currentCart = uiCartItems.value
        if (currentCart.isNotEmpty()) {
            val newOrder = Order(
                userId = userIdLong?.toInt(),
                items = currentCart,
                totalPrice = cartTotal.value,
                date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
                shippingAddress = shippingAddress,
                buyerName = buyerName,
                buyerEmail = buyerEmail
            )
            viewModelScope.launch { orderRepository.placeOrder(newOrder) }

            if (_isLoggedIn.value) {
                _currentUser.value?.id?.let { userId ->
                    viewModelScope.launch { cartRepository.clearCart(userId.toLong()) }
                }
            } else {
                _orders.update { currentOrders -> listOf(newOrder) + currentOrders }
                _guestCartItems.value = emptyList()
            }
            navigateTo(
                AppRoute.OrderConfirmation.route,
                popUpTo = AppRoute.Checkout.route,
                inclusive = true
            )
            return true
        } else {
            return false
        }
    }

    // --- Lógica del Carrito ---
    fun addToCart(product: Product) {
        viewModelScope.launch {
            _uiEvents.emit(UiEvent.ShowSnackbar("${product.name} añadido al carrito"))
        }

        if (_isLoggedIn.value) {
            // ✅ Corregido: Asegurar que el ID del producto es Long y el ID de usuario es Long
            _currentUser.value?.id?.let { userId ->
                viewModelScope.launch { cartRepository.addToCart(userId.toLong(), product.id) }
            }
        } else {
            _guestCartItems.update { cart ->
                val existing = cart.find { it.product.id == product.id }
                if (existing == null) cart + UiCartItem(
                    product,
                    1
                ) else cart.map { if (it.product.id == product.id) it.copy(quantity = it.quantity + 1) else it }
            }
        }
    }

    fun removeFromCart(productId: Long) { // ✅ De Int a Long
        if (_isLoggedIn.value) {
            _currentUser.value?.id?.let { userId ->
                viewModelScope.launch { cartRepository.removeFromCart(userId.toLong(), productId) }
            }
        } else {
            _guestCartItems.update { cart -> cart.filterNot { it.product.id == productId } }
        }
    }

    fun increaseCartItem(productId: Long) {
        if (_isLoggedIn.value) {
            _currentUser.value?.id?.let { userIdInt ->

                val userIdLong = userIdInt.toLong()

                viewModelScope.launch {
                    cartRepository.increaseQuantity(userIdLong, productId)
                }
            }
        } else {
            _guestCartItems.update { cart ->
                cart.map {
                    if (it.product.id == productId) {
                        it.copy(quantity = it.quantity + 1)
                    } else {
                        it
                    }
                }
            }
        }
    }

    fun decreaseCartItem(productId: Long) { // ✅ De Int a Long
        if (_isLoggedIn.value) {
            _currentUser.value?.id?.let { userId ->
                viewModelScope.launch {
                    // ... (Lógica para usuario logueado)
                    cartRepository.decreaseQuantity(
                        userId.toLong(),
                        productId
                    )
                }
            }
        } else {
            _guestCartItems.update { cart ->
                cart.mapNotNull {
                    if (it.product.id == productId) {
                        // ⭐ CORRECCIÓN CLAVE: Disminuir la cantidad en 1 ⭐
                        if (it.quantity > 1) {
                            it.copy(quantity = it.quantity - 1)
                        } else {
                            // Si la cantidad es 1, eliminar el item (mapNotNull lo maneja)
                            null
                        }
                    } else {
                        it
                    }
                }
            }
        }
    }

    // --- Funciones de Navegación ---
    fun navigateTo(
        route: String,
        popUpTo: String? = null,
        inclusive: Boolean = false,
        singleTop: Boolean = false
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            _navEvents.emit(
                NavigationEvent.NavigateTo(
                    route,
                    popUpTo,
                    inclusive,
                    singleTop
                )
            )
        }
    }

    fun navigateBack() {
        CoroutineScope(Dispatchers.Main).launch { _navEvents.emit(NavigationEvent.PopBackStack) }
    }

    fun navigateUp() {
        CoroutineScope(Dispatchers.Main).launch { _navEvents.emit(NavigationEvent.NavigateUp) }
    }

    fun initializeUserSession(usuario: Usuario) {
        // 1. Establece el estado de inicio de sesión
        _currentUser.value = usuario
        _isLoggedIn.value = true
        _guestCartItems.value = emptyList()

        // 2. Carga los datos asociados al usuario persistente
        usuario.id?.let { userId ->
            // Tu MainViewModel ya usa Long para loadCartForUser y loadOrdersForUser
            // Nos aseguramos que el ID sea Long, ya que el ID de Room es a menudo un Long.
            val userIdLong = userId.toLong()

            loadCartForUser(userIdLong)
            loadOrdersForUser(userIdLong)
        }
    }
}