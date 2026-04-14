package com.example.proyectopruebaappmusia1.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface FavoritesRepository {
    val favoriteIds: StateFlow<Set<String>>
    fun toggleFavorite(songId: String)
    fun isFavorite(songId: String): Boolean
}
