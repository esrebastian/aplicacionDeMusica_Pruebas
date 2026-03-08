package com.example.proyectopruebaappmusia1.service

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.common.ForwardingPlayer
import com.example.proyectopruebaappmusia1.MainActivity

class MusicService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var exoPlayer: ExoPlayer

    companion object {
        const val ACTION_NEXT = "com.example.proyectopruebaappmusia1.NEXT"
        const val ACTION_PREVIOUS = "com.example.proyectopruebaappmusia1.PREVIOUS"
    }

    override fun onCreate() {
        super.onCreate()
        exoPlayer = ExoPlayer.Builder(this).build()
        
        val player = object : ForwardingPlayer(exoPlayer) {
            override fun getAvailableCommands(): Player.Commands {
                return super.getAvailableCommands().buildUpon()
                    .add(Player.COMMAND_SEEK_TO_NEXT)
                    .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                    .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                    .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                    .build()
            }

            override fun seekToNext() {
                val intent = Intent(ACTION_NEXT).setPackage(packageName)
                sendBroadcast(intent)
            }

            override fun seekToNextMediaItem() {
                val intent = Intent(ACTION_NEXT).setPackage(packageName)
                sendBroadcast(intent)
            }

            override fun seekToPrevious() {
                val intent = Intent(ACTION_PREVIOUS).setPackage(packageName)
                sendBroadcast(intent)
            }

            override fun seekToPreviousMediaItem() {
                val intent = Intent(ACTION_PREVIOUS).setPackage(packageName)
                sendBroadcast(intent)
            }
        }

        // Crear el PendingIntent para abrir la app al tocar la notificación
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
