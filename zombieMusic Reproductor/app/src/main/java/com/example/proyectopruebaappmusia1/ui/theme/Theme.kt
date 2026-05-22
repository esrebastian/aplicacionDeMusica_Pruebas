package com.example.proyectopruebaappmusia1.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkZombieMusicColors.accentGreen,
    secondary = DarkZombieMusicColors.secondaryText,
    tertiary = DarkZombieMusicColors.cardGreenBg,
    background = DarkZombieMusicColors.darkGreenBg,
    surface = DarkZombieMusicColors.cardGreenBg,
    onPrimary = DarkZombieMusicColors.darkGreenBg,
    onSecondary = DarkZombieMusicColors.primaryText,
    onTertiary = DarkZombieMusicColors.primaryText,
    onBackground = DarkZombieMusicColors.primaryText,
    onSurface = DarkZombieMusicColors.primaryText
)

private val LightColorScheme = lightColorScheme(
    primary = LightZombieMusicColors.accentGreen,
    secondary = LightZombieMusicColors.secondaryText,
    tertiary = LightZombieMusicColors.cardGreenBg,
    background = LightZombieMusicColors.darkGreenBg,
    surface = LightZombieMusicColors.cardGreenBg,
    onPrimary = LightZombieMusicColors.darkGreenBg,
    onSecondary = LightZombieMusicColors.primaryText,
    onTertiary = LightZombieMusicColors.primaryText,
    onBackground = LightZombieMusicColors.primaryText,
    onSurface = LightZombieMusicColors.primaryText
)

@Composable
fun ProyectoPruebaAppMusia1Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(
        LocalZombieMusicColors provides if (darkTheme) DarkZombieMusicColors else LightZombieMusicColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
