package com.example.proyectopruebaappmusia1.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectopruebaappmusia1.domain.model.Song
import com.example.proyectopruebaappmusia1.domain.usecase.*
import com.example.proyectopruebaappmusia1.service.MusicPlayerService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class RepeatMode { NONE, ONE, ALL }
enum class FilterOption(val displayName: String) {
    TITLE("Título"), ARTIST("Artista"), DURATION("Duración")
}

class MusicPlayerViewModel(
    application: Application,
    private val getSongsUseCase: GetSongsUseCase,
    private val getFavoriteIdsUseCase: GetFavoriteIdsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val getRecentlyPlayedIdsUseCase: GetRecentlyPlayedIdsUseCase,
    private val addRecentlyPlayedUseCase: AddRecentlyPlayedUseCase
) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val musicService = MusicPlayerService(context)
    private val prefs = context.getSharedPreferences("music_prefs", Context.MODE_PRIVATE)
    
    // --- ESTADOS DE UI ---
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()
    
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _allSongs = MutableStateFlow<List<Song>>(emptyList())
    val playlist: StateFlow<List<Song>> = _allSongs.asStateFlow()

    // Filtros Globales (Ajustes)
    private val _minDurationFilter = MutableStateFlow(prefs.getInt("min_duration_filter", 0))
    val minDurationFilter: StateFlow<Int> = _minDurationFilter.asStateFlow()

    // Búsqueda y Filtro de Home
    private val _homeSearchQuery = MutableStateFlow("")
    val homeSearchQuery: StateFlow<String> = _homeSearchQuery.asStateFlow()

    private val _homeFilter = MutableStateFlow(FilterOption.TITLE)
    val homeFilter: StateFlow<FilterOption> = _homeFilter.asStateFlow()

    val filteredHomeSongs: StateFlow<List<Song>> = combine(
        _allSongs, _homeSearchQuery, _homeFilter, _minDurationFilter
    ) { songs, query, filter, minDur ->
        songs.filter { 
            (it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true)) &&
            (it.duration >= minDur * 1000L)
        }.let { list ->
            when (filter) {
                FilterOption.TITLE -> list.sortedBy { it.title }
                FilterOption.ARTIST -> list.sortedBy { it.artist }
                FilterOption.DURATION -> list.sortedByDescending { it.duration }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Librería
    private val _librarySearchQuery = MutableStateFlow("")
    val librarySearchQuery: StateFlow<String> = _librarySearchQuery.asStateFlow()

    private val _libraryFilter = MutableStateFlow(prefs.getString("library_filter", "De la A a la Z") ?: "De la A a la Z")
    val libraryFilter: StateFlow<String> = _libraryFilter.asStateFlow()

    val filteredLibrarySongs: StateFlow<List<Song>> = combine(
        _allSongs, _librarySearchQuery, _libraryFilter, _minDurationFilter
    ) { songs, query, filter, minDur ->
        songs.filter { 
            (it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true)) &&
            (it.duration >= minDur * 1000L)
        }.let { list ->
            when (filter) {
                "De la A a la Z" -> list.sortedBy { it.title }
                "Artista" -> list.sortedBy { it.artist }
                "Más recientes" -> list.sortedByDescending { it.dateAdded }
                "Duración más larga" -> list.sortedByDescending { it.duration }
                else -> list
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Favoritos e Historial
    val favoriteIds: StateFlow<Set<String>> = getFavoriteIdsUseCase()
    
    val recentlyPlayed: StateFlow<List<Song>> = combine(_allSongs, getRecentlyPlayedIdsUseCase()) { songs, ids ->
        ids.mapNotNull { id -> songs.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val favoriteSongs: StateFlow<List<Song>> = combine(_allSongs, favoriteIds) { songs, ids ->
        songs.filter { it.id in ids }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.ALL)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _sleepTimerRemaining = MutableStateFlow<Long?>(null)
    val sleepTimerRemaining: StateFlow<Long?> = _sleepTimerRemaining.asStateFlow()
    private var sleepTimerJob: Job? = null

    init {
        observeMusicService()
        loadRealSongs()
    }

    private fun loadRealSongs() {
        viewModelScope.launch { _allSongs.value = getSongsUseCase() }
    }

    // Acciones Ajustes
    fun setMinDurationFilter(seconds: Int) {
        _minDurationFilter.value = seconds
        prefs.edit().putInt("min_duration_filter", seconds).apply()
    }

    // Acciones Home
    fun onHomeSearch(query: String) { _homeSearchQuery.value = query }
    fun setHomeFilter(filter: FilterOption) { _homeFilter.value = filter }

    // Acciones Librería
    fun onSearchLibrary(query: String) { _librarySearchQuery.value = query }
    fun setLibraryFilter(filter: String) {
        _libraryFilter.value = filter
        prefs.edit().putString("library_filter", filter).apply()
    }

    fun selectSong(song: Song, autoPlay: Boolean = true, fromUserTap: Boolean = false) {
        _currentSong.value = song
        addRecentlyPlayedUseCase(song.id)
        musicService.loadSong(song.filePath, song.title, song.artist)
        if (autoPlay) musicService.play()
    }

    fun togglePlayPause() = musicService.togglePlayPause()
    fun nextSong() {
        val list = _allSongs.value
        val currentIndex = list.indexOfFirst { it.id == _currentSong.value?.id }
        if (currentIndex != -1 && currentIndex < list.size - 1) selectSong(list[currentIndex + 1])
    }
    fun previousSong() {
        val list = _allSongs.value
        val currentIndex = list.indexOfFirst { it.id == _currentSong.value?.id }
        if (currentIndex > 0) selectSong(list[currentIndex - 1])
    }

    fun toggleFavorite(song: Song?) { song?.let { toggleFavoriteUseCase(it.id) } }
    fun seekTo(position: Float) { musicService.seekTo((position * _duration.value).toLong()) }
    fun toggleShuffle() { _isShuffleEnabled.value = !_isShuffleEnabled.value }
    fun toggleRepeatMode() {
        _repeatMode.value = when(_repeatMode.value) {
            RepeatMode.NONE -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.NONE
        }
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes == 0) { _sleepTimerRemaining.value = null; return }
        val totalMs = minutes * 60 * 1000L
        _sleepTimerRemaining.value = totalMs
        sleepTimerJob = viewModelScope.launch {
            var remaining = totalMs
            while (remaining > 0) { delay(1000); remaining -= 1000; _sleepTimerRemaining.value = remaining }
            musicService.pause(); _sleepTimerRemaining.value = null
        }
    }

    private fun observeMusicService() {
        viewModelScope.launch { musicService.isPlaying.collect { _isPlaying.value = it } }
        viewModelScope.launch { musicService.currentPosition.collect { _currentPosition.value = it } }
        viewModelScope.launch { musicService.duration.collect { _duration.value = it } }
        viewModelScope.launch { musicService.songCompleted.collect { nextSong() } }
    }

    override fun onCleared() { musicService.release(); super.onCleared() }
}
