package com.example.proyectopruebaappmusia1.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectopruebaappmusia1.R
import com.example.proyectopruebaappmusia1.domain.model.Song
import com.example.proyectopruebaappmusia1.ui.theme.AccentGreen
import com.example.proyectopruebaappmusia1.ui.theme.PrimaryText
import com.example.proyectopruebaappmusia1.ui.theme.SecondaryText
import com.example.proyectopruebaappmusia1.util.TimeUtils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongListItem(
    song: Song,
    isCurrent: Boolean,
    onClick: (Song) -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    isSelected: Boolean = false,
    loadAlbumArt: Boolean = true,
    onFavoriteClick: ((Song) -> Unit)? = null,
    onAddToPlaylistClick: ((Song) -> Unit)? = null,
    onDeleteClick: ((Song) -> Unit)? = null,
    onLongClick: ((Song) -> Unit)? = null
) {
    // Memorizamos el formato del tiempo para evitar cálculos innecesarios en cada recomposición
    val durationText = remember(song.duration) { TimeUtils.formatTime(song.duration) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) AccentGreen.copy(alpha = 0.16f) else Color.Transparent)
            .combinedClickable(
                onClick = { onClick(song) },
                onLongClick = onLongClick?.let { { it(song) } }
            )
            .padding(horizontal = if (isSelected) 8.dp else 0.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlbumArtImage(
            albumArtId = if (loadAlbumArt) song.albumArt else null,
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
                color = PrimaryText,
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
            MusicIconButton(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.Favorite,
                contentDescription = stringResource(R.string.favorite_content_description),
                onClick = { onFavoriteClick(song) },
                tint = if (isFavorite) AccentGreen else SecondaryText
            )
        }
        
        if (onAddToPlaylistClick != null) {
            MusicIconButton(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar a playlist",
                onClick = { onAddToPlaylistClick(song) },
                tint = SecondaryText
            )
        }
        
        if (onDeleteClick != null) {
            MusicIconButton(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar",
                onClick = { onDeleteClick(song) },
                tint = Color.Red.copy(alpha = 0.7f),
                iconSize = 20.dp
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = durationText,
            color = SecondaryText,
            fontSize = 12.sp
        )
    }
}
