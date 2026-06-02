package com.example.proyectopruebaappmusia1.ui.screens

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.proyectopruebaappmusia1.ui.theme.*
import com.example.proyectopruebaappmusia1.viewmodel.ExploreViewModel
import com.example.proyectopruebaappmusia1.viewmodel.DownloadViewModel
import kotlinx.coroutines.launch

@Composable
fun ExploreScreen(
    exploreViewModel: ExploreViewModel,
    downloadViewModel: DownloadViewModel,
    modifier: Modifier = Modifier
) {
    val topTabs = listOf("Buscar", "YouTube", "Más", "Descargas")
    val pagerState = rememberPagerState(pageCount = { topTabs.size })
    val scope = rememberCoroutineScope()
    
    val isGeneralBrowserActive by exploreViewModel.isGeneralBrowserActive.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkGreenBg,
        topBar = {
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = DarkGreenBg,
                contentColor = AccentGreen,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = AccentGreen
                    )
                },
                divider = {}
            ) {
                topTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (pagerState.currentPage == index) PrimaryText else SecondaryText
                            )
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) { page ->
            when (page) {
                0 -> {
                    if (isGeneralBrowserActive) {
                        // Aquí irá el componente Browser
                        Text("Navegador General", color = PrimaryText)
                    } else {
                        // Aquí irá el componente Search
                        Text("Buscador Principal", color = PrimaryText)
                    }
                }
                1 -> Text("Navegador YouTube", color = PrimaryText)
                2 -> Text("Sitios Sugeridos", color = PrimaryText)
                3 -> Text("Lista de Descargas", color = PrimaryText)
            }
        }
    }
}
