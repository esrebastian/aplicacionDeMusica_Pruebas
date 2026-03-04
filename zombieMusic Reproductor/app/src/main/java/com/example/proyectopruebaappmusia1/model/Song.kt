package com.example.proyectopruebaappmusia1.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val duration: Long, // en milisegundos
    val filePath: String,
    val albumArt: String? = null
)

data class Playlist(
    val id: String,
    val name: String,
    val songCount: Int,
    val coverImage: String? = null,
    val songs: List<Song> = emptyList()
)
