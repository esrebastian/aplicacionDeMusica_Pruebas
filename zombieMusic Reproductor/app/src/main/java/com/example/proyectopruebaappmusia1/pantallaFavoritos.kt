package com.example.proyectopruebaappmusia1

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectopruebaappmusia1.model.Song
import com.example.proyectopruebaappmusia1.viewmodel.MusicPlayerViewModel

@Composable
fun pantallaFavoritos(
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
