package com.example.proyectopruebaappmusia1.domain.model

data class DownloadItem(
    val id: Long,
    val title: String,
    val status: Int,
    val progress: Float,
    val downloadedBytes: Long,
    val totalBytes: Long
)
