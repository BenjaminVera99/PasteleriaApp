package com.example.pasteleriaapp.navigation

sealed class NavigationEvent {
    data class NavigateTo(val route: String, val popUpTo: String? = null, val inclusive: Boolean = false, val singleTop: Boolean = false) : NavigationEvent()
    object PopBackStack : NavigationEvent()
    object NavigateUp : NavigationEvent()
}