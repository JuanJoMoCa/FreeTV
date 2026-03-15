package com.example.freetv.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlayerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        Box(
            modifier = Modifier
                .weight(0.75f)
                .fillMaxHeight()
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Señal de TV aquí",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .weight(0.25f)
                .fillMaxHeight()
                .background(Color(0xFF1E1E1E))
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.background(Color(0xFFD32F2F), CircleShape)
                ) {
                    Icon(Icons.Default.Home, contentDescription = "Inicio", tint = Color.White)
                }

                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.background(Color.DarkGray, CircleShape)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Configuraciones", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("CANAL", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledIconButton(
                        onClick = {  },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.DarkGray)
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Canal Anterior", tint = Color.White)
                    }
                    FilledIconButton(
                        onClick = {  },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.DarkGray)
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Canal Siguiente", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("VOLUMEN", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledIconButton(
                        onClick = {  },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.DarkGray)
                    ) {
                        Icon(Icons.Default.VolumeDown, contentDescription = "Bajar Volumen", tint = Color.White)
                    }
                    FilledIconButton(
                        onClick = {  },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.DarkGray)
                    ) {
                        Icon(Icons.Default.VolumeOff, contentDescription = "Mutear", tint = Color.White)
                    }
                    FilledIconButton(
                        onClick = {  },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.DarkGray)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Subir Volumen", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {  },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Categorías", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Categorías", fontSize = 12.sp)
            }
        }
    }
}