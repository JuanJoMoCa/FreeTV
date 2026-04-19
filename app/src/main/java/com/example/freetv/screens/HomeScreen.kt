package com.example.freetv.screens

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.freetv.data.Channel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: SharedTvViewModel,
    onNavigateToPlayer: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAddChannel: () -> Unit
) {
    val channels by viewModel.channels.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val recents by viewModel.recents.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Todas") }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    if (isSearchActive) {
                        TextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                viewModel.searchChannels(it)
                            },
                            placeholder = { Text("Buscar canales...", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    } else {
                        Column {
                            Text("FreeTV", fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                            Text("Tu televisión local-first", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        isSearchActive = !isSearchActive 
                        if (!isSearchActive) {
                            searchQuery = ""
                            viewModel.loadChannels()
                        }
                    }) {
                        Icon(if (isSearchActive) Icons.Default.Close else Icons.Default.Search, contentDescription = "Buscar")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Perfil")
                    }
                    IconButton(onClick = onNavigateToAddChannel) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar Canal")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { 
                                selectedCategory = category
                                viewModel.selectCategory(category)
                            },
                            label = { Text(category) },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            if (recents.isNotEmpty() && searchQuery.isEmpty() && selectedCategory == "Todas") {
                item {
                    SectionHeader("Continuar Viendo")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(recents) { channel ->
                            RecentChannelCard(channel, onClick = { onNavigateToPlayer(channel.streamUrl) })
                        }
                    }
                }
            }

            if (favorites.isNotEmpty() && searchQuery.isEmpty() && selectedCategory == "Todas") {
                item {
                    SectionHeader("Mis Favoritos")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(favorites) { channel ->
                            ChannelCardLarge(
                                channel = channel, 
                                onClick = { onNavigateToPlayer(channel.streamUrl) },
                                onToggleFav = { viewModel.toggleFavorite(channel) }
                            )
                        }
                    }
                }
            }

            item {
                SectionHeader(if (searchQuery.isNotEmpty()) "Resultados de búsqueda" else "Todos los Canales")
            }

            item {
                val configuration = LocalConfiguration.current
                val columns = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 4 else 2
                val chunks = channels.chunked(columns)
                Column(Modifier.padding(horizontal = 16.dp)) {
                    chunks.forEach { rowChannels ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowChannels.forEach { channel ->
                                Box(Modifier.weight(1f)) {
                                    ChannelCardCompact(
                                        channel = channel, 
                                        onClick = { onNavigateToPlayer(channel.streamUrl) },
                                        onToggleFav = { viewModel.toggleFavorite(channel) }
                                    )
                                }
                            }
                            repeat(columns - rowChannels.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp),
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun RecentChannelCard(channel: Channel, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(160.dp).height(90.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(model = channel.logoUrl, contentDescription = null, modifier = Modifier.fillMaxSize().padding(16.dp), contentScale = ContentScale.Fit)
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)))))
            Text(text = channel.nombre, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.BottomStart).padding(8.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun ChannelCardLarge(channel: Channel, onClick: () -> Unit, onToggleFav: () -> Unit) {
    Card(
        modifier = Modifier.width(140.dp).shadow(8.dp, RoundedCornerShape(16.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box {
                AsyncImage(model = channel.logoUrl, contentDescription = channel.nombre, modifier = Modifier.fillMaxWidth().height(120.dp).padding(12.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Fit)
                IconButton(onClick = onToggleFav, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).background(Color.Black.copy(alpha = 0.2f), CircleShape).size(32.dp)) {
                    Icon(imageVector = if (channel.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "Favorito", tint = if (channel.isFavorite) Color.Red else Color.White, modifier = Modifier.size(18.dp))
                }
            }
            Text(text = channel.nombre, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = channel.categoria, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(bottom = 8.dp))
        }
    }
}

@Composable
fun ChannelCardCompact(channel: Channel, onClick: () -> Unit, onToggleFav: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().height(180.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.weight(1f)) {
                AsyncImage(model = channel.logoUrl, contentDescription = channel.nombre, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Fit)
                IconButton(onClick = onToggleFav, modifier = Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp).size(36.dp)) {
                    Icon(imageVector = if (channel.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null, tint = if (channel.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = channel.nombre, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 16.sp)
            Text(text = channel.categoria, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary, maxLines = 1)
        }
    }
}
