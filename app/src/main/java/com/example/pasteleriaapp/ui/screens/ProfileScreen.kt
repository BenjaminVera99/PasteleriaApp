package com.example.pasteleriaapp.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.pasteleriaapp.viewmodel.MainViewModel
import com.example.pasteleriaapp.viewmodel.UsuarioViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreen(mainViewModel: MainViewModel, usuarioViewModel: UsuarioViewModel) {
    val userState by usuarioViewModel.estado.collectAsState()
    val currentUser by mainViewModel.currentUser.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Carga los datos del usuario del MainViewModel al UsuarioViewModel cuando la pantalla se muestra o los datos cambian
    LaunchedEffect(currentUser) {
        currentUser?.let { usuarioViewModel.onUserLoaded(it) }
    }

    // --- Lanzadores para la cámara y la galería ---
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempImageUri?.let { uri ->
                    usuarioViewModel.updateProfilePicture(uri, context) {
                        mainViewModel.onUserUpdated(it) // Sincroniza con MainViewModel
                    }
                }
            }
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                usuarioViewModel.updateProfilePicture(it, context) {
                    mainViewModel.onUserUpdated(it) // Sincroniza con MainViewModel
                }
            }
        }
    )

    // --- Permisos ---
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val galleryPermissionState = rememberPermissionState(Manifest.permission.READ_MEDIA_IMAGES)

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Foto de perfil") },
            text = { Text("Elige una opción para tu foto de perfil.") },
            confirmButton = {
                TextButton(onClick = {
                    if (cameraPermissionState.status.isGranted) {
                        val uri = createImageUri(context)
                        tempImageUri = uri
                        cameraLauncher.launch(uri)
                        showDialog = false
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                }) {
                    Text("Tomar foto")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    if (galleryPermissionState.status.isGranted) {
                        galleryLauncher.launch("image/*")
                        showDialog = false
                    } else {
                        galleryPermissionState.launchPermissionRequest()
                    }
                }) {
                    Text("Elegir de la galería")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("Mi Perfil", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(top = 32.dp))
        Spacer(modifier = Modifier.height(24.dp))

        // --- Foto de Perfil ---
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { showDialog = true },
            contentAlignment = Alignment.Center
        ) {
            if (userState.profilePictureUri != null) {
                AsyncImage(
                    model = Uri.parse(userState.profilePictureUri),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Añadir foto de perfil",
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))

        // Name (read-only)
        ProfileInfoRow(label = "Nombre", value = userState.nombre)

        // Email (editable)
        if (isEditing) {
            OutlinedTextField(
                value = userState.correo,
                onValueChange = usuarioViewModel::onCorreoChange,
                label = { Text("Correo") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            ProfileInfoRow(label = "Correo", value = userState.correo)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Address (editable)
        if (isEditing) {
            OutlinedTextField(
                value = userState.direccion,
                onValueChange = usuarioViewModel::onDireccionChange,
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            ProfileInfoRow(label = "Dirección", value = userState.direccion)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { isEditing = !isEditing },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isEditing) "Guardar Cambios" else "Editar Perfil")
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { mainViewModel.logout() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("Cerrar Sesión")
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun createImageUri(context: Context): Uri {
    val imageFile = File.createTempFile(
        "profile_pic_temp", ".jpg", context.cacheDir
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        imageFile
    )
}