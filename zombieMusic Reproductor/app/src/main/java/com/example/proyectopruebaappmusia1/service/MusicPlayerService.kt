package com.example.proyectopruebaappmusia1.service

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
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
    
    // Almacena una canción que se intentó cargar antes de que el controlador estuviera listo
    private var pendingSong: PendingSong? = null

    private data class PendingSong(val filePath: String, val title: String, val artist: String, val artworkUri: Uri?)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _songCompleted = Channel<Unit>(Channel.BUFFERED)
    val songCompleted = _songCompleted.receiveAsFlow()

    private val _skipNextEvent = Channel<Unit>(Channel.BUFFERED)
    val skipNextEvent = _skipNextEvent.receiveAsFlow()

    private val _skipPreviousEvent = Channel<Unit>(Channel.BUFFERED)
    val skipPreviousEvent = _skipPreviousEvent.receiveAsFlow()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                MusicService.ACTION_NEXT -> {
                    _skipNextEvent.trySend(Unit)
                }
                MusicService.ACTION_PREVIOUS -> {
                    _skipPreviousEvent.trySend(Unit)
                }
            }
        }
    }

    init {
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            setupController()
        }, MoreExecutors.directExecutor())

        // Registrar receiver para los botones de la notificación
        val filter = IntentFilter().apply {
            addAction(MusicService.ACTION_NEXT)
            addAction(MusicService.ACTION_PREVIOUS)
        }
        
        // Usar RECEIVER_EXPORTED para permitir que el Broadcast enviado por el Servicio llegue aquí
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }

        // Actualizar posición periódicamente
        scope.launch {
            while (true) {
                delay(200)
                mediaController?.let { controller ->
                    _isPlaying.value = controller.isPlaying
                    _currentPosition.value = controller.currentPosition.coerceAtLeast(0L)
                    _duration.value = controller.duration.coerceAtLeast(0L)
                }
            }
        }
    }

    private fun setupController() {
        // Cargar canción pendiente si existe
        pendingSong?.let { p ->
            loadSong(p.filePath, p.title, p.artist, p.artworkUri)
            pendingSong = null
        }

        mediaController?.let { controller ->
            _isPlaying.value = controller.isPlaying
            _currentPosition.value = controller.currentPosition.coerceAtLeast(0L)
            _duration.value = controller.duration.coerceAtLeast(0L)
            
            controller.addListener(object : Player.Listener {
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
    }

    fun loadSong(filePath: String, title: String, artist: String, artworkUri: Uri? = null) {
        val controller = mediaController
        if (controller == null) {
            pendingSong = PendingSong(filePath, title, artist, artworkUri)
            return
        }

        // Verificar si la canción ya está cargada para no reiniciarla
        val currentMediaItem = controller.currentMediaItem
        if (currentMediaItem?.localConfiguration?.uri.toString() == filePath) {
            return
        }

        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setArtworkUri(artworkUri)
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(filePath)
            .setMediaMetadata(metadata)
            .build()

        controller.setMediaItem(mediaItem)
        controller.prepare()
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
        try {
            context.unregisterReceiver(receiver)
        } catch (e: Exception) {
            // Ya desregistrado o no registrado
        }
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
    }
}
