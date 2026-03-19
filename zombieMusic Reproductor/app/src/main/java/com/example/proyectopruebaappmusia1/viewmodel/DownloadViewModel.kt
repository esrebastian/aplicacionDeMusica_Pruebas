package com.example.proyectopruebaappmusia1.viewmodel

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectopruebaappmusia1.model.DownloadItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DownloadViewModel(application: Application) : AndroidViewModel(application) {
    private val downloadManager = application.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    
    private val _downloads = MutableStateFlow<List<DownloadItem>>(emptyList())
    val downloads: StateFlow<List<DownloadItem>> = _downloads

    init {
        updateDownloads()
    }

    private fun updateDownloads() {
        viewModelScope.launch {
            while (true) {
                val query = DownloadManager.Query()
                val cursor: Cursor? = downloadManager.query(query)
                val newList = mutableListOf<DownloadItem>()
                
                cursor?.use {
                    if (it.moveToFirst()) {
                        do {
                            val id = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_ID))
                            val title = it.getString(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE))
                            val status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                            val downloaded = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            val total = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                            val progress = if (total > 0) downloaded.toFloat() / total.toFloat() else 0f
                            
                            newList.add(DownloadItem(id, title, status, progress, downloaded, total))
                        } while (it.moveToNext())
                    }
                }
                _downloads.value = newList
                delay(1000) // Actualizar cada segundo
            }
        }
    }

    fun removeDownload(id: Long) {
        downloadManager.remove(id)
    }
}
