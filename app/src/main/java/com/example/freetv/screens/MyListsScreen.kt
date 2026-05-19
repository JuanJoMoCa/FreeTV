package com.example.freetv.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.freetv.data.CustomListEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyListsScreen(
    viewModel: SharedTvViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCreateList: () -> Unit,
    onNavigateToListDetail: (Long) -> Unit
) {
    val customLists by viewModel.customLists.collectAsState()

    var listToEdit by remember { mutableStateOf<CustomListEntity?>(null) }
    var listToDelete by remember { mutableStateOf<CustomListEntity?>(null) }
    var newListName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Listas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateList,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Lista")
            }
        }
    ) { padding ->
        if (customLists.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no tienes listas.\nPresiona el botón + para crear una.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(customLists) { listEntity ->
                    var expanded by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onNavigateToListDetail(listEntity.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = listEntity.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )

                            Box {
                                IconButton(onClick = { expanded = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Editar nombre") },
                                        onClick = {
                                            expanded = false
                                            listToEdit = listEntity
                                            newListName = listEntity.name
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            expanded = false
                                            listToDelete = listEntity
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (listToEdit != null) {
        AlertDialog(
            onDismissRequest = { listToEdit = null },
            title = { Text("Renombrar lista") },
            text = {
                OutlinedTextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    label = { Text("Nuevo nombre") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.renameCustomList(listToEdit!!, newListName)
                    listToEdit = null
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { listToEdit = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (listToDelete != null) {
        AlertDialog(
            onDismissRequest = { listToDelete = null },
            title = { Text("Eliminar lista") },
            text = { Text("¿Estás seguro de que deseas eliminar la lista '${listToDelete?.name}'? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCustomList(listToDelete!!)
                        listToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { listToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}