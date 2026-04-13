package com.example.proyectopruebaappmusia1.domain.usecase

import com.example.proyectopruebaappmusia1.data.CobaltRequest
import com.example.proyectopruebaappmusia1.data.YouTubeDownloadApi
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DownloadFromUrlUseCase(
    private val api: YouTubeDownloadApi,
    private val context: Context
) {
    suspend operator fun invoke(url: String, format: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDownloadLink(CobaltRequest(url = url, audioFormat = format))
            val downloadUrl = response.url ?: return@withContext Result.failure(Exception("No se obtuvo enlace"))
            
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(Uri.parse(downloadUrl))
                .setTitle("Descargando de ZombieMusic")
                .setDescription(url)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "ZombieMusic_${System.currentTimeMillis()}.$format")
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            downloadManager.enqueue(request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
