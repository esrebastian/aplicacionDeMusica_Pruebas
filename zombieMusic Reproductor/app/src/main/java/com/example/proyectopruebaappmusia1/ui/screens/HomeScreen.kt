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
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.proyectopruebaappmusia1.R
import com.example.proyectopruebaappmusia1.domain.model.OnlineTrack
import com.example.proyectopruebaappmusia1.domain.model.Playlist
import com.example.proyectopruebaappmusia1.domain.model.Song
import com.example.proyectopruebaappmusia1.ui.components.OnlineSearchResultItem
import com.example.proyectopruebaappmusia1.ui.theme.*
import com.example.proyectopruebaappmusia1.ui.components.AlbumArtImage
import com.example.proyectopruebaappmusia1.ui.components.MusicIconButton
import com.example.proyectopruebaappmusia1.ui.components.SettingsButton
import com.example.proyectopruebaappmusia1.util.TimeUtils
import com.example.proyectopruebaappmusia1.viewmodel.MusicPlayerViewModel
import com.example.proyectopruebaappmusia1.viewmodel.FilterOption

@Composable
fun HomeScreen(
    viewModel: MusicPlayerViewModel,
    onHeroClick: () -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val searchQuery by viewModel.homeSearchQuery.collectAsStateWithLifecycle()
    val onlineResults by viewModel.homeOnlineResults.collectAsStateWithLifecycle()
    val searchLoading by viewModel.homeSearchLoading.collectAsStateWithLifecycle()
    val searchError by viewModel.homeSearchError.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.homeFilter.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val playlists by viewModel.homePlaylists.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val duration by viewModel.duration.collectAsStateWithLifecycle()
    val currentPos by viewModel.currentPosition.collectAsStateWithLifecycle()

    HomeContent(
        currentSong = currentSong,
        isPlaying = isPlaying,
        recentlyPlayed = recentlyPlayed,
        favoriteIds = favoriteIds,
        searchQuery = searchQuery,
        onlineResults = onlineResults,
        searchLoading = searchLoading,
        searchError = searchError,
        selectedFilter = selectedFilter,
        playlists = playlists,
        progress = progress,
        duration = duration,
        currentPos = currentPos,
        onHeroClick = onHeroClick,
        onPlaylistClick = onPlaylistClick,
        onSettingsClick = onSettingsClick,
        onToggleFavorite = { viewModel.toggleFavorite(currentSong) },
        onPlayPause = { viewModel.togglePlayPause() },
        onNext = { viewModel.nextSong() },
        onPrevious = { viewModel.previousSong() },
        onSeek = { viewModel.seekTo(it) },
        onSearchChange = { viewModel.onHomeSearch(it) },
        onOnlineTrackClick = { track ->
            viewModel.playOnlineTrack(track)
        },
        onFilterSelect = { viewModel.setHomeFilter(it) },
        onCreatePlaylist = { viewModel.createPlaylist(it) },
        onSongClick = { song -> viewModel.selectSong(song, newQueue = recentlyPlayed) },
        modifier = modifier
    )
}

@Composable
fun HomeContent(
    currentSong: Song?,
    isPlaying: Boolean,
    recentlyPlayed: List<Song>,
    favoriteIds: Set<String>,
    searchQuery: String,
    onlineResults: List<OnlineTrack>,
    searchLoading: Boolean,
    searchError: String?,
    selectedFilter: FilterOption,
    playlists: List<Playlist>,
    progress: Float,
    duration: Long,
    currentPos: Long,
    onHeroClick: () -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onSettingsClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Float) -> Unit,
    onSearchChange: (String) -> Unit,
    onOnlineTrackClick: (OnlineTrack) -> Unit,
    onFilterSelect: (FilterOption) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onSongClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(DarkGreenBg),
        contentPadding = PaddingValues(bottom = 80.dp, start = 16.dp, end = 16.dp, top = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { 
            HomeHeader(onSettingsClick = onSettingsClick) 
        }
        
        item {
            HeroPlayerCard(
                song = currentSong,
                isPlaying = isPlaying,
                progress = progress,
                duration = duration,
                currentPos = currentPos,
                isFavorite = currentSong?.id in favoriteIds,
                onToggleFavorite = onToggleFavorite,
                onPlayPause = onPlayPause,
                onNext = onNext,
                onPrevious = onPrevious,
                onSeek = onSeek,
                onClick = onHeroClick
            )
        }

        if (recentlyPlayed.isNotEmpty()) {
            item { SectionTitle("Escuchado recientemente") }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(recentlyPlayed, key = { it.id }, contentType = { "recent_song" }) { song ->
                        RecentSongItem(song) { onSongClick(song) }
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
                items(playlists, key = { it.id }, contentType = { "playlist" }) { playlist ->
                    PlaylistItem(playlist) { onPlaylistClick(playlist) }
                }
                
                item {
                    AddPlaylistItem { showCreateDialog = true }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HomeSearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchChange,
                    selectedFilter = selectedFilter,
                    onFilterSelect = onFilterSelect
                )
                if (searchQuery.isNotBlank()) {
                    HomeOnlineSearchResults(
                        results = onlineResults,
                        isLoading = searchLoading,
                        error = searchError,
                        onTrackClick = onOnlineTrackClick
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                onCreatePlaylist(name)
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
                color = PrimaryText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tu música, tus reglas",
                color = AccentGreen,
                fontSize = 13.sp
            )
        }
        
        SettingsButton(onClick = onSettingsClick)
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(title, color = PrimaryText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
}

@Composable
fun RecentSongItem(song: Song, onClick: () -> Unit) {
    Column(modifier = Modifier.width(120.dp).clickable { onClick() }) {
        AlbumArtImage(song.albumArt, null, Modifier.size(120.dp).clip(RoundedCornerShape(16.dp)))
        Text(song.title, color = PrimaryText, maxLines = 1, fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
        Text(playlist.name, color = PrimaryText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
        Text("Nueva Playlist", color = PrimaryText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("Crear", color = SecondaryText, fontSize = 12.sp)
    }
}

@Composable
fun CreatePlaylistDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardGreenBg,
        title = { Text("Nueva Playlist", color = PrimaryText) },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Nombre de la playlist", color = SecondaryText) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = DarkGreenBg,
                    unfocusedContainerColor = DarkGreenBg,
                    focusedTextColor = PrimaryText,
                    unfocusedTextColor = PrimaryText,
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
        BoxWithConstraints {
            val compactCard = maxWidth < 390.dp
            val albumSize = if (compactCard) 96.dp else 130.dp
            val albumCorner = if (compactCard) 18.dp else 20.dp
            val horizontalGap = if (compactCard) 10.dp else 16.dp
            val controlSize = 48.dp

            Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlbumArtImage(
                song?.albumArt,
                null,
                Modifier
                    .size(albumSize)
                    .clip(RoundedCornerShape(albumCorner))
            )
            
            Spacer(modifier = Modifier.width(horizontalGap))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    song?.title ?: "Sin música",
                    color = PrimaryText,
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MusicIconButton(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Anterior",
                        onClick = onPrevious,
                        tint = PrimaryText,
                        buttonSize = controlSize,
                        iconSize = 26.dp
                    )
                    
                    MusicIconButton(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Reproducir/Pausar",
                        onClick = onPlayPause,
                        tint = AccentGreen,
                        buttonSize = controlSize,
                        iconSize = 36.dp
                    )
                    
                    MusicIconButton(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Siguiente",
                        onClick = onNext,
                        tint = PrimaryText,
                        buttonSize = controlSize,
                        iconSize = 26.dp
                    )

                    MusicIconButton(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorito",
                        onClick = onToggleFavorite,
                        tint = if (isFavorite) AccentGreen else PrimaryText,
                        buttonSize = controlSize,
                        iconSize = 22.dp
                    )
                }
            }
        }
        }
    }
}

@Composable
fun HomeOnlineSearchResults(
    results: List<OnlineTrack>,
    isLoading: Boolean,
    error: String?,
    onTrackClick: (OnlineTrack) -> Unit
) {
    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentGreen, modifier = Modifier.size(28.dp))
            }
        }
        error != null -> {
            Text(error, color = SecondaryText, fontSize = 13.sp)
        }
        results.isEmpty() -> {
            Text("No se encontraron canciones", color = SecondaryText, fontSize = 13.sp)
        }
        else -> {
            Column {
                SectionTitle("Resultados en línea")
                results.forEach { track ->
                    OnlineSearchResultItem(track = track, onClick = { onTrackClick(track) })
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
                focusedTextColor = PrimaryText,
                unfocusedTextColor = PrimaryText
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
                        text = { Text(option.displayName, color = PrimaryText) },
                        onClick = { onFilterSelect(option); showMenu = false }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ProyectoPruebaAppMusia1Theme {
        HomeContent(
            currentSong = Song("1", "Canción de Prueba", "Artista Zombie", 300000, "", null),
            isPlaying = true,
            recentlyPlayed = listOf(
                Song("2", "Recent 1", "Artist 1", 200000, "", null),
                Song("3", "Recent 2", "Artist 2", 180000, "", null)
            ),
            favoriteIds = setOf("1"),
            searchQuery = "",
            onlineResults = emptyList(),
            searchLoading = false,
            searchError = null,
            selectedFilter = FilterOption.TITLE,
            playlists = listOf(
                Playlist("p1", "Mi Playlist Mix", 12, null)
            ),
            progress = 0.4f,
            duration = 300000,
            currentPos = 120000,
            onHeroClick = {},
            onPlaylistClick = {},
            onSettingsClick = {},
            onToggleFavorite = {},
            onPlayPause = {},
            onNext = {},
            onPrevious = {},
            onSeek = {},
            onSearchChange = {},
            onOnlineTrackClick = {},
            onFilterSelect = {},
            onCreatePlaylist = {},
            onSongClick = {}
        )
    }
}
