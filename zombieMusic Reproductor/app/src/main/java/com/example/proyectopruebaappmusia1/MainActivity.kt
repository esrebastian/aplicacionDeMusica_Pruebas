package com.example.proyectopruebaappmusia1

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectopruebaappmusia1.model.Song
import com.example.proyectopruebaappmusia1.ui.theme.ProyectoPruebaAppMusia1Theme
import com.example.proyectopruebaappmusia1.viewmodel.MusicPlayerViewModel

class MainActivity : ComponentActivity() {

    private lateinit var musicViewModel: MusicPlayerViewModel

    // Lanzador para permisos de archivos/audio
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            musicViewModel.loadRealSongs(this)
        }
    }

    // Lanzador para permisos de notificación (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            ProyectoPruebaAppMusia1Theme {
                val viewModel: MusicPlayerViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return MusicPlayerViewModel(application) as T
                        }
                    }
                )
                musicViewModel = viewModel

                // Verificar y pedir permisos al iniciar
                LaunchedEffect(Unit) {
                    checkAndRequestPermissions()
                }

                val isPlaying by viewModel.isPlaying.collectAsState()
                val currentSong by viewModel.currentSong.collectAsState()
                val progress by viewModel.progress.collectAsState()
                val playlist by viewModel.playlist.collectAsState()
                val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
                val favoriteIds by viewModel.favoriteIds.collectAsState()
                
                var selectedTab by rememberSaveable { mutableStateOf(BottomTab.HOME) }
                var showFullScreenPlayer by rememberSaveable { mutableStateOf(false) }

                BackHandler(enabled = selectedTab != BottomTab.HOME) {
                    selectedTab = BottomTab.HOME
                }

                // Estados de búsqueda y filtro para el Home
                var homeSearchQuery by rememberSaveable { mutableStateOf("") }
                var selectedFilter by rememberSaveable { mutableStateOf(FilterOption.TITLE) }

                val filteredPlaylist = remember(playlist, homeSearchQuery, selectedFilter) {
                    playlist.filter { song ->
                        song.title.contains(homeSearchQuery, ignoreCase = true) ||
                        song.artist.contains(homeSearchQuery, ignoreCase = true)
                    }.let { list ->
                        when (selectedFilter) {
                            FilterOption.TITLE -> list.sortedBy { it.title }
                            FilterOption.ARTIST -> list.sortedBy { it.artist }
                            FilterOption.DURATION -> list.sortedByDescending { it.duration }
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = DarkGreenBg,
                        bottomBar = {
                            Column(modifier = Modifier.background(DarkGreenBg).navigationBarsPadding()) {
                                currentSong?.let { song: Song ->
                                    NowPlayingMiniBar(
                                        song = song,
                                        isPlaying = isPlaying,
                                        progress = progress,
                                        onTap = { showFullScreenPlayer = true },
                                        onPlayPauseClick = { viewModel.togglePlayPause() },
                                        onNextClick = { viewModel.nextSong() }
                                    )
                                }
                                MusicBottomNavigation(
                                    selectedTab = if (selectedTab == BottomTab.RECENTLY_PLAYED_FULL) BottomTab.HOME else selectedTab,
                                    onTabSelected = { selectedTab = it }
                                )
                            }
                        }
                    ) { paddingValues ->
                        when (selectedTab) {
                            BottomTab.EXPLORE -> pantallaDeExplorar(viewModel = viewModel, modifier = Modifier.fillMaxSize().padding(paddingValues))
                            BottomTab.LIBRARY -> pantallaLibreria(playlist = playlist, currentSong = currentSong, viewModel = viewModel, modifier = Modifier.fillMaxSize().padding(paddingValues))
                            BottomTab.FAVORITES -> {
                                val favoriteSongs by viewModel.favoriteSongs.collectAsState()
                                pantallaFavoritos(favoriteSongs = favoriteSongs, currentSong = currentSong, viewModel = viewModel, modifier = Modifier.fillMaxSize().padding(paddingValues))
                            }
                            BottomTab.RECENTLY_PLAYED_FULL -> RecentlyPlayedFullScreen(recentlyPlayed = recentlyPlayed, currentSong = currentSong, viewModel = viewModel, onBack = { selectedTab = BottomTab.HOME }, modifier = Modifier.fillMaxSize().padding(paddingValues))
                            else -> pantallaInicio(
                                viewModel = viewModel,
                                currentSong = currentSong,
                                isPlaying = isPlaying,
                                recentlyPlayed = recentlyPlayed,
                                favoriteIds = favoriteIds,
                                filteredPlaylist = filteredPlaylist,
                                homeSearchQuery = homeSearchQuery,
                                onSearchQueryChange = { homeSearchQuery = it },
                                selectedFilter = selectedFilter,
                                onFilterSelected = { selectedFilter = it },
                                onHeroClick = { showFullScreenPlayer = true },
                                onSeeAllRecentlyPlayed = { selectedTab = BottomTab.RECENTLY_PLAYED_FULL },
                                paddingValues = paddingValues
                            )
                        }
                    }

                    if (showFullScreenPlayer) {
                        Dialog(
                            onDismissRequest = { showFullScreenPlayer = false },
                            properties = DialogProperties(usePlatformDefaultWidth = false)
                        ) {
                            NowPlayingFullScreen(
                                viewModel = viewModel,
                                onClose = { showFullScreenPlayer = false }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        // Permiso de almacenamiento/audio
        val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, storagePermission) == PackageManager.PERMISSION_GRANTED) {
            musicViewModel.loadRealSongs(this)
        } else {
            requestPermissionLauncher.launch(storagePermission)
        }

        // Permiso de notificaciones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
private fun RecentlyPlayedFullScreen(
    recentlyPlayed: List<Song>,
    currentSong: Song?,
    viewModel: MusicPlayerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    Column(modifier = modifier.background(DarkGreenBg).padding(horizontal = 16.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { 
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White) 
            }
            Text(stringResource(R.string.recently_played), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(recentlyPlayed) { song ->
                SongListItem(
                    song = song, 
                    isCurrent = currentSong?.id == song.id, 
                    isFavorite = song.id in favoriteIds, 
                    onFavoriteClick = { viewModel.toggleFavorite(song) }, 
                    onClick = { viewModel.selectSong(song) }, 
                    onDeleteClick = { viewModel.deleteRecentlyPlayedSong(song.id) }
                )
            }
        }
    }
}
