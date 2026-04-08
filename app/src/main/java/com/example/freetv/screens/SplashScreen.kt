package com.example.freetv.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSuccess: () -> Unit,
    sharedViewModel: SharedTvViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val isLoading by sharedViewModel.isLoading.collectAsState()
    val channels by sharedViewModel.channels.collectAsState()
    val error by sharedViewModel.error.collectAsState()

    // Automatic navigation when data is loaded
    LaunchedEffect(isLoading, channels) {
        if (!isLoading && channels.isNotEmpty()) {
            delay(1500) // Small delay for logo visibility
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
                fontSize = 56.sp,
                lineHeight = 60.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Proyecto Integrador",
                fontSize = 14.sp,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Sincronizando catálogo remoto...",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (error != null && channels.isEmpty()) {
                // Persistent error only if no local cache exists
                Text(
                    text = "No se pudo conectar. Verifica tu conexión a internet e intenta de nuevo.",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                Button(
                    onClick = { sharedViewModel.syncWithRemote() },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Reintentar")
                }
            } else {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFF4CAF50)
                )
                Text(
                    text = "Sistema Listo",
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
        
        Text(
            text = "Versión 2.0 - Arquitectura local-first",
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}
