package com.example.proyectopruebaappmusia1.viewmodel

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.proyectopruebaappmusia1.data.*
import com.example.proyectopruebaappmusia1.domain.usecase.*

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val appContext = context.applicationContext
        val application = appContext as Application

        return when {
            modelClass.isAssignableFrom(MusicPlayerViewModel::class.java) -> {
                val prefs = appContext.getSharedPreferences("music_prefs", Context.MODE_PRIVATE)
                val repository = MusicRepositoryImpl(appContext)
                val favoritesRepository = FavoritesRepositoryImpl(prefs)
                val recentlyPlayedRepository = RecentlyPlayedRepositoryImpl(prefs)

                MusicPlayerViewModel(
                    application = application,
                    getSongsUseCase = GetSongsUseCase(repository),
                    getFavoriteIdsUseCase = GetFavoriteIdsUseCase(favoritesRepository),
                    toggleFavoriteUseCase = ToggleFavoriteUseCase(favoritesRepository),
                    getRecentlyPlayedIdsUseCase = GetRecentlyPlayedIdsUseCase(recentlyPlayedRepository),
                    addRecentlyPlayedUseCase = AddRecentlyPlayedUseCase(recentlyPlayedRepository)
                ) as T
            }
            modelClass.isAssignableFrom(ExploreViewModel::class.java) -> {
                val api = YouTubeDownloadApi.create()
                ExploreViewModel(
                    downloadFromUrlUseCase = DownloadFromUrlUseCase(api, appContext)
                ) as T
            }
            modelClass.isAssignableFrom(DownloadViewModel::class.java) -> {
                val downloadManager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val repository = DownloadRepositoryImpl(downloadManager)
                DownloadViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
