package com.example.proyectopruebaappmusia1.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectopruebaappmusia1.domain.usecase.DownloadFromUrlUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExploreViewModel(
    private val downloadFromUrlUseCase: DownloadFromUrlUseCase
) : ViewModel() {

    private val _generalUrl = MutableStateFlow("https://www.google.com")
    val generalUrl = _generalUrl.asStateFlow()

    private val _youtubeUrl = MutableStateFlow("https://m.youtube.com")
    val youtubeUrl = _youtubeUrl.asStateFlow()

    private val _isGeneralBrowserActive = MutableStateFlow(false)
    val isGeneralBrowserActive = _isGeneralBrowserActive.asStateFlow()

    private val _navigationTrigger = MutableStateFlow(0)
    val navigationTrigger = _navigationTrigger.asStateFlow()

    fun updateGeneralUrl(url: String) {
        _generalUrl.value = url
    }

    fun updateYoutubeUrl(url: String) {
        _youtubeUrl.value = url
    }

    fun navigateToGeneral(url: String) {
        _generalUrl.value = url
        _isGeneralBrowserActive.value = true
        _navigationTrigger.value++
    }

    fun setGeneralBrowserActive(active: Boolean) {
        _isGeneralBrowserActive.value = active
    }

    fun startDownload(url: String, format: String) {
        viewModelScope.launch {
            downloadFromUrlUseCase(url, format)
                .onSuccess {
                    // Podrías emitir un evento de UI para mostrar un Toast de éxito
                }
                .onFailure {
                    // Podrías emitir un evento de UI para mostrar un Toast de error
                }
        }
    }
}
