package com.example.freetv.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SharedTvViewModel,
    onNavigateBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val showClearHistoryDialog by viewModel.showClearHistoryDialog.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Configuración Local", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text("Apariencia", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = "Icono de tema",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Tema Oscuro",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Cambia el esquema de colores",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { isDark ->
                            viewModel.toggleTheme(isDark)
                        }
                    )
                }
            }

            Divider()

            Text("Preferencias del Usuario", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

            PersistentSettingSwitch(
                titulo = "Aceleración por Hardware",
                descripcion = "Usa el GPU para decodificar video",
                key = "hw_acceleration",
                defaultValue = "true",
                currentSettings = settings,
                onValueChange = { viewModel.updateSetting("hw_acceleration", it) }
            )

            PersistentSettingSwitch(
                titulo = "Reproducción Automática",
                descripcion = "Inicia el siguiente canal al terminar",
                key = "autoplay",
                defaultValue = "false",
                currentSettings = settings,
                onValueChange = { viewModel.updateSetting("autoplay", it) }
            )

            Divider()

            Text("Calidad y Región", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

            PersistentSettingDropdown(
                titulo = "Calidad Máxima",
                descripcion = "Selecciona la resolución preferida",
                key = "max_resolution",
                defaultValue = "Automática",
                opciones = listOf("Automática", "1080p", "720p", "480p"),
                currentSettings = settings,
                onValueChange = { viewModel.updateSetting("max_resolution", it) }
            )

            Divider()

            Text("Mantenimiento", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

            Button(
                onClick = { viewModel.setShowClearHistoryDialog(true) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Limpiar Historial de Visualización")
            }

            if (showClearHistoryDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.setShowClearHistoryDialog(false) },
                    title = { Text("Limpiar Historial") },
                    text = { Text("¿Estás seguro de que deseas borrar tu historial de visualización?") },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearHistory() }) {
                            Text("Aceptar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.setShowClearHistoryDialog(false) }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Base de Datos Local (Room)", fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "Toda tu información se guarda de forma persistente en el dispositivo. No se utiliza servidor externo.",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Button(
                onClick = { viewModel.syncWithRemote() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Forzar Resincronización de Canales")
            }
        }
    }
}

@Composable
fun PersistentSettingSwitch(
    titulo: String,
    descripcion: String,
    key: String,
    defaultValue: String,
    currentSettings: Map<String, String>,
    onValueChange: (String) -> Unit
) {
    val isChecked = currentSettings[key]?.toBoolean() ?: defaultValue.toBoolean()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = titulo, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(text = descripcion, fontSize = 13.sp, color = Color.Gray)
        }
        Switch(checked = isChecked, onCheckedChange = { onValueChange(it.toString()) })
    }
}

@Composable
fun PersistentSettingDropdown(
    titulo: String,
    descripcion: String,
    key: String,
    defaultValue: String,
    opciones: List<String>,
    currentSettings: Map<String, String>,
    onValueChange: (String) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }
    val seleccionActual = currentSettings[key] ?: defaultValue

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expandido = true }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = titulo, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(text = descripcion, fontSize = 13.sp, color = Color.Gray)
        }

        Box {
            Text(
                text = seleccionActual,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )
            DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            onValueChange(opcion)
                            expandido = false
                        }
                    )
                }
            }
        }
    }
}