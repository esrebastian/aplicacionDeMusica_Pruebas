package com.example.proyectopruebaappmusia1

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectopruebaappmusia1.model.Song
import com.example.proyectopruebaappmusia1.viewmodel.MusicPlayerViewModel
import com.example.proyectopruebaappmusia1.viewmodel.RepeatMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingFullScreen(
    viewModel: MusicPlayerViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val isShuffleEnabled by viewModel.isShuffleEnabled.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val playlist by viewModel.playlist.collectAsState()

    var sliderPosition by remember(currentSong?.id) { mutableFloatStateOf(0f) }
    var isUserSeeking by remember { mutableStateOf(false) }
    
    var showQueueSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(currentPosition, duration, isUserSeeking) {
        if (!isUserSeeking && duration > 0) {
            sliderPosition = (currentPosition.toFloat() / duration).coerceIn(0f, 1f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkGreenBg, CardGreenBg.copy(alpha = 0.3f), DarkGreenBg)
                )
            )
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { /* Empty title */ },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = stringResource(R.string.close_player),
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: More options */ }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.more_options),
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                AlbumArtImage(
                    albumArtId = currentSong?.albumArt,
                    contentDescription = stringResource(R.string.album_art),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .shadow(16.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = currentSong?.title ?: stringResource(R.string.select_song),
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentSong?.artist ?: stringResource(R.string.unknown_artist),
                            color = SecondaryText,
                            fontSize = 16.sp,
                            maxLines = 1
                        )
                    }
                    val isFavorite = currentSong?.id in favoriteIds
                    IconButton(onClick = { viewModel.toggleFavorite(currentSong) }) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Outlined.Favorite,
                            contentDescription = stringResource(R.string.favorite_content_description),
                            tint = if (isFavorite) AccentGreen else SecondaryText,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = sliderPosition.coerceIn(0f, 1f),
                        onValueChange = {
                            isUserSeeking = true
                            sliderPosition = it
                        },
                        onValueChangeFinished = {
                            viewModel.seekTo(sliderPosition.coerceIn(0f, 1f))
                            isUserSeeking = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = AccentGreen,
                            activeTrackColor = AccentGreen,
                            inactiveTrackColor = Color(0xFF2D3D32)
                        )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatTime(currentPosition), color = SecondaryText, fontSize = 12.sp)
                        Text(formatTime(duration), color = SecondaryText, fontSize = 12.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón de Lista
                    IconButton(onClick = { /* TODO: Mostrar Lista */ }) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = "Lista",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(onClick = { viewModel.previousSong() }) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = stringResource(R.string.previous), tint = Color.White, modifier = Modifier.size(48.dp))
                    }

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(AccentGreen)
                            .clickable { viewModel.togglePlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = stringResource(R.string.play_pause),
                            tint = DarkGreenBg,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    IconButton(onClick = { viewModel.nextSong() }) {
                        Icon(Icons.Default.SkipNext, contentDescription = stringResource(R.string.next), tint = Color.White, modifier = Modifier.size(48.dp))
                    }

                    // Botón Aleatorio que abre la Fila
                    IconButton(onClick = { showQueueSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Fila",
                            tint = if (isShuffleEnabled) AccentGreen else Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // VENTANA DE FILA (QUEUE)
        if (showQueueSheet) {
            ModalBottomSheet(
                onDismissRequest = { showQueueSheet = false },
                sheetState = sheetState,
                containerColor = DarkGreenBg,
                dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray.copy(alpha = 0.5f)) }
            ) {
                QueueSheetContent(
                    viewModel = viewModel,
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    playlist = playlist,
                    isShuffleEnabled = isShuffleEnabled,
                    repeatMode = repeatMode,
                    progress = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
                )
            }
        }
    }
}

@Composable
fun QueueSheetContent(
    viewModel: MusicPlayerViewModel,
    currentSong: Song?,
    isPlaying: Boolean,
    playlist: List<Song>,
    isShuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    progress: Float
) {
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    val sleepTimerRemaining by viewModel.sleepTimerRemaining.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxHeight(0.9f)
            .padding(horizontal = 20.dp)
    ) {
        // Cabecera Persistente (Canción Actual)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlbumArtImage(
                albumArtId = currentSong?.albumArt,
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentSong?.title ?: "Sin título",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = currentSong?.artist ?: "Desconocido",
                    color = SecondaryText,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }
            IconButton(onClick = { viewModel.togglePlayPause() }) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(2.dp),
            color = AccentGreen,
            trackColor = Color.White.copy(alpha = 0.1f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Título "Fila"
        Text(
            text = "Fila",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(playlist) { song ->
                val isSelected = song.id == currentSong?.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectSong(song) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(56.dp)) {
                        AlbumArtImage(
                            albumArtId = song.albumArt,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.BarChart, null, tint = AccentGreen, modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            color = if (isSelected) AccentGreen else Color.White,
                            fontSize = 16.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song.artist,
                            color = SecondaryText,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                    Icon(Icons.Default.DragHandle, null, tint = SecondaryText, modifier = Modifier.size(24.dp))
                }
            }
        }

        // BARRA DE CONTROLES INFERIOR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val buttonColor = Color.White.copy(alpha = 0.1f)
            
            // Botón Repetir
            Surface(
                modifier = Modifier.weight(1f).height(48.dp).clickable { viewModel.toggleRepeatMode() },
                color = if (repeatMode != RepeatMode.NONE) AccentGreen.copy(alpha = 0.2f) else buttonColor,
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when(repeatMode) {
                            RepeatMode.ONE -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        },
                        null,
                        tint = if (repeatMode != RepeatMode.NONE) AccentGreen else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Botón Central (Temporizador de Apagado)
            Surface(
                modifier = Modifier
                    .weight(1.5f)
                    .height(48.dp)
                    .clickable { showSleepTimerDialog = true },
                color = if (sleepTimerRemaining != null) Color(0xFF89B4FF).copy(alpha = 0.4f) else Color(0xFF89B4FF).copy(alpha = 0.2f),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync, 
                        contentDescription = "Temporizador",
                        tint = if (sleepTimerRemaining != null) Color.White else Color(0xFF89B4FF), 
                        modifier = Modifier.size(22.dp)
                    )
                    if (sleepTimerRemaining != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatTimer(sleepTimerRemaining ?: 0L),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Botón Aleatorio
            Surface(
                modifier = Modifier.weight(1f).height(48.dp).clickable { viewModel.toggleShuffle() },
                color = if (isShuffleEnabled) AccentGreen.copy(alpha = 0.2f) else buttonColor,
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        null,
                        tint = if (isShuffleEnabled) AccentGreen else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }

    if (showSleepTimerDialog) {
        SleepTimerDialog(
            onDismiss = { showSleepTimerDialog = false },
            onSelectTime = { minutes ->
                viewModel.setSleepTimer(minutes)
                showSleepTimerDialog = false
            },
            currentTimer = sleepTimerRemaining
        )
    }
}

@Composable
fun SleepTimerDialog(
    onDismiss: () -> Unit,
    onSelectTime: (Int) -> Unit,
    currentTimer: Long?
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardGreenBg,
        title = {
            Text(
                "Temporizador de apagado",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val options = listOf(
                    0 to "Desactivado",
                    5 to "5 minutos",
                    15 to "15 minutos",
                    30 to "30 minutos",
                    60 to "60 minutos"
                )
                options.forEach { (mins, label) ->
                    Text(
                        text = label,
                        color = if (mins == 0 && currentTimer == null) AccentGreen else Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTime(mins) }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        fontSize = 16.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = AccentGreen)
            }
        }
    )
}

fun formatTimer(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
