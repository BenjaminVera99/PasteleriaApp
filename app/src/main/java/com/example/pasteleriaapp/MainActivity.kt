package com.example.pasteleriaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.ImageLoader // ⬅️ IMPORTACIÓN AGREGADA
import coil.ImageLoaderFactory // ⬅️ IMPORTACIÓN AGREGADA
import com.example.pasteleriaapp.navigation.AppRoute
import com.example.pasteleriaapp.navigation.NavigationEvent
import com.example.pasteleriaapp.ui.components.MainBottomBar
import com.example.pasteleriaapp.ui.screens.*
import com.example.pasteleriaapp.ui.theme.PasteleriaAppTheme
import com.example.pasteleriaapp.ui.theme.Pacifico
import com.example.pasteleriaapp.util.AppImageLoader // ⬅️ IMPORTACIÓN AGREGADA
import com.example.pasteleriaapp.viewmodel.MainViewModel
import com.example.pasteleriaapp.viewmodel.MainViewModelFactory
import com.example.pasteleriaapp.viewmodel.UsuarioViewModel
import com.example.pasteleriaapp.viewmodel.UsuarioViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
// ⭐ CORRECCIÓN CLAVE: La clase implementa ImageLoaderFactory ⭐
class MainActivity : ComponentActivity(), ImageLoaderFactory {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val mainViewModel: MainViewModel by viewModels { MainViewModelFactory(application) }
        val usuarioViewModel: UsuarioViewModel by viewModels { UsuarioViewModelFactory(application) }
        setContent {
            PasteleriaAppTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(Unit) {
                    mainViewModel.navEvents.collect { event ->
                        when (event) {
                            is NavigationEvent.NavigateTo -> {
                                navController.navigate(event.route) {
                                    event.popUpTo?.let {
                                        popUpTo(it) { inclusive = event.inclusive }
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
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    topBar = {
                        if (currentRoute == AppRoute.Home.route) {
                            TopAppBar(
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Image(
                                            painter = painterResource(id = R.drawable.milsabores),
                                            contentDescription = "Logo",
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Pastelería Mil Sabores",
                                            fontFamily = Pacifico,
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                            )
                        }
                    },
                    bottomBar = {
                        if (currentRoute in listOf(AppRoute.Home.route, AppRoute.Cart.route, AppRoute.OrderHistory.route, AppRoute.Profile.route)) {
                            MainBottomBar(navController = navController, mainViewModel = mainViewModel)
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = AppRoute.Welcome.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(AppRoute.Welcome.route) { WelcomeScreen(mainViewModel = mainViewModel) }
                        composable(AppRoute.Login.route) { LoginScreen(mainViewModel = mainViewModel, usuarioViewModel = usuarioViewModel) }
                        composable(AppRoute.Register.route) { RegistroScreen(usuarioViewModel = usuarioViewModel, mainViewModel = mainViewModel) }
                        composable(AppRoute.Home.route) { HomeScreen(viewModel = mainViewModel, snackbarHostState = snackbarHostState) }
                        composable(AppRoute.Profile.route) { ProfileScreen(mainViewModel = mainViewModel, usuarioViewModel = usuarioViewModel) }
                        composable(AppRoute.Cart.route) { CartScreen(mainViewModel = mainViewModel) }
                        composable(AppRoute.Checkout.route) { CheckoutScreen(mainViewModel = mainViewModel, usuarioViewModel = usuarioViewModel) }
                        composable(AppRoute.OrderHistory.route) { OrderHistoryScreen(mainViewModel = mainViewModel) }
                        composable(AppRoute.OrderConfirmation.route) { OrderConfirmationScreen(mainViewModel = mainViewModel) }
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
    override fun newImageLoader(): ImageLoader {
        return AppImageLoader(application).newImageLoader()
    }
}