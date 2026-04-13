package com.example.proyectopruebaappmusia1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectopruebaappmusia1.ui.components.MusicBottomNavigation
import com.example.proyectopruebaappmusia1.ui.screens.HomeScreen
import com.example.proyectopruebaappmusia1.ui.screens.ExploreScreen
import com.example.proyectopruebaappmusia1.ui.screens.LibraryScreen
import com.example.proyectopruebaappmusia1.ui.screens.FavoritesScreen
import com.example.proyectopruebaappmusia1.ui.theme.ProyectoPruebaAppMusia1Theme
import com.example.proyectopruebaappmusia1.viewmodel.DownloadViewModel
import com.example.proyectopruebaappmusia1.viewmodel.ExploreViewModel
import com.example.proyectopruebaappmusia1.viewmodel.MusicPlayerViewModel
import com.example.proyectopruebaappmusia1.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProyectoPruebaAppMusia1Theme {
                val context = LocalContext.current
                val factory = remember { ViewModelFactory(context) }
                
                val musicViewModel: MusicPlayerViewModel = viewModel(factory = factory)
                val exploreViewModel: ExploreViewModel = viewModel(factory = factory)
                val downloadViewModel: DownloadViewModel = viewModel(factory = factory)
                
                var selectedTab by remember { mutableStateOf(BottomTab.HOME) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        MusicBottomNavigation(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it }
                        )
                    }
                ) { innerPadding ->
                    val modifier = Modifier.padding(innerPadding)
                    when (selectedTab) {
                        BottomTab.HOME -> HomeScreen(
                            viewModel = musicViewModel,
                            onHeroClick = { /* Navegar a pantalla completa si es necesario */ },
                            modifier = modifier
                        )
                        BottomTab.EXPLORE -> ExploreScreen(
                            exploreViewModel = exploreViewModel,
                            downloadViewModel = downloadViewModel,
                            modifier = modifier
                        )
                        BottomTab.LIBRARY -> LibraryScreen(
                            viewModel = musicViewModel,
                            onSettingsClick = { /* Abrir Ajustes */ },
                            modifier = modifier
                        )
                        BottomTab.FAVORITES -> FavoritesScreen(
                            viewModel = musicViewModel,
                            onSettingsClick = { /* Abrir Ajustes */ },
                            modifier = modifier
                        )
                    }
                }
            }
        }
    }
}
