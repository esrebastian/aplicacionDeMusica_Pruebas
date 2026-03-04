package com.example.proyectopruebaappmusia1.data

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val PREF_NAME = "music_prefs"
private const val KEY_FAVORITE_IDS = "favorite_song_ids"
private const val SEPARATOR = ","

class FavoritesRepository(private val prefs: SharedPreferences) {

    private val _favoriteIds = MutableStateFlow(loadIds())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    fun isFavorite(songId: String): Boolean = songId in _favoriteIds.value

    fun toggleFavorite(songId: String) {
        val current = _favoriteIds.value.toMutableSet()
        if (songId in current) {
            current.remove(songId)
        } else {
            current.add(songId)
        }
        saveIds(current)
        _favoriteIds.value = current
    }

    fun addFavorite(songId: String) {
        val current = _favoriteIds.value.toMutableSet()
        current.add(songId)
        saveIds(current)
        _favoriteIds.value = current
    }

    fun removeFavorite(songId: String) {
        val current = _favoriteIds.value.toMutableSet()
        current.remove(songId)
        saveIds(current)
        _favoriteIds.value = current
    }

    private fun loadIds(): Set<String> {
        val raw = prefs.getString(KEY_FAVORITE_IDS, null) ?: return emptySet()
        return raw.split(SEPARATOR).filter { it.isNotBlank() }.toSet()
    }

    private fun saveIds(ids: Set<String>) {
        prefs.edit()
            .putString(KEY_FAVORITE_IDS, ids.joinToString(SEPARATOR))
            .apply()
    }

    companion object {
        fun create(prefs: SharedPreferences): FavoritesRepository =
            FavoritesRepository(prefs)
    }
}
