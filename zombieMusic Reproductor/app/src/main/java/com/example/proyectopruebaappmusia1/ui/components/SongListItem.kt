package com.example.proyectopruebaappmusia1.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.proyectopruebaappmusia1.ui.theme.SecondaryText
import com.example.proyectopruebaappmusia1.util.TimeUtils

@Composable
fun SongListItem(
    song: Song,
    isCurrent: Boolean,
    onClick: () -> Unit,
    isFavorite: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null
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
        if (onDeleteClick != null) {
            IconButton(
                onClick = { onDeleteClick() },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color.Red.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = TimeUtils.formatTime(song.duration),
            color = SecondaryText,
            fontSize = 12.sp
        )
    }
}
