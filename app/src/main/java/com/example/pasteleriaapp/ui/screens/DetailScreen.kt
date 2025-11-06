package com.example.pasteleriaapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.pasteleriaapp.data.DataSource
import com.example.pasteleriaapp.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(itemId: String, mainViewModel: MainViewModel) {
    val product = DataSource.products.find { it.id.toString() == itemId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = product?.name ?: "Detalle del Producto") },
                navigationIcon = {
                    IconButton(onClick = { mainViewModel.navigateUp() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            if (product != null) {
                Text(text = "Detalles de ${product.name}")
            } else {
                Text(text = "Producto no encontrado")
            }
        }
    }
}