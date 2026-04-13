package com.example.proyectopruebaappmusia1.domain.usecase

import com.example.proyectopruebaappmusia1.domain.repository.RecentlyPlayedRepository
import kotlinx.coroutines.flow.StateFlow

class GetRecentlyPlayedIdsUseCase(private val repository: RecentlyPlayedRepository) {
    operator fun invoke(): StateFlow<List<String>> = repository.recentlyPlayedIds
}

class AddRecentlyPlayedUseCase(private val repository: RecentlyPlayedRepository) {
    operator fun invoke(songId: String) = repository.addRecentlyPlayed(songId)
}

class RemoveRecentlyPlayedUseCase(private val repository: RecentlyPlayedRepository) {
    operator fun invoke(songId: String) = repository.removeRecentlyPlayed(songId)
}
