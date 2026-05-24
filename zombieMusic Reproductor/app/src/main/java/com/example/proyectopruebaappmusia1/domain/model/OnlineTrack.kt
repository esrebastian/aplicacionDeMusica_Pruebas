package com.example.proyectopruebaappmusia1.domain.model

data class OnlineTrack(
    val id: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String?,
    val source: String,
    val externalUrl: String
)
