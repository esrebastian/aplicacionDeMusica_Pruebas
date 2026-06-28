package com.example.proyectopruebaappmusia1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.proyectopruebaappmusia1.domain.model.Playlist
import com.example.proyectopruebaappmusia1.domain.model.Song
import com.example.proyectopruebaappmusia1.ui.components.AlbumArtImage
import com.example.proyectopruebaappmusia1.ui.components.MusicIconButton
import com.example.proyectopruebaappmusia1.ui.components.SongListItem
import com.example.proyectopruebaappmusia1.ui.theme.*
import com.example.proyectopruebaappmusia1.viewmodel.MusicPlayerViewModel
import kotlin.math.min

@Composable
fun PlaylistDetailScreen(
    viewModel: MusicPlayerViewModel,
    playlist: Playlist,
    onBack: () -> Unit
) {
    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val allSongs by viewModel.playlist.collectAsStateWithLifecycle()
    var showAddSongsDialog by remember { mutableStateOf(false) }

    // Memorizamos las acciones para que el scroll no las regenere
    val onPlayClick = remember(playlist.id) { { viewModel.playPlaylist(playlist, shuffle = false) } }
    val onShuffleClick = remember(playlist.id) { { viewModel.playPlaylist(playlist, shuffle = true) } }
    val onSongClick = remember(playlist.songs) { { song: Song -> viewModel.selectSong(song, newQueue = playlist.songs) } }
    val onFavoriteToggle = remember { { song: Song -> viewModel.toggleFavorite(song) } }

    PlaylistDetailContent(
        playlist = playlist,
        currentSong = currentSong,
        favoriteIds = favoriteIds,
        canEditSongs = playlist.id.startsWith("custom_"),
        onBack = onBack,
        onPlayClick = onPlayClick,
        onShuffleClick = onShuffleClick,
        onSongClick = onSongClick,
        onFavoriteClick = onFavoriteToggle,
        onAddSongsClick = { showAddSongsDialog = true }
    )

    if (showAddSongsDialog) {
        AddSongsToPlaylistDialog(
            playlist = playlist,
            songs = allSongs,
            onDismiss = { showAddSongsDialog = false },
            onAddSong = { song -> viewModel.addSongToPlaylist(playlist.id, song) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailContent(
    playlist: Playlist,
    currentSong: Song?,
    favoriteIds: Set<String>,
    canEditSongs: Boolean,
    onBack: () -> Unit,
    onPlayClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onSongClick: (Song) -> Unit,
    onFavoriteClick: (Song) -> Unit,
    onAddSongsClick: () -> Unit
) {
    val scrollState = rememberLazyListState()
    val headerHeight = 300.dp
    
    // Determinamos el estado de la barra de forma booleana para evitar recomposiciones constantes
    val isBarSolid by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 450 }
    }

    // Paginación para que la lista sea ligera
    var limit by remember(playlist.id, playlist.songs) {
        mutableIntStateOf(40.coerceAtMost(playlist.songs.size))
    }
    
    val visiblePlaylistSongs = remember(playlist.songs, limit) {
        playlist.songs.take(limit)
    }

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= limit - 5 && limit < playlist.songs.size) {
                    limit = (limit + 40).coerceAtMost(playlist.songs.size)
                }
            }
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkGreenBg)) {
        // Cabezal Parallax: Usamos graphicsLayer para que el movimiento sea procesado por la GPU, no por Compose
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .graphicsLayer {
                    val offset = scrollState.firstVisibleItemScrollOffset.toFloat()
                    alpha = if (scrollState.firstVisibleItemIndex > 0) 0f else 1f - min(1f, offset / 500f)
                    translationY = -offset * 0.4f
                }
        ) {
            AlbumArtImage(
                albumArtId = playlist.coverImage,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                highQuality = true
            )
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, DarkGreenBg), startY = 300f)))
        }

        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = headerHeight, bottom = 80.dp)
        ) {
            item(key = "header_section") {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text(playlist.name, color = PrimaryText, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Text("${playlist.songCount} canciones", color = SecondaryText)
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = onPlayClick, modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)) {
                            Icon(Icons.Default.PlayArrow, null, tint = DarkGreenBg)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reproducir", color = DarkGreenBg, fontWeight = FontWeight.Bold)
                        }
                        MusicIconButton(Icons.Default.Shuffle, null, onShuffleClick, containerColor = CardGreenBg, shape = CircleShape)
                        if (canEditSongs) {
                            MusicIconButton(Icons.Default.Add, null, onAddSongsClick, containerColor = CardGreenBg, shape = CircleShape)
                        }
                    }
                }
            }

            items(items = visiblePlaylistSongs, key = { it.id }) { song ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SongListItem(
                        song = song,
                        isCurrent = song.id == currentSong?.id,
                        isFavorite = song.id in favoriteIds,
                        onFavoriteClick = onFavoriteClick,
                        onClick = onSongClick
                    )
                }
            }
            
            if (limit < playlist.songs.size) {
                item(key = "loader") {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentGreen, strokeWidth = 2.dp)
                    }
                }
            }
        }

        TopAppBar(
            title = { if (isBarSolid) Text(playlist.name, color = PrimaryText, fontSize = 18.sp) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PrimaryText)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = if (isBarSolid) DarkGreenBg else Color.Transparent)
        )
    }
}

@Composable
fun AddSongsToPlaylistDialog(
    playlist: Playlist,
    songs: List<Song>,
    onDismiss: () -> Unit,
    onAddSong: (Song) -> Unit
) {
    val playlistSongIds = remember(playlist.songs) { playlist.songs.map { it.id }.toSet() }
    val available = remember(songs, playlistSongIds) { songs.filterNot { it.id in playlistSongIds } }
    var limit by remember { mutableIntStateOf(50) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardGreenBg,
        title = { Text("Agregar canciones", color = PrimaryText) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                items(available.take(limit), key = { it.id }) { song ->
                    TextButton(onClick = { onAddSong(song) }, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(song.title, color = PrimaryText, maxLines = 1)
                            Text(song.artist, color = SecondaryText, fontSize = 11.sp)
                        }
                        Icon(Icons.Default.Add, null, tint = AccentGreen)
                    }
                }
                if (limit < available.size) {
                    item { TextButton(onClick = { limit += 50 }) { Text("Cargar más", color = AccentGreen) } }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("LISTO", color = AccentGreen) } }
    )
}
