package com.example.proyectopruebaappmusia1.domain.repository

import com.example.proyectopruebaappmusia1.domain.model.Song

interface MusicRepository {
    suspend fun getAllSongs(): List<Song>
}
