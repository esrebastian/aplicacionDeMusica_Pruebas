package com.example.proyectopruebaappmusia1.data

import android.content.SharedPreferences
import com.example.proyectopruebaappmusia1.domain.model.Playlist
import com.example.proyectopruebaappmusia1.domain.model.Song
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val KEY_CUSTOM_PLAYLISTS = "custom_playlists_json"

class PlaylistRepository(private val prefs: SharedPreferences) {

    private val gson = Gson()
    private val _customPlaylists = MutableStateFlow<List<Playlist>>(loadPlaylists())
    val customPlaylists: StateFlow<List<Playlist>> = _customPlaylists.asStateFlow()

    private fun loadPlaylists(): List<Playlist> {
        val json = prefs.getString(KEY_CUSTOM_PLAYLISTS, null) ?: return emptyList()
        val type = object : TypeToken<List<Playlist>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun savePlaylists(playlists: List<Playlist>) {
        val json = gson.toJson(playlists)
        prefs.edit().putString(KEY_CUSTOM_PLAYLISTS, json).apply()
        _customPlaylists.value = playlists
    }

    fun createPlaylist(name: String): String {
        val current = loadPlaylists().toMutableList()
        val playlistId = "custom_${System.currentTimeMillis()}"
        val newPlaylist = Playlist(
            id = playlistId,
            name = name,
            songCount = 0,
            songs = emptyList()
        )
        current.add(newPlaylist)
        savePlaylists(current)
        return playlistId
    }

    fun addSongToPlaylist(playlistId: String, song: Song) {
        val current = loadPlaylists().map { playlist ->
            if (playlist.id == playlistId) {
                val updatedSongs = playlist.songs.toMutableList()
                if (!updatedSongs.any { it.id == song.id }) {
                    updatedSongs.add(song)
                }
                playlist.copy(songs = updatedSongs, songCount = updatedSongs.size)
            } else {
                playlist
            }
        }
        savePlaylists(current)
    }

    fun createPlaylistWithSong(name: String, song: Song): String {
        val playlistId = createPlaylist(name)
        addSongToPlaylist(playlistId, song)
        return playlistId
    }

    companion object {
        fun create(prefs: SharedPreferences): PlaylistRepository = PlaylistRepository(prefs)
    }
}
