package com.example.proyectopruebaappmusia1.model

data class DownloadItem(
    val id: Long,
    val title: String,
    val status: Int,
    val progress: Float,
    val bytesDownloaded: Long,
    val totalBytes: Long
)
