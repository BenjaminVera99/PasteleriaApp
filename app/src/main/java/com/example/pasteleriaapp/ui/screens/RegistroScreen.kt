package com.example.pasteleriaapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
// ⭐ NUEVOS IMPORTS para Visibilidad ⭐
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
// ⭐ IMPORTS DE TEXTO MODIFICADOS ⭐
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pasteleriaapp.navigation.AppRoute // 🔑 Importación necesaria para la navegación
import com.example.pasteleriaapp.viewmodel.MainViewModel
import com.example.pasteleriaapp.viewmodel.UsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(usuarioViewModel: UsuarioViewModel, mainViewModel: MainViewModel) {
    val estado by usuarioViewModel.estado.collectAsState()

    val isPasswordVisible = estado.isPasswordVisible
    val passwordError = estado.errores.contrasena

    val scrollState = rememberScrollState()

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
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = estado.nombre,
                    onValueChange = usuarioViewModel::onNombreChange,
                    label = { Text("Nombre") },
                    isError = estado.errores.nombre != null,
                    singleLine = true,
                    supportingText = { estado.errores.nombre?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = estado.apellidos,
                    onValueChange = usuarioViewModel::onApellidosChange,
                    label = { Text("Apellidos") },
                    isError = estado.errores.apellidos != null,
                    singleLine = true,
                    supportingText = { estado.errores.apellidos?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = estado.correo,
                    onValueChange = usuarioViewModel::onCorreoChange,
                    label = { Text("Email") },
                    isError = estado.errores.correo != null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    supportingText = { estado.errores.correo?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = estado.contrasena,
                    onValueChange = usuarioViewModel::onContrasenaChange,
                    label = { Text("Contraseña") },
                    isError = passwordError != null,

                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),

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
                    supportingText = { passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = estado.fechaNacimiento,
                    onValueChange = usuarioViewModel::onFechaNacimientoChange,
                    label = { Text("Fecha Nac. (DD-MM-AAAA)") },
                    isError = estado.errores.fechaNacimiento != null,
                    singleLine = true,
                    supportingText = { estado.errores.fechaNacimiento?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = estado.direccion,
                    onValueChange = usuarioViewModel::onDireccionChange,
                    label = { Text("Dirección") },
                    isError = estado.errores.direccion != null,
                    singleLine = true,
                    supportingText = { estado.errores.direccion?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = estado.aceptaTerminos,
                            onCheckedChange = usuarioViewModel::onAceptarTerminosChange
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Acepto los términos y condiciones")
                    }
                    estado.errores.terminos?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        usuarioViewModel.registrarUsuario { usuarioRegistrado ->
                            if (usuarioRegistrado != null) {
                                mainViewModel.navigateTo(AppRoute.Login.route)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registrar")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    )
}