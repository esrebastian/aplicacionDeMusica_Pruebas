package com.example.proyectopruebaappmusia1.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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

    PlaylistDetailContent(
        playlist = playlist,
        currentSong = currentSong,
        favoriteIds = favoriteIds,
        canEditSongs = playlist.id.startsWith("custom_"),
        onBack = onBack,
        onPlayClick = { viewModel.playPlaylist(playlist, shuffle = false) },
        onShuffleClick = { viewModel.playPlaylist(playlist, shuffle = true) },
        onSongClick = { song -> viewModel.selectSong(song, newQueue = playlist.songs) },
        onFavoriteClick = { viewModel.toggleFavorite(it) },
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
    val scrollOffset = remember { derivedStateOf { scrollState.firstVisibleItemScrollOffset.toFloat() } }
    val isFirstItemVisible = remember { derivedStateOf { scrollState.firstVisibleItemIndex == 0 } }
    
    val collapseFactor = remember {
        derivedStateOf {
            if (isFirstItemVisible.value) {
                min(1f, scrollOffset.value / 500f)
            } else 1f
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkGreenBg)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .graphicsLayer {
                    alpha = 1f - collapseFactor.value
                    translationY = -scrollOffset.value * 0.5f
                }
        ) {
            AlbumArtImage(
                albumArtId = playlist.coverImage,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, DarkGreenBg),
                            startY = 200f
                        )
                    )
            )
        }

        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = headerHeight, bottom = 20.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 20.dp)) {
                    Text(
                        text = playlist.name,
                        color = PrimaryText,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${playlist.songCount} canciones • ZombieMusic",
                        color = SecondaryText,
                        fontSize = 14.sp
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onPlayClick,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                        ) {
                            Icon(Icons.Default.PlayArrow, null, tint = DarkGreenBg)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reproducir", color = DarkGreenBg, fontWeight = FontWeight.Bold)
                        }
                        
                        MusicIconButton(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = null,
                            onClick = onShuffleClick,
                            tint = PrimaryText,
                            containerColor = PrimaryText.copy(alpha = 0.1f),
                            shape = CircleShape,
                            buttonSize = 48.dp
                        )

                        if (canEditSongs) {
                            MusicIconButton(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Agregar canciones",
                                onClick = onAddSongsClick,
                                tint = PrimaryText,
                                containerColor = PrimaryText.copy(alpha = 0.1f),
                                shape = CircleShape,
                                buttonSize = 48.dp
                            )
                        }
                    }
                }
            }

            if (playlist.songs.isEmpty() && canEditSongs) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Esta playlist esta vacia", color = PrimaryText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Agrega canciones desde aqui o desde Biblioteca.", color = SecondaryText, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onAddSongsClick,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                        ) {
                            Icon(Icons.Default.Add, null, tint = DarkGreenBg)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Agregar canciones", color = DarkGreenBg, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                items(playlist.songs, key = { it.id }, contentType = { "playlist_song" }) { song ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        SongListItem(
                            song = song,
                            isCurrent = song.id == currentSong?.id,
                            isFavorite = song.id in favoriteIds,
                            onFavoriteClick = { onFavoriteClick(song) },
                            onClick = { onSongClick(song) }
                        )
                    }
                }
            }
        }

        TopAppBar(
            title = {
                if (collapseFactor.value > 0.8f) {
                    Text(playlist.name, color = PrimaryText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, null, tint = PrimaryText)
                }
            },
            actions = {
                IconButton(onClick = { /* Menú de opciones */ }) {
                    Icon(Icons.Default.MoreVert, null, tint = PrimaryText)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = if (collapseFactor.value > 0.8f) DarkGreenBg else Color.Transparent
            )
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
    val availableSongs = remember(songs, playlistSongIds) { songs.filterNot { it.id in playlistSongIds } }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardGreenBg,
        title = { Text("Agregar canciones", color = PrimaryText) },
        text = {
            if (availableSongs.isEmpty()) {
                Text("No hay canciones pendientes para agregar.", color = SecondaryText)
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(availableSongs, key = { it.id }, contentType = { "available_song" }) { song ->
                        TextButton(
                            onClick = { onAddSong(song) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(song.title, color = PrimaryText, maxLines = 1)
                                Text(song.artist, color = SecondaryText, fontSize = 12.sp, maxLines = 1)
                            }
                            Icon(Icons.Default.Add, null, tint = AccentGreen)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("LISTO", color = AccentGreen, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PlaylistDetailPreview() {
    ProyectoPruebaAppMusia1Theme {
        val dummySongs = listOf(
            Song("1", "Song A", "Artist A", 180000, "", null),
            Song("2", "Song B", "Artist B", 240000, "", null),
            Song("3", "Song C", "Artist C", 210000, "", null)
        )
        PlaylistDetailContent(
            playlist = Playlist("1", "Rock Classics", 3, null, dummySongs),
            currentSong = dummySongs[0],
            favoriteIds = setOf("1"),
            canEditSongs = true,
            onBack = {},
            onPlayClick = {},
            onShuffleClick = {},
            onSongClick = {},
            onFavoriteClick = {},
            onAddSongsClick = {}
        )
    }
}
