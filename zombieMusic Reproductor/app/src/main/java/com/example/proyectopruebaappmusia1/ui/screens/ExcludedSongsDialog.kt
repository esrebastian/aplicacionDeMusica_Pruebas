package com.example.proyectopruebaappmusia1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectopruebaappmusia1.domain.model.Song
import com.example.proyectopruebaappmusia1.ui.components.AlbumArtImage
import com.example.proyectopruebaappmusia1.ui.theme.AccentGreen
import com.example.proyectopruebaappmusia1.ui.theme.CardGreenBg
import com.example.proyectopruebaappmusia1.ui.theme.DarkGreenBg
import com.example.proyectopruebaappmusia1.ui.theme.PrimaryText
import com.example.proyectopruebaappmusia1.ui.theme.SecondaryText
import com.example.proyectopruebaappmusia1.util.TimeUtils

@Composable
fun ExcludedSongsDialog(
    excludedSongs: List<Song>,
    minDurationFilter: Int,
    useEnglish: Boolean,
    onDismiss: () -> Unit,
    onAllowSong: (Song) -> Unit
) {
    var selectedSong by remember { mutableStateOf<Song?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardGreenBg,
        title = {
            Text(
                if (useEnglish) "Excluded songs" else "Canciones excluidas",
                color = PrimaryText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (excludedSongs.isEmpty()) {
                Text(
                    if (useEnglish) {
                        "There are no songs outside the library with the current filter."
                    } else {
                        "No hay canciones fuera de la biblioteca con el filtro actual."
                    },
                    color = SecondaryText
                )
            } else {
                Column {
                    Text(
                        if (useEnglish) {
                            "These songs are $minDurationFilter seconds or shorter."
                        } else {
                            "Estas canciones duran $minDurationFilter segundos o menos."
                        },
                        color = SecondaryText,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(excludedSongs, key = { it.id }) { song ->
                            ExcludedSongRow(
                                song = song,
                                onClick = { selectedSong = song }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (useEnglish) "DONE" else "LISTO", color = AccentGreen, fontWeight = FontWeight.Bold)
            }
        }
    )

    selectedSong?.let { song ->
        ExcludedSongDetailsDialog(
            song = song,
            useEnglish = useEnglish,
            onDismiss = { selectedSong = null },
            onAllowSong = {
                onAllowSong(song)
                selectedSong = null
            }
        )
    }
}

@Composable
private fun ExcludedSongRow(
    song: Song,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = DarkGreenBg,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlbumArtImage(
                albumArtId = song.albumArt,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(song.title, color = PrimaryText, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(song.artist, color = SecondaryText, fontSize = 12.sp, maxLines = 1)
            }
            Text(TimeUtils.formatTime(song.duration), color = AccentGreen, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ExcludedSongDetailsDialog(
    song: Song,
    useEnglish: Boolean,
    onDismiss: () -> Unit,
    onAllowSong: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardGreenBg,
        title = { Text(if (useEnglish) "Song details" else "Detalles de la cancion", color = PrimaryText) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AlbumArtImage(
                    albumArtId = song.albumArt,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(DarkGreenBg, RoundedCornerShape(8.dp))
                )
                DetailLine(if (useEnglish) "Title" else "Titulo", song.title)
                DetailLine(if (useEnglish) "Artist" else "Artista", song.artist)
                DetailLine(if (useEnglish) "Duration" else "Duracion", TimeUtils.formatTime(song.duration))
                DetailLine(if (useEnglish) "File" else "Archivo", song.filePath.ifBlank { "-" })
            }
        },
        confirmButton = {
            Button(
                onClick = onAllowSong,
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = DarkGreenBg)
                Text(
                    if (useEnglish) "REMOVE FROM FILTER" else "SACAR DEL FILTRO",
                    color = DarkGreenBg,
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (useEnglish) "CANCEL" else "CANCELAR", color = SecondaryText)
            }
        }
    )
}

@Composable
private fun DetailLine(label: String, value: String) {
    Column {
        Text(label, color = AccentGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(value, color = PrimaryText, fontSize = 14.sp, maxLines = 2)
    }
}
