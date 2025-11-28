package com.example.pasteleriaapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.pasteleriaapp.R
import com.example.pasteleriaapp.util.formatPrice
import com.example.pasteleriaapp.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(itemId: String, mainViewModel: MainViewModel) {
    val products by mainViewModel.products.collectAsState()

    // ✅ CORRECCIÓN 1: Convertir la String del argumento a Long, no a Int.
    val productIdLong = itemId.toLongOrNull()

    // Buscamos el producto usando el ID Long
    val product = products.find { it.id == productIdLong }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = product?.name ?: "Detalle del Producto", style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = { mainViewModel.navigateUp() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (product != null) {
                AsyncImage(
                    model = "http://10.0.2.2:9090/" + product.img,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.milsabores)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // ✅ CORRECCIÓN 2: Usar la propiedad 'name' o 'category' para un texto descriptivo
                // Asumiendo que tu modelo Product NO tiene la propiedad 'description',
                // mostramos el nombre del producto o la categoría como sustituto.
                // Si SÍ tienes la propiedad 'description', úsala aquí.
                Text(
                    text = "${product.name} (${product.category})", // Mostrar nombre y categoría
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        mainViewModel.addToCart(product)
                        // Ya no necesitas Toast si el ViewModel emite UiEvent.ShowSnackbar
                        // Pero si quieres mantenerlo, está bien:
                        Toast.makeText(context, "${product.name} añadido al carrito", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Añadir al Carrito (${formatPrice(product.price)})")
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Producto no encontrado")
                }
            }
        }
    }
}