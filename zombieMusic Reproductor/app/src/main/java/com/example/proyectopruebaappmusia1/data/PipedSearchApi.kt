package com.example.proyectopruebaappmusia1.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class PipedSearchItem(
    val url: String? = null,
    val title: String? = null,
    val uploaderName: String? = null,
    val thumbnail: String? = null,
    val type: String? = null
)

interface PipedSearchApi {
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("filter") filter: String = "music_videos"
    ): PipedSearchResponse

    companion object {
        // Usamos una instancia pública de Piped
        private const val BASE_URL = "https://pipedapi.kavin.rocks/"

        fun create(): PipedSearchApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PipedSearchApi::class.java)
        }
    }
}

data class PipedSearchResponse(
    val items: List<PipedSearchItem>? = null
)
