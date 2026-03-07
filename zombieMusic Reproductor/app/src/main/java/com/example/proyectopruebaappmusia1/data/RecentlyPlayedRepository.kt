package com.example.proyectopruebaappmusia1.data

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val KEY_RECENTLY_PLAYED_IDS = "recently_played_song_ids"
private const val SEPARATOR = ","
private const val MAX_RECENT = 10

class RecentlyPlayedRepository(private val prefs: SharedPreferences) {

    private val _recentlyPlayedIds = MutableStateFlow(loadIds())
    val recentlyPlayedIds: StateFlow<List<String>> = _recentlyPlayedIds.asStateFlow()

    fun addRecentlyPlayed(songId: String) {
        val current = _recentlyPlayedIds.value.toMutableList()
        // Remove if already exists to move it to the front
        current.remove(songId)
        current.add(0, songId)
        
        // Keep only the latest MAX_RECENT
        val updated = if (current.size > MAX_RECENT) {
            current.take(MAX_RECENT)
        } else {
            current
        }
        
        saveIds(updated)
        _recentlyPlayedIds.value = updated
    }

    private fun loadIds(): List<String> {
        val raw = prefs.getString(KEY_RECENTLY_PLAYED_IDS, null) ?: return emptyList()
        return raw.split(SEPARATOR).filter { it.isNotBlank() }
    }

    private fun saveIds(ids: List<String>) {
        prefs.edit()
            .putString(KEY_RECENTLY_PLAYED_IDS, ids.joinToString(SEPARATOR))
            .apply()
    }

    companion object {
        fun create(prefs: SharedPreferences): RecentlyPlayedRepository =
            RecentlyPlayedRepository(prefs)
    }
}
