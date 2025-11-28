package com.example.pasteleriaapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.pasteleriaapp.R
import com.example.pasteleriaapp.model.Product
import com.example.pasteleriaapp.navigation.AppRoute
import com.example.pasteleriaapp.ui.theme.Pacifico
import com.example.pasteleriaapp.util.formatPrice
import com.example.pasteleriaapp.viewmodel.MainViewModel
import com.example.pasteleriaapp.viewmodel.UiEvent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeScreen(viewModel: MainViewModel, snackbarHostState: SnackbarHostState) {
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(key1 = true) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message
                    )
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (isLoading && products.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (products.isEmpty()) {
            Text(
                text = "No hay productos disponibles en este momento.",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(products) { product ->
                    ProductCard(
                        product = product,
                        onCardClick = { viewModel.navigateTo(AppRoute.Detail.createRoute(product.id.toString())) },
                        onAddToCartClick = { viewModel.addToCart(product) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, onCardClick: () -> Unit, onAddToCartClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = "http://10.0.2.2:9090/" + product.img,
                contentDescription = product.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.milsabores)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatPrice(product.price),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { onAddToCartClick() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Añadir")
            }
        }
    }
}