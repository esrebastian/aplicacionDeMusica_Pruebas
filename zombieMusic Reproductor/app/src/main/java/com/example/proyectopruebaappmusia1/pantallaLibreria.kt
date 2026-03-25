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
import androidx.compose.material.icons.filled.Search
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
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val selectedFilter by viewModel.libraryFilter.collectAsState()
    
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showFilterMenu by remember { mutableStateOf(false) }
    
    val filterOptions = listOf(
        "Más reproducido",
        "De la A a la Z",
        "Más recientes",
        "Artista",
        "Duración más larga"
    )

    // Estado del scroll para poder resetearlo
    val listState = rememberLazyListState()

    // Cada vez que el filtro cambie, volvemos al principio de la lista
    LaunchedEffect(selectedFilter) {
        listState.scrollToItem(0)
    }

    // Filtrar solo por búsqueda, ya que el ViewModel nos da la lista ya ordenada por el filtro seleccionado
    val filteredSongs: List<Song> = remember(playlist, searchQuery) {
        if (searchQuery.isBlank()) {
            playlist
        } else {
            playlist.filter { song: Song ->
                song.title.contains(searchQuery, ignoreCase = true) ||
                        song.artist.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .background(DarkGreenBg)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabecera con título y botón de ajustes
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.library),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardGreenBg)
                    .clickable { onSettingsClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Ajustes",
                    tint = SecondaryText,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Barra de búsqueda y botón de filtro estilizados
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(12.dp),
                color = CardGreenBg
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = SecondaryText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.search_hint),
                                color = SecondaryText,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = AccentGreen,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                    )
                }
            }

            Box {
                Surface(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showFilterMenu = true },
                    shape = RoundedCornerShape(12.dp),
                    color = CardGreenBg
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtrar",
                            tint = AccentGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
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
                                viewModel.setLibraryFilter(option)
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
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(filteredSongs, key = { it.id }) { song ->
                SongListItem(
                    song = song,
                    isCurrent = currentSong?.id == song.id,
                    isFavorite = song.id in favoriteIds,
                    onFavoriteClick = { viewModel.toggleFavorite(song) },
                    onClick = { viewModel.selectSong(song, fromUserTap = true) }
                )
            }
        }
    }
}
