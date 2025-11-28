package com.example.pasteleriaapp.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.pasteleriaapp.R
import com.example.pasteleriaapp.navigation.AppRoute
import com.example.pasteleriaapp.ui.model.UiCartItem
import com.example.pasteleriaapp.util.formatPrice
import com.example.pasteleriaapp.viewmodel.MainViewModel

@Composable
fun CartScreen(mainViewModel: MainViewModel) {
    val cartItems by mainViewModel.uiCartItems.collectAsState()
    val total by mainViewModel.cartTotal.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Mi Carrito",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (cartItems.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "El carrito está vacío")
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(cartItems) { item ->
                    CartItemRow(
                        item = item,
                        onIncrease = { mainViewModel.increaseCartItem(item.product.id) },
                        onDecrease = { mainViewModel.decreaseCartItem(item.product.id) },
                        onRemove = { mainViewModel.removeFromCart(item.product.id) }
                    )
                }
            }
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                text = "Total: ${formatPrice(total)}",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.End)
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Button(
                onClick = { mainViewModel.navigateTo(AppRoute.Checkout.route) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Pagar")
            }
        }
    }
}

@Composable
fun CartItemRow(item: UiCartItem, onIncrease: () -> Unit, onDecrease: () -> Unit, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = "http://10.0.2.2:9090/" + item.product.img,
                contentDescription = item.product.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.milsabores)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = item.product.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrease) {
                    Box(
                        modifier = Modifier
                            .width(14.dp)
                            .height(2.dp)
                            .background(LocalContentColor.current)
                    )
                }
                Text(
                    text = "${item.quantity}",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(onClick = onIncrease) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir uno")
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar producto")
                }
            }
        }
    }
}
