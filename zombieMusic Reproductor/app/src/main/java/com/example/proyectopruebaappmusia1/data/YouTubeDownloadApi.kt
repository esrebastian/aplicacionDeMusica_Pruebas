package com.example.proyectopruebaappmusia1.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class CobaltRequest(
    val url: String,
    val downloadMode: String = "audio",
    val audioFormat: String = "mp3",
    val videoQuality: String = "720"
)

data class CobaltResponse(
    val status: String? = null,
    val url: String? = null,
    val text: String? = null
)

interface YouTubeDownloadApi {
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @POST("api/json")
    suspend fun getDownloadLink(@Body request: CobaltRequest): CobaltResponse

    companion object {
        // Usando una instancia de Cobalt (puedes cambiar la URL base si tienes una propia)
        private const val BASE_URL = "https://cobalt.tools/" 

        fun create(): YouTubeDownloadApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(YouTubeDownloadApi::class.java)
        }
    }
}
