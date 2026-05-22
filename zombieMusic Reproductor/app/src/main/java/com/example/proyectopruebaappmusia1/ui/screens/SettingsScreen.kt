package com.example.proyectopruebaappmusia1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.proyectopruebaappmusia1.ui.theme.AccentGreen
import com.example.proyectopruebaappmusia1.ui.theme.CardGreenBg
import com.example.proyectopruebaappmusia1.ui.theme.DarkGreenBg
import com.example.proyectopruebaappmusia1.ui.theme.SecondaryText
import com.example.proyectopruebaappmusia1.viewmodel.MusicPlayerViewModel

@Composable
fun SettingsScreen(
    viewModel: MusicPlayerViewModel,
    onBack: () -> Unit
) {
    val minDurationFilter by viewModel.minDurationFilter.collectAsStateWithLifecycle()
    val allSongs by viewModel.playlist.collectAsStateWithLifecycle()
    val visibleSongs by viewModel.filteredLibrarySongs.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()

    SettingsContent(
        minDurationFilter = minDurationFilter,
        totalSongs = allSongs.size,
        visibleSongs = visibleSongs.size,
        isPlaying = isPlaying,
        onBack = onBack,
        onMinDurationChange = { viewModel.setMinDurationFilter(it) },
        onReloadSongs = { viewModel.loadRealSongs() },
        onPlayPause = { viewModel.togglePlayPause() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    minDurationFilter: Int,
    totalSongs: Int,
    visibleSongs: Int,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onMinDurationChange: (Int) -> Unit,
    onReloadSongs: () -> Unit,
    onPlayPause: () -> Unit
) {
    val removedSongs = (totalSongs - visibleSongs).coerceAtLeast(0)
    val durationLabel = if (minDurationFilter == 0) {
        "Sin filtro"
    } else {
        "$minDurationFilter s o menos"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGreenBg)
    ) {
        TopAppBar(
            title = { Text("Ajustes", color = Color.White, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkGreenBg)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            SettingsSectionTitle("Biblioteca")

            SettingsActionRow(
                icon = { Icon(Icons.Default.LibraryMusic, null, tint = AccentGreen) },
                title = "Canciones visibles",
                subtitle = "$visibleSongs de $totalSongs disponibles"
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CardGreenBg,
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = AccentGreen.copy(alpha = 0.14f)
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = AccentGreen,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text("Sacar canciones cortas", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(durationLabel, color = SecondaryText, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Slider(
                        value = minDurationFilter.toFloat(),
                        onValueChange = { onMinDurationChange(it.toInt()) },
                        valueRange = 0f..180f,
                        steps = 17,
                        colors = SliderDefaults.colors(
                            thumbColor = AccentGreen,
                            activeTrackColor = AccentGreen,
                            inactiveTrackColor = Color.White.copy(alpha = 0.18f)
                        )
                    )

                    Text(
                        text = if (minDurationFilter == 0) {
                            "No se excluye ninguna cancion por duracion."
                        } else {
                            "Se excluyen $removedSongs canciones de $minDurationFilter segundos o menos."
                        },
                        color = SecondaryText,
                        fontSize = 13.sp
                    )
                }
            }

            SettingsSectionTitle("Acciones rapidas")

            Button(
                onClick = onReloadSongs,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CardGreenBg),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Refresh, null, tint = AccentGreen)
                Text("Recargar canciones", color = Color.White, modifier = Modifier.padding(start = 8.dp))
            }

            Button(
                onClick = onPlayPause,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    null,
                    tint = DarkGreenBg
                )
                Text(
                    if (isPlaying) "Pausar reproduccion" else "Reproducir",
                    color = DarkGreenBg,
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(text: String) {
    Text(text, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun SettingsActionRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardGreenBg,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = AccentGreen.copy(alpha = 0.14f)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    icon()
                }
            }

            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = SecondaryText, fontSize = 13.sp)
            }
        }
    }
}
