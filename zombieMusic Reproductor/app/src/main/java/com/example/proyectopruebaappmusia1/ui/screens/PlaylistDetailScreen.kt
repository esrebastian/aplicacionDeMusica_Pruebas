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
import com.example.proyectopruebaappmusia1.domain.model.Playlist
import com.example.proyectopruebaappmusia1.domain.model.Song
import com.example.proyectopruebaappmusia1.ui.components.AlbumArtImage
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
    val currentSong by viewModel.currentSong.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()

    PlaylistDetailContent(
        playlist = playlist,
        currentSong = currentSong,
        favoriteIds = favoriteIds,
        onBack = onBack,
        onPlayClick = { viewModel.playPlaylist(playlist, shuffle = false) },
        onShuffleClick = { viewModel.playPlaylist(playlist, shuffle = true) },
        onSongClick = { song -> viewModel.selectSong(song, newQueue = playlist.songs) },
        onFavoriteClick = { viewModel.toggleFavorite(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailContent(
    playlist: Playlist,
    currentSong: Song?,
    favoriteIds: Set<String>,
    onBack: () -> Unit,
    onPlayClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onSongClick: (Song) -> Unit,
    onFavoriteClick: (Song) -> Unit
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
                        color = Color.White,
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
                        
                        IconButton(
                            onClick = onShuffleClick,
                            modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape).size(48.dp)
                        ) {
                            Icon(Icons.Default.Shuffle, null, tint = Color.White)
                        }
                    }
                }
            }

            items(playlist.songs) { song ->
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

        TopAppBar(
            title = {
                if (collapseFactor.value > 0.8f) {
                    Text(playlist.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                }
            },
            actions = {
                IconButton(onClick = { /* Menú de opciones */ }) {
                    Icon(Icons.Default.MoreVert, null, tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = if (collapseFactor.value > 0.8f) DarkGreenBg else Color.Transparent
            )
        )
    }
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
            onBack = {},
            onPlayClick = {},
            onShuffleClick = {},
            onSongClick = {},
            onFavoriteClick = {}
        )
    }
}
