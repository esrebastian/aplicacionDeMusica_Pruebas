package com.example.proyectopruebaappmusia1.domain.repository

import com.example.proyectopruebaappmusia1.domain.model.DownloadItem
import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    fun getActiveDownloads(): Flow<List<DownloadItem>>
    fun removeDownload(id: Long)
}
