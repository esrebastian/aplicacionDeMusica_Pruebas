package com.example.proyectopruebaappmusia1.domain.usecase

import com.example.proyectopruebaappmusia1.data.CobaltRequest
import com.example.proyectopruebaappmusia1.data.YouTubeDownloadApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetStreamingUrlUseCase(
    private val api: YouTubeDownloadApi
) {
    suspend operator fun invoke(url: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDownloadLink(CobaltRequest(url = url, audioFormat = "mp3"))
            val streamUrl = response.url ?: return@withContext Result.failure(Exception("No se obtuvo enlace de streaming"))
            Result.success(streamUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
