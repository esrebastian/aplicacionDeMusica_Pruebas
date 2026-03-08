package com.example.proyectopruebaappmusia1

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.proyectopruebaappmusia1.viewmodel.MusicPlayerViewModel
import kotlinx.coroutines.launch

private val DarkGreenBg = Color(0xFF0D1410)
private val CardGreenBg = Color(0xFF1B261F)
private val AccentGreen = Color(0xFFC1F153)
private val SecondaryText = Color(0xFF8BA08E)

@SuppressLint("StaticFieldLeak")
object YouTubeManager {
    var webView: WebView? = null
    
    fun getOrCreateWebView(context: android.content.Context, viewModel: MusicPlayerViewModel): WebView {
        if (webView == null) {
            webView = WebView(context.applicationContext).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        injectPlaybackListener()
                    }
                    override fun onLoadResource(view: WebView?, url: String?) {
                        injectPlaybackListener()
                    }
                }
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onVideoPlay() {
                        post { viewModel.pause() }
                    }
                }, "Android")
                loadUrl("https://www.youtube.com")
            }
        }
        return webView!!
    }

    fun pauseVideo() {
        webView?.post {
            webView?.loadUrl("javascript:(function() { " +
                    "var videos = document.querySelectorAll('video'); " +
                    "for(var i=0; i<videos.length; i++) { videos[i].pause(); } " +
                    "})()")
        }
    }

    private fun injectPlaybackListener() {
        webView?.loadUrl("""
            javascript:(function() {
                var videos = document.querySelectorAll('video');
                for (var i = 0; i < videos.length; i++) {
                    if (!videos[i].getAttribute('data-listener-added')) {
                        videos[i].setAttribute('data-listener-added', 'true');
                        videos[i].addEventListener('play', function() {
                            Android.onVideoPlay();
                        });
                    }
                }
            })()
        """.trimIndent())
    }
}

@Composable
fun pantallaDeExplorar(
    modifier: Modifier = Modifier,
    viewModel: MusicPlayerViewModel
) {
    val topTabs = listOf("Buscar", "YouTube", "Descargas", "Ajustes")
    val pagerState = rememberPagerState(pageCount = { topTabs.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkGreenBg,
        topBar = {
            Column(modifier = Modifier.background(DarkGreenBg)) {
                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = Color.Transparent,
                    contentColor = AccentGreen,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        if (pagerState.currentPage < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                color = AccentGreen
                            )
                        }
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
                                    color = if (pagerState.currentPage == index) Color.White else SecondaryText
                                )
                            }
                        )
                    }
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
                0 -> SearchMainTab()
                1 -> YouTubeTab(viewModel)
                2 -> CenterMessage("Lista de Descargas")
                3 -> CenterMessage("Configuración")
            }
        }
    }
}

@Composable
private fun SearchMainTab() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("ZombieMusic", fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = AccentGreen)
        Spacer(modifier = Modifier.height(40.dp))
        Surface(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            color = CardGreenBg
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FileDownload, null, tint = SecondaryText)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Buscar en YouTube...", color = SecondaryText, modifier = Modifier.weight(1f))
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(AccentGreen), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Search, "Buscar", tint = DarkGreenBg)
                }
            }
        }
    }
}

@Composable
private fun YouTubeTab(viewModel: MusicPlayerViewModel) {
    val context = LocalContext.current
    val webView = remember { YouTubeManager.getOrCreateWebView(context, viewModel) }

    AndroidView(
        factory = { webView },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun CenterMessage(msg: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = msg, color = SecondaryText)
    }
}
