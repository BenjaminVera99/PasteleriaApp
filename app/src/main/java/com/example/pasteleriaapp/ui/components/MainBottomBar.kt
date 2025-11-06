package com.example.pasteleriaapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.pasteleriaapp.navigation.AppRoute
import com.example.pasteleriaapp.viewmodel.MainViewModel

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem(AppRoute.Home.route, Icons.Default.Home, "Catálogo")
    object Cart : BottomNavItem(AppRoute.Cart.route, Icons.Default.ShoppingCart, "Carrito")
    object OrderHistory : BottomNavItem(AppRoute.OrderHistory.route, Icons.Default.List, "Mis Pedidos")
    object Profile : BottomNavItem(AppRoute.Profile.route, Icons.Default.Person, "Mi Perfil")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainBottomBar(navController: NavController, mainViewModel: MainViewModel) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isLoggedIn by mainViewModel.isLoggedIn.collectAsState()
    val cartItemCount by mainViewModel.cartItemCount.collectAsState()

    val navigationItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Cart,
        BottomNavItem.OrderHistory,
        BottomNavItem.Profile
    )

    NavigationBar {
        navigationItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                icon = {
                    if (item.route == AppRoute.Cart.route) {
                        BadgedBox(badge = {
                            if (cartItemCount > 0) {
                                Badge { Text(cartItemCount.toString()) }
                            }
                        }) {
                            Icon(item.icon, contentDescription = item.label)
                        }
                    } else {
                        Icon(item.icon, contentDescription = item.label)
                    }
                },
                label = { Text(item.label) },
                selected = selected,
                alwaysShowLabel = false, // Only show label for selected item
                onClick = {
                    if (item.route == AppRoute.Profile.route && !isLoggedIn) {
                        mainViewModel.navigateTo(AppRoute.Welcome)
                    } else {
                        navController.navigate(item.route) {
                            navController.graph.startDestinationRoute?.let {
                                popUpTo(it) {
                                    saveState = true
                                }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                )
            )
        }
    }
}