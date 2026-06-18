package com.example.proyectopruebaappmusia1.data

import android.content.Context
import com.example.proyectopruebaappmusia1.domain.model.Song
import com.example.proyectopruebaappmusia1.domain.repository.MusicRepository
import com.example.proyectopruebaappmusia1.util.MusicProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusicRepositoryImpl(private val context: Context) : MusicRepository {
    override suspend fun getAllSongs(): List<Song> {
        return withContext(Dispatchers.IO) {
            MusicProvider.getSongsFromDevice(context)
        }
    }
}
