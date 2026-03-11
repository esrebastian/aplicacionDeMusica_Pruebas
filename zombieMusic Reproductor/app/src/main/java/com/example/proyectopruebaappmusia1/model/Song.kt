package com.example.proyectopruebaappmusia1.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val duration: Long, // en milisegundos
    val filePath: String,
    val albumArt: String? = null,
    val playCount: Int = 0, // Contador para el filtro de "Más reproducido"
    val lastPlayed: Long = 0 // Timestamp para el filtro de "Más recientes"
)

data class Playlist(
    val id: String,
    val name: String,
    val songCount: Int,
    val coverImage: String? = null,
    val songs: List<Song> = emptyList()
)
