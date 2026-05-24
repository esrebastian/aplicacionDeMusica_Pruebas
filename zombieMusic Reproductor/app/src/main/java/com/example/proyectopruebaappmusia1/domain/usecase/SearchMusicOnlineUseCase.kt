package com.example.proyectopruebaappmusia1.domain.usecase

import com.example.proyectopruebaappmusia1.data.OnlineMusicSearchRepository
import com.example.proyectopruebaappmusia1.domain.model.OnlineTrack

class SearchMusicOnlineUseCase(
    private val repository: OnlineMusicSearchRepository
) {
    suspend operator fun invoke(query: String): Result<List<OnlineTrack>> {
        return repository.search(query)
    }
}
