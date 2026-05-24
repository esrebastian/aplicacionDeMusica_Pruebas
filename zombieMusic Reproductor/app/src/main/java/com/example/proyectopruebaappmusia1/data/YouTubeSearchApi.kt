package com.example.proyectopruebaappmusia1.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class YouTubeSearchResponse(
    val items: List<YouTubeSearchItem>? = null
)

data class YouTubeSearchItem(
    val id: YouTubeVideoId? = null,
    val snippet: YouTubeSnippet? = null
)

data class YouTubeVideoId(
    val videoId: String? = null
)

data class YouTubeSnippet(
    val title: String? = null,
    val channelTitle: String? = null,
    val thumbnails: YouTubeThumbnails? = null
)

data class YouTubeThumbnails(
    val medium: YouTubeThumbnail? = null,
    val default: YouTubeThumbnail? = null
)

data class YouTubeThumbnail(
    val url: String? = null
)

interface YouTubeSearchApi {
    @GET("youtube/v3/search")
    suspend fun search(
        @Query("part") part: String = "snippet",
        @Query("type") type: String = "video",
        @Query("maxResults") maxResults: Int = 15,
        @Query("q") query: String,
        @Query("key") apiKey: String
    ): YouTubeSearchResponse

    companion object {
        private const val BASE_URL = "https://www.googleapis.com/"

        fun create(): YouTubeSearchApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(YouTubeSearchApi::class.java)
        }
    }
}
