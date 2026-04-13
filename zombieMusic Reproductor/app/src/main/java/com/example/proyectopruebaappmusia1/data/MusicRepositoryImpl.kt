package com.example.proyectopruebaappmusia1.data

import android.content.Context
import com.example.proyectopruebaappmusia1.domain.model.Song
import com.example.proyectopruebaappmusia1.domain.repository.MusicRepository
import com.example.proyectopruebaappmusia1.util.MusicProvider

class MusicRepositoryImpl(private val context: Context) : MusicRepository {
    override suspend fun getAllSongs(): List<Song> {
        return MusicProvider.getSongsFromDevice(context)
    }
}
