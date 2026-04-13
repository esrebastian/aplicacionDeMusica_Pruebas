package com.example.proyectopruebaappmusia1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectopruebaappmusia1.domain.model.DownloadItem
import com.example.proyectopruebaappmusia1.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DownloadViewModel(
    private val repository: DownloadRepository
) : ViewModel() {

    // El ViewModel ahora es reactivo: observa el repositorio y expone el estado
    val downloads: StateFlow<List<DownloadItem>> = repository.getActiveDownloads()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun removeDownload(id: Long) {
        repository.removeDownload(id)
    }
}
