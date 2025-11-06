package com.example.pasteleriaapp.navigation

sealed class AppRoute(val route:String) {
    data object Welcome: AppRoute("welcome")
    data object Login: AppRoute("login")
    data object Home:AppRoute("home")
    data object Cart: AppRoute("cart")
    data object Register: AppRoute("register")
    data object Profile: AppRoute("profile")
    data object Settings: AppRoute("settings")

    data class Detail (val itemId:String): AppRoute("detail/{itemId}")
    {
        fun buildRoute():String{
            return route.replace("{itemId}",itemId)
        }
    }


}