package com.example.proyectopruebaappmusia1.service

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Servicio de reproducción basado en ExoPlayer.
 * Soporta muchos más formatos de audio que MediaPlayer.
 */
class MusicPlayerService(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        // Duración disponible
                        _duration.value = duration.coerceAtLeast(0L)
                    }
                    Player.STATE_ENDED -> {
                        _isPlaying.value = false
                        _currentPosition.value = 0L
                        _songCompleted.trySend(Unit)
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _isPlaying.value = false
            }
        })
    }

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _currentSongPath = MutableStateFlow("")
    val currentSongPath: StateFlow<String> = _currentSongPath.asStateFlow()

    private val _songCompleted = Channel<Unit>(Channel.BUFFERED)
    val songCompleted = _songCompleted.receiveAsFlow()

    init {
        // Actualizar posición de reproducción periódicamente
        scope.launch {
            while (true) {
                delay(200)
                if (_isPlaying.value) {
                    _currentPosition.value = exoPlayer.currentPosition.coerceAtLeast(0L)
                }
            }
        }
    }

    fun loadSong(filePath: String) {
        try {
            val mediaItem = MediaItem.fromUri(filePath)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            _currentSongPath.value = filePath
            _currentPosition.value = 0L
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun play() {
        try {
            if (exoPlayer.playbackState == Player.STATE_IDLE && _currentSongPath.value.isNotEmpty()) {
                // Si no hay nada preparado, vuelve a preparar el último mediaItem
                exoPlayer.setMediaItem(MediaItem.fromUri(_currentSongPath.value))
                exoPlayer.prepare()
            }
            exoPlayer.playWhenReady = true
            _isPlaying.value = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pause() {
        try {
            exoPlayer.pause()
            _isPlaying.value = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        try {
            exoPlayer.stop()
            _isPlaying.value = false
            _currentPosition.value = 0L
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun seekTo(position: Long) {
        try {
            exoPlayer.seekTo(position.coerceAtLeast(0L))
            _currentPosition.value = position.coerceAtLeast(0L)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    fun release() {
        scope.launch {
            exoPlayer.release()
            _isPlaying.value = false
        }
    }
}
