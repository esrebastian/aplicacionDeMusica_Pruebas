package com.example.proyectopruebaappmusia1.service

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
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
 * Cliente para el MusicService. Se encarga de conectar el ViewModel con el servicio real.
 */
class MusicPlayerService(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _songCompleted = Channel<Unit>(Channel.BUFFERED)
    val songCompleted = _songCompleted.receiveAsFlow()

    init {
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            setupController()
        }, MoreExecutors.directExecutor())

        // Actualizar posición periódicamente
        scope.launch {
            while (true) {
                delay(200)
                mediaController?.let { controller ->
                    if (controller.isPlaying) {
                        _currentPosition.value = controller.currentPosition.coerceAtLeast(0L)
                        _duration.value = controller.duration.coerceAtLeast(0L)
                    }
                }
            }
        }
    }

    private fun setupController() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    _songCompleted.trySend(Unit)
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _isPlaying.value = false
            }
        })
    }

    fun loadSong(filePath: String, title: String, artist: String) {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(filePath)
            .setMediaMetadata(metadata)
            .build()

        mediaController?.let { controller ->
            controller.setMediaItem(mediaItem)
            controller.prepare()
        }
    }

    fun play() = mediaController?.play()
    fun pause() = mediaController?.pause()
    fun togglePlayPause() {
        if (mediaController?.isPlaying == true) pause() else play()
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    fun release() {
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
    }
}
