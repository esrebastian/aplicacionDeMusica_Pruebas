package com.example.proyectopruebaappmusia1

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectopruebaappmusia1.model.Song
import com.example.proyectopruebaappmusia1.viewmodel.MusicPlayerViewModel

@Composable
fun pantallaLibreria(
    playlist: List<Song>,
    currentSong: Song?,
    viewModel: MusicPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showFilterMenu by remember { mutableStateOf(false) }
    
    val filterOptions = listOf(
        "Más reproducido",
        "De la A a la Z",
        "Más recientes",
        "Artista",
        "Duración más larga"
    )
    var selectedFilter by rememberSaveable { mutableStateOf(filterOptions[1]) }

    // Estado del scroll para poder resetearlo
    val listState = rememberLazyListState()

    // Cada vez que el filtro cambie, volvemos al principio de la lista
    LaunchedEffect(selectedFilter) {
        listState.scrollToItem(0)
    }

    val filteredSongs: List<Song> = remember(playlist, searchQuery, selectedFilter) {
        val baseList: List<Song> = if (searchQuery.isBlank()) {
            playlist
        } else {
            playlist.filter { song: Song ->
                song.title.contains(searchQuery, ignoreCase = true) ||
                        song.artist.contains(searchQuery, ignoreCase = true)
            }
        }
        
        when (selectedFilter) {
            "Más reproducido" -> baseList.sortedByDescending { it.playCount }
            "De la A a la Z" -> baseList.sortedBy { it.title }
            "Artista" -> baseList.sortedBy { it.artist }
            "Duración más larga" -> baseList.sortedByDescending { it.duration }
            "Más recientes" -> baseList.sortedByDescending { it.lastPlayed }
            else -> baseList 
        }
    }

    Column(
        modifier = modifier
            .background(DarkGreenBg)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.library),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardGreenBg)
                    .clickable { /* TODO: Pantalla de ajustes */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Ajustes",
                    tint = SecondaryText,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_hint),
                        color = SecondaryText
                    )
                },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentGreen,
                    unfocusedBorderColor = CardGreenBg,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = AccentGreen
                )
            )

            Box {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardGreenBg)
                        .clickable { showFilterMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filtrar",
                        tint = AccentGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }

                DropdownMenu(
                    expanded = showFilterMenu,
                    onDismissRequest = { showFilterMenu = false },
                    modifier = Modifier.background(CardGreenBg)
                ) {
                    filterOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = option, 
                                    color = if (selectedFilter == option) AccentGreen else Color.White 
                                ) 
                            },
                            onClick = {
                                selectedFilter = option
                                showFilterMenu = false
                            },
                            leadingIcon = {
                                if (selectedFilter == option) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = AccentGreen
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        LazyColumn(
            state = listState, // Vinculamos el estado del scroll
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(filteredSongs, key = { it.id }) { song ->
                SongListItem(
                    song = song,
                    isCurrent = currentSong?.id == song.id,
                    isFavorite = song.id in favoriteIds,
                    onFavoriteClick = { viewModel.toggleFavorite(song) },
                    onClick = { viewModel.selectSong(song) }
                )
            }
        }
    }
}
