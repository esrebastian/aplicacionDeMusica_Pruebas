package com.example.proyectopruebaappmusia1.ui.screens

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.ViewCompact
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.proyectopruebaappmusia1.domain.model.Song
import com.example.proyectopruebaappmusia1.ui.components.SettingIconBubble
import com.example.proyectopruebaappmusia1.ui.components.SettingsActionRow
import com.example.proyectopruebaappmusia1.ui.components.SettingsChoiceCard
import com.example.proyectopruebaappmusia1.ui.components.SettingsSectionTitle
import com.example.proyectopruebaappmusia1.ui.components.SettingsToggleRow
import com.example.proyectopruebaappmusia1.ui.theme.AccentGreen
import com.example.proyectopruebaappmusia1.ui.theme.CardGreenBg
import com.example.proyectopruebaappmusia1.ui.theme.DarkGreenBg
import com.example.proyectopruebaappmusia1.ui.theme.PrimaryText
import com.example.proyectopruebaappmusia1.ui.theme.SecondaryText
import com.example.proyectopruebaappmusia1.viewmodel.MusicPlayerViewModel

private const val SETTINGS_PREFS = "zombie_music_settings"

@Composable
fun SettingsScreen(
    viewModel: MusicPlayerViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE) }
    val minDurationFilter by viewModel.minDurationFilter.collectAsStateWithLifecycle()
    val allSongs by viewModel.playlist.collectAsStateWithLifecycle()
    val visibleSongs by viewModel.filteredLibrarySongs.collectAsStateWithLifecycle()
    val excludedSongs by viewModel.excludedLibrarySongs.collectAsStateWithLifecycle()

    SettingsContent(
        prefs = prefs,
        minDurationFilter = minDurationFilter,
        totalSongs = allSongs.size,
        visibleSongs = visibleSongs.size,
        excludedSongs = excludedSongs,
        onBack = onBack,
        onMinDurationChange = { viewModel.setMinDurationFilter(it) },
        onReloadSongs = { viewModel.loadRealSongs() },
        onClearCache = { context.cacheDir.deleteRecursively() },
        onAllowFilteredSong = { viewModel.allowSongInLibrary(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    prefs: SharedPreferences,
    minDurationFilter: Int,
    totalSongs: Int,
    visibleSongs: Int,
    excludedSongs: List<Song>,
    onBack: () -> Unit,
    onMinDurationChange: (Int) -> Unit,
    onReloadSongs: () -> Unit,
    onClearCache: () -> Unit,
    onAllowFilteredSong: (Song) -> Unit
) {
    var language by remember { mutableStateOf(prefs.getString("language", "Espanol") ?: "Espanol") }
    val useEnglish = language == "English"
    val text = remember(useEnglish) { SettingsText(useEnglish) }
    val removedSongs = (totalSongs - visibleSongs).coerceAtLeast(0)

    var theme by remember { mutableStateOf(prefs.getString("theme", text.automatic) ?: text.automatic) }
    var accentColor by remember { mutableStateOf(prefs.getString("accent_color", text.zombieGreen) ?: text.zombieGreen) }
    var textSize by remember { mutableStateOf(prefs.getString("text_size", text.normal) ?: text.normal) }
    var audioQuality by remember { mutableStateOf(prefs.getString("audio_quality", text.high) ?: text.high) }
    var animationsEnabled by remember { mutableStateOf(prefs.getBoolean("animations", true)) }
    var dynamicBackground by remember { mutableStateOf(prefs.getBoolean("dynamic_background", true)) }
    var compactDesign by remember { mutableStateOf(prefs.getBoolean("compact_design", false)) }
    var wifiDownloadsOnly by remember { mutableStateOf(prefs.getBoolean("wifi_downloads_only", true)) }
    var notificationsEnabled by remember { mutableStateOf(prefs.getBoolean("notifications", true)) }
    var privateHistory by remember { mutableStateOf(prefs.getBoolean("private_history", false)) }
    var cacheMessage by remember { mutableStateOf(text.cacheSubtitle) }
    var showExcludedSongs by remember { mutableStateOf(false) }

    fun saveString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun saveBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun themeDisplayValue(value: String): String {
        return when (value) {
            "Claro", "Light" -> text.light
            "Oscuro", "Dark" -> text.dark
            else -> text.automatic
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGreenBg)
    ) {
        TopAppBar(
            title = { Text(text.settings, color = PrimaryText, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = text.back, tint = PrimaryText)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkGreenBg)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                SettingsSummary(
                    totalSongs = totalSongs,
                    visibleSongs = visibleSongs,
                    removedSongs = removedSongs,
                    text = text,
                    onClick = { showExcludedSongs = true }
                )
            }

            item {
                SettingsSectionTitle(text.language)
                Spacer(modifier = Modifier.height(10.dp))
                SettingsChoiceCard(
                    icon = { Icon(Icons.Default.Language, null, tint = AccentGreen) },
                    title = text.appLanguage,
                    subtitle = text.languageSubtitle,
                    options = listOf("Espanol", "English"),
                    selectedOption = language,
                    onOptionSelected = {
                        language = it
                        saveString("language", it)
                    }
                )
            }

            item {
                SettingsSectionTitle(text.songFilter)
                Spacer(modifier = Modifier.height(10.dp))
                DurationFilterCard(
                    minDurationFilter = minDurationFilter,
                    removedSongs = removedSongs,
                    text = text,
                    onMinDurationChange = onMinDurationChange
                )
            }

            item {
                SettingsSectionTitle(text.playback)
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                SettingsSectionTitle(text.visualCustomization)
                Spacer(modifier = Modifier.height(10.dp))
                SettingsChoiceCard(
                    icon = { Icon(Icons.Default.Palette, null, tint = AccentGreen) },
                    title = text.theme,
                    subtitle = text.themeSubtitle,
                    options = listOf(text.light, text.dark, text.automatic),
                    selectedOption = themeDisplayValue(theme),
                    onOptionSelected = {
                        theme = it
                        saveString("theme", it)
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                SettingsSectionTitle(text.appearance)
                Spacer(modifier = Modifier.height(10.dp))
                SettingsChoiceCard(
                    icon = { Icon(Icons.Default.ColorLens, null, tint = AccentGreen) },
                    title = text.accentColor,
                    subtitle = text.accentSubtitle,
                    options = listOf(text.zombieGreen, text.lime, text.mint),
                    selectedOption = accentColor,
                    onOptionSelected = {
                        accentColor = it
                        saveString("accent_color", it)
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                SettingsChoiceCard(
                    icon = { Icon(Icons.Default.TextFields, null, tint = AccentGreen) },
                    title = text.textSize,
                    subtitle = text.textSizeSubtitle,
                    options = listOf(text.small, text.normal, text.large),
                    selectedOption = textSize,
                    onOptionSelected = {
                        textSize = it
                        saveString("text_size", it)
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                SettingsToggleRow(
                    icon = { Icon(Icons.Default.AutoAwesome, null, tint = AccentGreen) },
                    title = text.animations,
                    subtitle = if (animationsEnabled) text.animationsOn else text.animationsOff,
                    checked = animationsEnabled,
                    onCheckedChange = {
                        animationsEnabled = it
                        saveBoolean("animations", it)
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                SettingsToggleRow(
                    icon = { Icon(Icons.Default.Palette, null, tint = AccentGreen) },
                    title = text.dynamicBackground,
                    subtitle = if (dynamicBackground) text.dynamicOn else text.dynamicOff,
                    checked = dynamicBackground,
                    onCheckedChange = {
                        dynamicBackground = it
                        saveBoolean("dynamic_background", it)
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                SettingsToggleRow(
                    icon = { Icon(Icons.Default.ViewCompact, null, tint = AccentGreen) },
                    title = text.compactLayout,
                    subtitle = if (compactDesign) text.compactOn else text.compactOff,
                    checked = compactDesign,
                    onCheckedChange = {
                        compactDesign = it
                        saveBoolean("compact_design", it)
                    }
                )
            }

            item {
                SettingsSectionTitle(text.general)
                Spacer(modifier = Modifier.height(10.dp))
                SettingsChoiceCard(
                    icon = { Icon(Icons.Default.MusicNote, null, tint = AccentGreen) },
                    title = text.audioQuality,
                    subtitle = text.audioQualitySubtitle,
                    options = listOf(text.high, text.medium, text.saver),
                    selectedOption = audioQuality,
                    onOptionSelected = {
                        audioQuality = it
                        saveString("audio_quality", it)
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                SettingsToggleRow(
                    icon = { Icon(Icons.Default.Download, null, tint = AccentGreen) },
                    title = text.wifiDownloads,
                    subtitle = if (wifiDownloadsOnly) text.wifiOn else text.wifiOff,
                    checked = wifiDownloadsOnly,
                    onCheckedChange = {
                        wifiDownloadsOnly = it
                        saveBoolean("wifi_downloads_only", it)
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                SettingsActionRow(
                    icon = { Icon(Icons.Default.Cached, null, tint = AccentGreen) },
                    title = text.cache,
                    subtitle = cacheMessage,
                    actionText = text.clear,
                    onClick = {
                        onClearCache()
                        cacheMessage = text.cacheCleared
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                SettingsToggleRow(
                    icon = { Icon(Icons.Default.Notifications, null, tint = AccentGreen) },
                    title = text.notifications,
                    subtitle = if (notificationsEnabled) text.notificationsOn else text.notificationsOff,
                    checked = notificationsEnabled,
                    onCheckedChange = {
                        notificationsEnabled = it
                        saveBoolean("notifications", it)
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                SettingsToggleRow(
                    icon = { Icon(Icons.Default.PrivacyTip, null, tint = AccentGreen) },
                    title = text.basicPrivacy,
                    subtitle = if (privateHistory) text.privacyOn else text.privacyOff,
                    checked = privateHistory,
                    onCheckedChange = {
                        privateHistory = it
                        saveBoolean("private_history", it)
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                SettingsActionRow(
                    icon = { Icon(Icons.Default.Refresh, null, tint = AccentGreen) },
                    title = text.reloadLibrary,
                    subtitle = text.reloadSubtitle,
                    actionText = text.reload,
                    onClick = onReloadSongs
                )
            }
        }
    }

    if (showExcludedSongs) {
        ExcludedSongsDialog(
            excludedSongs = excludedSongs,
            minDurationFilter = minDurationFilter,
            useEnglish = useEnglish,
            onDismiss = { showExcludedSongs = false },
            onAllowSong = onAllowFilteredSong
        )
    }
}

@Composable
private fun SettingsSummary(
    totalSongs: Int,
    visibleSongs: Int,
    removedSongs: Int,
    text: SettingsText,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color(0xFF223329),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingIconBubble { Icon(Icons.Default.LibraryMusic, null, tint = AccentGreen) }
            Column(modifier = Modifier.weight(1f)) {
                Text(text.yourLibrary, color = PrimaryText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text.libraryCount(visibleSongs, totalSongs), color = SecondaryText, fontSize = 13.sp)
                Text(text.tapToExcluded, color = AccentGreen, fontSize = 12.sp)
            }
            Text("-$removedSongs", color = AccentGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DurationFilterCard(
    minDurationFilter: Int,
    removedSongs: Int,
    text: SettingsText,
    onMinDurationChange: (Int) -> Unit
) {
    var secondsText by remember(minDurationFilter) {
        mutableStateOf(if (minDurationFilter == 0) "" else minDurationFilter.toString())
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardGreenBg,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SettingIconBubble { Icon(Icons.Default.FilterAlt, null, tint = AccentGreen) }
                Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(text.excludeShortSongs, color = PrimaryText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text.excludeShortSubtitle, color = SecondaryText, fontSize = 13.sp)
                }
            }

            OutlinedTextField(
                value = secondsText,
                onValueChange = { value ->
                    val cleanValue = value.filter { it.isDigit() }.take(4)
                    secondsText = cleanValue
                    onMinDurationChange(cleanValue.toIntOrNull() ?: 0)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text.seconds, color = SecondaryText) },
                placeholder = { Text("35", color = SecondaryText) },
                suffix = { Text("s", color = AccentGreen, fontWeight = FontWeight.Bold) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PrimaryText,
                    unfocusedTextColor = PrimaryText,
                    cursorColor = AccentGreen,
                    focusedBorderColor = AccentGreen,
                    unfocusedBorderColor = SecondaryText.copy(alpha = 0.45f),
                    focusedContainerColor = DarkGreenBg,
                    unfocusedContainerColor = DarkGreenBg
                )
            )

            Text(
                text = if (minDurationFilter == 0) {
                    text.filterOff
                } else {
                    text.filterActive(removedSongs, minDurationFilter)
                },
                color = SecondaryText,
                fontSize = 13.sp
            )
        }
    }
}

private class SettingsText(private val english: Boolean) {
    val settings = if (english) "Settings" else "Ajustes"
    val back = if (english) "Back" else "Volver"
    val yourLibrary = if (english) "Your library" else "Tu biblioteca"
    val tapToExcluded = if (english) "Tap to see excluded songs" else "Toca para ver las excluidas"
    val language = if (english) "Language" else "Idioma"
    val appLanguage = if (english) "App language" else "Idioma de la aplicacion"
    val languageSubtitle = if (english) "Changes this settings screen immediately" else "Cambia esta pantalla al instante"
    val songFilter = if (english) "Song filter" else "Filtro de canciones"
    val playback = if (english) "Playback" else "Reproduccion"
    val visualCustomization = if (english) "Visual customization" else "Personalizacion visual"
    val theme = if (english) "Theme" else "Tema"
    val themeSubtitle = if (english) "Light, dark, or system default" else "Claro, oscuro o segun el sistema"
    val appearance = if (english) "Appearance" else "Apariencia"
    val accentColor = if (english) "Accent color" else "Color de acento"
    val accentSubtitle = if (english) "Defines the main control color" else "Define el color principal de controles"
    val textSize = if (english) "Text size" else "Tamano del texto"
    val textSizeSubtitle = if (english) "Adjusts lists and cards readability" else "Ajusta legibilidad en listas y tarjetas"
    val animations = if (english) "Animations" else "Animaciones"
    val animationsOn = if (english) "Transitions enabled" else "Transiciones activas"
    val animationsOff = if (english) "More direct interface" else "Interfaz mas directa"
    val dynamicBackground = if (english) "Dynamic background" else "Fondo dinamico"
    val dynamicOn = if (english) "Uses tones from album art" else "Usa tonos del arte de la cancion"
    val dynamicOff = if (english) "Fixed dark green background" else "Fondo fijo verde oscuro"
    val compactLayout = if (english) "Compact layout" else "Diseno compacto"
    val compactOn = if (english) "More songs per screen" else "Mas canciones por pantalla"
    val compactOff = if (english) "Comfortable layout with more spacing" else "Diseno comodo con mas aire"
    val general = "General"
    val audioQuality = if (english) "Audio quality" else "Calidad de audio"
    val audioQualitySubtitle = if (english) "Preference for future streaming or downloads" else "Preferencia para streaming o descargas futuras"
    val wifiDownloads = if (english) "Wi-Fi downloads" else "Descargas Wi-Fi"
    val wifiOn = if (english) "Avoids mobile data usage" else "Evita usar datos moviles"
    val wifiOff = if (english) "Allows downloads with mobile data" else "Permite descargar con datos"
    val cache = if (english) "Cache" else "Cache"
    val cacheSubtitle = if (english) "Playback and image temporary files" else "Archivos temporales de reproduccion e imagenes"
    val cacheCleared = if (english) "Cache cleared" else "Cache borrada"
    val notifications = if (english) "Notifications" else "Notificaciones"
    val notificationsOn = if (english) "Player controls visible" else "Controles del reproductor visibles"
    val notificationsOff = if (english) "Notifications disabled in internal settings" else "Notificaciones desactivadas en ajustes internos"
    val basicPrivacy = if (english) "Basic privacy" else "Privacidad basica"
    val privacyOn = if (english) "Reduces local history data" else "Reduce datos de historial local"
    val privacyOff = if (english) "Keeps recent songs for quick access" else "Guarda recientes para acceso rapido"
    val reloadLibrary = if (english) "Reload library" else "Recargar biblioteca"
    val reloadSubtitle = if (english) "Finds recent storage changes" else "Busca cambios recientes en tu almacenamiento"
    val reload = if (english) "Reload" else "Recargar"
    val clear = if (english) "Clear" else "Borrar"
    val excludeShortSongs = if (english) "Exclude short songs" else "Sacar canciones cortas"
    val excludeShortSubtitle = if (english) "Excludes songs with this duration or less" else "Excluye canciones con esa duracion o menos"
    val seconds = if (english) "Seconds" else "Segundos"
    val filterOff = if (english) "Filter off. Every song can appear." else "Filtro apagado. Todas las canciones pueden aparecer."
    val light = if (english) "Light" else "Claro"
    val dark = if (english) "Dark" else "Oscuro"
    val automatic = if (english) "Automatic" else "Automatico"
    val zombieGreen = if (english) "Zombie Green" else "Verde Zombie"
    val lime = if (english) "Lime" else "Lima"
    val mint = if (english) "Mint" else "Menta"
    val small = if (english) "Small" else "Pequeno"
    val normal = if (english) "Normal" else "Normal"
    val large = if (english) "Large" else "Grande"
    val high = if (english) "High" else "Alta"
    val medium = if (english) "Medium" else "Media"
    val saver = if (english) "Saver" else "Ahorro"

    fun libraryCount(visibleSongs: Int, totalSongs: Int): String {
        return if (english) "$visibleSongs visible of $totalSongs songs" else "$visibleSongs visibles de $totalSongs canciones"
    }

    fun filterActive(removedSongs: Int, seconds: Int): String {
        return if (english) {
            "$removedSongs songs are outside because they last $seconds seconds or less."
        } else {
            "$removedSongs canciones quedan fuera por durar $seconds segundos o menos."
        }
    }
}
