package com.example.freetv.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.ui.PlayerView
import com.example.freetv.player.VideoPlayerManager
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun PlayerScreen(
    initialStreamUrl: String,
    viewModel: SharedTvViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val view = LocalView.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    var showControls by remember { mutableStateOf(true) }
    var currentUrl by remember { mutableStateOf(initialStreamUrl) }
    var lastVolumeBeforeMute by remember { mutableFloatStateOf(1f) }
    var isMuted by remember { mutableStateOf(false) }

    val decodedUrl = remember(currentUrl) {
        URLDecoder.decode(currentUrl, StandardCharsets.UTF_8.toString())
    }
    
    val videoPlayerManager = remember { VideoPlayerManager(context) }
    val exoPlayer = remember(decodedUrl) { videoPlayerManager.getPlayer(decodedUrl) }

    val activity = context as? Activity
    
    // Manage Status/Navigation Bar (Netflix Style)
    LaunchedEffect(isLandscape) {
        val window = activity?.window ?: return@LaunchedEffect
        val controller = WindowCompat.getInsetsController(window, view)
        
        if (isLandscape) {
            // Hide bars in Landscape
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            // Show bars in Portrait
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    val toggleOrientation = {
        activity?.requestedOrientation = if (isLandscape) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    LaunchedEffect(initialStreamUrl) {
        viewModel.selectChannelByUrl(initialStreamUrl)
    }

    DisposableEffect(Unit) {
        onDispose {
            videoPlayerManager.releasePlayer()
            // Reset everything when leaving
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            val window = activity?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { showControls = !showControls }
    ) {
        if (isLandscape) {
            PlayerContainer(exoPlayer, modifier = Modifier.fillMaxSize())

            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it }),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                PlayerControlsColumn(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(185.dp) // even wider to be safe
                        .background(Color(0xE61E1E1E)) // Darker for better visibility
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    onNavigateBack = onNavigateBack,
                    onNavigateToSettings = onNavigateToSettings,
                    onPrevChannel = { viewModel.previousChannel()?.let { currentUrl = it } },
                    onNextChannel = { viewModel.nextChannel()?.let { currentUrl = it } },
                    onVolumeChange = { exoPlayer.volume = it },
                    onToggleMute = {
                        if (isMuted) {
                            exoPlayer.volume = lastVolumeBeforeMute
                            isMuted = false
                        } else {
                            lastVolumeBeforeMute = exoPlayer.volume
                            exoPlayer.volume = 0f
                            isMuted = true
                        }
                    },
                    onToggleOrientation = toggleOrientation,
                    currentVolume = exoPlayer.volume,
                    isMuted = isMuted,
                    channelName = viewModel.getCurrentChannelName(),
                    isLandscape = true
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.DarkGray)
                ) {
                    PlayerContainer(exoPlayer, modifier = Modifier.fillMaxSize())
                }

                PlayerControlsColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFF1E1E1E))
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    onNavigateBack = onNavigateBack,
                    onNavigateToSettings = onNavigateToSettings,
                    onPrevChannel = { viewModel.previousChannel()?.let { currentUrl = it } },
                    onNextChannel = { viewModel.nextChannel()?.let { currentUrl = it } },
                    onVolumeChange = { exoPlayer.volume = it },
                    onToggleMute = {
                        if (isMuted) {
                            exoPlayer.volume = lastVolumeBeforeMute
                            isMuted = false
                        } else {
                            lastVolumeBeforeMute = exoPlayer.volume
                            exoPlayer.volume = 0f
                            isMuted = true
                        }
                    },
                    onToggleOrientation = toggleOrientation,
                    currentVolume = exoPlayer.volume,
                    isMuted = isMuted,
                    channelName = viewModel.getCurrentChannelName(),
                    isLandscape = false
                )
            }
        }
    }
}

@Composable
fun PlayerContainer(exoPlayer: androidx.media3.exoplayer.ExoPlayer, modifier: Modifier) {
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
            }
        },
        modifier = modifier
    )
}

@Composable
fun PlayerControlsColumn(
    modifier: Modifier,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onPrevChannel: () -> Unit,
    onNextChannel: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onToggleOrientation: () -> Unit,
    currentVolume: Float,
    isMuted: Boolean,
    channelName: String,
    isLandscape: Boolean
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp) // More space between sections
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(channelName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                IconButton(onClick = onNavigateBack, modifier = Modifier.background(Color(0xFFD32F2F), CircleShape).size(36.dp)) {
                    Icon(Icons.Default.Home, contentDescription = "Inicio", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onNavigateToSettings, modifier = Modifier.background(Color.Gray, CircleShape).size(36.dp)) {
                    Icon(Icons.Default.Settings, contentDescription = "Config", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }

        Divider(modifier = Modifier.padding(horizontal = 4.dp), color = Color.DarkGray)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("CANAL", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilledIconButton(onClick = onPrevChannel, modifier = Modifier.size(40.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.DarkGray)) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Prev", tint = Color.White)
                }
                FilledIconButton(onClick = onNextChannel, modifier = Modifier.size(40.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.DarkGray)) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Next", tint = Color.White)
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("VOLUMEN", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                FilledIconButton(
                    onClick = { onVolumeChange((currentVolume - 0.1f).coerceAtLeast(0f)) },
                    modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.DarkGray)
                ) {
                    Icon(Icons.Default.VolumeDown, contentDescription = "Low", tint = Color.White, modifier = Modifier.size(16.dp))
                }
                FilledIconButton(
                    onClick = onToggleMute,
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = if (isMuted) Color.Red else Color.DarkGray)
                ) {
                    Icon(if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeMute, contentDescription = "Mute", tint = Color.White)
                }
                FilledIconButton(
                    onClick = { onVolumeChange((currentVolume + 0.1f).coerceAtMost(1f)) },
                    modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.DarkGray)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = "High", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("PANTALLA", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            FilledIconButton(
                onClick = onToggleOrientation,
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(
                    imageVector = if (isLandscape) Icons.Default.StayCurrentPortrait else Icons.Default.StayCurrentLandscape,
                    contentDescription = "Girar Pantalla",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNavigateBack,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Icon(Icons.Default.Menu, contentDescription = "Lista", modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Ver Lista", fontSize = 13.sp)
        }
    }
}