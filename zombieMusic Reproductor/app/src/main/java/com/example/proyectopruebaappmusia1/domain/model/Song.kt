package com.example.proyectopruebaappmusia1.domain.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val duration: Long,
    val filePath: String,
    val albumArt: String? = null,
    val playCount: Int = 0,
    val lastPlayed: Long = 0,
    val dateAdded: Long = 0
)

data class Playlist(
    val id: String,
    val name: String,
    val songCount: Int,
    val coverImage: String? = null,
    val songs: List<Song> = emptyList()
)
