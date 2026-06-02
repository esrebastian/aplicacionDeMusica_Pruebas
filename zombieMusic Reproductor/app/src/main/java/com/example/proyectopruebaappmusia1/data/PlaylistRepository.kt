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
            val playlists = gson.fromJson<List<Playlist>>(json, type).orEmpty()
            playlists.map { it.withNormalizedMetadata() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun savePlaylists(playlists: List<Playlist>) {
        val normalizedPlaylists = playlists.map { it.withNormalizedMetadata() }
        val json = gson.toJson(normalizedPlaylists)
        prefs.edit().putString(KEY_CUSTOM_PLAYLISTS, json).apply()
        _customPlaylists.value = normalizedPlaylists
    }

    private fun Song.validAlbumArt(): String? {
        return albumArt?.takeIf { it.isNotBlank() && it != "0" }
    }

    private fun Playlist.withNormalizedMetadata(): Playlist {
        val normalizedCover = coverImage
            ?.takeIf { it.isNotBlank() && it != "0" }
            ?: songs.firstNotNullOfOrNull { it.validAlbumArt() }

        return copy(
            songCount = songs.size,
            coverImage = normalizedCover
        )
    }

    fun createPlaylist(name: String): String {
        val current = _customPlaylists.value.toMutableList()
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
        val current = _customPlaylists.value.map { playlist ->
            if (playlist.id == playlistId) {
                val updatedSongs = playlist.songs.toMutableList()
                if (!updatedSongs.any { it.id == song.id }) {
                    updatedSongs.add(song)
                }
                playlist.copy(songs = updatedSongs).withNormalizedMetadata()
            } else {
                playlist
            }
        }
        savePlaylists(current)
    }

    fun createPlaylistWithSong(name: String, song: Song): String {
        val current = _customPlaylists.value.toMutableList()
        val playlistId = "custom_${System.currentTimeMillis()}"
        current.add(
            Playlist(
                id = playlistId,
                name = name,
                songCount = 1,
                coverImage = song.validAlbumArt(),
                songs = listOf(song)
            )
        )
        savePlaylists(current)
        return playlistId
    }

    companion object {
        fun create(prefs: SharedPreferences): PlaylistRepository = PlaylistRepository(prefs)
    }
}
