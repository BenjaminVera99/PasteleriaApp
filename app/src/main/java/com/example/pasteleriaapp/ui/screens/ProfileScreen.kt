package com.example.pasteleriaapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pasteleriaapp.viewmodel.MainViewModel
import com.example.pasteleriaapp.viewmodel.UsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(mainViewModel: MainViewModel, usuarioViewModel: UsuarioViewModel) {
    val userState by usuarioViewModel.estado.collectAsState()
    var isEditing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("Mi Perfil", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(top = 32.dp))
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
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}