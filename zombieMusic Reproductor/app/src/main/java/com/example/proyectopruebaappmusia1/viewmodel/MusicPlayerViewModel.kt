package com.example.proyectopruebaappmusia1.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectopruebaappmusia1.model.Song
import com.example.proyectopruebaappmusia1.service.MusicPlayerService
import com.example.proyectopruebaappmusia1.util.MusicProvider
import com.example.proyectopruebaappmusia1.data.FavoritesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicPlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val musicService = MusicPlayerService(context)
    private val favoritesRepo = FavoritesRepository.create(
        context.getSharedPreferences("music_prefs", Context.MODE_PRIVATE)
    )
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()
    
    private val _playlist = MutableStateFlow<List<Song>>(emptyList())
    val playlist: StateFlow<List<Song>> = _playlist.asStateFlow()
    
    private val _currentSongIndex = MutableStateFlow(0)
    val currentSongIndex: StateFlow<Int> = _currentSongIndex.asStateFlow()
    
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _recentlyPlayed = MutableStateFlow<List<Song>>(emptyList())
    val recentlyPlayed: StateFlow<List<Song>> = _recentlyPlayed.asStateFlow()

    val favoriteIds: StateFlow<Set<String>> = favoritesRepo.favoriteIds
    val favoriteSongs: StateFlow<List<Song>> = combine(playlist, favoriteIds) { list, ids ->
        list.filter { it.id in ids }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            musicService.isPlaying.collect { isPlaying ->
                _isPlaying.value = isPlaying
            }
        }
        
        viewModelScope.launch {
            musicService.currentPosition.collect { position ->
                _currentPosition.value = position
                updateProgress()
            }
        }
        
        viewModelScope.launch {
            musicService.duration.collect { duration ->
                _duration.value = duration
                updateProgress()
            }
        }
        
        // Actualizar progreso continuamente cuando está en reproducción
        viewModelScope.launch {
            while (true) {
                delay(100)
                if (_isPlaying.value) {
                    _currentPosition.value = musicService.currentPosition.value
                    updateProgress()
                }
            }
        }

        // Reproducir automáticamente la siguiente canción cuando termina la actual
        viewModelScope.launch {
            musicService.songCompleted.collect {
                nextSong()
            }
        }
    }
    
    fun setSamplePlaylist() {
        val sampleSongs = listOf(
            Song(
                id = "sample_1",
                title = "Mi Canción",
                artist = "Artista Principal",
                duration = 174000, // 2:54
                filePath = ""
            ),
            Song(
                id = "sample_2",
                title = "Vibes Nostálgicas",
                artist = "Green Beats",
                duration = 210000,
                filePath = ""
            ),
            Song(
                id = "sample_3",
                title = "Mood Oscuro",
                artist = "Emerald Skull",
                duration = 180000,
                filePath = ""
            )
        )
        _playlist.value = sampleSongs
        if (sampleSongs.isNotEmpty()) {
            _currentSong.value = sampleSongs[0]
        }
    }
    
    fun loadRealSongs(context: Context) {
        viewModelScope.launch {
            val realSongs = MusicProvider.getSongsFromDevice(context)
            if (realSongs.isNotEmpty()) {
                _playlist.value = realSongs
                // Seleccionamos la primera canción pero sin reproducirla automáticamente
                selectSong(realSongs[0], autoPlay = false)
            } else {
                // Si no hay canciones en el dispositivo, usa ejemplos
                setSamplePlaylist()
            }
        }
    }
    
    fun loadSong(song: Song) {
        // Mantener compatibilidad con llamadas existentes: solo carga, no reproduce.
        selectSong(song, autoPlay = false)
    }

    fun selectSong(song: Song, autoPlay: Boolean = true) {
        val list = _playlist.value
        val index = list.indexOfFirst { it.id == song.id }
        if (index != -1) {
            _currentSongIndex.value = index
        }
        _currentSong.value = song
        
        // Agregar a escuchados recientemente
        addToRecentlyPlayed(song)
        
        if (song.filePath.isNotEmpty()) {
            musicService.loadSong(song.filePath)
            if (autoPlay) {
                musicService.play()
            }
        }
    }

    private fun addToRecentlyPlayed(song: Song) {
        val currentList = _recentlyPlayed.value.toMutableList()
        // Remover si ya existe para moverlo al principio
        currentList.removeAll { it.id == song.id }
        // Agregar al principio
        currentList.add(0, song)
        // Limitar a una lista manejable (ej. las últimas 50 para el "Ver todo")
        _recentlyPlayed.value = currentList.take(50)
    }
    
    fun play() {
        musicService.play()
    }
    
    fun pause() {
        musicService.pause()
    }
    
    fun togglePlayPause() {
        musicService.togglePlayPause()
    }
    
    fun nextSong() {
        val list = _playlist.value
        val currentIndex = _currentSongIndex.value
        if (currentIndex < list.size - 1) {
            selectSong(list[currentIndex + 1], autoPlay = true)
        }
    }
    
    fun previousSong() {
        val list = _playlist.value
        val currentIndex = _currentSongIndex.value
        if (currentIndex > 0) {
            selectSong(list[currentIndex - 1], autoPlay = true)
        }
    }
    
    fun seekTo(position: Float) {
        val seekPosition = (position * _duration.value).toLong()
        musicService.seekTo(seekPosition)
    }

    fun isFavorite(songId: String?): Boolean =
        songId != null && favoritesRepo.isFavorite(songId)

    fun toggleFavorite(song: Song?) {
        song ?: return
        favoritesRepo.toggleFavorite(song.id)
    }

    private fun updateProgress() {
        if (_duration.value > 0) {
            _progress.value = _currentPosition.value.toFloat() / _duration.value.toFloat()
        }
    }
    
    override fun onCleared() {
        musicService.release()
        super.onCleared()
    }
}
