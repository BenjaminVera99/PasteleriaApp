package com.example.pasteleriaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pasteleriaapp.model.CartItem
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

class MainViewModel : ViewModel() {

    private val _navEvents = MutableSharedFlow<NavigationEvent>()
    val navEvents = _navEvents.asSharedFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    val cartTotal: StateFlow<Double> = cartItems.map { items ->
        var total = 0.0
        for (item in items) {
            total += item.product.price * item.quantity
        }
        total
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0.0)


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
                currentCart // Do nothing if quantity is 1 or less
            }
        }
    }

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