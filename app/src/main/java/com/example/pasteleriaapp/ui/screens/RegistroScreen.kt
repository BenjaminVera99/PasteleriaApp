package com.example.pasteleriaapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.pasteleriaapp.navigation.AppRoute
import com.example.pasteleriaapp.viewmodel.MainViewModel
import com.example.pasteleriaapp.viewmodel.UsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(usuarioViewModel: UsuarioViewModel, mainViewModel: MainViewModel) {
    val estado by usuarioViewModel.estado.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrarse") },
                navigationIcon = {
                    IconButton(onClick = { mainViewModel.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver atrás"
                        )
                    }
                }
            )
        },
        containerColor = Color.White,
        content = { paddingValues ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(estado.nombre)
                OutlinedTextField(
                    value = estado.nombre,
                    onValueChange = usuarioViewModel::onNombreChange,
                    label = { Text("Nombre") },
                    isError = estado.errores.nombre != null,
                    singleLine = true,
                    supportingText = {
                        estado.errores.nombre?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = estado.correo,
                    onValueChange = usuarioViewModel::onCorreoChange,
                    label = { Text("Email") },
                    isError = estado.errores.correo != null,
                    singleLine = true,
                    supportingText = {
                        estado.errores.correo?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = estado.contrasena,
                    onValueChange = usuarioViewModel::onContrasenaChange,
                    label = { Text("Contraseña") },
                    isError = estado.errores.contrasena != null,
                    visualTransformation = PasswordVisualTransformation(),
                    supportingText = {
                        estado.errores.contrasena?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = estado.direccion,
                    onValueChange = usuarioViewModel::onDireccionChange,
                    label = { Text("Dirección") },
                    isError = estado.errores.direccion != null,
                    singleLine = true,
                    supportingText = {
                        estado.errores.direccion?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = estado.aceptaTerminos,
                        onCheckedChange = usuarioViewModel::onAceptarTerminosChange
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Acepto los términos y condiciones")
                }

                Button(
                    onClick = {
                        if (usuarioViewModel.estaValidadoElFormulario() && estado.aceptaTerminos) {
                            mainViewModel.navigateTo(
                                appRoute = AppRoute.Home,
                                popUpRoute = AppRoute.Welcome, // Changed from Register to Welcome
                                inclusive = true
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registrar")
                }
            }
        }
    )
}