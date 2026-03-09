package com.example.proyectopruebaappmusia1

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
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

@SuppressLint("StaticFieldLeak")
object YouTubeManager {
    var webView: WebView? = null
    var currentUrl = mutableStateOf("https://m.youtube.com")
    
    fun getOrCreateWebView(context: Context, viewModel: MusicPlayerViewModel): WebView {
        if (webView == null) {
            webView = WebView(context.applicationContext).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    userAgentString = null 
                }
                
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        url?.let { currentUrl.value = it }
                        injectPlaybackListener()
                    }
                    
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        url?.let { currentUrl.value = it }
                        return false
                    }
                }
                
                webChromeClient = WebChromeClient()

                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onVideoPlay() {
                        post { 
                            viewModel.pause() 
                        }
                    }
                }, "Android")
                
                loadUrl(currentUrl.value)
            }
        }
        return webView!!
    }

    fun pauseVideo() {
        webView?.post {
            webView?.loadUrl("javascript:(function() { var videos = document.querySelectorAll('video'); for (var i = 0; i < videos.length; i++) { videos[i].pause(); } })()")
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
    val currentUrl by YouTubeManager.currentUrl
    
    val isVideo = currentUrl.contains("watch?v=")
    var showDownloadDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardGreenBg)
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    IconButton(onClick = { if (webView.canGoBack()) webView.goBack() }) {
                        Icon(Icons.Default.ArrowBack, "Atrás", tint = Color.White)
                    }
                    IconButton(onClick = { if (webView.canGoForward()) webView.goForward() }) {
                        Icon(Icons.Default.ArrowForward, "Adelante", tint = Color.White)
                    }
                    IconButton(onClick = { webView.reload() }) {
                        Icon(Icons.Default.Refresh, "Recargar", tint = Color.White)
                    }
                }
                
                Row {
                    IconButton(onClick = {
                        val url = webView.url
                        if (url != null) {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("URL de YouTube", url)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Enlace copiado", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.ContentCopy, "Copiar Link", tint = Color.White)
                    }
                    IconButton(onClick = {
                        val url = webView.url
                        if (url != null) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    }) {
                        Icon(Icons.Default.OpenInBrowser, "Abrir en navegador", tint = Color.White)
                    }
                }
            }

            AndroidView(
                factory = { webView },
                modifier = Modifier.fillMaxSize()
            )
        }

        if (isVideo) {
            FloatingActionButton(
                onClick = { showDownloadDialog = true },
                containerColor = AccentGreen,
                contentColor = DarkGreenBg,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(56.dp),
                shape = CircleShape
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = "Descargar")
            }
        }
    }

    if (showDownloadDialog) {
        AlertDialog(
            onDismissRequest = { showDownloadDialog = false },
            containerColor = CardGreenBg,
            title = { Text("Descargar Video", color = Color.White) },
            text = {
                Column {
                    Text("Selecciona el formato y calidad:", color = SecondaryText, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    QualityItem("MP3 - Alta Calidad", "Audio") { 
                        iniciarDescarga(context, currentUrl, "mp3")
                        showDownloadDialog = false 
                    }
                    QualityItem("MP4 - Video 720p", "Video") { 
                        iniciarDescarga(context, currentUrl, "mp4")
                        showDownloadDialog = false 
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDownloadDialog = false }) {
                    Text("CANCELAR", color = AccentGreen)
                }
            }
        )
    }
}

private fun iniciarDescarga(context: Context, url: String, formato: String) {
    // Extraer ID del video de la URL
    val videoId = url.split("v=").getOrNull(1)?.split("&")?.getOrNull(0) ?: "video"
    
    Toast.makeText(context, "Iniciando descarga de $formato...", Toast.LENGTH_SHORT).show()

    // IMPORTANTE: Esto es un ejemplo de lógica. Para que funcione realmente necesitas un extractor.
    // Aquí usamos el DownloadManager de Android.
    val request = DownloadManager.Request(Uri.parse(url)) // En un caso real aquí iría el link directo al archivo
        .setTitle("ZombieMusic Download")
        .setDescription("Descargando $videoId en formato $formato")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, "ZombieMusic_$videoId.$formato")
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(true)

    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    downloadManager.enqueue(request)
}

@Composable
fun QualityItem(label: String, type: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (type == "Audio") Icons.Default.MusicNote else Icons.Default.Videocam,
                null,
                tint = AccentGreen,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, color = Color.White, fontSize = 16.sp)
        }
    }
}

@Composable
private fun CenterMessage(msg: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = msg, color = SecondaryText)
    }
}
