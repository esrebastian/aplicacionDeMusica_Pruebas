package com.example.proyectopruebaappmusia1.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.proyectopruebaappmusia1.domain.model.Playlist
import com.example.proyectopruebaappmusia1.domain.model.Song
import com.example.proyectopruebaappmusia1.ui.theme.*
import com.example.proyectopruebaappmusia1.ui.components.MusicIconButton
import com.example.proyectopruebaappmusia1.ui.components.SettingsButton
import com.example.proyectopruebaappmusia1.ui.components.SongListItem
import com.example.proyectopruebaappmusia1.viewmodel.MusicPlayerViewModel

@Composable
fun LibraryScreen(
    viewModel: MusicPlayerViewModel,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val songs by viewModel.filteredLibrarySongs.collectAsStateWithLifecycle(initialValue = emptyList())
    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle(initialValue = null)
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle(initialValue = emptySet())
    val libraryFilter by viewModel.libraryFilter.collectAsStateWithLifecycle(initialValue = "De la A a la Z")
    val searchQuery by viewModel.librarySearchQuery.collectAsStateWithLifecycle(initialValue = "")
    val customPlaylists by viewModel.customPlaylists.collectAsStateWithLifecycle(initialValue = emptyList())
    
    LibraryContent(
        songs = songs,
        currentSong = currentSong,
        favoriteIds = favoriteIds,
        customPlaylists = customPlaylists,
        libraryFilter = libraryFilter,
        searchQuery = searchQuery,
        onSettingsClick = onSettingsClick,
        onSearchChange = { viewModel.onSearchLibrary(it) },
        onFilterSelect = { viewModel.setLibraryFilter(it) },
        onSongClick = { song -> viewModel.selectSong(song, newQueue = songs) },
        onFavoriteClick = { viewModel.toggleFavorite(it) },
        onAddToPlaylist = { playlistId, song -> viewModel.addSongToPlaylist(playlistId, song) },
        onCreatePlaylistWithSong = { name, song -> viewModel.createPlaylistWithSong(name, song) },
        onAddSongsToPlaylist = { playlistId, selectedSongs ->
            selectedSongs.forEach { song -> viewModel.addSongToPlaylist(playlistId, song) }
        },
        onCreatePlaylistWithSongs = { name, selectedSongs ->
            val playlistId = viewModel.createPlaylist(name)
            selectedSongs.forEach { song -> viewModel.addSongToPlaylist(playlistId, song) }
        },
        onExcludeSong = { viewModel.excludeSongFromLibrary(it) },
        onDeleteSong = { viewModel.deleteSong(it) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryContent(
    songs: List<Song>,
    currentSong: Song?,
    favoriteIds: Set<String>,
    customPlaylists: List<Playlist>,
    libraryFilter: String,
    searchQuery: String,
    onSettingsClick: () -> Unit,
    onSearchChange: (String) -> Unit,
    onFilterSelect: (String) -> Unit,
    onSongClick: (Song) -> Unit,
    onFavoriteClick: (Song) -> Unit,
    onAddToPlaylist: (String, Song) -> Unit,
    onCreatePlaylistWithSong: (String, Song) -> Unit,
    onAddSongsToPlaylist: (String, List<Song>) -> Unit,
    onCreatePlaylistWithSongs: (String, List<Song>) -> Unit,
    onExcludeSong: (Song) -> Unit,
    onDeleteSong: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilterMenu by remember { mutableStateOf(false) }
    var songsToAdd by remember { mutableStateOf<List<Song>>(emptyList()) }
    var selectedSongIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showSongActions by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val selectedSongs = remember(songs, selectedSongIds) {
        songs.filter { it.id in selectedSongIds }
    }
    LaunchedEffect(songs) {
        selectedSongIds = selectedSongIds.intersect(songs.map { it.id }.toSet())
    }
    val filterOptions = listOf("De la A a la Z", "Artista", "Más recientes", "Duración más larga")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkGreenBg)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LibraryHeader(
            selectedCount = selectedSongs.size,
            showSongActions = showSongActions,
            onSettingsClick = onSettingsClick,
            onActionsClick = { showSongActions = true },
            onDismissActions = { showSongActions = false },
            onAddToPlaylist = {
                songsToAdd = selectedSongs
                showSongActions = false
            },
            onExclude = {
                selectedSongs.forEach(onExcludeSong)
                selectedSongIds = emptySet()
                showSongActions = false
            },
            onShare = {
                shareSongs(context, selectedSongs)
                showSongActions = false
            },
            onDelete = {
                selectedSongs.forEach(onDeleteSong)
                selectedSongIds = emptySet()
                showSongActions = false
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Buscar en tu música...", color = SecondaryText) },
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)),
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
                IconButton(
                    onClick = { showFilterMenu = true },
                    modifier = Modifier.background(CardGreenBg, RoundedCornerShape(12.dp)).size(52.dp)
                ) {
                    Icon(Icons.Default.FilterList, null, tint = AccentGreen)
                }
                
                DropdownMenu(
                    expanded = showFilterMenu,
                    onDismissRequest = { showFilterMenu = false },
                    modifier = Modifier.background(CardGreenBg)
                ) {
                    filterOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, color = if (libraryFilter == option) AccentGreen else PrimaryText) },
                            onClick = { 
                                onFilterSelect(option)
                                showFilterMenu = false 
                            }
                        )
                    }
                }
            }
        }

        LazyColumn(
            state = listState, 
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(songs, key = { it.id }, contentType = { "library_song" }) { song ->
                SongListItem(
                    song = song,
                    isCurrent = song.id == currentSong?.id,
                    isFavorite = song.id in favoriteIds,
                    isSelected = song.id in selectedSongIds,
                    onFavoriteClick = { onFavoriteClick(song) },
                    onClick = {
                        if (selectedSongIds.isNotEmpty()) {
                            selectedSongIds = if (song.id in selectedSongIds) {
                                selectedSongIds - song.id
                            } else {
                                selectedSongIds + song.id
                            }
                        } else {
                            onSongClick(song)
                        }
                    },
                    onLongClick = { selectedSongIds = selectedSongIds + song.id }
                )
            }
        }
    }

    if (songsToAdd.isNotEmpty()) {
        AddToPlaylistDialog(
            songs = songsToAdd,
            playlists = customPlaylists,
            onDismiss = { songsToAdd = emptyList() },
            onAddToPlaylist = { playlistId ->
                if (songsToAdd.size == 1) {
                    onAddToPlaylist(playlistId, songsToAdd.first())
                } else {
                    onAddSongsToPlaylist(playlistId, songsToAdd)
                }
                songsToAdd = emptyList()
                selectedSongIds = emptySet()
            },
            onCreatePlaylistWithSong = { name ->
                if (songsToAdd.size == 1) {
                    onCreatePlaylistWithSong(name, songsToAdd.first())
                } else {
                    onCreatePlaylistWithSongs(name, songsToAdd)
                }
                songsToAdd = emptyList()
                selectedSongIds = emptySet()
            }
        )
    }
}

@Composable
private fun LibraryHeader(
    selectedCount: Int,
    showSongActions: Boolean,
    onSettingsClick: () -> Unit,
    onActionsClick: () -> Unit,
    onDismissActions: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onExclude: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val hasSelectedSong = selectedCount > 0
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = if (hasSelectedSong) "$selectedCount seleccionadas" else "Biblioteca",
            color = PrimaryText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Box {
            if (hasSelectedSong) {
                MusicIconButton(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Opciones de cancion",
                    onClick = onActionsClick,
                    tint = SecondaryText,
                    containerColor = CardGreenBg,
                    buttonSize = 48.dp,
                    iconSize = 24.dp
                )
            } else {
                SettingsButton(onClick = onSettingsClick)
            }

            DropdownMenu(
                expanded = showSongActions && hasSelectedSong,
                onDismissRequest = onDismissActions,
                modifier = Modifier.background(CardGreenBg)
            ) {
                DropdownMenuItem(
                    text = { Text("Agregar a una playlist", color = PrimaryText) },
                    leadingIcon = { Icon(Icons.Default.PlaylistAdd, null, tint = AccentGreen) },
                    onClick = onAddToPlaylist
                )
                DropdownMenuItem(
                    text = { Text("Sacar de la cola", color = PrimaryText) },
                    leadingIcon = { Icon(Icons.Default.RemoveCircleOutline, null, tint = AccentGreen) },
                    onClick = onExclude
                )
                DropdownMenuItem(
                    text = { Text("Compartir", color = PrimaryText) },
                    leadingIcon = { Icon(Icons.Default.Share, null, tint = AccentGreen) },
                    onClick = onShare
                )
                DropdownMenuItem(
                    text = { Text("Eliminar", color = Color.Red.copy(alpha = 0.82f)) },
                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.82f)) },
                    onClick = onDelete
                )
            }
        }
    }
}

private fun shareSongs(context: Context, songs: List<Song>) {
    if (songs.isEmpty()) return

    val uris = ArrayList(
        songs.mapNotNull { song ->
            song.filePath.takeIf { it.isNotBlank() }?.let(Uri::parse)
        }
    )
    val shareText = songs.joinToString(separator = "\n") { "${it.title} - ${it.artist}" }
    val intent = if (uris.size > 1) {
        Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "audio/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    } else {
        Intent(Intent.ACTION_SEND).apply {
            type = if (uris.isNotEmpty()) "audio/*" else "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            uris.firstOrNull()?.let {
                putExtra(Intent.EXTRA_STREAM, it)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
    }
    context.startActivity(Intent.createChooser(intent, "Compartir cancion"))
}

@Composable
fun AddToPlaylistDialog(
    songs: List<Song>,
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onAddToPlaylist: (String) -> Unit,
    onCreatePlaylistWithSong: (String) -> Unit
) {
    var newPlaylistName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardGreenBg,
        title = { Text("Agrega a una playlist", color = PrimaryText) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    if (songs.size == 1) songs.first().title else "${songs.size} canciones seleccionadas",
                    color = SecondaryText,
                    fontSize = 14.sp,
                    maxLines = 1
                )

                if (playlists.isNotEmpty()) {
                    playlists.forEach { playlist ->
                        TextButton(
                            onClick = { onAddToPlaylist(playlist.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                playlist.name,
                                color = AccentGreen,
                                modifier = Modifier.weight(1f)
                            )
                            Text("${playlist.songCount}", color = SecondaryText)
                        }
                    }
                } else {
                    Text("Crea una playlist y la agregala", color = SecondaryText, fontSize = 14.sp)
                }

                TextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    placeholder = { Text("Nueva playlist", color = SecondaryText) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DarkGreenBg,
                        unfocusedContainerColor = DarkGreenBg,
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText,
                        cursorColor = AccentGreen,
                        focusedIndicatorColor = AccentGreen
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreatePlaylistWithSong(newPlaylistName.trim()) },
                enabled = newPlaylistName.isNotBlank()
            ) {
                Text("CREAR Y AGREGAR", color = AccentGreen, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = SecondaryText)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun LibraryScreenPreview() {
    ProyectoPruebaAppMusia1Theme {
        LibraryContent(
            songs = listOf(
                Song("1", "Song One", "Artist A", 180000, "", null),
                Song("2", "Song Two", "Artist B", 240000, "", null)
            ),
            currentSong = null,
            favoriteIds = setOf("1"),
            customPlaylists = emptyList(),
            libraryFilter = "De la A a la Z",
            searchQuery = "",
            onSettingsClick = {},
            onSearchChange = {},
            onFilterSelect = {},
            onSongClick = {},
            onFavoriteClick = {},
            onAddToPlaylist = { _, _ -> },
            onCreatePlaylistWithSong = { _, _ -> },
            onAddSongsToPlaylist = { _, _ -> },
            onCreatePlaylistWithSongs = { _, _ -> },
            onExcludeSong = {},
            onDeleteSong = {}
        )
    }
}
