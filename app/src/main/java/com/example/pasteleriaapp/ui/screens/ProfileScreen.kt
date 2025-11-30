package com.example.pasteleriaapp.ui.screens

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday // Usamos CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Constante para el formato de fecha (DD-MM-AAAA)
private const val DATE_FORMAT = "dd-MM-yyyy"

@Composable
fun ProfileScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    usuarioViewModel: UsuarioViewModel
) {
    val userState by usuarioViewModel.estado.collectAsState()
    val currentUser by mainViewModel.currentUser.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    var showPhotoDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var tempImageUri by remember { mutableStateOf<Uri?>(null) } // URI temporal para la cámara


    // Cargar los datos del usuario actual al ViewModel cuando la pantalla se carga
    LaunchedEffect(Unit) {
        usuarioViewModel.loadCurrentUserProfile()
    }

    // --- Launchers y Permisos (Código completo) ---

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempImageUri?.let {
                    usuarioViewModel.updateProfilePicture(it) { updatedUser ->
                        mainViewModel.onUserUpdated(updatedUser)
                    }
                }
            }
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                // Persistir el permiso de lectura si es necesario (para acceder a la imagen después)
                val flag = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(it, flag)
                usuarioViewModel.updateProfilePicture(it) { updatedUser ->
                    mainViewModel.onUserUpdated(updatedUser)
                }
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                val uri = createImageUri(context)
                tempImageUri = uri
                cameraLauncher.launch(uri)
            } else {
                Toast.makeText(context, "Permiso de cámara denegado.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                galleryLauncher.launch(arrayOf("image/*"))
            } else {
                Toast.makeText(context, "Permiso de galería denegado.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // --- Date Picker Dialog ---
    val datePickerDialog = remember {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }.time
                val formatter = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
                usuarioViewModel.onFechaNacimientoChange(formatter.format(selectedDate))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = Date().time // No permitir seleccionar fechas futuras
        }
    }

    // --- DIÁLOGOS DE ALERTA ---

    if (showPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoDialog = false },
            title = { Text("Cambiar Foto de Perfil") },
            text = { Text("Selecciona una opción para cambiar tu imagen de perfil.") },
            confirmButton = {
                TextButton(onClick = {
                    showPhotoDialog = false
                    // Lógica para abrir la cámara
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }) {
                    Text("Abrir Cámara")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPhotoDialog = false
                    // Lógica para abrir la galería (permisos condicionales por versión de Android)
                    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Manifest.permission.READ_MEDIA_IMAGES
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    }
                    if (ContextCompat.checkSelfPermission(context, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        galleryLauncher.launch(arrayOf("image/*"))
                    } else {
                        galleryPermissionLauncher.launch(permission)
                    }
                }) {
                    Text("Elegir de Galería")
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Eliminar Cuenta") },
            text = { Text("¿Estás seguro de que quieres eliminar tu cuenta? Esta acción es irreversible.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmDialog = false
                    usuarioViewModel.deleteCurrentUser { isSuccess, message ->
                        if (isSuccess) {
                            mainViewModel.logout() // Limpieza de estado global
                        }
                        Toast.makeText(context, message ?: "Operación completada.", Toast.LENGTH_SHORT).show()
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

    // --- ESTRUCTURA PRINCIPAL DE LA PANTALLA ---

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("Mi Perfil", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(top = 32.dp))
        Spacer(modifier = Modifier.height(24.dp))

        // Foto de Perfil con opción de edición
        Box {
            ImagenInteligente(
                imageUri = userState.profilePictureUri,
                size = 150.dp,
                modifier = Modifier
                    .clickable(enabled = isEditing) { showPhotoDialog = true }
                    .then(
                        if (isEditing) Modifier.border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ) else Modifier
                    )
            )
            if (isEditing) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar foto",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(8.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- CAMPOS DE PERFIL (Nombre, Apellidos, Correo, Dirección, FechaNac) ---

        // 1. Nombre
        ProfileField(
            isEditing = isEditing,
            label = "Nombre",
            value = userState.nombre,
            onValueChange = usuarioViewModel::onNombreChange,
            error = userState.errores.nombre
        )

        // 2. Apellidos
        ProfileField(
            isEditing = isEditing,
            label = "Apellidos",
            value = userState.apellidos,
            onValueChange = usuarioViewModel::onApellidosChange,
            error = userState.errores.apellidos,
            placeholder = "No especificado"
        )

        // 3. Correo (NO EDITABLE)
        ProfileField(
            isEditing = isEditing,
            label = "Correo",
            value = userState.correo,
            onValueChange = usuarioViewModel::onCorreoChange,
            readOnly = true, // Correo solo lectura
            placeholder = "No editable"
        )

        // 4. Dirección
        if (isEditing) {
            ProfileField(
                isEditing = true,
                label = "Dirección",
                value = userState.direccion,
                onValueChange = usuarioViewModel::onDireccionChange,
                error = userState.errores.direccion,
                placeholder = "No especificado"
            )
        } else {
            // ⭐ CORRECCIÓN: Usamos ProfileInfoRow DIRECTAMENTE (como la fecha)
            ProfileInfoRow(
                label = "Dirección",
                value = userState.direccion.ifBlank { "No especificado" }
            )
        }

        // 5. Fecha de Nacimiento (con DatePicker)
        if (isEditing) {
            OutlinedTextField(
                value = userState.fechaNacimiento,
                onValueChange = { /* Solo se cambia con el DatePicker */ },
                label = { Text("Fecha Nacimiento (DD-MM-AAAA)") },
                readOnly = true,
                trailingIcon = {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = "Seleccionar fecha",
                        modifier = Modifier.clickable { datePickerDialog.show() }
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
        } else {
            ProfileInfoRow(
                label = "Fecha de Nacimiento",
                value = userState.fechaNacimiento.ifBlank { "No especificado" }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Botones de Acción ---
        if (isEditing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        // Revertir cambios con los datos originales
                        currentUser?.let { usuarioViewModel.onUserLoaded(it) }
                        isEditing = false
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick = {
                        // Guardar cambios llamando a la función del ViewModel
                        usuarioViewModel.saveChanges { updatedUser ->
                            mainViewModel.onUserUpdated(updatedUser)
                            Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                        }
                        isEditing = false
                    },
                    modifier = Modifier.weight(1f),
                    // Habilitado si el formulario de perfil está validado
                    enabled = usuarioViewModel.estaValidadoElPerfil()
                ) {
                    Text("Guardar Cambios")
                }
            }
        } else {
            Button(
                onClick = { isEditing = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Editar Perfil")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Cerrar Sesión
        Button(
            onClick = {
                mainViewModel.logout() // Limpia estado global
                usuarioViewModel.logout() // Limpia token y navega
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Cerrar Sesión")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Eliminar Cuenta
        TextButton(onClick = { showDeleteConfirmDialog = true }) {
            Text("Eliminar Cuenta", color = MaterialTheme.colorScheme.error)
        }
    }
}

// --- Componentes Auxiliares ---

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    // ⭐ MODIFICACIÓN CLAVE: Manejo de "string" y "blank" ⭐
    val displayValue = if (value.isBlank() || value.equals("string", ignoreCase = true)) {
        "No especificado"
    } else {
        value
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        Text(text = displayValue, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ProfileField(
    isEditing: Boolean,
    label: String,
    value: String,
    onValueChange: (String) -> Unit = {},
    error: String? = null,
    readOnly: Boolean = false,
    placeholder: String? = null
) {
    if (isEditing) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            readOnly = readOnly,
            isError = error != null,
            supportingText = {
                if (error != null) {
                    Text(error)
                } else if (readOnly && placeholder != null) {
                    Text(placeholder)
                }
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
    } else {
        val displayValue = value.ifBlank { "No especificado" }
        ProfileInfoRow(
            label = label,
            value = displayValue
        )
    }
}

// Función auxiliar para crear la URI temporal de la cámara
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