package com.example.proyectopruebaappmusia1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.proyectopruebaappmusia1.domain.model.Song
import com.example.proyectopruebaappmusia1.ui.theme.*
import com.example.proyectopruebaappmusia1.ui.components.AlbumArtImage
import com.example.proyectopruebaappmusia1.ui.components.MusicIconButton
import com.example.proyectopruebaappmusia1.util.TimeUtils
import com.example.proyectopruebaappmusia1.viewmodel.MusicPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingFullScreen(
    viewModel: MusicPlayerViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle(null)
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle(false)
    val duration by viewModel.duration.collectAsStateWithLifecycle(0L)
    val currentPosition by viewModel.currentPosition.collectAsStateWithLifecycle(0L)
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle(emptySet())
    val isShuffleEnabled by viewModel.isShuffleEnabled.collectAsStateWithLifecycle(false)

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
                            Icon(Icons.Default.KeyboardArrowDown, "Cerrar", tint = PrimaryText, modifier = Modifier.size(32.dp))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .shadow(16.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentSong?.title ?: "Selecciona una canción",
                            color = PrimaryText, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1
                        )
                        Text(
                            text = currentSong?.artist ?: "Desconocido",
                            color = SecondaryText, fontSize = 16.sp, maxLines = 1
                        )
                    }
                    val isFavorite = currentSong?.id in favoriteIds
                    MusicIconButton(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.Favorite,
                        contentDescription = null,
                        onClick = { viewModel.toggleFavorite(currentSong) },
                        tint = if (isFavorite) AccentGreen else SecondaryText,
                        buttonSize = 48.dp,
                        iconSize = 28.dp
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = sliderPosition,
                        onValueChange = { isUserSeeking = true; sliderPosition = it },
                        onValueChangeFinished = { viewModel.seekTo(sliderPosition); isUserSeeking = false },
                        colors = SliderDefaults.colors(thumbColor = AccentGreen, activeTrackColor = AccentGreen)
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(TimeUtils.formatTime(currentPosition), color = SecondaryText, fontSize = 12.sp)
                        Text(TimeUtils.formatTime(duration), color = SecondaryText, fontSize = 12.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MusicIconButton(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = null,
                        onClick = { viewModel.previousSong() },
                        tint = PrimaryText,
                        buttonSize = 56.dp,
                        iconSize = 48.dp
                    )
                    Box(
                        modifier = Modifier.size(72.dp).clip(CircleShape).background(AccentGreen).clickable { viewModel.togglePlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = DarkGreenBg, modifier = Modifier.size(40.dp))
                    }
                    MusicIconButton(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = null,
                        onClick = { viewModel.nextSong() },
                        tint = PrimaryText,
                        buttonSize = 56.dp,
                        iconSize = 48.dp
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MusicIconButton(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = null,
                        onClick = { viewModel.toggleShuffle() },
                        tint = if (isShuffleEnabled) AccentGreen else PrimaryText,
                        buttonSize = 48.dp
                    )
                    MusicIconButton(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = null,
                        onClick = { showQueueSheet = true },
                        tint = PrimaryText,
                        buttonSize = 48.dp
                    )
                }
            }
        }

        if (showQueueSheet) {
            ModalBottomSheet(onDismissRequest = { showQueueSheet = false }, sheetState = sheetState, containerColor = DarkGreenBg) {
                QueueSheetContent(viewModel)
            }
        }
    }
}

@Composable
fun QueueSheetContent(viewModel: MusicPlayerViewModel) {
    val songsQueue by viewModel.playbackQueue.collectAsStateWithLifecycle(emptyList<Song>())
    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle(null)
    val sleepTimerRemaining by viewModel.sleepTimerRemaining.collectAsStateWithLifecycle(null)
    var showSleepTimerDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxHeight(0.8f).padding(16.dp)) {
        Text("Fila de reproducción", color = PrimaryText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(songsQueue, key = { it.id }, contentType = { "queue_song" }) { song ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.selectSong(song) }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AlbumArtImage(song.albumArt, null, Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(song.title, color = if (song.id == currentSong?.id) AccentGreen else PrimaryText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(song.artist, color = SecondaryText, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }

        Button(
            onClick = { showSleepTimerDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = CardGreenBg),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Timer, null, tint = AccentGreen)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                if (sleepTimerRemaining != null) "Apagado en ${TimeUtils.formatTimer(sleepTimerRemaining!!)}" else "Temporizador de apagado",
                color = PrimaryText
            )
        }
    }

    if (showSleepTimerDialog) {
        SleepTimerDialog(
            onDismiss = { showSleepTimerDialog = false },
            onSelectTime = { viewModel.setSleepTimer(it) }
        )
    }
}

@Composable
fun SleepTimerDialog(onDismiss: () -> Unit, onSelectTime: (Int) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardGreenBg,
        title = { Text("Temporizador", color = PrimaryText) },
        text = {
            Column {
                listOf(0 to "Desactivado", 15 to "15 min", 30 to "30 min", 60 to "60 min").forEach { (mins, label) ->
                    Text(label, color = PrimaryText, modifier = Modifier.fillMaxWidth().clickable { onSelectTime(mins); onDismiss() }.padding(16.dp))
                }
            }
        },
        confirmButton = {}
    )
}
