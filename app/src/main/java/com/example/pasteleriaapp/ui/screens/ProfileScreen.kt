package com.example.pasteleriaapp.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.example.pasteleriaapp.ui.components.ImagenInteligente
import com.example.pasteleriaapp.viewmodel.MainViewModel
import com.example.pasteleriaapp.viewmodel.UsuarioViewModel
import java.io.File


@Composable
fun ProfileScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    usuarioViewModel: UsuarioViewModel
) {
    val userState by usuarioViewModel.estado.collectAsState()
    val currentUser by mainViewModel.currentUser.collectAsState()

    // Si el usuario actual es nulo, deberíamos redirigir o mostrar un mensaje.
    // Para simplificar, asumiremos que el NavHost ya maneja la redirección.

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // --- Launchers y Permisos (Mantenemos la estructura simplificada) ---
    // NO ES NECESARIO MOSTRAR TODA LA LÓGICA DE CÁMARA/GALERÍA AQUÍ.

    // --- Diálogo de Eliminación de Cuenta ---
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Eliminar Cuenta") },
            text = { Text("¿Estás seguro de que quieres eliminar tu cuenta? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmDialog = false

                    // Llama a la función del ViewModel que elimina en el servidor
                    usuarioViewModel.deleteCurrentUser { isSuccess, message ->
                        if (isSuccess) {
                            // 1. LIMPIEZA CRUCIAL: Solo llamamos a logout en MainVM para notificar el estado global
                            mainViewModel.logout()

                            // 2. Navegación: El UsuarioViewModel ya emite un evento NavigateTo(Home)

                            // 3. Mostrar mensaje de éxito
                            Toast.makeText(context, message ?: "Cuenta eliminada exitosamente.", Toast.LENGTH_SHORT).show()

                        } else {
                            // Si la eliminación falló (ej. error 401, error de red):
                            Toast.makeText(context, message ?: "Fallo al eliminar.", Toast.LENGTH_LONG).show()
                        }
                    }
                }) {
                    Text("Confirmar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    // --- Fin del Diálogo ---


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        Text("Mi Perfil", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(top = 32.dp))
        Spacer(modifier = Modifier.height(24.dp))

        // Foto de Perfil simplificada
        Box {
            ImagenInteligente(
                imageUri = userState.profilePictureUri,
                size = 150.dp,
                modifier = Modifier
            )
            // Ya no mostramos el ícono de edición
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Detalles del Usuario (Vista simple) ---
        // Usamos el currentUser directamente para evitar el estado complejo de edición
        currentUser?.let { user ->
            ProfileInfoRow(label = "Nombre", value = user.nombre.ifBlank { "N/A" })
            ProfileInfoRow(label = "Correo", value = user.correo.ifBlank { "N/A" })
            ProfileInfoRow(label = "Dirección", value = user.direccion.ifBlank { "N/A" })
            // Nota: Apellidos y FechaNacimiento no se muestran para simplificar
        } ?: run {
            Text("Cargando datos del perfil...", color = Color.Gray)
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- Botones de Acción ---

        // El botón de Editar Perfil solo es un placeholder
        Button(
            onClick = {
                Toast.makeText(context, "Funcionalidad de edición deshabilitada temporalmente.", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Editar Perfil")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ⭐ RESTAURAMOS LA FUNCIÓN DE CERRAR SESIÓN ⭐
        Button(
            onClick = {
                // 1. Llama al logout del MainViewModel para limpiar el estado global.
                mainViewModel.logout() // <--- ⭐ LLAMAR A MAINVIEWMODEL ⭐

                // 2. Llama al logout del UsuarioViewModel para limpiar el token y navegar.
                usuarioViewModel.logout()

                Toast.makeText(context, "Sesión cerrada.", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Cerrar Sesión")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ⭐ RESTAURAMOS LA FUNCIÓN DE ELIMINAR CUENTA ⭐
        TextButton(onClick = { showDeleteConfirmDialog = true }) {
            Text("Eliminar Cuenta", color = MaterialTheme.colorScheme.error)
        }
    }
}

// Composable auxiliar
@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

// Función auxiliar
private fun createImageUri(context: Context): Uri {
    val imageFile = File.createTempFile(
        "profile_pic_temp_", ".jpg", context.cacheDir
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        imageFile
    )
}