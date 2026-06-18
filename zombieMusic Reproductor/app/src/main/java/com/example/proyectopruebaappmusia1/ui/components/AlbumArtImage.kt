package com.example.proyectopruebaappmusia1.ui.components

import android.content.ContentUris
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import coil.request.ImageRequest
import coil.size.Precision
import coil.compose.AsyncImage
import com.example.proyectopruebaappmusia1.ui.theme.IconPlaceholderColor

@Composable
fun AlbumArtImage(
    albumArtId: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    highQuality: Boolean = false
) {
    val context = LocalContext.current
    val uri = remember(albumArtId) {
        if (!albumArtId.isNullOrBlank() && albumArtId != "0") {
            ContentUris.withAppendedId(
                "content://media/external/audio/albumart".toUri(),
                albumArtId.toLongOrNull() ?: 0L
            )
        } else {
            null
        }
    }
    val imageModel = remember(context, uri, highQuality) {
        uri?.let {
            ImageRequest.Builder(context)
                .data(it)
                .memoryCacheKey("album_art_${albumArtId}_${if (highQuality) "large" else "thumb"}")
                .diskCacheKey("album_art_${albumArtId}_${if (highQuality) "large" else "thumb"}")
                .size(if (highQuality) 1024 else 256)
                .precision(Precision.INEXACT)
                .crossfade(false)
                .build()
        }
    }

    if (imageModel != null) {
        AsyncImage(
            model = imageModel,
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
