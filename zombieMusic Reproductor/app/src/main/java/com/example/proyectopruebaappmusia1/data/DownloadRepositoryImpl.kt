package com.example.proyectopruebaappmusia1.data

import android.app.DownloadManager
import android.database.Cursor
import com.example.proyectopruebaappmusia1.domain.model.DownloadItem
import com.example.proyectopruebaappmusia1.domain.repository.DownloadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class DownloadRepositoryImpl(private val downloadManager: DownloadManager) : DownloadRepository {

    override fun getActiveDownloads(): Flow<List<DownloadItem>> = flow {
        while (true) {
            val query = DownloadManager.Query()
            val cursor: Cursor? = downloadManager.query(query)
            val newList = mutableListOf<DownloadItem>()

            cursor?.use {
                if (it.moveToFirst()) {
                    do {
                        val id = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_ID))
                        val title = it.getString(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE)) ?: "Desconocido"
                        val status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                        val downloaded = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        val total = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        val progress = if (total > 0) downloaded.toFloat() / total.toFloat() else 0f

                        newList.add(
                            DownloadItem(id, title, status, progress, downloaded, total)
                        )
                    } while (it.moveToNext())
                }
            }
            emit(newList)
            delay(1000) // Emitimos actualizaciones cada segundo
        }
    }.distinctUntilChanged().flowOn(Dispatchers.IO)

    override fun removeDownload(id: Long) {
        downloadManager.remove(id)
    }
}
