package com.example.proyectopruebaappmusia1

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectopruebaappmusia1.data.CobaltRequest
import com.example.proyectopruebaappmusia1.data.YouTubeDownloadApi
import com.example.proyectopruebaappmusia1.model.DownloadItem
import com.example.proyectopruebaappmusia1.viewmodel.DownloadViewModel
import com.example.proyectopruebaappmusia1.viewmodel.MusicPlayerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("StaticFieldLeak")
object WebBrowserManager {
    var generalWebView: WebView? = null
    var youtubeWebView: WebView? = null
    
    var generalUrl = mutableStateOf("https://www.google.com")
    var youtubeUrl = mutableStateOf("https://m.youtube.com")

    var isGeneralBrowserActive = mutableStateOf(false)
    
    // Contador para forzar la navegación desde fuera del WebView
    var navigationTrigger = mutableIntStateOf(0)

    fun getGeneralWebView(context: Context, viewModel: MusicPlayerViewModel): WebView {
        if (generalWebView == null) {
            generalWebView = createBaseWebView(context, viewModel) { url ->
                generalUrl.value = url ?: ""
            }
        }
        return generalWebView!!
    }

    fun getYouTubeWebView(context: Context, viewModel: MusicPlayerViewModel): WebView {
        if (youtubeWebView == null) {
            youtubeWebView = createBaseWebView(context, viewModel) { url ->
                youtubeUrl.value = url ?: ""
            }
            youtubeWebView?.loadUrl("https://m.youtube.com")
        }
        return youtubeWebView!!
    }

    private fun createBaseWebView(context: Context, viewModel: MusicPlayerViewModel, onUrlChange: (String?) -> Unit): WebView {
        return WebView(context.applicationContext).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                userAgentString = null 
            }
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    onUrlChange(url)
                    injectPlaybackListener(view)
                }
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    onUrlChange(url)
                    return false
                }
            }
            webChromeClient = WebChromeClient()
            addJavascriptInterface(object {
                @JavascriptInterface
                fun onVideoPlay() {
                    post { viewModel.pause() }
                }
            }, "Android")
        }
    }

    fun navigateToGeneral(url: String) {
        generalUrl.value = url
        isGeneralBrowserActive.value = true
        navigationTrigger.intValue++ // Incrementamos para avisar al ZombieBrowser
    }

    fun pauseVideo() = pauseAllVideos()

    fun pauseAllVideos() {
        listOf(generalWebView, youtubeWebView).forEach { wv ->
            wv?.post {
                wv.loadUrl("javascript:(function() { var videos = document.querySelectorAll('video'); for (var i = 0; i < videos.length; i++) { videos[i].pause(); } })()")
            }
        }
    }

    private fun injectPlaybackListener(wv: WebView?) {
        wv?.loadUrl("""
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
    val topTabs = listOf("Buscar", "YouTube", "Más", "Descargas", "Ajustes")
    val pagerState = rememberPagerState(pageCount = { topTabs.size })
    val scope = rememberCoroutineScope()
    val downloadViewModel: DownloadViewModel = viewModel()
    val context = LocalContext.current
    
    val isGeneralBrowserActive by WebBrowserManager.isGeneralBrowserActive

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
                0 -> {
                    if (isGeneralBrowserActive) {
                        ZombieBrowser(
                            webView = WebBrowserManager.getGeneralWebView(context, viewModel),
                            currentUrl = WebBrowserManager.generalUrl.value,
                            onClose = { WebBrowserManager.isGeneralBrowserActive.value = false }
                        )
                    } else {
                        SearchMainTab(onSearch = { query ->
                            WebBrowserManager.navigateToGeneral("https://www.google.com/search?q=$query")
                        })
                    }
                }
                1 -> {
                    ZombieBrowser(
                        webView = WebBrowserManager.getYouTubeWebView(context, viewModel),
                        currentUrl = WebBrowserManager.youtubeUrl.value,
                        onClose = { /* YouTube no se cierra */ },
                        showHomeButton = false
                    )
                }
                2 -> {
                    MoreSitesTab(onEnterSite = { url ->
                        WebBrowserManager.navigateToGeneral(url)
                        scope.launch { pagerState.animateScrollToPage(0) }
                    })
                }
                3 -> DownloadListTab(downloadViewModel)
                4 -> SettingsExplorarTab()
            }
        }
    }
}

@Composable
fun ZombieBrowser(
    webView: WebView,
    currentUrl: String,
    onClose: () -> Unit,
    showHomeButton: Boolean = true
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isVideo = currentUrl.contains("watch?v=")
    var showDownloadDialog by remember { mutableStateOf(false) }
    
    // Escuchar el disparador de navegación forzada
    val trigger by WebBrowserManager.navigationTrigger
    LaunchedEffect(trigger) {
        if (trigger > 0) {
            webView.loadUrl(currentUrl)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().background(CardGreenBg).padding(vertical = 4.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showHomeButton) {
                IconButton(onClick = onClose) { Icon(Icons.Default.Home, "Inicio", tint = AccentGreen) }
            }
            Surface(modifier = Modifier.weight(1f).height(36.dp), shape = RoundedCornerShape(18.dp), color = DarkGreenBg.copy(alpha = 0.5f)) {
                Box(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                    Text(text = currentUrl, color = SecondaryText, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            IconButton(onClick = { if (webView.canGoBack()) webView.goBack() }) { Icon(Icons.Default.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
            IconButton(onClick = { if (webView.canGoForward()) webView.goForward() }) { Icon(Icons.Default.ArrowForward, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
            IconButton(onClick = { webView.reload() }) { Icon(Icons.Default.Refresh, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
            IconButton(onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("URL", currentUrl))
                Toast.makeText(context, "Enlace copiado", Toast.LENGTH_SHORT).show()
            }) { Icon(Icons.Default.ContentCopy, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { 
                    webView.apply {
                        (parent as? ViewGroup)?.removeView(this)
                    }
                }, 
                update = { view ->
                    // Asegurar que el WebView no tenga padre anterior al ser reutilizado
                    (view.parent as? ViewGroup)?.let {
                        if (it != view.parent) it.removeView(view)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            if (isVideo) {
                FloatingActionButton(
                    onClick = { showDownloadDialog = true },
                    containerColor = AccentGreen, contentColor = DarkGreenBg,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).size(56.dp), shape = CircleShape
                ) { Icon(Icons.Default.FileDownload, null) }
            }
        }
    }
    if (showDownloadDialog) {
        DownloadDialog(onDismiss = { showDownloadDialog = false }, onDownload = { formato ->
            iniciarDescarga(context, scope, currentUrl, formato)
            showDownloadDialog = false
        })
    }
}

@Composable
private fun SearchMainTab(onSearch: (String) -> Unit) {
    var textState by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("ZombieMusic", fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = AccentGreen)
        Spacer(modifier = Modifier.height(40.dp))
        Surface(modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(28.dp), color = CardGreenBg) {
            Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, null, tint = SecondaryText)
                Spacer(modifier = Modifier.width(12.dp))
                TextField(
                    value = textState, onValueChange = { textState = it },
                    placeholder = { Text("Buscar en la web...", color = SecondaryText) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = AccentGreen, focusedTextColor = Color.White, unfocusedTextColor = Color.White
                    ),
                    singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { if (textState.isNotBlank()) onSearch(textState); focusManager.clearFocus() })
                )
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(AccentGreen).clickable { if (textState.isNotBlank()) onSearch(textState) }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ArrowForward, null, tint = DarkGreenBg)
                }
            }
        }
    }
}

@Composable
private fun MoreSitesTab(onEnterSite: (String) -> Unit) {
    val sites = listOf(
        SiteInfo("YouTube", "https://m.youtube.com", Icons.Default.PlayArrow, Color.Red),
        SiteInfo("WhatsApp Status", "https://web.whatsapp.com", Icons.Default.Chat, Color(0xFF25D366)),
        SiteInfo("Instagram", "https://www.instagram.com", Icons.Default.CameraAlt, Color(0xFFE4405F)),
        SiteInfo("Facebook", "https://m.facebook.com", Icons.Default.Facebook, Color(0xFF1877F2)),
        SiteInfo("Pinterest", "https://www.pinterest.com", Icons.Default.Pin, Color(0xFFBD081C))
    )
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(sites) { site -> MoreSiteCard(site, onEnterSite) }
    }
}

data class SiteInfo(val name: String, val url: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val iconColor: Color)

@Composable
private fun MoreSiteCard(site: SiteInfo, onEnterSite: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardGreenBg), shape = RoundedCornerShape(24.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
            Icon(imageVector = site.icon, contentDescription = null, tint = site.iconColor.copy(alpha = 0.15f), modifier = Modifier.size(110.dp).align(Alignment.CenterEnd).offset(x = 10.dp, y = 10.dp))
            Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FileDownload, null, tint = SecondaryText, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = site.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onEnterSite(site.url) },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    Text(text = "Ingresar", color = DarkGreenBg, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DownloadListTab(viewModel: DownloadViewModel) {
    val downloads by viewModel.downloads.collectAsState()
    if (downloads.isEmpty()) CenterMessage("No hay descargas activas")
    else LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(downloads, key = { it.id }) { item -> DownloadItemRow(item, onRemove = { viewModel.removeDownload(item.id) }) }
    }
}

@Composable
private fun SettingsExplorarTab() {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Ajustes de Exploración", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        SettingsOption("Limpiar historial de búsqueda", Icons.Default.History)
        SettingsOption("Bloquear anuncios (Próximamente)", Icons.Default.Block)
        SettingsOption("Modo incógnito", Icons.Default.VisibilityOff)
    }
}

@Composable
private fun SettingsOption(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = AccentGreen, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, color = Color.White, fontSize = 16.sp); Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = SecondaryText)
    }
}

@Composable
private fun DownloadDialog(onDismiss: () -> Unit, onDownload: (String) -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, containerColor = CardGreenBg, title = { Text("Descargar Video", color = Color.White) }, text = {
        Column {
            Text("Selecciona el formato y calidad:", color = SecondaryText, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
            QualityItem("MP3 - Alta Calidad", "Audio") { onDownload("mp3") }
            QualityItem("MP4 - Video 720p", "Video") { onDownload("mp4") }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("CANCELAR", color = AccentGreen) } })
}

@Composable
private fun DownloadItemRow(item: DownloadItem, onRemove: () -> Unit) {
    val animatedProgress by animateFloatAsState(targetValue = item.progress, label = "progress")
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardGreenBg), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = getStatusText(item.status), color = if (item.status == DownloadManager.STATUS_FAILED) Color.Red else SecondaryText, fontSize = 12.sp)
                }
                IconButton(onClick = onRemove) { Icon(Icons.Default.Close, null, tint = SecondaryText, modifier = Modifier.size(20.dp)) }
            }
            if (item.status == DownloadManager.STATUS_RUNNING || item.status == DownloadManager.STATUS_PAUSED || item.status == DownloadManager.STATUS_PENDING) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(progress = { animatedProgress }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape), color = AccentGreen, trackColor = DarkGreenBg)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "${(item.progress * 100).toInt()}% - ${formatBytes(item.bytesDownloaded)} / ${formatBytes(item.totalBytes)}", color = SecondaryText, fontSize = 11.sp, modifier = Modifier.align(Alignment.End))
            }
        }
    }
}

private fun getStatusText(status: Int): String = when (status) {
    DownloadManager.STATUS_PENDING -> "Pendiente..."
    DownloadManager.STATUS_RUNNING -> "Descargando..."
    DownloadManager.STATUS_PAUSED -> "Pausado"
    DownloadManager.STATUS_SUCCESSFUL -> "Completado"
    DownloadManager.STATUS_FAILED -> "Fallido"
    else -> "Desconocido"
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = listOf("B", "KB", "MB", "GB")
    val digitGroup = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.1f %s", bytes / Math.pow(1024.0, digitGroup.toDouble()), units[digitGroup])
}

private fun iniciarDescarga(context: Context, scope: CoroutineScope, url: String, formato: String) {
    val videoId = url.split("v=").getOrNull(1)?.split("&")?.getOrNull(0) ?: "video"
    Toast.makeText(context, "Obteniendo enlace de descarga...", Toast.LENGTH_SHORT).show()
    scope.launch(Dispatchers.IO) {
        try {
            val api = YouTubeDownloadApi.create()
            val requestBody = CobaltRequest(url = url, downloadMode = if (formato == "mp3") "audio" else "video", audioFormat = if (formato == "mp3") "mp3" else "best")
            val response = api.getDownloadLink(requestBody)
            withContext(Dispatchers.Main) {
                if ((response.status == "stream" || response.status == "redirect") && response.url != null) {
                    val downloadUri = Uri.parse(response.url)
                    val request = DownloadManager.Request(downloadUri).setTitle("ZombieMusic - $videoId").setDescription("Descargando en formato $formato").setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED).setDestinationInExternalPublicDir(if (formato == "mp3") Environment.DIRECTORY_MUSIC else Environment.DIRECTORY_MOVIES, "ZombieMusic_$videoId.$formato").setAllowedOverMetered(true).setAllowedOverRoaming(true)
                    (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
                    Toast.makeText(context, "Descarga iniciada", Toast.LENGTH_SHORT).show()
                } else { Toast.makeText(context, "Error: ${response.text ?: "Error desconocido"}", Toast.LENGTH_LONG).show() }
            }
        } catch (e: Exception) { withContext(Dispatchers.Main) { Toast.makeText(context, "Error de red: ${e.localizedMessage}", Toast.LENGTH_LONG).show() } }
    }
}

@Composable
fun QualityItem(label: String, type: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (type == "Audio") Icons.Default.MusicNote else Icons.Default.Videocam, null, tint = AccentGreen, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, color = Color.White, fontSize = 16.sp)
        }
    }
}

@Composable
private fun CenterMessage(msg: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text = msg, color = SecondaryText) }
}
