package com.example.proyectopruebaappmusia1.util

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.example.proyectopruebaappmusia1.model.Song

object MusicProvider {
    fun getSongsFromDevice(context: Context): List<Song> {
        val songs = mutableListOf<Song>()
        
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID
        )
        
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        
        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )
        
        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            
            while (it.moveToNext()) {
                val mediaId = it.getLong(idColumn)
                val id = mediaId.toString()
                val title = it.getString(titleColumn) ?: "Desconocida"
                val artist = it.getString(artistColumn) ?: "Artista Desconocido"
                val duration = it.getLong(durationColumn)
                val albumId = it.getLong(albumIdColumn)
                // Usar content URI (compatible Android 10+ scoped storage)
                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    mediaId
                )
                val filePath = contentUri.toString()
                
                songs.add(
                    Song(
                        id = id,
                        title = title,
                        artist = artist,
                        duration = duration,
                        filePath = filePath,
                        albumArt = albumId.toString()
                    )
                )
            }
        }
        
        return songs
    }
    
    fun getSampleSongs(): List<Song> {
        return listOf(
            Song(
                id = "sample_1",
                title = "Mi Canción",
                artist = "Artista Principal",
                duration = 174000,
                filePath = "",
                albumArt = null
            ),
            Song(
                id = "sample_2",
                title = "Vibes Nostálgicas",
                artist = "Green Beats",
                duration = 210000,
                filePath = "",
                albumArt = null
            ),
            Song(
                id = "sample_3",
                title = "Mood Oscuro",
                artist = "Emerald Skull",
                duration = 180000,
                filePath = "",
                albumArt = null
            )
        )
    }
}
