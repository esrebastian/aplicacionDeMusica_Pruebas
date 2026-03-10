package com.example.proyectopruebaappmusia1

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
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectopruebaappmusia1.model.Song
import com.example.proyectopruebaappmusia1.viewmodel.MusicPlayerViewModel

@Composable
fun pantallaInicio(
    viewModel: MusicPlayerViewModel,
    currentSong: Song?,
    isPlaying: Boolean,
    recentlyPlayed: List<Song>,
    favoriteIds: Set<String>,
    filteredPlaylist: List<Song>,
    homeSearchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: FilterOption,
    onFilterSelected: (FilterOption) -> Unit,
    onHeroClick: () -> Unit,
    onSeeAllRecentlyPlayed: () -> Unit,
    paddingValues: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { TopHeader() }
        item {
            NowPlayingHeroCard(
                currentSong = currentSong,
                isPlaying = isPlaying,
                viewModel = viewModel,
                favoriteIds = favoriteIds,
                onClick = onHeroClick
            )
        }
        
        if (recentlyPlayed.isNotEmpty()) {
            item {
                SectionHeader(
                    title = stringResource(R.string.recently_played),
                    showSeeAll = true,
                    onSeeAllClick = onSeeAllRecentlyPlayed
                )
            }
            item { 
                RecentlyPlayedRow(
                    recentlyPlayed.take(10),
                    viewModel 
                ) 
            }
        }

        item {
            SectionHeader(
                title = stringResource(R.string.trending_playlists),
                showSeeAll = true
            )
        }
        item { TrendingPlaylistsColumn() }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader(
                    title = stringResource(R.string.all_songs),
                    showSeeAll = false
                )
                HomeSearchBarWithFilter(
                    searchQuery = homeSearchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    selectedFilter = selectedFilter,
                    onFilterSelected = onFilterSelected
                )
            }
        }
        
        items(filteredPlaylist) { song ->
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

@Composable
fun TopHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón de Notificaciones a la izquierda
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CardGreenBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = SecondaryText,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Contenedor central para el usuario
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(AccentGreen.copy(alpha = 0.2f))
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_zombie_logo),
                    contentDescription = stringResource(R.string.content_description_profile),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.username),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = stringResource(R.string.music_subtitle),
                    color = AccentGreen,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Botón de Ajustes a la derecha
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
}

@Composable
fun NowPlayingHeroCard(
    currentSong: Song?,
    isPlaying: Boolean,
    viewModel: MusicPlayerViewModel,
    favoriteIds: Set<String>,
    onClick: () -> Unit
) {
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    var isUserSeeking by remember { mutableStateOf(false) }
    val duration = viewModel.duration.collectAsState().value
    val currentPosition = viewModel.currentPosition.collectAsState().value

    LaunchedEffect(currentPosition, duration, isUserSeeking) {
        if (!isUserSeeking && duration > 0L) {
            sliderPosition = (currentPosition.toFloat() / duration).coerceIn(0f, 1f)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(12.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = CardGreenBg,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlbumArtImage(
                albumArtId = currentSong?.albumArt,
                contentDescription = stringResource(R.string.album_art),
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentSong?.title ?: stringResource(R.string.my_song),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentSong?.artist ?: stringResource(R.string.artist_name),
                    color = SecondaryText,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = sliderPosition.coerceIn(0f, 1f),
                    onValueChange = { value ->
                        isUserSeeking = true
                        sliderPosition = value
                    },
                    onValueChangeFinished = {
                        if (duration > 0L) {
                            viewModel.seekTo(sliderPosition.coerceIn(0f, 1f))
                        }
                        isUserSeeking = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = AccentGreen,
                        activeTrackColor = AccentGreen,
                        inactiveTrackColor = Color(0xFF2D3D32)
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        formatTime(viewModel.currentPosition.collectAsState().value),
                        color = SecondaryText,
                        fontSize = 11.sp
                    )
                    Text(
                        formatTime(viewModel.duration.collectAsState().value),
                        color = SecondaryText,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isFavorite = currentSong?.id in favoriteIds
                    IconButton(
                        onClick = { viewModel.toggleFavorite(currentSong) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Outlined.Favorite,
                            contentDescription = stringResource(R.string.favorite_content_description),
                            tint = if (isFavorite) AccentGreen else SecondaryText,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.previousSong() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.SkipPrevious,
                            contentDescription = null,
                            tint = AccentGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(AccentGreen)
                            .clickable { viewModel.togglePlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = DarkGreenBg,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.nextSong() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.SkipNext,
                            contentDescription = null,
                            tint = AccentGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(
                        onClick = { },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = null,
                            tint = AccentGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, showSeeAll: Boolean = true, onSeeAllClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )
        if (showSeeAll) {
            Text(
                text = stringResource(R.string.see_all),
                color = AccentGreen,
                fontSize = 14.sp,
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }
    }
}

@Composable
fun RecentlyPlayedRow(songs: List<Song>, viewModel: MusicPlayerViewModel) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(songs) { song ->
            RecentlyPlayedItem(
                song = song,
                onClick = {
                    viewModel.selectSong(song)
                }
            )
        }
    }
}

@Composable
fun TrendingPlaylistsColumn() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TrendingPlaylistItem(stringResource(R.string.music_mix), 56)
        TrendingPlaylistItem(stringResource(R.string.chill_beats), 36)
    }
}

@Composable
fun TrendingPlaylistItem(title: String, songCount: Int) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = CardGreenBg,
        tonalElevation = 4.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                AccentGreen.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = ColorPainter(Color.Gray),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.song_count_format, songCount),
                        color = SecondaryText,
                        fontSize = 14.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AccentGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(AccentGreen)
                    )
                }
            }
        }
    }
}

@Composable
fun RecentlyPlayedItem(
    song: Song,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .width(130.dp)
            .clickable { onClick() }
    ) {
        AlbumArtImage(
            albumArtId = song.albumArt,
            contentDescription = null,
            modifier = Modifier
                .size(130.dp)
                .clip(RoundedCornerShape(20.dp))
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = song.title,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            text = song.artist,
            color = SecondaryText,
            fontSize = 13.sp,
            maxLines = 1
        )
    }
}

@Composable
fun HomeSearchBarWithFilter(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: FilterOption,
    onFilterSelected: (FilterOption) -> Unit
) {
    var showFilterMenu by remember { mutableStateOf(false) }

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
            shape = RoundedCornerShape(16.dp),
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
                    onValueChange = onSearchQueryChange,
                    placeholder = { 
                        Text(
                            text = "Buscar canciones...", 
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
                    .clickable { showFilterMenu = true },
                shape = RoundedCornerShape(16.dp),
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
                FilterOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = option.displayName, 
                                color = if (selectedFilter == option) AccentGreen else Color.White 
                            ) 
                        },
                        onClick = {
                            onFilterSelected(option)
                            showFilterMenu = false
                        },
                        leadingIcon = {
                            if (selectedFilter == option) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = AccentGreen)
                            }
                        }
                    )
                }
            }
        }
    }
}
