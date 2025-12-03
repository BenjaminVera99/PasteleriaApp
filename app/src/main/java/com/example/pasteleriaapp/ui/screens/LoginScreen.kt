package com.example.pasteleriaapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
// ⭐ NUEVOS IMPORTS para Visibilidad ⭐
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
// ⭐ NUEVOS IMPORTS para Visibilidad ⭐
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pasteleriaapp.R
import com.example.pasteleriaapp.navigation.AppRoute
import com.example.pasteleriaapp.viewmodel.MainViewModel
import com.example.pasteleriaapp.viewmodel.UsuarioViewModel

@Composable
fun LoginScreen(mainViewModel: MainViewModel, usuarioViewModel: UsuarioViewModel) {
    val userState by usuarioViewModel.estado.collectAsState()
    val context = LocalContext.current

    val isPasswordVisible = userState.isPasswordVisible
    val passwordError = userState.errores.contrasena

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.milsabores),
                contentDescription = "Logo Pastelería",
                modifier = Modifier.size(200.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = userState.correo,
                onValueChange = usuarioViewModel::onCorreoChange,
                label = { Text("Correo Electrónico") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), // Mejor UX: teclado de email
                isError = userState.errores.correo != null,
                supportingText = {
                    userState.errores.correo?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = userState.contrasena,
                onValueChange = usuarioViewModel::onContrasenaChange,
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),

                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),

                singleLine = true,

                trailingIcon = {
                    val image = if (isPasswordVisible)
                        Icons.Filled.Visibility
                    else
                        Icons.Filled.VisibilityOff

                    val description = if (isPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña"

                    IconButton(onClick = { usuarioViewModel.togglePasswordVisibility() }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                },

                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), // Teclado de contraseña

                isError = passwordError != null,
                supportingText = {
                    passwordError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (usuarioViewModel.estaValidadoElLogin()) {
                        usuarioViewModel.authenticateUser { usuario, token ->
                            if (usuario != null && token != null) {
                                mainViewModel.login(usuario)
                                mainViewModel.navigateTo(AppRoute.Home.route)
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Acceder")
            }
        }

        IconButton(
            onClick = { mainViewModel.navigateUp() },
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
        }
    }
}