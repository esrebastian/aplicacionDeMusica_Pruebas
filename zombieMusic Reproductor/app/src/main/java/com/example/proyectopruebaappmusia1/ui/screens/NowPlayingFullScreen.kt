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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.proyectopruebaappmusia1.domain.model.Song
import com.example.proyectopruebaappmusia1.ui.theme.*
import com.example.proyectopruebaappmusia1.ui.components.AlbumArtImage
import com.example.proyectopruebaappmusia1.ui.components.MusicIconButton
import com.example.proyectopruebaappmusia1.util.TimeUtils
import com.example.proyectopruebaappmusia1.viewmodel.MusicPlayerViewModel
import com.example.proyectopruebaappmusia1.viewmodel.RepeatMode

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
                    title = { /* Vacío */ },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.KeyboardArrowDown, "Cerrar", tint = PrimaryText, modifier = Modifier.size(36.dp))
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
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                AlbumArtImage(
                    albumArtId = currentSong?.albumArt,
                    contentDescription = null,
                    highQuality = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .shadow(24.dp, RoundedCornerShape(28.dp))
                        .clip(RoundedCornerShape(28.dp))
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentSong?.title ?: "Sin título",
                            color = PrimaryText, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1
                        )
                        Text(
                            text = currentSong?.artist ?: "Desconocido",
                            color = SecondaryText, fontSize = 18.sp, maxLines = 1
                        )
                    }
                    val isFavorite = currentSong?.id in favoriteIds
                    MusicIconButton(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.Favorite,
                        contentDescription = null,
                        onClick = { viewModel.toggleFavorite(currentSong) },
                        tint = if (isFavorite) AccentGreen else SecondaryText,
                        buttonSize = 52.dp,
                        iconSize = 32.dp
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
                    IconButton(onClick = { viewModel.previousSong() }, modifier = Modifier.size(64.dp)) {
                        Icon(Icons.Default.SkipPrevious, null, tint = PrimaryText, modifier = Modifier.size(48.dp))
                    }
                    Box(
                        modifier = Modifier.size(80.dp).clip(CircleShape).background(AccentGreen).clickable { viewModel.togglePlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = DarkGreenBg, modifier = Modifier.size(44.dp))
                    }
                    IconButton(onClick = { viewModel.nextSong() }, modifier = Modifier.size(64.dp)) {
                        Icon(Icons.Default.SkipNext, null, tint = PrimaryText, modifier = Modifier.size(48.dp))
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    MusicIconButton(Icons.Default.Shuffle, null, { viewModel.toggleShuffle() }, tint = if (isShuffleEnabled) AccentGreen else PrimaryText)
                    MusicIconButton(Icons.AutoMirrored.Filled.QueueMusic, null, { showQueueSheet = true })
                }
            }
        }

        if (showQueueSheet) {
            ModalBottomSheet(
                onDismissRequest = { showQueueSheet = false },
                sheetState = sheetState,
                containerColor = DarkGreenBg,
                dragHandle = null,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                QueueSheetContent(viewModel, onClose = { showQueueSheet = false })
            }
        }
    }
}

@Composable
fun QueueSheetContent(viewModel: MusicPlayerViewModel, onClose: () -> Unit) {
    val songsQueue by viewModel.playbackQueue.collectAsStateWithLifecycle(emptyList())
    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle(null)
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle(false)
    val duration by viewModel.duration.collectAsStateWithLifecycle(0L)
    val currentPosition by viewModel.currentPosition.collectAsStateWithLifecycle(0L)
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle(RepeatMode.ALL)
    val isShuffleEnabled by viewModel.isShuffleEnabled.collectAsStateWithLifecycle(false)
    val sleepTimerRemaining by viewModel.sleepTimerRemaining.collectAsStateWithLifecycle(null)
    
    var showSleepTimerDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxHeight(0.94f)
            .fillMaxWidth()
            .background(DarkGreenBg)
    ) {
        // --- 1. CABEZAL MINI PLAYER ---
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                AlbumArtImage(currentSong?.albumArt, null, Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = currentSong?.let { "${it.artist} - ${it.title}" } ?: "Nada",
                    color = PrimaryText, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { viewModel.togglePlayPause() }) {
                    Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = PrimaryText, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            val progress = if (duration > 0) currentPosition.toFloat() / duration else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(2.dp).clip(CircleShape),
                color = AccentGreen,
                trackColor = Color.Gray.copy(alpha = 0.2f)
            )
            Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
                IconButton(onClick = onClose) { Icon(Icons.Default.KeyboardArrowDown, null, tint = SecondaryText.copy(alpha = 0.4f)) }
            }
        }

        // --- 2. TÍTULO "FILA" ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Fila", color = PrimaryText, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Row {
                IconButton(onClick = {}) { Icon(Icons.Default.CenterFocusWeak, null, tint = SecondaryText) }
                IconButton(onClick = {}) { Icon(Icons.Default.SwapVert, null, tint = SecondaryText) }
            }
        }

        // --- 3. LISTA DE CANCIONES ---
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(songsQueue, key = { it.id }) { song ->
                val isCurrent = song.id == currentSong?.id
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.selectSong(song, newQueue = songsQueue) }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(54.dp)) {
                        AlbumArtImage(song.albumArt, null, Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)))
                        if (isCurrent) {
                            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Pause, null, tint = AccentGreen, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(song.title, color = if (isCurrent) AccentGreen else PrimaryText, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(song.artist, color = SecondaryText, fontSize = 12.sp, maxLines = 1)
                    }
                    Icon(Icons.Default.DragHandle, null, tint = SecondaryText.copy(alpha = 0.6f), modifier = Modifier.size(24.dp))
                }
            }
        }

        // --- 4. BARRA DE ACCIONES INFERIOR ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(52.dp).clip(CircleShape).background(CardGreenBg.copy(alpha = 0.5f)).clickable { viewModel.toggleRepeatMode() },
                contentAlignment = Alignment.Center
            ) {
                Icon(if (repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat, null, tint = if (repeatMode != RepeatMode.NONE) AccentGreen else PrimaryText)
            }
            Box(
                modifier = Modifier.size(52.dp).clip(CircleShape).background(CardGreenBg.copy(alpha = 0.5f)).clickable { showSleepTimerDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Timer, null, tint = if (sleepTimerRemaining != null) AccentGreen else PrimaryText)
            }
            Surface(
                onClick = { viewModel.toggleShuffle() },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(26.dp),
                color = if (isShuffleEnabled) AccentGreen else CardGreenBg.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Shuffle, null, tint = if (isShuffleEnabled) DarkGreenBg else PrimaryText) }
            }
        }
    }

    if (showSleepTimerDialog) {
        SleepTimerDialog(onDismiss = { showSleepTimerDialog = false }, onSelectTime = { viewModel.setSleepTimer(it) })
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
