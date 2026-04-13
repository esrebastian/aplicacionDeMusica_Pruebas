package com.example.proyectopruebaappmusia1.ui.components

import android.content.ContentUris
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.proyectopruebaappmusia1.ui.theme.IconPlaceholderColor

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
