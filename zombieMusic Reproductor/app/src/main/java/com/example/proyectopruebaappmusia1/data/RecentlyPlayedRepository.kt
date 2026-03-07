package com.example.proyectopruebaappmusia1.data

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val KEY_RECENTLY_PLAYED_IDS = "recently_played_song_ids"
private const val SEPARATOR = ","
private const val MAX_RECENT = 50 // Aumentamos para soportar el "Ver todo"

class RecentlyPlayedRepository(private val prefs: SharedPreferences) {

    private val _recentlyPlayedIds = MutableStateFlow(loadIds())
    val recentlyPlayedIds: StateFlow<List<String>> = _recentlyPlayedIds.asStateFlow()

    private fun loadIds(): List<String> {
        val raw = prefs.getString(KEY_RECENTLY_PLAYED_IDS, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(SEPARATOR).filter { it.isNotBlank() }
    }

    private fun saveIds(ids: List<String>) {
        prefs.edit()
            .putString(KEY_RECENTLY_PLAYED_IDS, ids.joinToString(SEPARATOR))
            .apply()
    }

    fun addRecentlyPlayed(songId: String) {
        val current = loadIds().toMutableList()
        current.remove(songId)
        current.add(0, songId)
        
        val updated = current.take(MAX_RECENT)
        
        saveIds(updated)
        _recentlyPlayedIds.value = updated
    }

    fun removeRecentlyPlayed(songId: String) {
        val current = loadIds().toMutableList()
        if (current.remove(songId)) {
            saveIds(current)
            _recentlyPlayedIds.value = current
        }
    }

    companion object {
        fun create(prefs: SharedPreferences): RecentlyPlayedRepository =
            RecentlyPlayedRepository(prefs)
    }
}
