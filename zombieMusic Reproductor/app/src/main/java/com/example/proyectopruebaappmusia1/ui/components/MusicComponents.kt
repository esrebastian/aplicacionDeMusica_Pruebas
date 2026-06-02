package com.example.proyectopruebaappmusia1.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectopruebaappmusia1.domain.model.Song
import com.example.proyectopruebaappmusia1.ui.theme.*

@Composable
fun SettingsButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    MusicIconButton(
        imageVector = Icons.Default.Settings,
        contentDescription = "Ajustes",
        onClick = onClick,
        tint = SecondaryText,
        containerColor = CardGreenBg,
        buttonSize = 48.dp,
        iconSize = 24.dp,
        modifier = modifier
    )
}

@Composable
fun MusicIconButton(
    imageVector: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    buttonSize: Dp = 40.dp,
    iconSize: Dp = 22.dp,
    containerColor: Color? = null,
    shape: Shape = RoundedCornerShape(12.dp)
) {
    val sizedModifier = modifier.size(buttonSize)
    val buttonModifier = if (containerColor != null) {
        sizedModifier.clip(shape).background(containerColor)
    } else {
        sizedModifier
    }

    val iconTint = tint ?: PrimaryText

    IconButton(
        onClick = onClick,
        modifier = buttonModifier
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun ScreenHeader(
    title: String,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = PrimaryText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        SettingsButton(onClick = onSettingsClick)
    }
}

@Composable
fun MiniPlayer(
    song: Song,
    isPlaying: Boolean,
    progress: Float,
    onTap: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(64.dp).clickable { onTap() },
        color = CardGreenBg
    ) {
        Column {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = AccentGreen,
                trackColor = Color.Transparent
            )
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AlbumArtImage(
                    albumArtId = song.albumArt,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(song.title, color = PrimaryText, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(song.artist, color = SecondaryText, fontSize = 12.sp, maxLines = 1)
                }
                MusicIconButton(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    onClick = onPlayPauseClick
                )
                MusicIconButton(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = null,
                    onClick = onNextClick
                )
            }
        }
    }
}
