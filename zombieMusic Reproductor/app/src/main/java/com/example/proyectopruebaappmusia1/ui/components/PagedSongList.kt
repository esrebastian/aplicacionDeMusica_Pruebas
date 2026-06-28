package com.example.proyectopruebaappmusia1.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyectopruebaappmusia1.domain.model.Song
import com.example.proyectopruebaappmusia1.ui.theme.AccentGreen
import com.example.proyectopruebaappmusia1.ui.theme.SecondaryText
import kotlinx.coroutines.delay

@Composable
fun PagedSongList(
    songs: List<Song>,
    currentSong: Song?,
    modifier: Modifier = Modifier,
    favoriteIds: Set<String> = emptySet(),
    selectedSongIds: Set<String> = emptySet(),
    listState: LazyListState = rememberLazyListState(),
    initialBatchSize: Int = 40,
    nextBatchSize: Int = 40,
    preloadThreshold: Int = 5,
    autoLoadMore: Boolean = true,
    loadAlbumArt: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(bottom = 80.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(4.dp),
    onSongClick: (Song) -> Unit,
    onSongLongClick: ((Song) -> Unit)? = null,
    onFavoriteClick: ((Song) -> Unit)? = null
) {
    var visibleCount by remember(songs) {
        mutableIntStateOf(initialBatchSize.coerceAtMost(songs.size))
    }
    
    val visibleSongs = remember(songs, visibleCount) {
        songs.take(visibleCount)
    }

    // Optimizamos la detección del final de la lista para que no use CPU innecesaria
    val shouldLoadMore by remember(visibleCount, songs.size) {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && visibleCount < songs.size && lastVisibleItem.index >= visibleCount - preloadThreshold
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (autoLoadMore && shouldLoadMore) {
            delay(50) // Pequeño respiro para el hilo de UI
            visibleCount = (visibleCount + nextBatchSize).coerceAtMost(songs.size)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement
    ) {
        items(
            items = visibleSongs,
            key = { it.id }, // Clave única vital para que Compose no se pierda
            contentType = { "song_item" }
        ) { song ->
            SongListItem(
                song = song,
                isCurrent = song.id == currentSong?.id,
                isFavorite = song.id in favoriteIds,
                isSelected = song.id in selectedSongIds,
                loadAlbumArt = loadAlbumArt,
                onClick = onSongClick, // Pasamos la función directamente
                onFavoriteClick = onFavoriteClick,
                onLongClick = onSongLongClick
            )
        }

        if (visibleCount < songs.size) {
            item(key = "loader_status", contentType = "status") {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (autoLoadMore) {
                        CircularProgressIndicator(color = AccentGreen, strokeWidth = 2.dp)
                    } else {
                        TextButton(onClick = { visibleCount = (visibleCount + nextBatchSize).coerceAtMost(songs.size) }) {
                            Text("Cargar más canciones", color = AccentGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
