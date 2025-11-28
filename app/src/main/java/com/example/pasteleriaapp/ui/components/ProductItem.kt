package com.example.pasteleriaapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.pasteleriaapp.model.Product
import androidx.compose.ui.platform.LocalContext // ⬅️ Importación necesaria
import coil.request.ImageRequest // ⬅️ Importación necesaria

@Composable
fun ProductItem(product: Product, modifier: Modifier = Modifier) {
    // Obtener el contexto local para pasárselo al ImageRequest
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // 1. Cargador de Imagen (Coil)
        AsyncImage(
            // ⭐ ⭐ CAMBIO CLAVE: Usamos ImageRequest.Builder ⭐ ⭐
            model = ImageRequest.Builder(context)
                .data(product.fullImageUrl) // Usamos la URL que ya funciona
                .crossfade(true) // Efecto visual al cargar
                .build(),

            contentDescription = "Imagen de ${product.name}",
            modifier = Modifier
                .size(64.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        // 2. Información del Producto
        Column {
            Text(text = product.name, fontWeight = FontWeight.Bold)
            Text(text = "$${product.price} (Cód: ${product.code})")
        }
    }
    // Separador visual
    Divider()
}