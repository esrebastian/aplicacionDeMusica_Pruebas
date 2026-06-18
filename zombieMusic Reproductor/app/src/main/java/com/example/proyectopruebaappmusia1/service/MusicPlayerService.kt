package com.example.proyectopruebaappmusia1.service

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionToken
import com.example.proyectopruebaappmusia1.domain.model.Song
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
 * Optimizado para que los controles de notificación funcionen en dispositivos modernos (Samsung A24, etc).
 */
class MusicPlayerService(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var positionJob: Job? = null
    private var loadedMediaIds: List<String> = emptyList()
    private val mediaItemCache = mutableMapOf<String, MediaItem>()
    private var positionUpdateIntervalMs = 1_000L
    
    private var pendingPlaylist: PendingPlaylist? = null
    private data class PendingPlaylist(val songs: List<Song>, val startIndex: Int, val autoPlay: Boolean)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _currentMediaId = MutableStateFlow<String?>(null)
    val currentMediaId: StateFlow<String?> = _currentMediaId.asStateFlow()

    private val _songCompleted = Channel<Unit>(Channel.BUFFERED)
    val songCompleted = _songCompleted.receiveAsFlow()

    private val _skipNextEvent = Channel<Unit>(Channel.BUFFERED)
    val skipNextEvent = _skipNextEvent.receiveAsFlow()

    private val _skipPreviousEvent = Channel<Unit>(Channel.BUFFERED)
    val skipPreviousEvent = _skipPreviousEvent.receiveAsFlow()

    private val _favoriteEvent = Channel<Unit>(Channel.BUFFERED)
    val favoriteEvent = _favoriteEvent.receiveAsFlow()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                MusicService.ACTION_NEXT -> _skipNextEvent.trySend(Unit)
                MusicService.ACTION_PREVIOUS -> _skipPreviousEvent.trySend(Unit)
                MusicService.ACTION_FAVORITE -> _favoriteEvent.trySend(Unit)
            }
        }
    }

    init {
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        
        controllerFuture = MediaController.Builder(context, sessionToken)
            .setListener(object : MediaController.Listener {
                override fun onCustomCommand(
                    controller: MediaController,
                    command: SessionCommand,
                    args: Bundle
                ): ListenableFuture<SessionResult> {
                    when (command.customAction) {
                        MusicService.CUSTOM_COMMAND_NEXT -> _skipNextEvent.trySend(Unit)
                        MusicService.CUSTOM_COMMAND_PREVIOUS -> _skipPreviousEvent.trySend(Unit)
                        MusicService.CUSTOM_COMMAND_FAVORITE -> _favoriteEvent.trySend(Unit)
                    }
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
            })
            .buildAsync()

        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            setupController()
        }, MoreExecutors.directExecutor())

        val filter = IntentFilter().apply {
            addAction(MusicService.ACTION_NEXT)
            addAction(MusicService.ACTION_PREVIOUS)
            addAction(MusicService.ACTION_FAVORITE)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }

    }

    private fun setupController() {
        pendingPlaylist?.let { p ->
            loadPlaylist(p.songs, p.startIndex, p.autoPlay)
            pendingPlaylist = null
        }

        mediaController?.let { controller ->
            _isPlaying.value = controller.isPlaying
            _currentPosition.value = controller.currentPosition.coerceAtLeast(0L)
            _duration.value = controller.duration.coerceAtLeast(0L)
            _currentMediaId.value = controller.currentMediaItem?.mediaId
            if (controller.isPlaying) startPositionUpdates()
            
            controller.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                    if (isPlaying) startPositionUpdates() else stopPositionUpdates()
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    _currentMediaId.value = mediaItem?.mediaId
                    updatePlaybackSnapshot()
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    updatePlaybackSnapshot()
                    if (playbackState == Player.STATE_ENDED) {
                        _songCompleted.trySend(Unit)
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    _isPlaying.value = false
                    stopPositionUpdates()
                }
            })
        }
    }

    private fun updatePlaybackSnapshot() {
        mediaController?.let { controller ->
            _currentPosition.value = controller.currentPosition.coerceAtLeast(0L)
            _duration.value = controller.duration.coerceAtLeast(0L)
            _currentMediaId.value = controller.currentMediaItem?.mediaId
        }
    }

    private fun startPositionUpdates() {
        if (positionJob?.isActive == true) return
        positionJob = scope.launch {
            while (mediaController?.isPlaying == true) {
                updatePlaybackSnapshot()
                delay(positionUpdateIntervalMs)
            }
            updatePlaybackSnapshot()
        }
    }

    private fun stopPositionUpdates() {
        positionJob?.cancel()
        positionJob = null
        updatePlaybackSnapshot()
    }

    fun loadPlaylist(songs: List<Song>, startIndex: Int, autoPlay: Boolean = true) {
        val controller = mediaController
        if (controller == null) {
            pendingPlaylist = PendingPlaylist(songs, startIndex, autoPlay)
            return
        }

        if (songs.isEmpty()) return
        val safeStartIndex = startIndex.coerceIn(0, songs.lastIndex)
        val requestedIds = songs.map { it.id }
        if (requestedIds == loadedMediaIds && controller.mediaItemCount == songs.size) {
            controller.seekTo(safeStartIndex, 0L)
            if (controller.playbackState == Player.STATE_IDLE) controller.prepare()
            if (autoPlay) controller.play()
            updatePlaybackSnapshot()
            return
        }

        val mediaItems = songs.map { song -> mediaItemCache.getOrPut(song.mediaItemCacheKey()) { song.toMediaItem() } }

        controller.setMediaItems(mediaItems, safeStartIndex, 0L)
        loadedMediaIds = requestedIds
        controller.prepare()
        if (autoPlay) controller.play()
        updatePlaybackSnapshot()
    }

    fun play() = mediaController?.play()
    fun pause() = mediaController?.pause()
    fun togglePlayPause() {
        if (mediaController?.isPlaying == true) pause() else play()
    }

    fun seekToNext() = mediaController?.seekToNext()
    fun seekToPrevious() = mediaController?.seekToPrevious()

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    fun setPositionUpdateInterval(intervalMs: Long) {
        val normalizedInterval = intervalMs.coerceAtLeast(250L)
        if (positionUpdateIntervalMs == normalizedInterval) return
        positionUpdateIntervalMs = normalizedInterval
        if (mediaController?.isPlaying == true) {
            stopPositionUpdates()
            startPositionUpdates()
        }
    }

    fun updateFavoriteState(isFavorite: Boolean) {
        val controller = mediaController ?: return
        val args = Bundle().apply {
            putBoolean(MusicService.ARG_IS_FAVORITE, isFavorite)
        }
        controller.sendCustomCommand(
            SessionCommand(MusicService.CUSTOM_COMMAND_UPDATE_FAVORITE, Bundle.EMPTY),
            args
        )
    }

    fun release() {
        stopPositionUpdates()
        try { context.unregisterReceiver(receiver) } catch (e: Exception) {}
        controllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
        loadedMediaIds = emptyList()
        mediaItemCache.clear()
    }

    private fun Song.mediaItemCacheKey(): String = "$id|$filePath|$title|$artist|$albumArt"

    private fun Song.toMediaItem(): MediaItem {
        val artworkUri = if (!albumArt.isNullOrBlank() && albumArt != "0") {
            ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"),
                albumArt.toLongOrNull() ?: 0L
            )
        } else null

        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setArtworkUri(artworkUri)
            .build()

        return MediaItem.Builder()
            .setMediaId(id)
            .setUri(filePath)
            .setMediaMetadata(metadata)
            .build()
    }
}
