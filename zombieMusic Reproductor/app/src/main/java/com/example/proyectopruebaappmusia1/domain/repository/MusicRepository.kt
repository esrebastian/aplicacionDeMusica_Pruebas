package com.example.proyectopruebaappmusia1.domain.repository

import com.example.proyectopruebaappmusia1.domain.model.Song
import kotlinx.coroutines.flow.StateFlow

interface MusicRepository {
    suspend fun getAllSongs(): List<Song>
}

interface FavoritesRepository {
    val favoriteIds: StateFlow<Set<String>>
    fun toggleFavorite(songId: String)
    fun isFavorite(songId: String): Boolean
}

interface RecentlyPlayedRepository {
    val recentlyPlayedIds: StateFlow<List<String>>
    fun addRecentlyPlayed(songId: String)
    fun removeRecentlyPlayed(songId: String)
}
