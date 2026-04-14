package com.example.proyectopruebaappmusia1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectopruebaappmusia1.ui.BottomTab
import com.example.proyectopruebaappmusia1.ui.components.MusicBottomNavigation
import com.example.proyectopruebaappmusia1.ui.components.NowPlayingMiniBar
import com.example.proyectopruebaappmusia1.ui.screens.*
import com.example.proyectopruebaappmusia1.ui.theme.ProyectoPruebaAppMusia1Theme
import com.example.proyectopruebaappmusia1.viewmodel.DownloadViewModel
import com.example.proyectopruebaappmusia1.viewmodel.ExploreViewModel
import com.example.proyectopruebaappmusia1.viewmodel.MusicPlayerViewModel
import com.example.proyectopruebaappmusia1.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProyectoPruebaAppMusia1Theme {
                val context = LocalContext.current
                val factory = remember { ViewModelFactory(context) }
                
                val musicViewModel: MusicPlayerViewModel = viewModel(factory = factory)
                val exploreViewModel: ExploreViewModel = viewModel(factory = factory)
                val downloadViewModel: DownloadViewModel = viewModel(factory = factory)
                
                var selectedTab by remember { mutableStateOf(BottomTab.HOME) }
                var isFullScreenPlayerVisible by remember { mutableStateOf(false) }
                
                val selectedPlaylist by musicViewModel.selectedPlaylist.collectAsStateWithLifecycle()
                val currentSong by musicViewModel.currentSong.collectAsStateWithLifecycle()
                val isPlaying by musicViewModel.isPlaying.collectAsStateWithLifecycle()
                val progress by musicViewModel.progress.collectAsStateWithLifecycle()

                BackHandler(enabled = isFullScreenPlayerVisible || selectedPlaylist != null) {
                    when {
                        isFullScreenPlayerVisible -> isFullScreenPlayerVisible = false
                        selectedPlaylist != null -> musicViewModel.selectPlaylistForDetail(null)
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            Column {
                                AnimatedVisibility(
                                    visible = currentSong != null && !isFullScreenPlayerVisible,
                                    enter = slideInVertically { it },
                                    exit = slideOutVertically { it }
                                ) {
                                    currentSong?.let { song ->
                                        NowPlayingMiniBar(
                                            song = song,
                                            isPlaying = isPlaying,
                                            progress = progress,
                                            onTap = { isFullScreenPlayerVisible = true },
                                            onPlayPauseClick = { musicViewModel.togglePlayPause() },
                                            onNextClick = { musicViewModel.nextSong() }
                                        )
                                    }
                                }

                                MusicBottomNavigation(
                                    selectedTab = selectedTab,
                                    onTabSelected = { selectedTab = it }
                                )
                            }
                        }
                    ) { innerPadding ->
                        // Aplicamos innerPadding aquí para que las pantallas respeten la barra de estado y la de navegación
                        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                            when (selectedTab) {
                                BottomTab.HOME -> HomeScreen(
                                    viewModel = musicViewModel,
                                    onHeroClick = { isFullScreenPlayerVisible = true },
                                    onPlaylistClick = { musicViewModel.selectPlaylistForDetail(it) }
                                )
                                BottomTab.EXPLORE -> ExploreScreen(
                                    exploreViewModel = exploreViewModel,
                                    downloadViewModel = downloadViewModel
                                )
                                BottomTab.LIBRARY -> LibraryScreen(
                                    viewModel = musicViewModel,
                                    onSettingsClick = { /* Abrir Ajustes */ }
                                )
                                BottomTab.FAVORITES -> FavoritesScreen(
                                    viewModel = musicViewModel,
                                    onSettingsClick = { /* Abrir Ajustes */ }
                                )
                            }

                            AnimatedVisibility(
                                visible = selectedPlaylist != null,
                                enter = fadeIn() + slideInHorizontally { it },
                                exit = fadeOut() + slideOutHorizontally { it }
                            ) {
                                selectedPlaylist?.let { playlist ->
                                    PlaylistDetailScreen(
                                        viewModel = musicViewModel,
                                        playlist = playlist,
                                        onBack = { musicViewModel.selectPlaylistForDetail(null) }
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = isFullScreenPlayerVisible,
                        enter = slideInVertically { it },
                        exit = slideOutVertically { it }
                    ) {
                        NowPlayingFullScreen(
                            viewModel = musicViewModel,
                            onClose = { isFullScreenPlayerVisible = false }
                        )
                    }
                }
            }
        }
    }
}
