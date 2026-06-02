package com.example.proyectopruebaappmusia1.data

import android.content.SharedPreferences
import com.example.proyectopruebaappmusia1.domain.repository.RecentlyPlayedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val KEY_RECENTLY_PLAYED_IDS = "recently_played_song_ids"
private const val SEPARATOR = ","
private const val MAX_RECENT = 60

class RecentlyPlayedRepositoryImpl(private val prefs: SharedPreferences) : RecentlyPlayedRepository {

    private val _recentlyPlayedIds = MutableStateFlow(loadIds())
    override val recentlyPlayedIds: StateFlow<List<String>> = _recentlyPlayedIds.asStateFlow()

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

    override fun addRecentlyPlayed(songId: String) {
        val current = _recentlyPlayedIds.value.toMutableList()
        current.remove(songId)
        current.add(0, songId)
        
        val updated = current.take(MAX_RECENT)
        
        saveIds(updated)
        _recentlyPlayedIds.value = updated
    }

    override fun removeRecentlyPlayed(songId: String) {
        val current = _recentlyPlayedIds.value.toMutableList()
        if (current.remove(songId)) {
            saveIds(current)
            _recentlyPlayedIds.value = current
        }
    }
}
