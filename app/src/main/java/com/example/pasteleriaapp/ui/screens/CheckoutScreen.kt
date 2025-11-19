package com.example.pasteleriaapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pasteleriaapp.util.formatPrice
import com.example.pasteleriaapp.viewmodel.MainViewModel
import com.example.pasteleriaapp.viewmodel.UsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(mainViewModel: MainViewModel, usuarioViewModel: UsuarioViewModel) {
    val cartItems by mainViewModel.uiCartItems.collectAsState()
    val total by mainViewModel.cartTotal.collectAsState()
    val userState by usuarioViewModel.estado.collectAsState()
    val isLoggedIn by mainViewModel.isLoggedIn.collectAsState()
    val context = LocalContext.current
    var hasValidationError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finalizar Compra") },
                navigationIcon = {
                    IconButton(onClick = { mainViewModel.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Resumen del Pedido", style = typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                LazyColumn(modifier = Modifier.padding(16.dp).height(150.dp)) {
                    items(cartItems) { item ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${item.quantity}x ${item.product.name}")
                            Text(formatPrice(item.product.price * item.quantity))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Total: ${formatPrice(total)}",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Información de Envío", style = typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = userState.direccion,
                onValueChange = {
                    usuarioViewModel.onDireccionChange(it)
                    hasValidationError = false // Limpia el error al empezar a escribir
                },
                label = { Text("Dirección de Envío") },
                modifier = Modifier.fillMaxWidth(),
                isError = hasValidationError && userState.direccion.isBlank(),
                supportingText = {
                    if (hasValidationError && userState.direccion.isBlank()) {
                        Text("La dirección de envío es obligatoria", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { 
                    if (userState.direccion.isBlank()) {
                        hasValidationError = true
                    } else {
                        val buyerName = if (isLoggedIn) userState.nombre else "Invitado"
                        val buyerEmail = if (isLoggedIn) userState.correo else ""
                        val success = mainViewModel.placeOrder(userState.direccion, buyerName, buyerEmail)
                        if (success) {
                            Toast.makeText(context, "¡Pedido realizado con éxito!", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar Pedido")
            }
        }
    }
}