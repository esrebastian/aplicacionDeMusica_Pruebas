package com.example.proyectopruebaappmusia1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectopruebaappmusia1.R
import com.example.proyectopruebaappmusia1.domain.model.Song
import com.example.proyectopruebaappmusia1.ui.theme.*
import com.example.proyectopruebaappmusia1.ui.components.ScreenHeader
import com.example.proyectopruebaappmusia1.ui.components.SongListItem
import com.example.proyectopruebaappmusia1.viewmodel.MusicPlayerViewModel

@Composable
fun FavoritesScreen(
    viewModel: MusicPlayerViewModel,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val favoriteSongs by viewModel.favoriteSongs.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()

    FavoritesContent(
        favoriteSongs = favoriteSongs,
        currentSong = currentSong,
        onSettingsClick = onSettingsClick,
        onFavoriteClick = { viewModel.toggleFavorite(it) },
        onSongClick = { viewModel.selectSong(it) },
        modifier = modifier
    )
}

@Composable
fun FavoritesContent(
    favoriteSongs: List<Song>,
    currentSong: Song?,
    onSettingsClick: () -> Unit,
    onFavoriteClick: (Song) -> Unit,
    onSongClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkGreenBg)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader(title = stringResource(R.string.favorites), onSettingsClick = onSettingsClick)

        if (favoriteSongs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.favorites_empty),
                    color = SecondaryText,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(favoriteSongs, key = { it.id }) { song ->
                    SongListItem(
                        song = song,
                        isCurrent = currentSong?.id == song.id,
                        isFavorite = true,
                        onFavoriteClick = { onFavoriteClick(song) },
                        onClick = { onSongClick(song) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FavoritesScreenPreview() {
    ProyectoPruebaAppMusia1Theme {
        FavoritesContent(
            favoriteSongs = listOf(
                Song("1", "Favorite Song", "Artist X", 200000, "", null),
                Song("2", "Another Favorite", "Artist Y", 150000, "", null)
            ),
            currentSong = null,
            onSettingsClick = {},
            onFavoriteClick = {},
            onSongClick = {}
        )
    }
}
