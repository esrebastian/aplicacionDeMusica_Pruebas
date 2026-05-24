package com.example.proyectopruebaappmusia1.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

data class SpotifyTokenResponse(
    val access_token: String? = null
)

data class SpotifySearchResponse(
    val tracks: SpotifyTracksPage? = null
)

data class SpotifyTracksPage(
    val items: List<SpotifyTrackItem>? = null
)

data class SpotifyTrackItem(
    val id: String? = null,
    val name: String? = null,
    val external_urls: SpotifyExternalUrls? = null,
    val artists: List<SpotifyArtist>? = null,
    val album: SpotifyAlbum? = null
)

data class SpotifyExternalUrls(
    val spotify: String? = null
)

data class SpotifyArtist(
    val name: String? = null
)

data class SpotifyAlbum(
    val images: List<SpotifyImage>? = null
)

data class SpotifyImage(
    val url: String? = null
)

interface SpotifyAuthApi {
    @FormUrlEncoded
    @POST("api/token")
    suspend fun getToken(
        @Header("Authorization") authorization: String,
        @Field("grant_type") grantType: String = "client_credentials"
    ): SpotifyTokenResponse

    companion object {
        private const val BASE_URL = "https://accounts.spotify.com/"

        fun create(): SpotifyAuthApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SpotifyAuthApi::class.java)
        }
    }
}

interface SpotifySearchApi {
    @GET("v1/search")
    suspend fun searchTracks(
        @Header("Authorization") authorization: String,
        @Query("q") query: String,
        @Query("type") type: String = "track",
        @Query("limit") limit: Int = 15
    ): SpotifySearchResponse

    companion object {
        private const val BASE_URL = "https://api.spotify.com/"

        fun create(): SpotifySearchApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SpotifySearchApi::class.java)
        }
    }
}
