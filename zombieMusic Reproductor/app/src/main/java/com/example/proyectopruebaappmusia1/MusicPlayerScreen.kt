package com.example.proyectopruebaappmusia1

import android.app.Application
import android.content.ContentUris
import androidx.core.net.toUri
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
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectopruebaappmusia1.ui.theme.ProyectoPruebaAppMusia1Theme
import com.example.proyectopruebaappmusia1.viewmodel.MusicPlayerViewModel
import com.example.proyectopruebaappmusia1.model.Song
import com.example.proyectopruebaappmusia1.util.MusicPermissionLauncher
import coil.compose.AsyncImage

private val DarkGreenBg = Color(0xFF0D1410)
private val CardGreenBg = Color(0xFF1B261F)
private val AccentGreen = Color(0xFFC1F153)
private val SecondaryText = Color(0xFF8BA08E)
private val IconPlaceholderColor = Color(0xFF6B7E6F)

private enum class BottomTab {
    HOME, EXPLORE, LIBRARY, FAVORITES
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerScreen(
    modifier: Modifier = Modifier,
    viewModel: MusicPlayerViewModel = run {
        val application = LocalContext.current.applicationContext as Application
        viewModel(
            factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return MusicPlayerViewModel(application) as T
                }
            }
        )
    }
) {
    val context = LocalContext.current
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val playlist by viewModel.playlist.collectAsState()
    var selectedTab by rememberSaveable { mutableStateOf(BottomTab.HOME) }
    var showFullScreenPlayer by rememberSaveable { mutableStateOf(false) }

    MusicPermissionLauncher {
        // Cargar canciones reales del dispositivo una vez concedido el permiso
        viewModel.loadRealSongs(context)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = DarkGreenBg,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                Column(
                    modifier = Modifier
                        .background(DarkGreenBg)
                        .navigationBarsPadding()
                ) {
                    currentSong?.let { song ->
                        NowPlayingMiniBar(
                            song = song,
                            isPlaying = isPlaying,
                            progress = progress,
                            onTap = { showFullScreenPlayer = true },
                            onPlayPauseClick = { viewModel.togglePlayPause() }
                        )
                    }
                    MusicBottomNavigation(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )
                }
            }
        ) { paddingValues ->
            when (selectedTab) {
                BottomTab.LIBRARY -> {
                    LibraryScreen(
                        playlist = playlist,
                        currentSong = currentSong,
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
                BottomTab.FAVORITES -> {
                    val favoriteSongs by viewModel.favoriteSongs.collectAsState()
                    FavoritesScreen(
                        favoriteSongs = favoriteSongs,
                        currentSong = currentSong,
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(28.dp)
                    ) {
                        item { TopHeader() }
                        item {
                            NowPlayingHeroCard(
                                currentSong = currentSong,
                                isPlaying = isPlaying,
                                viewModel = viewModel,
                                onClick = { showFullScreenPlayer = true }
                            )
                        }
                        item {
                            SectionHeader(
                                title = stringResource(R.string.recently_played),
                                showSeeAll = true
                            )
                        }
                        item { RecentlyPlayedRow(playlist, viewModel) }
                        item {
                            SectionHeader(
                                title = stringResource(R.string.all_songs),
                                showSeeAll = false
                            )
                        }
                        items(playlist) { song ->
                            SongListItem(
                                song = song,
                                isCurrent = currentSong?.id == song.id,
                                isFavorite = viewModel.isFavorite(song.id),
                                onFavoriteClick = { viewModel.toggleFavorite(song) },
                                onClick = { viewModel.selectSong(song) }
                            )
                        }
                        item {
                            SectionHeader(
                                title = stringResource(R.string.trending_playlists),
                                showSeeAll = true
                            )
                        }
                        item { TrendingPlaylistsColumn() }
                    }
                }
            }
        }

        if (showFullScreenPlayer) {
            Dialog(
                onDismissRequest = { showFullScreenPlayer = false },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = false
                )
            ) {
                NowPlayingFullScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    onClose = { showFullScreenPlayer = false }
                )
            }
        }
    }
}

@Composable
private fun TopHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
        Column(modifier = Modifier.weight(1f)) {
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
        SearchField(modifier = Modifier.width(140.dp))
        Spacer(modifier = Modifier.width(12.dp))
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
    }
}

@Composable
private fun SearchField(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(20.dp),
        color = CardGreenBg,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search_hint),
                tint = SecondaryText,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.search_hint),
                color = SecondaryText,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun NowPlayingHeroCard(
    currentSong: Song?,
    isPlaying: Boolean,
    viewModel: MusicPlayerViewModel,
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
                // Slider interactivo para adelantar/retroceder
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
                    val isFavorite = viewModel.isFavorite(currentSong?.id)
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
private fun SectionHeader(title: String, showSeeAll: Boolean) {
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
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun RecentlyPlayedRow(
    playlist: List<Song>,
    viewModel: MusicPlayerViewModel
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(playlist) { song ->
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
private fun LibraryScreen(
    playlist: List<Song>,
    currentSong: Song?,
    viewModel: MusicPlayerViewModel,
    modifier: Modifier = Modifier
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val filteredSongs = if (searchQuery.isBlank()) {
        playlist
    } else {
        playlist.filter { song ->
            song.title.contains(searchQuery, ignoreCase = true) ||
                    song.artist.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .background(DarkGreenBg)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.library),
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

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
            modifier = Modifier
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentGreen,
                unfocusedBorderColor = CardGreenBg,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = AccentGreen
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(filteredSongs) { song ->
                SongListItem(
                    song = song,
                    isCurrent = currentSong?.id == song.id,
                    isFavorite = viewModel.isFavorite(song.id),
                    onFavoriteClick = { viewModel.toggleFavorite(song) },
                    onClick = { viewModel.selectSong(song) }
                )
            }
        }
    }
}

@Composable
private fun FavoritesScreen(
    favoriteSongs: List<Song>,
    currentSong: Song?,
    viewModel: MusicPlayerViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(DarkGreenBg)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.favorites),
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        if (favoriteSongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.favorites_empty),
                    color = SecondaryText,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(favoriteSongs) { song ->
                    SongListItem(
                        song = song,
                        isCurrent = currentSong?.id == song.id,
                        isFavorite = true,
                        onFavoriteClick = { viewModel.toggleFavorite(song) },
                        onClick = { viewModel.selectSong(song) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SongListItem(
    song: Song,
    isCurrent: Boolean,
    onClick: () -> Unit,
    isFavorite: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlbumArtImage(
            albumArtId = song.albumArt,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1
            )
            Text(
                text = song.artist,
                color = SecondaryText,
                fontSize = 13.sp,
                maxLines = 1
            )
        }
        if (onFavoriteClick != null) {
            IconButton(
                onClick = { onFavoriteClick() },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    if (isFavorite) Icons.Default.Favorite else Icons.Outlined.Favorite,
                    contentDescription = stringResource(R.string.favorite_content_description),
                    tint = if (isFavorite) AccentGreen else SecondaryText,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = formatTime(song.duration),
            color = SecondaryText,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun RecentlyPlayedItem(
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
private fun TrendingPlaylistsColumn() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TrendingPlaylistItem(stringResource(R.string.music_mix), 56)
        TrendingPlaylistItem(stringResource(R.string.chill_beats), 36)
    }
}

@Composable
private fun TrendingPlaylistItem(title: String, songCount: Int) {
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
private fun NowPlayingMiniBar(
    song: Song,
    isPlaying: Boolean,
    progress: Float,
    onTap: () -> Unit,
    onPlayPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onTap)
            .shadow(8.dp, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
        color = CardGreenBg,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        tonalElevation = 4.dp
    ) {
        Column {
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = AccentGreen,
                trackColor = Color(0xFF2D3D32)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AlbumArtImage(
                    albumArtId = song.albumArt,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    Text(
                        text = song.artist,
                        color = SecondaryText,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                IconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) stringResource(R.string.pause) else stringResource(R.string.play),
                        tint = AccentGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MusicBottomNavigation(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DarkGreenBg,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = if (selectedTab == BottomTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                label = stringResource(R.string.home),
                isSelected = selectedTab == BottomTab.HOME
            ) { onTabSelected(BottomTab.HOME) }
            BottomNavItem(
                icon = if (selectedTab == BottomTab.EXPLORE) Icons.Filled.Explore else Icons.Outlined.Explore,
                label = stringResource(R.string.explore),
                isSelected = selectedTab == BottomTab.EXPLORE
            ) { onTabSelected(BottomTab.EXPLORE) }
            BottomNavItem(
                icon = if (selectedTab == BottomTab.LIBRARY) Icons.Filled.LibraryMusic else Icons.Outlined.LibraryMusic,
                label = stringResource(R.string.library),
                isSelected = selectedTab == BottomTab.LIBRARY
            ) { onTabSelected(BottomTab.LIBRARY) }
            BottomNavItem(
                icon = if (selectedTab == BottomTab.FAVORITES) Icons.Default.Favorite else Icons.Outlined.Favorite,
                label = stringResource(R.string.favorites),
                isSelected = selectedTab == BottomTab.FAVORITES
            ) { onTabSelected(BottomTab.FAVORITES) }
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) AccentGreen else SecondaryText,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isSelected) AccentGreen else SecondaryText,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun AlbumArtImage(
    albumArtId: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val uri = if (!albumArtId.isNullOrBlank() && albumArtId != "0") {
        ContentUris.withAppendedId(
            "content://media/external/audio/albumart".toUri(),
            albumArtId.toLongOrNull() ?: 0L
        )
    } else null

    if (uri != null) {
        AsyncImage(
            model = uri,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(IconPlaceholderColor),
            error = ColorPainter(IconPlaceholderColor)
        )
    } else {
        Image(
            painter = ColorPainter(IconPlaceholderColor),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}

private fun formatTime(milliseconds: Long): String {
    val seconds = (milliseconds / 1000) % 60
    val minutes = (milliseconds / (1000 * 60)) % 60
    return String.format(java.util.Locale.US, "%d:%02d", minutes, seconds)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NowPlayingFullScreen(
    viewModel: MusicPlayerViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()

    var sliderPosition by remember(currentSong?.id) { mutableFloatStateOf(0f) }
    var isUserSeeking by remember { mutableStateOf(false) }

    LaunchedEffect(currentPosition, duration, isUserSeeking) {
        if (!isUserSeeking && duration > 0) {
            sliderPosition = (currentPosition.toFloat() / duration).coerceIn(0f, 1f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkGreenBg, CardGreenBg.copy(alpha = 0.3f), DarkGreenBg)
                )
            )
    ) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { /* Empty title */ },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.close_player),
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: More options */ }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.more_options),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            // Album Art
            AlbumArtImage(
                albumArtId = currentSong?.albumArt,
                contentDescription = stringResource(R.string.album_art),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
            )

            // Song Info and Favorite button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = currentSong?.title ?: stringResource(R.string.select_song),
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentSong?.artist ?: stringResource(R.string.unknown_artist),
                        color = SecondaryText,
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                }
                val isFavorite = viewModel.isFavorite(currentSong?.id)
                IconButton(onClick = { viewModel.toggleFavorite(currentSong) }) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Outlined.Favorite,
                        contentDescription = stringResource(R.string.favorite_content_description),
                        tint = if (isFavorite) AccentGreen else SecondaryText,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Seek Bar and Time
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = sliderPosition.coerceIn(0f, 1f),
                    onValueChange = {
                        isUserSeeking = true
                        sliderPosition = it
                    },
                    onValueChangeFinished = {
                        viewModel.seekTo(sliderPosition.coerceIn(0f, 1f))
                        isUserSeeking = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = AccentGreen,
                        activeTrackColor = AccentGreen,
                        inactiveTrackColor = Color(0xFF2D3D32)
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp), // To align with slider thumbs
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatTime(currentPosition), color = SecondaryText, fontSize = 12.sp)
                    Text(formatTime(duration), color = SecondaryText, fontSize = 12.sp)
                }
            }

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.previousSong() }) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = stringResource(R.string.previous), tint = Color.White, modifier = Modifier.size(48.dp))
                }

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(AccentGreen)
                        .clickable { viewModel.togglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = stringResource(R.string.play_pause),
                        tint = DarkGreenBg,
                        modifier = Modifier.size(40.dp)
                    )
                }

                IconButton(onClick = { viewModel.nextSong() }) {
                    Icon(Icons.Default.SkipNext, contentDescription = stringResource(R.string.next), tint = Color.White, modifier = Modifier.size(48.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Spacer at the bottom
        }
    }
    }
}

@Preview(showBackground = true)
@Composable
private fun MusicPlayerScreenPreview() {
    ProyectoPruebaAppMusia1Theme {
        MusicPlayerScreen()
    }
}
