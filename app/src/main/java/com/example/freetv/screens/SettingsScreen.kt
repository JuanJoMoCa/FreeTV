package com.example.freetv.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class SettingsViewModel : ViewModel() {
    var subtitulosActivados by mutableStateOf(false)
        private set
    var aceleracionHardware by mutableStateOf(true)
        private set
    var resolucionSeleccionada by mutableStateOf("Automática")
        private set
    var idiomaSeleccionado by mutableStateOf("Español (Latino)")
        private set

    fun toggleSubtitulos(activo: Boolean) { subtitulosActivados = activo }
    fun toggleAceleracion(activo: Boolean) { aceleracionHardware = activo }
    fun setResolucion(res: String) { resolucionSeleccionada = res }
    fun setIdioma(idioma: String) { idiomaSeleccionado = idioma }

    fun limpiarCache() {
        println("Limpiando caché de canales...")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuraciones", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
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
            Text("Reproducción de Video", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

            SettingDropdown(
                titulo = "Calidad de Video",
                descripcion = "Reduce la calidad si tu internet es lento",
                opciones = listOf("Automática", "1080p", "720p", "480p"),
                seleccionActual = viewModel.resolucionSeleccionada,
                onSeleccion = { viewModel.setResolucion(it) }
            )

            SettingSwitch(
                titulo = "Aceleración por hardware",
                descripcion = "Mejora el rendimiento del video (desactívalo si la pantalla parpadea)",
                checado = viewModel.aceleracionHardware,
                onCambio = { viewModel.toggleAceleracion(it) }
            )

            Divider()

            Text("Audio y Subtítulos", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

            SettingDropdown(
                titulo = "Idioma de Audio Preferido",
                descripcion = "Se aplicará si el canal tiene múltiples pistas de audio",
                opciones = listOf("Español (Latino)", "Inglés", "Idioma Original"),
                seleccionActual = viewModel.idiomaSeleccionado,
                onSeleccion = { viewModel.setIdioma(it) }
            )

            SettingSwitch(
                titulo = "Mostrar Subtítulos",
                descripcion = "Activa los subtítulos cerrados (Closed Captions) si están disponibles",
                checado = viewModel.subtitulosActivados,
                onCambio = { viewModel.toggleSubtitulos(it) }
            )

            Divider()

            Text("Avanzado", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

            Button(
                onClick = { viewModel.limpiarCache() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Limpiar caché de canales y reiniciar lista")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SettingSwitch(titulo: String, descripcion: String, checado: Boolean, onCambio: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = titulo, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Text(text = descripcion, fontSize = 14.sp, color = Color.Gray)
        }
        Switch(checked = checado, onCheckedChange = onCambio)
    }
}

@Composable
fun SettingDropdown(titulo: String, descripcion: String, opciones: List<String>, seleccionActual: String, onSeleccion: (String) -> Unit) {
    var expandido by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expandido = true }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = titulo, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Text(text = descripcion, fontSize = 14.sp, color = Color.Gray)
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
                            onSeleccion(opcion)
                            expandido = false
                        }
                    )
                }
            }
        }
    }
}