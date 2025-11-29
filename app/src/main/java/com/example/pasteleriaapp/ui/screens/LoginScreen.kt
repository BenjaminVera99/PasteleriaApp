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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import com.example.pasteleriaapp.R
import com.example.pasteleriaapp.navigation.AppRoute
import com.example.pasteleriaapp.viewmodel.MainViewModel
import com.example.pasteleriaapp.viewmodel.UsuarioViewModel

@Composable
fun LoginScreen(mainViewModel: MainViewModel, usuarioViewModel: UsuarioViewModel) {
    val userState by usuarioViewModel.estado.collectAsState()
    val context = LocalContext.current

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

            // --- CAMPO CORREO ---
            OutlinedTextField(
                value = userState.correo,
                onValueChange = usuarioViewModel::onCorreoChange,
                label = { Text("Correo Electrónico") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = userState.errores.correo != null,
                supportingText = {
                    userState.errores.correo?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // --- CAMPO CONTRASEÑA ---
            OutlinedTextField(
                value = userState.contrasena,
                onValueChange = usuarioViewModel::onContrasenaChange,
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                isError = userState.errores.contrasena != null,
                supportingText = {
                    userState.errores.contrasena?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            )
            Spacer(modifier = Modifier.height(24.dp))

            // --- BOTÓN ACCEDER (Lógica Remota) ---
            Button(
                // 🔑 MODIFICACIÓN CLAVE: Manejar usuario y token
                onClick = {
                    // 1. Validar campos básicos
                    if (usuarioViewModel.estaValidadoElLogin()) {
                        // 2. Llamar a la autenticación remota
                        usuarioViewModel.authenticateUser { usuario, token ->
                            if (usuario != null && token != null) {
                                // 3. ÉXITO: Iniciar sesión en el MainViewModel y navegar
                                mainViewModel.login(usuario)
                                mainViewModel.navigateTo(AppRoute.Home.route)
                            }
                            // 4. FALLO: No hacemos Toast. El error ya se muestra en el OutlinedTextField
                            // gracias a userState.errores.correo, que el ViewModel actualiza
                            // con el mensaje de error del backend (ej: "Credenciales incorrectas").
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