package com.example.proyectopruebaappmusia1.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectopruebaappmusia1.data.PlaylistRepository
import com.example.proyectopruebaappmusia1.domain.model.Playlist
import com.example.proyectopruebaappmusia1.domain.model.Song
import com.example.proyectopruebaappmusia1.domain.usecase.*
import com.example.proyectopruebaappmusia1.service.MusicPlayerService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.random.Random

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
    private val addRecentlyPlayedUseCase: AddRecentlyPlayedUseCase,
    private val playlistRepository: PlaylistRepository
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

    private val _playbackQueue = MutableStateFlow<List<Song>>(emptyList())
    val playbackQueue: StateFlow<List<Song>> = _playbackQueue.asStateFlow()

    // --- PLAYLISTS ---
    private val _homePlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    val homePlaylists: StateFlow<List<Playlist>> = _homePlaylists.asStateFlow()

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()
    val customPlaylists: StateFlow<List<Playlist>> = playlistRepository.customPlaylists

    // --- FILTROS Y BÚSQUEDA ---
    private val _minDurationFilter = MutableStateFlow(prefs.getInt("min_duration_filter", 0))
    val minDurationFilter: StateFlow<Int> = _minDurationFilter.asStateFlow()

    // Home
    private val _homeSearchQuery = MutableStateFlow("")
    val homeSearchQuery: StateFlow<String> = _homeSearchQuery.asStateFlow()

    private val _homeFilter = MutableStateFlow(FilterOption.TITLE)
    val homeFilter: StateFlow<FilterOption> = _homeFilter.asStateFlow()

    val filteredHomeSongs: StateFlow<List<Song>> = combine(
        _allSongs, _homeSearchQuery, _homeFilter, _minDurationFilter
    ) { songs, query, filter, minDur ->
        songs.filter { 
            (it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true)) &&
            (minDur == 0 || it.duration > minDur * 1000L)
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
            (minDur == 0 || it.duration > minDur * 1000L)
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
        
        viewModelScope.launch {
            combine(favoriteSongs, _allSongs, playlistRepository.customPlaylists) { favorites, all, custom ->
                val result = mutableListOf<Playlist>()
                result.add(Playlist("fav_playlist", "Favoritos", favorites.size, favorites.firstOrNull()?.albumArt, favorites))
                if (all.isNotEmpty()) {
                    val calendar = Calendar.getInstance()
                    val seed = calendar.get(Calendar.YEAR) * 1000 + calendar.get(Calendar.DAY_OF_YEAR)
                    val dailyMixSongs = all.shuffled(Random(seed)).take(20)
                    result.add(Playlist("daily_mix", "Mix Diario", dailyMixSongs.size, dailyMixSongs.firstOrNull()?.albumArt, dailyMixSongs))
                }
                result.addAll(custom)
                result
            }.collect { playlists ->
                _homePlaylists.value = playlists
                _selectedPlaylist.value?.let { selected ->
                    playlists.find { it.id == selected.id }?.let { _selectedPlaylist.value = it }
                }
            }
        }
    }

    fun loadRealSongs() {
        viewModelScope.launch { 
            val songs = getSongsUseCase()
            _allSongs.value = songs
            if (_playbackQueue.value.isEmpty()) _playbackQueue.value = songs
            
            if (_currentSong.value == null) {
                val lastPlayedIds = getRecentlyPlayedIdsUseCase().firstOrNull()
                if (!lastPlayedIds.isNullOrEmpty()) {
                    val lastSongId = lastPlayedIds.first()
                    val lastSong = songs.find { it.id == lastSongId }
                    if (lastSong != null) selectSong(lastSong, autoPlay = false)
                }
            }
        }
    }

    // --- ACCIONES ---
    fun onHomeSearch(query: String) { _homeSearchQuery.value = query }
    fun setHomeFilter(filter: FilterOption) { _homeFilter.value = filter }
    
    fun onSearchLibrary(query: String) { _librarySearchQuery.value = query }
    fun setLibraryFilter(filter: String) {
        _libraryFilter.value = filter
        prefs.edit().putString("library_filter", filter).apply()
    }
    fun setMinDurationFilter(seconds: Int) {
        _minDurationFilter.value = seconds
        prefs.edit().putInt("min_duration_filter", seconds).apply()
    }

    fun createPlaylist(name: String): String = playlistRepository.createPlaylist(name)
    fun addSongToPlaylist(playlistId: String, song: Song) { playlistRepository.addSongToPlaylist(playlistId, song) }
    fun createPlaylistWithSong(name: String, song: Song): String = playlistRepository.createPlaylistWithSong(name, song)
    fun selectPlaylistForDetail(playlist: Playlist?) { _selectedPlaylist.value = playlist }

    fun playPlaylist(playlist: Playlist, shuffle: Boolean = false) {
        if (playlist.songs.isEmpty()) return
        val newQueue = if (shuffle) playlist.songs.shuffled() else playlist.songs
        _playbackQueue.value = newQueue
        _currentSong.value = newQueue.first()
        musicService.loadPlaylist(newQueue, 0, autoPlay = true)
    }

    fun selectSong(song: Song, autoPlay: Boolean = true, fromUserTap: Boolean = false, newQueue: List<Song>? = null) {
        _currentSong.value = song
        newQueue?.let { _playbackQueue.value = it }
        addRecentlyPlayedUseCase(song.id)
        val queue = _playbackQueue.value
        val index = queue.indexOf(song)
        if (index != -1) {
            // CORREGIDO: Se cambió loadQueue por loadPlaylist (nombre correcto en MusicPlayerService)
            musicService.loadPlaylist(queue, index, autoPlay = autoPlay)
        }
    }

    fun togglePlayPause() = musicService.togglePlayPause()

    fun nextSong() {
        val queue = _playbackQueue.value
        val currentIndex = queue.indexOfFirst { it.id == _currentSong.value?.id }
        if (currentIndex != -1) {
            if (currentIndex < queue.size - 1) {
                selectSong(queue[currentIndex + 1])
            } else if (_repeatMode.value == RepeatMode.ALL) {
                selectSong(queue.first())
            }
        }
    }

    fun previousSong() {
        val queue = _playbackQueue.value
        val currentIndex = queue.indexOfFirst { it.id == _currentSong.value?.id }
        if (currentIndex > 0) {
            selectSong(queue[currentIndex - 1])
        } else if (currentIndex == 0 && _repeatMode.value == RepeatMode.ALL) {
            selectSong(queue.last())
        }
    }

    fun toggleFavorite(song: Song?) { song?.let { toggleFavoriteUseCase(it.id) } }
    fun seekTo(position: Float) { musicService.seekTo((position * _duration.value).toLong()) }
    
    fun toggleShuffle() { 
        _isShuffleEnabled.value = !_isShuffleEnabled.value
        if (_isShuffleEnabled.value) {
            _playbackQueue.value = _playbackQueue.value.shuffled()
        }
    }
    
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

        // --- SINCRONIZACIÓN CON COLA NATIVA ---
        viewModelScope.launch {
            musicService.currentMediaId.collect { mediaId ->
                if (mediaId != null) {
                    val song = _playbackQueue.value.find { it.id == mediaId }
                    if (song != null && song.id != _currentSong.value?.id) {
                        _currentSong.value = song
                        addRecentlyPlayedUseCase(song.id)
                    }
                }
            }
        }

        // --- UNIFICACIÓN CON NOTIFICACIÓN ---
        viewModelScope.launch { 
            musicService.skipNextEvent.collect { nextSong() } 
        }
        viewModelScope.launch { 
            musicService.skipPreviousEvent.collect { previousSong() } 
        }
        viewModelScope.launch { 
            musicService.favoriteEvent.collect { toggleFavorite(_currentSong.value) } 
        }
        
        // Actualizar el estado de favorito en la notificación cuando cambie la canción o los favoritos
        viewModelScope.launch {
            combine(_currentSong, favoriteIds) { song, ids ->
                song?.id in ids
            }.collect { isFav ->
                musicService.updateFavoriteState(isFav)
            }
        }
    }

    override fun onCleared() { musicService.release(); super.onCleared() }
}
