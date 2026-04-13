package com.example.proyectopruebaappmusia1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectopruebaappmusia1.R
import com.example.proyectopruebaappmusia1.ui.theme.*
import com.example.proyectopruebaappmusia1.ui.components.SongListItem
import com.example.proyectopruebaappmusia1.viewmodel.MusicPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: MusicPlayerViewModel,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val songs by viewModel.filteredLibrarySongs.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val selectedFilter by viewModel.libraryFilter.collectAsState()
    val searchQuery by viewModel.librarySearchQuery.collectAsState()
    
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
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, null, tint = SecondaryText)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchLibrary(it) },
                placeholder = { Text("Buscar en tu música...", color = SecondaryText) },
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CardGreenBg,
                    unfocusedContainerColor = CardGreenBg,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                leadingIcon = { Icon(Icons.Default.Search, null, tint = SecondaryText) }
            )

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
                        text = { Text(option, color = if (selectedFilter == option) AccentGreen else Color.White) },
                        onClick = { viewModel.setLibraryFilter(option); showFilterMenu = false },
                        leadingIcon = { if (selectedFilter == option) Icon(Icons.Default.Check, null, tint = AccentGreen) }
                    )
                }
            }
        }

        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            items(songs, key = { it.id }) { song ->
                SongListItem(
                    song = song,
                    isCurrent = song.id == currentSong?.id,
                    isFavorite = song.id in favoriteIds,
                    onFavoriteClick = { viewModel.toggleFavorite(song) },
                    onClick = { viewModel.selectSong(song, fromUserTap = true) }
                )
            }
        }
    }
}
