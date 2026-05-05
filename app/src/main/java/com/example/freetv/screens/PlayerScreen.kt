package com.example.freetv.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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
import androidx.media3.ui.AspectRatioFrameLayout
import com.example.freetv.player.VideoPlayerManager
import kotlinx.coroutines.flow.collectLatest
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun PlayerScreen(
    initialStreamUrl: String,
    viewModel: SharedTvViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (String) -> Unit,
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

    var playbackError by remember { mutableStateOf<String?>(null) }

    val videoPlayerManager = remember { VideoPlayerManager(context) }

    val player = videoPlayerManager.getPlayer(
        url = currentUrl,
        onError = {
            Toast.makeText(context, "Error al cargar datos", Toast.LENGTH_LONG).show()
            val prevUrl = viewModel.previousChannel()
            if (prevUrl != null) {
                currentUrl = prevUrl
            }
        }
    )

    val isTimerActive by viewModel.isTimerActive.collectAsState()
    val timeRemaining by viewModel.timeRemaining.collectAsState()
    val timerMenuExpanded by viewModel.timerMenuExpanded.collectAsState()
    val aspectRatioMode by viewModel.aspectRatioMode.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.aspectRatioSnackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val currentResizeMode = remember(aspectRatioMode) {
        when (aspectRatioMode) {
            AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
            AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
            AspectRatioMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        }
    }

    LaunchedEffect(Unit) {
        viewModel.timerFinishedEvent.collectLatest {
            player.pause()
            Toast.makeText(context, "Temporizador: Video detenido", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(player) {
        val listener = object : androidx.media3.common.Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                playbackError = "Canal no disponible"
            }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == androidx.media3.common.Player.STATE_READY) {
                    playbackError = null
                }
            }
        }
        player.addListener(listener)
        if (player.playerError != null) playbackError = "Canal no disponible"
    }

    val activity = context as? Activity

    LaunchedEffect(isLandscape) {
        val window = activity?.window ?: return@LaunchedEffect
        val controller = WindowCompat.getInsetsController(window, view)

        if (isLandscape) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
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

    val retryPlayback = {
        playbackError = null
        player.prepare()
        player.play()
    }

    LaunchedEffect(initialStreamUrl) {
        viewModel.selectChannelByUrl(initialStreamUrl)
    }

    DisposableEffect(Unit) {
        onDispose {
            videoPlayerManager.releasePlayer()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            val window = activity?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    val navigateToNextChannel = {
        val newUrl = viewModel.nextChannel()
        if (newUrl != null) {
            currentUrl = newUrl
        } else {
            Toast.makeText(context, "Fin de la lista", Toast.LENGTH_SHORT).show()
        }
    }

    val navigateToPrevChannel = {
        val prevUrl = viewModel.previousChannel()
        if (prevUrl != null) {
            currentUrl = prevUrl
        } else {
            Toast.makeText(context, "Inicio de la lista", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (isLandscape) {
            Box(modifier = Modifier.fillMaxSize()) {
                PlayerContainer(player, currentResizeMode, modifier = Modifier.fillMaxSize())

                VideoGestureOverlay(
                    onToggleControls = { showControls = !showControls },
                    onSwipeLeft = navigateToNextChannel,
                    onSwipeRight = navigateToPrevChannel
                )

                if (playbackError != null) {
                    ErrorOverlay(message = playbackError!!, onRetry = retryPlayback)
                }
            }

            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it }),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                PlayerControlsColumn(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(185.dp)
                        .background(Color(0xE61E1E1E))
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    onNavigateBack = onNavigateBack,
                    onNavigateToDetails = { onNavigateToDetails(currentUrl) },
                    onNavigateToSettings = onNavigateToSettings,
                    onPrevChannel = navigateToPrevChannel,
                    onNextChannel = navigateToNextChannel,
                    onVolumeChange = { player.volume = it },
                    onToggleMute = {
                        if (isMuted) {
                            player.volume = lastVolumeBeforeMute
                            isMuted = false
                        } else {
                            lastVolumeBeforeMute = player.volume
                            player.volume = 0f
                            isMuted = true
                        }
                    },
                    onToggleOrientation = toggleOrientation,
                    currentVolume = player.volume,
                    isMuted = isMuted,
                    channelName = viewModel.getCurrentChannelName(),
                    isLandscape = true,
                    isTimerActive = isTimerActive,
                    timeRemaining = timeRemaining,
                    timerMenuExpanded = timerMenuExpanded,
                    onToggleTimerMenu = { viewModel.setTimerMenuExpanded(it) },
                    onStartTimer = {
                        viewModel.startSleepTimer(it)
                        Toast.makeText(context, "Temporizador: $it min", Toast.LENGTH_SHORT).show()
                    },
                    onCancelTimer = {
                        viewModel.cancelSleepTimer()
                        Toast.makeText(context, "Temporizador cancelado", Toast.LENGTH_SHORT).show()
                    },
                    onToggleAspectRatio = { viewModel.toggleAspectRatio() }
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
                    PlayerContainer(player, currentResizeMode, modifier = Modifier.fillMaxSize())

                    VideoGestureOverlay(
                        onToggleControls = { showControls = !showControls },
                        onSwipeLeft = navigateToNextChannel,
                        onSwipeRight = navigateToPrevChannel
                    )

                    if (playbackError != null) {
                        ErrorOverlay(message = playbackError!!, onRetry = retryPlayback)
                    }
                }

                PlayerControlsColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFF1E1E1E))
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    onNavigateBack = onNavigateBack,
                    onNavigateToDetails = { onNavigateToDetails(currentUrl) },
                    onNavigateToSettings = onNavigateToSettings,
                    onPrevChannel = navigateToPrevChannel,
                    onNextChannel = navigateToNextChannel,
                    onVolumeChange = { player.volume = it },
                    onToggleMute = {
                        if (isMuted) {
                            player.volume = lastVolumeBeforeMute
                            isMuted = false
                        } else {
                            lastVolumeBeforeMute = player.volume
                            player.volume = 0f
                            isMuted = true
                        }
                    },
                    onToggleOrientation = toggleOrientation,
                    currentVolume = player.volume,
                    isMuted = isMuted,
                    channelName = viewModel.getCurrentChannelName(),
                    isLandscape = false,
                    isTimerActive = isTimerActive,
                    timeRemaining = timeRemaining,
                    timerMenuExpanded = timerMenuExpanded,
                    onToggleTimerMenu = { viewModel.setTimerMenuExpanded(it) },
                    onStartTimer = {
                        viewModel.startSleepTimer(it)
                        Toast.makeText(context, "Temporizador: $it min", Toast.LENGTH_SHORT).show()
                    },
                    onCancelTimer = {
                        viewModel.cancelSleepTimer()
                        Toast.makeText(context, "Temporizador cancelado", Toast.LENGTH_SHORT).show()
                    },
                    onToggleAspectRatio = { viewModel.toggleAspectRatio() }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}

@Composable
fun VideoGestureOverlay(
    onToggleControls: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    val swipeThreshold = 100f
    var offsetX by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onToggleControls() }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX < -swipeThreshold) {
                            onSwipeLeft()
                        } else if (offsetX > swipeThreshold) {
                            onSwipeRight()
                        }
                        offsetX = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount
                    }
                )
            }
    )
}

@Composable
fun PlayerContainer(exoPlayer: androidx.media3.exoplayer.ExoPlayer, resizeModeInt: Int, modifier: Modifier) {
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                useController = false
            }
        },
        update = { view ->
            view.player = exoPlayer
            view.resizeMode = resizeModeInt
        },
        modifier = modifier
    )
}

@Composable
fun PlayerControlsColumn(
    modifier: Modifier,
    onNavigateBack: () -> Unit,
    onNavigateToDetails: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onPrevChannel: () -> Unit,
    onNextChannel: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onToggleOrientation: () -> Unit,
    currentVolume: Float,
    isMuted: Boolean,
    channelName: String,
    isLandscape: Boolean,
    isTimerActive: Boolean,
    timeRemaining: Long?,
    timerMenuExpanded: Boolean,
    onToggleTimerMenu: (Boolean) -> Unit,
    onStartTimer: (Int) -> Unit,
    onCancelTimer: () -> Unit,
    onToggleAspectRatio: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(channelName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                IconButton(onClick = onNavigateBack, modifier = Modifier.background(Color(0xFFD32F2F), CircleShape).size(36.dp)) {
                    Icon(Icons.Default.Home, contentDescription = "Inicio", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onNavigateToDetails, modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape).size(36.dp)) {
                    Icon(Icons.Default.Info, contentDescription = "Detalles", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onNavigateToSettings, modifier = Modifier.background(Color.Gray, CircleShape).size(36.dp)) {
                    Icon(Icons.Default.Settings, contentDescription = "Config", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 4.dp), color = Color.DarkGray)

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
                    Icon(Icons.AutoMirrored.Filled.VolumeDown, contentDescription = "Low", tint = Color.White, modifier = Modifier.size(16.dp))
                }
                FilledIconButton(
                    onClick = onToggleMute,
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = if (isMuted) Color.Red else Color.DarkGray)
                ) {
                    Icon(if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeMute, contentDescription = "Mute", tint = Color.White)
                }
                FilledIconButton(
                    onClick = { onVolumeChange((currentVolume + 0.1f).coerceAtMost(1f)) },
                    modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.DarkGray)
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "High", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("PANTALLA", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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

                FilledIconButton(
                    onClick = onToggleAspectRatio,
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.DarkGray)
                ) {
                    Icon(
                        imageVector = Icons.Default.AspectRatio,
                        contentDescription = "Aspect Ratio",
                        tint = Color.White
                    )
                }

                Box {
                    FilledIconButton(
                        onClick = { onToggleTimerMenu(true) },
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isTimerActive) Color(0xFF4CAF50) else Color.DarkGray
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Sleep Timer",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = timerMenuExpanded,
                        onDismissRequest = { onToggleTimerMenu(false) },
                        modifier = Modifier.background(Color(0xFF2C2C2C))
                    ) {
                        if (isTimerActive) {
                            val minutes = (timeRemaining ?: 0L) / 60000
                            val seconds = ((timeRemaining ?: 0L) % 60000) / 1000
                            DropdownMenuItem(
                                text = { Text("Quedan: ${minutes}m ${seconds}s", color = Color.White) },
                                onClick = { }
                            )
                            DropdownMenuItem(
                                text = { Text("Desactivar temporizador", color = Color.Red) },
                                onClick = {
                                    onCancelTimer()
                                    onToggleTimerMenu(false)
                                }
                            )
                            HorizontalDivider(color = Color.DarkGray)
                        }
                        listOf(1, 5, 10, 15, 30, 60).forEach { mins ->
                            DropdownMenuItem(
                                text = { Text("$mins min", color = Color.White) },
                                onClick = {
                                    onStartTimer(mins)
                                    onToggleTimerMenu(false)
                                }
                            )
                        }
                    }
                }
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

@Composable
fun ErrorOverlay(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reintentar")
            }
        }
    }
}