package com.example.pasteleriaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pasteleriaapp.navigation.AppRoute
import com.example.pasteleriaapp.navigation.NavigationEvent
import com.example.pasteleriaapp.ui.components.MainBottomBar
import com.example.pasteleriaapp.ui.screens.CartScreen
import com.example.pasteleriaapp.ui.screens.CheckoutScreen
import com.example.pasteleriaapp.ui.screens.DetailScreen
import com.example.pasteleriaapp.ui.screens.HomeScreen
import com.example.pasteleriaapp.ui.screens.LoginScreen
import com.example.pasteleriaapp.ui.screens.OrderHistoryScreen
import com.example.pasteleriaapp.ui.screens.ProfileScreen
import com.example.pasteleriaapp.ui.screens.RegistroScreen
import com.example.pasteleriaapp.ui.screens.WelcomeScreen
import com.example.pasteleriaapp.ui.theme.PasteleriaAppTheme
import com.example.pasteleriaapp.viewmodel.MainViewModel
import com.example.pasteleriaapp.viewmodel.UsuarioViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val mainViewModel: MainViewModel by viewModels()
        val usuarioViewModel: UsuarioViewModel by viewModels()
        setContent {
            PasteleriaAppTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                LaunchedEffect(Unit) {
                    mainViewModel.navEvents.collect { event ->
                        when (event) {
                            is NavigationEvent.NavigateTo -> {
                                navController.navigate(event.appRoute.route) {
                                    event.popUpRoute?.let {
                                        popUpTo(it.route) {
                                            inclusive = event.inclusive
                                        }
                                    }
                                    launchSingleTop = event.singleTop
                                }
                            }
                            NavigationEvent.PopBackStack -> navController.popBackStack()
                            NavigationEvent.NavigateUp -> navController.navigateUp()
                        }
                    }
                }

                Scaffold(
                    bottomBar = {
                        if (currentRoute in listOf(AppRoute.Home.route, AppRoute.OrderHistory.route, AppRoute.Profile.route)) {
                            MainBottomBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = AppRoute.Welcome.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(AppRoute.Welcome.route) {
                            WelcomeScreen(mainViewModel = mainViewModel)
                        }
                        composable(AppRoute.Login.route) {
                            LoginScreen(mainViewModel = mainViewModel)
                        }
                        composable(AppRoute.Register.route) {
                            RegistroScreen(
                                usuarioViewModel = usuarioViewModel,
                                mainViewModel = mainViewModel
                            )
                        }
                        composable(AppRoute.Home.route) {
                            HomeScreen(viewModel = mainViewModel)
                        }
                        composable(AppRoute.Profile.route) {
                            ProfileScreen(
                                mainViewModel = mainViewModel, 
                                usuarioViewModel = usuarioViewModel
                            )
                        }
                        composable(AppRoute.Cart.route) {
                            CartScreen(mainViewModel = mainViewModel)
                        }
                        composable(AppRoute.Checkout.route) {
                            CheckoutScreen(
                                mainViewModel = mainViewModel,
                                usuarioViewModel = usuarioViewModel
                            )
                        }
                        composable(AppRoute.OrderHistory.route) {
                            OrderHistoryScreen(mainViewModel = mainViewModel)
                        }
                        composable(
                            route = AppRoute.Detail.routePattern,
                            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
                            DetailScreen(itemId = itemId, mainViewModel = mainViewModel)
                        }
                    }
                }
            }
        }
    }
}