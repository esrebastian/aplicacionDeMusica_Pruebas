package com.example.proyectopruebaappmusia1.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.common.ForwardingPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.example.proyectopruebaappmusia1.MainActivity
import com.example.proyectopruebaappmusia1.R
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

@OptIn(UnstableApi::class)
class MusicService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var exoPlayer: ExoPlayer
    private var isCurrentFavorite = false

    companion object {
        const val ACTION_NEXT = "com.example.proyectopruebaappmusia1.NEXT"
        const val ACTION_PREVIOUS = "com.example.proyectopruebaappmusia1.PREVIOUS"
        const val ACTION_FAVORITE = "com.example.proyectopruebaappmusia1.FAVORITE"
        
        // Comandos personalizados para comunicación directa con el controlador
        const val CUSTOM_COMMAND_NEXT = "ACTION_NEXT"
        const val CUSTOM_COMMAND_PREVIOUS = "ACTION_PREVIOUS"
        const val CUSTOM_COMMAND_FAVORITE = "ACTION_FAVORITE"
        const val CUSTOM_COMMAND_CLOSE = "ACTION_CLOSE"
        const val CUSTOM_COMMAND_UPDATE_FAVORITE = "UPDATE_FAVORITE_STATE"
        
        const val ARG_IS_FAVORITE = "is_favorite"
    }

    override fun onCreate() {
        super.onCreate()
        
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .build()

        // El ForwardingPlayer permite "engañar" al sistema para habilitar botones
        val forwardingPlayer = object : ForwardingPlayer(exoPlayer) {
            override fun getAvailableCommands(): Player.Commands {
                return super.getAvailableCommands().buildUpon()
                    .add(Player.COMMAND_SEEK_TO_NEXT)
                    .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                    .build()
            }

            // Android comprueba estos métodos para mostrar/ocultar botones en la notificación
            override fun hasNextMediaItem(): Boolean = true
            override fun hasPreviousMediaItem(): Boolean = true

            override fun seekToNext() {
                sendCustomEvent(CUSTOM_COMMAND_NEXT)
                triggerAction(ACTION_NEXT)
            }

            override fun seekToPrevious() {
                sendCustomEvent(CUSTOM_COMMAND_PREVIOUS)
                triggerAction(ACTION_PREVIOUS)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, forwardingPlayer)
            .setId("ZombieMusicSession")
            .setSessionActivity(pendingIntent)
            .setCallback(CustomSessionCallback())
            .build()
            
        updateNotificationLayout()
    }

    private inner class CustomSessionCallback : MediaSession.Callback {
        override fun onConnect(session: MediaSession, controller: MediaSession.ControllerInfo): MediaSession.ConnectionResult {
            val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                .add(SessionCommand(CUSTOM_COMMAND_NEXT, Bundle.EMPTY))
                .add(SessionCommand(CUSTOM_COMMAND_PREVIOUS, Bundle.EMPTY))
                .add(SessionCommand(CUSTOM_COMMAND_FAVORITE, Bundle.EMPTY))
                .add(SessionCommand(CUSTOM_COMMAND_CLOSE, Bundle.EMPTY))
                .add(SessionCommand(CUSTOM_COMMAND_UPDATE_FAVORITE, Bundle.EMPTY))
                .build()
            
            val playerCommands = MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
                .add(Player.COMMAND_SEEK_TO_NEXT)
                .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                .build()
                
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .setAvailablePlayerCommands(playerCommands)
                .setCustomLayout(listOf(getFavoriteButton(), getCloseButton()))
                .build()
        }

        override fun onCustomCommand(session: MediaSession, controller: MediaSession.ControllerInfo, customCommand: SessionCommand, args: Bundle): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                CUSTOM_COMMAND_FAVORITE -> {
                    sendCustomEvent(CUSTOM_COMMAND_FAVORITE)
                    triggerAction(ACTION_FAVORITE)
                }
                CUSTOM_COMMAND_CLOSE -> {
                    exoPlayer.stop()
                    stopSelf()
                }
                CUSTOM_COMMAND_UPDATE_FAVORITE -> {
                    isCurrentFavorite = args.getBoolean(ARG_IS_FAVORITE, false)
                    updateNotificationLayout()
                }
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    private fun sendCustomEvent(action: String) {
        mediaSession?.let { session ->
            val command = SessionCommand(action, Bundle.EMPTY)
            session.broadcastCustomCommand(command, Bundle.EMPTY)
        }
    }

    private fun getFavoriteButton(): CommandButton {
        val favoriteIcon = if (isCurrentFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        return CommandButton.Builder()
            .setDisplayName("Favorito")
            .setSessionCommand(SessionCommand(CUSTOM_COMMAND_FAVORITE, Bundle.EMPTY))
            .setIconResId(favoriteIcon)
            .setEnabled(true)
            .build()
    }

    private fun getCloseButton(): CommandButton {
        return CommandButton.Builder()
            .setDisplayName("Cerrar")
            .setSessionCommand(SessionCommand(CUSTOM_COMMAND_CLOSE, Bundle.EMPTY))
            .setIconResId(R.drawable.ic_close)
            .setEnabled(true)
            .build()
    }

    private fun updateNotificationLayout() {
        mediaSession?.setCustomLayout(listOf(getFavoriteButton(), getCloseButton()))
    }

    private fun triggerAction(action: String) {
        val intent = Intent(action).apply {
            setPackage(packageName)
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        }
        sendBroadcast(intent)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }
}
