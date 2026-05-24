package com.example.proyectopruebaappmusia1.data

import android.util.Base64
import com.example.proyectopruebaappmusia1.BuildConfig
import com.example.proyectopruebaappmusia1.domain.model.OnlineTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OnlineMusicSearchRepository(
    private val youtubeApi: YouTubeSearchApi = YouTubeSearchApi.create(),
    private val spotifyAuthApi: SpotifyAuthApi = SpotifyAuthApi.create(),
    private val spotifySearchApi: SpotifySearchApi = SpotifySearchApi.create()
) {

    suspend fun search(query: String): Result<List<OnlineTrack>> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@withContext Result.success(emptyList())

        val youtubeKey = BuildConfig.YOUTUBE_API_KEY.trim()
        if (youtubeKey.isNotEmpty()) {
            searchYouTube(trimmed, youtubeKey)?.let { return@withContext Result.success(it) }
        }

        val spotifyClientId = BuildConfig.SPOTIFY_CLIENT_ID.trim()
        val spotifyClientSecret = BuildConfig.SPOTIFY_CLIENT_SECRET.trim()
        if (spotifyClientId.isNotEmpty() && spotifyClientSecret.isNotEmpty()) {
            searchSpotify(trimmed, spotifyClientId, spotifyClientSecret)?.let {
                return@withContext Result.success(it)
            }
        }

        Result.failure(
            IllegalStateException(
                "Agrega YOUTUBE_API_KEY o SPOTIFY_CLIENT_ID y SPOTIFY_CLIENT_SECRET en local.properties"
            )
        )
    }

    private suspend fun searchYouTube(query: String, apiKey: String): List<OnlineTrack>? {
        return try {
            val response = youtubeApi.search(query = query, apiKey = apiKey)
            response.items.orEmpty().mapNotNull { item ->
                val videoId = item.id?.videoId ?: return@mapNotNull null
                val snippet = item.snippet ?: return@mapNotNull null
                OnlineTrack(
                    id = videoId,
                    title = snippet.title.orEmpty(),
                    artist = snippet.channelTitle.orEmpty(),
                    thumbnailUrl = snippet.thumbnails?.medium?.url
                        ?: snippet.thumbnails?.default?.url,
                    source = "YouTube",
                    externalUrl = "https://www.youtube.com/watch?v=$videoId"
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun searchSpotify(
        query: String,
        clientId: String,
        clientSecret: String
    ): List<OnlineTrack>? {
        return try {
            val credentials = Base64.encodeToString(
                "$clientId:$clientSecret".toByteArray(),
                Base64.NO_WRAP
            )
            val token = spotifyAuthApi.getToken(authorization = "Basic $credentials")
                .access_token ?: return null

            val response = spotifySearchApi.searchTracks(
                authorization = "Bearer $token",
                query = query
            )

            response.tracks?.items.orEmpty().mapNotNull { item ->
                val trackId = item.id ?: return@mapNotNull null
                OnlineTrack(
                    id = trackId,
                    title = item.name.orEmpty(),
                    artist = item.artists.orEmpty().joinToString(", ") { it.name.orEmpty() },
                    thumbnailUrl = item.album?.images?.firstOrNull()?.url,
                    source = "Spotify",
                    externalUrl = item.external_urls?.spotify
                        ?: "https://open.spotify.com/track/$trackId"
                )
            }
        } catch (_: Exception) {
            null
        }
    }
}
