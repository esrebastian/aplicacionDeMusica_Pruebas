package com.example.proyectopruebaappmusia1.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectopruebaappmusia1.model.Song
import com.example.proyectopruebaappmusia1.service.MusicPlayerService
import com.example.proyectopruebaappmusia1.util.MusicProvider
import com.example.proyectopruebaappmusia1.data.FavoritesRepository
import com.example.proyectopruebaappmusia1.data.RecentlyPlayedRepository
import com.example.proyectopruebaappmusia1.WebBrowserManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class RepeatMode {
    NONE, ONE, ALL
}

class MusicPlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val musicService = MusicPlayerService(context)
    private val prefs = context.getSharedPreferences("music_prefs", Context.MODE_PRIVATE)
    
    private val favoritesRepo = FavoritesRepository.create(prefs)
    private val recentlyPlayedRepo = RecentlyPlayedRepository.create(prefs)
    
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

    private val _originalPlaylist = MutableStateFlow<List<Song>>(emptyList())
    
    private val _currentSongIndex = MutableStateFlow(0)
    val currentSongIndex: StateFlow<Int> = _currentSongIndex.asStateFlow()
    
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.ALL)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    // Filtro de duración mínima (Predeterminado: 40 segundos)
    private val _minDurationFilter = MutableStateFlow(prefs.getInt("min_duration_seconds", 40))
    val minDurationFilter: StateFlow<Int> = _minDurationFilter.asStateFlow()

    // Temporizador de apagado (Sleep Timer)
    private val _sleepTimerRemaining = MutableStateFlow<Long?>(null)
    val sleepTimerRemaining: StateFlow<Long?> = _sleepTimerRemaining.asStateFlow()
    private var sleepTimerJob: Job? = null

    val recentlyPlayed: StateFlow<List<Song>> = combine(playlist, recentlyPlayedRepo.recentlyPlayedIds) { list, ids ->
        ids.mapNotNull { id -> list.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

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
        
        viewModelScope.launch {
            while (true) {
                delay(100)
                if (_isPlaying.value) {
                    _currentPosition.value = musicService.currentPosition.value
                    updateProgress()
                }
            }
        }

        viewModelScope.launch {
            musicService.songCompleted.collect {
                handleSongCompletion()
            }
        }

        viewModelScope.launch {
            musicService.skipNextEvent.collect {
                nextSong()
            }
        }

        viewModelScope.launch {
            musicService.skipPreviousEvent.collect {
                previousSong()
            }
        }
    }

    private fun handleSongCompletion() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                _currentSong.value?.let { selectSong(it) }
            }
            RepeatMode.ALL -> {
                nextSong()
            }
            RepeatMode.NONE -> {
                val list = _playlist.value
                val currentIndex = _currentSongIndex.value
                if (currentIndex < list.size - 1) {
                    nextSong()
                } else {
                    pause()
                    seekTo(0f)
                }
            }
        }
    }
    
    fun setSamplePlaylist() {
        val sampleSongs = listOf(
            Song(id = "sample_1", title = "Mi Canción", artist = "Artista Principal", duration = 174000, filePath = ""),
            Song(id = "sample_2", title = "Vibes Nostálgicas", artist = "Green Beats", duration = 210000, filePath = ""),
            Song(id = "sample_3", title = "Mood Oscuro", artist = "Emerald Skull", duration = 180000, filePath = "")
        )
        _originalPlaylist.value = sampleSongs
        applyFilters()
        if (_playlist.value.isNotEmpty()) {
            selectSong(_playlist.value[0], autoPlay = false)
        }
    }
    
    fun loadRealSongs(context: Context) {
        viewModelScope.launch {
            val realSongs = MusicProvider.getSongsFromDevice(context)
            if (realSongs.isNotEmpty()) {
                _originalPlaylist.value = realSongs
                applyFilters()
                
                val lastPlayedId = recentlyPlayedRepo.recentlyPlayedIds.value.firstOrNull()
                val lastPlayedSong = _playlist.value.find { it.id == lastPlayedId }
                
                if (lastPlayedSong != null) {
                    selectSong(lastPlayedSong, autoPlay = false)
                } else if (_playlist.value.isNotEmpty()) {
                    selectSong(_playlist.value[0], autoPlay = false)
                }
            } else {
                setSamplePlaylist()
            }
        }
    }

    fun setMinDurationFilter(seconds: Int) {
        _minDurationFilter.value = seconds
        prefs.edit().putInt("min_duration_seconds", seconds).apply()
        applyFilters()
    }

    private fun applyFilters() {
        val minMillis = _minDurationFilter.value * 1000L
        val filtered = _originalPlaylist.value.filter { it.duration >= minMillis }
        _playlist.value = if (_isShuffleEnabled.value) filtered.shuffled() else filtered
        
        // Si la canción actual ya no está en la lista filtrada, paramos la música o buscamos otra
        val current = _currentSong.value
        if (current != null && !_playlist.value.any { it.id == current.id }) {
            pause()
            _currentSong.value = null
            if (_playlist.value.isNotEmpty()) {
                selectSong(_playlist.value[0], autoPlay = false)
            }
        }
    }

    fun selectSong(song: Song, autoPlay: Boolean = true) {
        val currentList = _playlist.value
        val updatedList = currentList.map {
            if (it.id == song.id) it.copy(playCount = it.playCount + 1) else it
        }
        
        val updatedSong = updatedList.find { it.id == song.id } ?: song
        _playlist.value = updatedList
        
        val index = updatedList.indexOfFirst { it.id == updatedSong.id }
        if (index != -1) {
            _currentSongIndex.value = index
        }
        
        _currentSong.value = updatedSong
        recentlyPlayedRepo.addRecentlyPlayed(updatedSong.id)
        
        if (updatedSong.filePath.isNotEmpty()) {
            if (autoPlay) {
                WebBrowserManager.pauseVideo()
                musicService.loadSong(updatedSong.filePath, updatedSong.title, updatedSong.artist)
                musicService.play()
            } else {
                musicService.loadSong(updatedSong.filePath, updatedSong.title, updatedSong.artist)
            }
        }
    }

    fun toggleShuffle() {
        val currentShuffle = !_isShuffleEnabled.value
        _isShuffleEnabled.value = currentShuffle
        
        val currentSong = _currentSong.value
        if (currentShuffle) {
            _playlist.value = _playlist.value.shuffled().toMutableList().apply {
                currentSong?.let {
                    remove(it)
                    add(0, it)
                }
            }
        } else {
            // Cuando quitamos el aleatorio, volvemos al orden original pero manteniendo el filtro
            val minMillis = _minDurationFilter.value * 1000L
            _playlist.value = _originalPlaylist.value.filter { it.duration >= minMillis }
        }
        
        // Actualizar el índice después de mezclar
        _currentSongIndex.value = _playlist.value.indexOfFirst { it.id == currentSong?.id }.coerceAtLeast(0)
    }

    fun toggleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.NONE -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.NONE
        }
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes == 0) {
            _sleepTimerRemaining.value = null
            return
        }
        
        val durationMillis = minutes * 60 * 1000L
        _sleepTimerRemaining.value = durationMillis
        
        sleepTimerJob = viewModelScope.launch {
            var remaining = durationMillis
            while (remaining > 0) {
                delay(1000)
                remaining -= 1000
                _sleepTimerRemaining.value = remaining
            }
            pause()
            _sleepTimerRemaining.value = null
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _sleepTimerRemaining.value = null
    }

    fun deleteRecentlyPlayedSong(songId: String) {
        val wasCurrent = _currentSong.value?.id == songId
        recentlyPlayedRepo.removeRecentlyPlayed(songId)

        if (wasCurrent) {
            val nextInRecent = recentlyPlayed.value.firstOrNull()
            if (nextInRecent != null) {
                selectSong(nextInRecent, autoPlay = isPlaying.value)
            } else {
                val firstInPlaylist = _playlist.value.firstOrNull()
                if (firstInPlaylist != null) {
                    selectSong(firstInPlaylist, autoPlay = isPlaying.value)
                }
            }
        }
    }
    
    fun play() {
        WebBrowserManager.pauseVideo()
        musicService.play()
    }
    
    fun pause() = musicService.pause()
    
    fun togglePlayPause() {
        if (!_isPlaying.value) {
            WebBrowserManager.pauseVideo()
        }
        musicService.togglePlayPause()
    }
    
    fun nextSong() {
        val list = _playlist.value
        val currentIndex = _currentSongIndex.value
        if (currentIndex < list.size - 1) {
            selectSong(list[currentIndex + 1])
        } else if (_repeatMode.value == RepeatMode.ALL && list.isNotEmpty()) {
            selectSong(list[0])
        }
    }
    
    fun previousSong() {
        val currentIndex = _currentSongIndex.value
        if (currentIndex > 0) {
            selectSong(_playlist.value[currentIndex - 1])
        } else if (_repeatMode.value == RepeatMode.ALL && _playlist.value.isNotEmpty()) {
            selectSong(_playlist.value.last())
        }
    }
    
    fun seekTo(position: Float) {
        val seekPosition = (position * _duration.value).toLong()
        musicService.seekTo(seekPosition)
    }

    fun isFavorite(songId: String?): Boolean = songId != null && favoritesRepo.isFavorite(songId)
    fun toggleFavorite(song: Song?) = song?.let { favoritesRepo.toggleFavorite(it.id) }

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
