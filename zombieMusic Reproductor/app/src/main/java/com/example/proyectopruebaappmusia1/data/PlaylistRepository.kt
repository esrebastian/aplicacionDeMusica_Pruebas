package com.example.proyectopruebaappmusia1.data

import android.content.SharedPreferences
import com.example.proyectopruebaappmusia1.domain.model.Playlist
import com.example.proyectopruebaappmusia1.domain.model.Song
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val KEY_CUSTOM_PLAYLISTS = "custom_playlists_json"

class PlaylistRepository(private val prefs: SharedPreferences) {

    private val gson = Gson()
    private var songLookup: Map<String, Song> = emptyMap()
    private var fallbackSongLookup: Map<String, Song> = emptyMap()
    private var storedPlaylists: List<StoredPlaylist> = loadStoredPlaylists()
    private val _customPlaylists = MutableStateFlow(hydratePlaylists(storedPlaylists))
    val customPlaylists: StateFlow<List<Playlist>> = _customPlaylists.asStateFlow()

    private data class StoredPlaylist(
        val id: String,
        val name: String,
        val coverImage: String? = null,
        val songIds: List<String> = emptyList()
    )

    private fun loadStoredPlaylists(): List<StoredPlaylist> {
        val json = prefs.getString(KEY_CUSTOM_PLAYLISTS, null) ?: return emptyList()
        return try {
            val root = JsonParser().parse(json).asJsonArray
            val firstObject = root.firstOrNull()?.getAsJsonObject()
            if (firstObject?.has("songIds") == true) {
                val type = object : TypeToken<List<StoredPlaylist>>() {}.type
                gson.fromJson<List<StoredPlaylist>>(json, type).orEmpty()
            } else {
                val type = object : TypeToken<List<Playlist>>() {}.type
                val legacyPlaylists = gson.fromJson<List<Playlist>>(json, type).orEmpty()
                fallbackSongLookup = legacyPlaylists.flatMap { it.songs }.associateBy { it.id }
                legacyPlaylists.map { playlist ->
                    StoredPlaylist(
                        id = playlist.id,
                        name = playlist.name,
                        coverImage = playlist.coverImage,
                        songIds = playlist.songs.map { it.id }
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun savePlaylists(playlists: List<StoredPlaylist>) {
        storedPlaylists = playlists
        val json = gson.toJson(playlists)
        prefs.edit().putString(KEY_CUSTOM_PLAYLISTS, json).apply()
        _customPlaylists.value = hydratePlaylists(playlists)
    }

    private fun Song.validAlbumArt(): String? {
        return albumArt?.takeIf { it.isNotBlank() && it != "0" }
    }

    private fun hydratePlaylists(playlists: List<StoredPlaylist>): List<Playlist> {
        return playlists.map { playlist ->
            val songs = playlist.songIds.mapNotNull { id -> songLookup[id] ?: fallbackSongLookup[id] }
            Playlist(
                id = playlist.id,
                name = playlist.name,
                songCount = songs.size,
                coverImage = playlist.coverImage
                    ?.takeIf { it.isNotBlank() && it != "0" }
                    ?: songs.firstNotNullOfOrNull { it.validAlbumArt() },
                songs = songs
            )
        }
    }

    fun syncSongs(songs: List<Song>) {
        songLookup = songs.associateBy { it.id }
        _customPlaylists.value = hydratePlaylists(storedPlaylists)
    }

    private fun StoredPlaylist.withSongsAdded(songs: List<Song>): StoredPlaylist {
        val updatedIds = (songIds + songs.map { it.id }).distinct()
        return copy(
            songIds = updatedIds,
            coverImage = coverImage
                ?.takeIf { it.isNotBlank() && it != "0" }
                ?: songs.firstNotNullOfOrNull { it.validAlbumArt() }
        )
    }

    fun createPlaylist(name: String): String {
        val current = storedPlaylists.toMutableList()
        val playlistId = "custom_${System.currentTimeMillis()}"
        val newPlaylist = StoredPlaylist(
            id = playlistId,
            name = name,
            songIds = emptyList()
        )
        current.add(newPlaylist)
        savePlaylists(current)
        return playlistId
    }

    fun addSongToPlaylist(playlistId: String, song: Song) {
        addSongsToPlaylist(playlistId, listOf(song))
    }

    fun addSongsToPlaylist(playlistId: String, songs: List<Song>) {
        if (songs.isEmpty()) return
        val current = storedPlaylists.map { playlist ->
            if (playlist.id == playlistId) {
                playlist.withSongsAdded(songs)
            } else {
                playlist
            }
        }
        savePlaylists(current)
    }

    fun createPlaylistWithSong(name: String, song: Song): String {
        return createPlaylistWithSongs(name, listOf(song))
    }

    fun createPlaylistWithSongs(name: String, songs: List<Song>): String {
        val current = storedPlaylists.toMutableList()
        val playlistId = "custom_${System.currentTimeMillis()}"
        current.add(
            StoredPlaylist(
                id = playlistId,
                name = name,
                coverImage = songs.firstNotNullOfOrNull { it.validAlbumArt() },
                songIds = songs.map { it.id }.distinct()
            )
        )
        savePlaylists(current)
        return playlistId
    }

    companion object {
        fun create(prefs: SharedPreferences): PlaylistRepository = PlaylistRepository(prefs)
    }
}
