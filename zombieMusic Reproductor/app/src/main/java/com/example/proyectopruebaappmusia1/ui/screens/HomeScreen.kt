package com.example.proyectopruebaappmusia1.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectopruebaappmusia1.R
import com.example.proyectopruebaappmusia1.domain.model.Playlist
import com.example.proyectopruebaappmusia1.domain.model.Song
import com.example.proyectopruebaappmusia1.ui.theme.*
import com.example.proyectopruebaappmusia1.ui.components.AlbumArtImage
import com.example.proyectopruebaappmusia1.util.TimeUtils
import com.example.proyectopruebaappmusia1.viewmodel.MusicPlayerViewModel
import com.example.proyectopruebaappmusia1.viewmodel.FilterOption

@Composable
fun HomeScreen(
    viewModel: MusicPlayerViewModel,
    onHeroClick: () -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val filteredSongs by viewModel.filteredHomeSongs.collectAsState()
    val searchQuery by viewModel.homeSearchQuery.collectAsState()
    val selectedFilter by viewModel.homeFilter.collectAsState()
    val playlists by viewModel.homePlaylists.collectAsState()
    
    var showCreateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp, start = 16.dp, end = 16.dp, top = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { 
            HomeHeader(
                onSettingsClick = { /* Acción de ajustes */ }
            ) 
        }
        
        item {
            HeroPlayerCard(
                song = currentSong,
                isPlaying = isPlaying,
                progress = viewModel.progress.collectAsState().value,
                duration = viewModel.duration.collectAsState().value,
                currentPos = viewModel.currentPosition.collectAsState().value,
                isFavorite = currentSong?.id in favoriteIds,
                onToggleFavorite = { viewModel.toggleFavorite(currentSong) },
                onPlayPause = { viewModel.togglePlayPause() },
                onNext = { viewModel.nextSong() },
                onPrevious = { viewModel.previousSong() },
                onSeek = { viewModel.seekTo(it) },
                onClick = onHeroClick
            )
        }

        if (recentlyPlayed.isNotEmpty()) {
            item { SectionTitle("Escuchado recientemente") }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(recentlyPlayed) { song ->
                        RecentSongItem(song) { 
                            // Al tocar aquí, la cola será el historial reciente
                            viewModel.selectSong(song, newQueue = recentlyPlayed) 
                        }
                    }
                }
            }
        }

        item { SectionTitle("Tus Playlists") }
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                items(playlists) { playlist ->
                    PlaylistItem(playlist) { onPlaylistClick(playlist) }
                }
                
                // Botón para Crear Playlist al FINAL
                item {
                    AddPlaylistItem { showCreateDialog = true }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Todas las canciones")
                HomeSearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.onHomeSearch(it) },
                    selectedFilter = selectedFilter,
                    onFilterSelect = { viewModel.setHomeFilter(it) }
                )
            }
        }

        items(filteredSongs) { song ->
            // Usamos SongListItem para consistencia también en la lista del home
            com.example.proyectopruebaappmusia1.ui.components.SongListItem(
                song = song,
                isCurrent = song.id == currentSong?.id,
                isFavorite = song.id in favoriteIds,
                onFavoriteClick = { viewModel.toggleFavorite(song) },
                onClick = { viewModel.selectSong(song, newQueue = filteredSongs) }
            )
        }
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                viewModel.createPlaylist(name)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun HomeHeader(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_zombie_logo),
            contentDescription = "Avatar",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(CardGreenBg)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "ZombieMusic",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tu música, tus reglas",
                color = AccentGreen,
                fontSize = 13.sp
            )
        }
        
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Ajustes",
                tint = Color.White
            )
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
}

@Composable
fun RecentSongItem(song: Song, onClick: () -> Unit) {
    Column(modifier = Modifier.width(120.dp).clickable { onClick() }) {
        AlbumArtImage(song.albumArt, null, Modifier.size(120.dp).clip(RoundedCornerShape(16.dp)))
        Text(song.title, color = Color.White, maxLines = 1, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text(song.artist, color = SecondaryText, maxLines = 1, fontSize = 12.sp)
    }
}

@Composable
fun PlaylistItem(playlist: Playlist, onClick: () -> Unit) {
    Column(modifier = Modifier.width(150.dp).clickable { onClick() }) {
        AlbumArtImage(
            albumArtId = playlist.coverImage,
            contentDescription = null,
            modifier = Modifier.size(150.dp).clip(RoundedCornerShape(20.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(playlist.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("${playlist.songCount} canciones", color = SecondaryText, fontSize = 12.sp)
    }
}

@Composable
fun AddPlaylistItem(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(CardGreenBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Crear Playlist",
                tint = AccentGreen,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Nueva Playlist", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("Crear", color = SecondaryText, fontSize = 12.sp)
    }
}

@Composable
fun CreatePlaylistDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardGreenBg,
        title = { Text("Nueva Playlist", color = Color.White) },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Nombre de la playlist", color = SecondaryText) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = DarkGreenBg,
                    unfocusedContainerColor = DarkGreenBg,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = AccentGreen,
                    focusedIndicatorColor = AccentGreen
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onCreate(name) },
                enabled = name.isNotBlank()
            ) {
                Text("CREAR", color = AccentGreen, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = SecondaryText)
            }
        }
    )
}

@Composable
fun HeroPlayerCard(
    song: Song?,
    isPlaying: Boolean,
    progress: Float,
    duration: Long,
    currentPos: Long,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Float) -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        color = CardGreenBg
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlbumArtImage(
                song?.albumArt,
                null,
                Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    song?.title ?: "Sin música",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    song?.artist ?: "Selecciona un tema",
                    color = SecondaryText,
                    fontSize = 14.sp,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Slider(
                    value = progress,
                    onValueChange = { onSeek(it) },
                    colors = SliderDefaults.colors(
                        thumbColor = AccentGreen,
                        activeTrackColor = AccentGreen,
                        inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.height(20.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(TimeUtils.formatTime(currentPos), color = SecondaryText, fontSize = 10.sp)
                    Text(TimeUtils.formatTime(duration), color = SecondaryText, fontSize = 10.sp)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPrevious) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior", tint = Color.White)
                    }
                    
                    IconButton(onClick = onPlayPause) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Reproducir/Pausar",
                            tint = AccentGreen,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    IconButton(onClick = onNext) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Siguiente", tint = Color.White)
                    }

                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (isFavorite) AccentGreen else Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedFilter: FilterOption,
    onFilterSelect: (FilterOption) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)),
            placeholder = { Text("Buscar...", color = SecondaryText) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CardGreenBg, 
                unfocusedContainerColor = CardGreenBg,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            leadingIcon = { Icon(Icons.Default.Search, null, tint = SecondaryText) }
        )
        Box {
            IconButton(onClick = { showMenu = true }, modifier = Modifier.background(CardGreenBg, RoundedCornerShape(12.dp))) {
                Icon(Icons.Default.FilterList, null, tint = AccentGreen)
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(CardGreenBg)) {
                FilterOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.displayName, color = Color.White) },
                        onClick = { onFilterSelect(option); showMenu = false }
                    )
                }
            }
        }
    }
}
