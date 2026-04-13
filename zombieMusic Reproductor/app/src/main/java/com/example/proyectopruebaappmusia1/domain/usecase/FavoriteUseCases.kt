package com.example.proyectopruebaappmusia1.domain.usecase

import com.example.proyectopruebaappmusia1.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.StateFlow

class GetFavoriteIdsUseCase(private val repository: FavoritesRepository) {
    operator fun invoke(): StateFlow<Set<String>> = repository.favoriteIds
}

class ToggleFavoriteUseCase(private val repository: FavoritesRepository) {
    operator fun invoke(songId: String) = repository.toggleFavorite(songId)
}
