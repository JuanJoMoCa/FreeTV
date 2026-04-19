package com.example.freetv.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChannelScreen(
    viewModel: SharedTvViewModel,
    onNavigateBack: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Añadir Canal") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Cancelar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it; errorMessage = null },
                label = { Text("Nombre del canal") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = url,
                onValueChange = { url = it; errorMessage = null },
                label = { Text("URL (m3u8)") },
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onNavigateBack) { // FA-02: Cancelar
                    Text("Cancelar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    // FA-01: Campos vacíos
                    if (nombre.isBlank() || url.isBlank()) {
                        errorMessage = "Campos obligatorios"
                    }
                    // Ex-01: Formato de URL inválido
                    else if (!url.trim().endsWith(".m3u8", ignoreCase = true)) {
                        errorMessage = "Error: Formato no compatible"
                    }
                    // Flujo normal: Guardar
                    else {
                        viewModel.addCustomChannel(nombre.trim(), url.trim())
                        onNavigateBack()
                    }
                }) {
                    Text("Guardar")
                }
            }
        }
    }
}