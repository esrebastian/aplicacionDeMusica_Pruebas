package com.example.proyectopruebaappmusia1.service

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.common.ForwardingPlayer
import com.example.proyectopruebaappmusia1.MainActivity

@OptIn(UnstableApi::class)
class MusicService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var exoPlayer: ExoPlayer

    companion object {
        const val ACTION_NEXT = "com.example.proyectopruebaappmusia1.NEXT"
        const val ACTION_PREVIOUS = "com.example.proyectopruebaappmusia1.PREVIOUS"
    }

    override fun onCreate() {
        super.onCreate()
        
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        // Configuración del Reproductor Local optimizada
        // El parámetro 'true' en setAudioAttributes ya activa la gestión automática de foco de audio
        exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .build()

        val localForwardingPlayer = object : ForwardingPlayer(exoPlayer) {
            override fun getAvailableCommands(): Player.Commands {
                return super.getAvailableCommands().buildUpon()
                    .add(Player.COMMAND_SEEK_TO_NEXT)
                    .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                    .build()
            }
            override fun seekToNext() { 
                sendBroadcast(Intent(ACTION_NEXT).setPackage(packageName)) 
            }
            override fun seekToPrevious() { 
                sendBroadcast(Intent(ACTION_PREVIOUS).setPackage(packageName)) 
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, localForwardingPlayer)
            .setId("LocalMusicSession")
            .setSessionActivity(pendingIntent)
            .build()
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
