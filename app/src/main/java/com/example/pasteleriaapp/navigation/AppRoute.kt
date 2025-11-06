package com.example.pasteleriaapp.navigation

sealed class AppRoute(val route: String) {
    object Welcome : AppRoute("welcome")
    object Login : AppRoute("login")
    object Home : AppRoute("home")
    object Cart : AppRoute("cart")
    object Checkout : AppRoute("checkout")
    object OrderHistory : AppRoute("order_history") // Added this line
    object Register : AppRoute("register")
    object Profile : AppRoute("profile")
    object Settings : AppRoute("settings")

    // The class now builds the real navigation route
    class Detail(itemId: String) : AppRoute("detail/$itemId") {
        // The companion object holds the route pattern for the NavHost definition
        companion object {
            const val routePattern = "detail/{itemId}"
        }
    }
}