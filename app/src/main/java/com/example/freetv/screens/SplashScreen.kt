package com.example.freetv.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freetv.data.ApiClient
import com.example.freetv.data.ChannelRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CheckStatus {
    object Idle : CheckStatus()
    object Checking : CheckStatus()
    object Success : CheckStatus()
    data class Error(val message: String, val type: ErrorType) : CheckStatus()
}

enum class ErrorType {
    FIREWALL, SERVER_DOWN, NETWORK_ISSUE
}

class SplashViewModel(private val repository: ChannelRepository = ChannelRepository()) : ViewModel() {
    private val _status = MutableStateFlow<CheckStatus>(CheckStatus.Idle)
    val status = _status.asStateFlow()

    fun runDiagnostics() {
        viewModelScope.launch {
            _status.value = CheckStatus.Checking
            delay(1000)
            
            repository.getChannels()
                .onSuccess {
                    _status.value = CheckStatus.Success
                }
                .onFailure { e ->
                    val errorMsg = e.message ?: ""
                    val type = when {
                        errorMsg.contains("timeout", ignoreCase = true) -> ErrorType.FIREWALL
                        errorMsg.contains("connection refused", ignoreCase = true) -> ErrorType.SERVER_DOWN
                        else -> ErrorType.NETWORK_ISSUE
                    }
                    
                    val detailedMessage = when(type) {
                        ErrorType.FIREWALL -> "¡Posible bloqueo de Firewall! El celular no puede 'ver' tu computadora. Revisa que el puerto 8080 esté abierto."
                        ErrorType.SERVER_DOWN -> "El servidor Backend no responde. ¿Olvidaste correr .\\mvnw spring-boot:run?"
                        ErrorType.NETWORK_ISSUE -> "Problema de red. Asegúrate de que el celular y la PC estén en el mismo Wi-Fi o red local."
                    }
                    
                    _status.value = CheckStatus.Error(detailedMessage, type)
                }
        }
    }

    fun updateIp(newIp: String) {
        ApiClient.setIp(newIp)
        runDiagnostics()
    }

    fun enableUsbMode() {
        ApiClient.setUsbMode(true)
        runDiagnostics()
    }

    fun disableUsbMode() {
        ApiClient.setUsbMode(false)
        runDiagnostics()
    }
}

@Composable
fun SplashScreen(
    onSuccess: () -> Unit,
    viewModel: SplashViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val status by viewModel.status.collectAsState()
    var showIpDialog by remember { mutableStateOf(false) }
    var tempIp by remember { mutableStateOf("192.168.0.103") }

    LaunchedEffect(Unit) {
        viewModel.runDiagnostics()
    }

    LaunchedEffect(status) {
        if (status is CheckStatus.Success) {
            delay(1000)
            onSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "FreeTV",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Diagnóstico del Sistema",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            AnimatedContent(
                targetState = status,
                transitionSpec = { fadeIn() togetherWith fadeOut() }, 
                label = ""
            ) { state ->
                when (state) {
                    is CheckStatus.Checking -> DiagnosticItem(
                        icon = Icons.Default.Search,
                        text = "Verificando conexión con el Backend...",
                        loading = true
                    )
                    is CheckStatus.Success -> DiagnosticItem(
                        icon = Icons.Default.CheckCircle,
                        text = "¡Conexión exitosa! Todo listo.",
                        color = Color(0xFF4CAF50)
                    )
                    is CheckStatus.Error -> DiagnosticError(
                        message = state.message,
                        onRetry = { viewModel.runDiagnostics() },
                        onChangeMode = { showIpDialog = true }
                    )
                    else -> {}
                }
            }
        }
        
        // Footer info showing current settings
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Modo: ${if (ApiClient.isUsbModeActive()) "USB (localhost)" else "Wi-Fi (${ApiClient.getBaseUrl()})"}",
                fontSize = 11.sp,
                color = Color.Gray
            )
            if (status is CheckStatus.Error) {
                TextButton(onClick = { showIpDialog = true }) {
                    Text("Cambiar Configuración de Red", fontSize = 12.sp)
                }
            }
        }
    }

    // Settings Dialog
    if (showIpDialog) {
        AlertDialog(
            onDismissRequest = { showIpDialog = false },
            title = { Text("Configuración de Conexión") },
            text = {
                Column {
                    Text("Si estás en la escuela o usas cable USB:", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { 
                            viewModel.enableUsbMode()
                            showIpDialog = false 
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Usb, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Modo USB (localhost)")
                    }
                    Text("Recuerda correr: adb reverse tcp:8080 tcp:8080", fontSize = 10.sp, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Modo Wi-Fi (IP Manual):", fontSize = 14.sp)
                    OutlinedTextField(
                        value = tempIp,
                        onValueChange = { tempIp = it },
                        label = { Text("Nueva IP de PC") },
                        placeholder = { Text("Ej: 192.168.1.15") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { 
                            viewModel.updateIp(tempIp)
                            showIpDialog = false 
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Wifi, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Usar esta IP (Wi-Fi)")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showIpDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun DiagnosticItem(icon: ImageVector, text: String, loading: Boolean = false, color: Color = MaterialTheme.colorScheme.primary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = color
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = text, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
        if (loading) {
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(modifier = Modifier.width(150.dp))
        }
    }
}

@Composable
fun DiagnosticError(message: String, onRetry: () -> Unit, onChangeMode: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "ERROR DE CONEXIÓN",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                textAlign = TextAlign.Center,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reintentar")
                }
                OutlinedButton(
                    onClick = onChangeMode,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Configurar", fontSize = 12.sp)
                }
            }
        }
    }
}
