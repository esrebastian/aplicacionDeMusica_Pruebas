package com.example.proyectopruebaappmusia1

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectopruebaappmusia1.ui.BottomTab
import com.example.proyectopruebaappmusia1.ui.components.MusicBottomNavigation
import com.example.proyectopruebaappmusia1.ui.components.NowPlayingMiniBar
import com.example.proyectopruebaappmusia1.ui.screens.*
import com.example.proyectopruebaappmusia1.ui.theme.*
import com.example.proyectopruebaappmusia1.viewmodel.DownloadViewModel
import com.example.proyectopruebaappmusia1.viewmodel.ExploreViewModel
import com.example.proyectopruebaappmusia1.viewmodel.MusicPlayerViewModel
import com.example.proyectopruebaappmusia1.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val systemInDarkTheme = isSystemInDarkTheme()
            val settingsPrefs = remember(context) {
                context.getSharedPreferences("zombie_music_settings", MODE_PRIVATE)
            }
            var selectedTheme by remember {
                mutableStateOf(settingsPrefs.getString("theme", "Automatico") ?: "Automatico")
            }

            DisposableEffect(settingsPrefs) {
                val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
                    if (key == "theme") {
                        selectedTheme = prefs.getString("theme", "Automatico") ?: "Automatico"
                    }
                }
                settingsPrefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose { settingsPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
            }

            val useDarkTheme = when (selectedTheme) {
                "Claro", "Light" -> false
                "Oscuro", "Dark" -> true
                else -> systemInDarkTheme
            }

            ProyectoPruebaAppMusia1Theme(
                darkTheme = useDarkTheme,
                dynamicColor = false
            ) {
                val factory = remember { ViewModelFactory(context) }
                
                val musicViewModel: MusicPlayerViewModel = viewModel(factory = factory)
                val exploreViewModel: ExploreViewModel = viewModel(factory = factory)
                val downloadViewModel: DownloadViewModel = viewModel(factory = factory)
                
                var selectedTab by remember { mutableStateOf(BottomTab.HOME) }
                var isFullScreenPlayerVisible by remember { mutableStateOf(false) }
                var isSettingsVisible by remember { mutableStateOf(false) }
                
                val selectedPlaylist by musicViewModel.selectedPlaylist.collectAsStateWithLifecycle()
                val currentSong by musicViewModel.currentSong.collectAsStateWithLifecycle()
                val isPlaying by musicViewModel.isPlaying.collectAsStateWithLifecycle()
                val progress by musicViewModel.progress.collectAsStateWithLifecycle()

                // --- GESTIÓN DE PERMISOS ---
                val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    arrayOf(Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                }

                var showPermissionDialog by remember { 
                    mutableStateOf(!permissionsToRequest.all { 
                        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED 
                    }) 
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val allGranted = permissions.values.all { it }
                    if (allGranted) {
                        // CORRECCIÓN: Ahora llamamos a cargar las canciones inmediatamente al dar permisos
                        musicViewModel.loadRealSongs()
                    }
                }

                if (showPermissionDialog) {
                    PermissionExplanationDialog(
                        onConfirm = {
                            showPermissionDialog = false
                            permissionLauncher.launch(permissionsToRequest)
                        }
                    )
                }

                BackHandler(enabled = isFullScreenPlayerVisible || selectedPlaylist != null || isSettingsVisible) {
                    when {
                        isSettingsVisible -> isSettingsVisible = false
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
                                    onTabSelected = {
                                        selectedTab = it
                                        musicViewModel.selectPlaylistForDetail(null)
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                            when (selectedTab) {
                                BottomTab.HOME -> HomeScreen(
                                    viewModel = musicViewModel,
                                    onHeroClick = { isFullScreenPlayerVisible = true },
                                    onPlaylistClick = { musicViewModel.selectPlaylistForDetail(it) },
                                    onSettingsClick = { isSettingsVisible = true }
                                )
                                BottomTab.EXPLORE -> ExploreScreen(
                                    exploreViewModel = exploreViewModel,
                                    downloadViewModel = downloadViewModel
                                )
                                BottomTab.LIBRARY -> LibraryScreen(
                                    viewModel = musicViewModel,
                                    onSettingsClick = { isSettingsVisible = true }
                                )
                                BottomTab.FAVORITES -> FavoritesScreen(
                                    viewModel = musicViewModel,
                                    onSettingsClick = { isSettingsVisible = true }
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
                        visible = isSettingsVisible,
                        enter = fadeIn() + slideInHorizontally { it },
                        exit = fadeOut() + slideOutHorizontally { it }
                    ) {
                        SettingsScreen(
                            viewModel = musicViewModel,
                            onBack = { isSettingsVisible = false }
                        )
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

@Composable
fun PermissionExplanationDialog(onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = { /* No permitir cerrar sin decidir */ },
        containerColor = CardGreenBg,
        title = { 
            Text("¡Bienvenido a ZombieMusic!", color = PrimaryText, fontWeight = FontWeight.Bold) 
        },
        text = {
            Column {
                Text(
                    "Para que tus reglas dominen la música, necesitamos permiso para:",
                    color = PrimaryText,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("• Leer tus archivos de audio.", color = AccentGreen, fontSize = 13.sp)
                Text("• Mostrar el reproductor en notificaciones.", color = AccentGreen, fontSize = 13.sp)
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("DAR PERMISOS", color = DarkGreenBg, fontWeight = FontWeight.Bold)
            }
        }
    )
}
