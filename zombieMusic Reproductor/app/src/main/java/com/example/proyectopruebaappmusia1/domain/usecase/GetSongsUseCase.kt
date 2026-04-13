package com.example.proyectopruebaappmusia1.domain.usecase

import com.example.proyectopruebaappmusia1.domain.model.Song
import com.example.proyectopruebaappmusia1.domain.repository.MusicRepository

class GetSongsUseCase(private val repository: MusicRepository) {
    suspend operator fun invoke(): List<Song> = repository.getAllSongs()
}
