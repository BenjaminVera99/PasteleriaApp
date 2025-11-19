package com.example.pasteleriaapp.navigation

sealed class AppRoute(val route: String) {
    object Welcome : AppRoute("welcome")
    object Login : AppRoute("login")
    object Register : AppRoute("register")
    object Home : AppRoute("home")
    object Profile : AppRoute("profile")
    object Cart : AppRoute("cart")
    object Checkout : AppRoute("checkout")
    object OrderHistory : AppRoute("order_history")
    object OrderConfirmation : AppRoute("order_confirmation") // Nueva ruta

    object Detail : AppRoute("detail/{itemId}") {
        fun createRoute(itemId: String) = "detail/$itemId"
    }
    val routePattern: String get() = if (this is Detail) "detail/{itemId}" else route
}