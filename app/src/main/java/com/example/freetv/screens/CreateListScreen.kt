package com.example.freetv.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListScreen(
    viewModel: SharedTvViewModel,
    onNavigateBack: () -> Unit
) {
    var listName by remember { mutableStateOf("") }
    val selectedChannels = remember { mutableStateListOf<String>() }

    val creationState by viewModel.listCreationState.collectAsState()
    val channels by viewModel.channels.collectAsState()

    LaunchedEffect(creationState) {
        if (creationState is SharedTvViewModel.ListCreationState.Success) {
            viewModel.resetListCreationState()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Lista") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetListCreationState()
                        onNavigateBack()
                    }) {
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
                value = listName,
                onValueChange = { listName = it; viewModel.resetListCreationState() },
                label = { Text("Nombre de la carpeta personalizada") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (creationState is SharedTvViewModel.ListCreationState.Error) {
                Text(
                    text = (creationState as SharedTvViewModel.ListCreationState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Selecciona canales para añadir:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(channels) { channel ->
                    val isSelected = selectedChannels.contains(channel.streamUrl)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isSelected) selectedChannels.remove(channel.streamUrl)
                                else selectedChannels.add(channel.streamUrl)
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = channel.nombre)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = {
                    viewModel.resetListCreationState()
                    onNavigateBack()
                }) {
                    Text("Cancelar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        viewModel.createCustomList(listName, selectedChannels)
                    },
                    enabled = creationState !is SharedTvViewModel.ListCreationState.Loading
                ) {
                    if (creationState is SharedTvViewModel.ListCreationState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Text("Crear Lista")
                    }
                }
            }
        }
    }
}