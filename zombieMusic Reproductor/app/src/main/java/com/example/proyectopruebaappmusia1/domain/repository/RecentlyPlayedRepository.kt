package com.example.proyectopruebaappmusia1.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface RecentlyPlayedRepository {
    val recentlyPlayedIds: StateFlow<List<String>>
    fun addRecentlyPlayed(songId: String)
    fun removeRecentlyPlayed(songId: String)
}
