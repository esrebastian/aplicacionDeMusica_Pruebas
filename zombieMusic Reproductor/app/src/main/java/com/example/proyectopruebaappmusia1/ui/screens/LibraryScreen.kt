package com.example.proyectopruebaappmusia1.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.proyectopruebaappmusia1.domain.model.Song
import com.example.proyectopruebaappmusia1.ui.theme.*
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
    
    LibraryContent(
        songs = songs,
        currentSong = currentSong,
        favoriteIds = favoriteIds,
        libraryFilter = libraryFilter,
        searchQuery = searchQuery,
        onSettingsClick = onSettingsClick,
        onSearchChange = { viewModel.onSearchLibrary(it) },
        onFilterSelect = { viewModel.setLibraryFilter(it) },
        onSongClick = { song -> viewModel.selectSong(song, newQueue = songs) },
        onFavoriteClick = { viewModel.toggleFavorite(it) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryContent(
    songs: List<Song>,
    currentSong: Song?,
    favoriteIds: Set<String>,
    libraryFilter: String,
    searchQuery: String,
    onSettingsClick: () -> Unit,
    onSearchChange: (String) -> Unit,
    onFilterSelect: (String) -> Unit,
    onSongClick: (Song) -> Unit,
    onFavoriteClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilterMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val filterOptions = listOf("De la A a la Z", "Artista", "Más recientes", "Duración más larga")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkGreenBg)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Tu Librería", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardGreenBg)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Ajustes",
                    tint = SecondaryText
                )
            }
        }

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
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
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
                            text = { Text(option, color = if (libraryFilter == option) AccentGreen else Color.White) },
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
            items(songs, key = { it.id }) { song ->
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
            libraryFilter = "De la A a la Z",
            searchQuery = "",
            onSettingsClick = {},
            onSearchChange = {},
            onFilterSelect = {},
            onSongClick = {},
            onFavoriteClick = {}
        )
    }
}
