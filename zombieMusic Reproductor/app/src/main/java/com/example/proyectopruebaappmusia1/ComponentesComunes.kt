package com.example.proyectopruebaappmusia1

import android.content.ContentUris
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.proyectopruebaappmusia1.model.Song

// Colores globales de ZombieMusic
val DarkGreenBg = Color(0xFF0D1410)
val CardGreenBg = Color(0xFF1B261F)
val AccentGreen = Color(0xFFC1F153)
val SecondaryText = Color(0xFF8BA08E)
val IconPlaceholderColor = Color(0xFF6B7E6F)

// Enums compartidos para navegación y filtros
enum class BottomTab {
    HOME, EXPLORE, LIBRARY, FAVORITES, RECENTLY_PLAYED_FULL
}

enum class FilterOption(val displayName: String) {
    TITLE("Título"),
    ARTIST("Artista"),
    DURATION("Duración")
}

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
            text = formatTime(song.duration),
            color = SecondaryText,
            fontSize = 12.sp
        )
    }
}

@Composable
fun NowPlayingMiniBar(
    song: Song,
    isPlaying: Boolean,
    progress: Float,
    onTap: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(CardGreenBg)
            .clickable { onTap() }
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .align(Alignment.TopCenter),
            color = AccentGreen,
            trackColor = Color.Transparent
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlbumArtImage(
                albumArtId = song.albumArt,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = song.artist,
                    color = SecondaryText,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
            IconButton(onClick = onPlayPauseClick) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            IconButton(onClick = onNextClick) {
                Icon(
                    Icons.Default.SkipNext,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun MusicBottomNavigation(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit
) {
    NavigationBar(
        containerColor = DarkGreenBg,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == BottomTab.HOME,
            onClick = { onTabSelected(BottomTab.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.home)) },
            label = { Text(stringResource(R.string.home)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentGreen,
                selectedTextColor = AccentGreen,
                unselectedIconColor = SecondaryText,
                unselectedTextColor = SecondaryText,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selectedTab == BottomTab.EXPLORE,
            onClick = { onTabSelected(BottomTab.EXPLORE) },
            icon = { Icon(Icons.Default.Explore, contentDescription = stringResource(R.string.explore)) },
            label = { Text(stringResource(R.string.explore)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentGreen,
                selectedTextColor = AccentGreen,
                unselectedIconColor = SecondaryText,
                unselectedTextColor = SecondaryText,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selectedTab == BottomTab.LIBRARY,
            onClick = { onTabSelected(BottomTab.LIBRARY) },
            icon = { Icon(Icons.Default.LibraryMusic, contentDescription = stringResource(R.string.library)) },
            label = { Text(stringResource(R.string.library)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentGreen,
                selectedTextColor = AccentGreen,
                unselectedIconColor = SecondaryText,
                unselectedTextColor = SecondaryText,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selectedTab == BottomTab.FAVORITES,
            onClick = { onTabSelected(BottomTab.FAVORITES) },
            icon = { Icon(Icons.Default.Favorite, contentDescription = stringResource(R.string.favorites)) },
            label = { Text(stringResource(R.string.favorites)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentGreen,
                selectedTextColor = AccentGreen,
                unselectedIconColor = SecondaryText,
                unselectedTextColor = SecondaryText,
                indicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
fun AlbumArtImage(
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

fun formatTime(milliseconds: Long): String {
    val seconds = (milliseconds / 1000) % 60
    val minutes = (milliseconds / (1000 * 60)) % 60
    return String.format(java.util.Locale.US, "%d:%02d", minutes, seconds)
}
