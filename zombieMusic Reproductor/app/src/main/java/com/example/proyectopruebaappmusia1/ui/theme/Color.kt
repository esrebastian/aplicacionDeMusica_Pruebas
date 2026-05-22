package com.example.proyectopruebaappmusia1.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class ZombieMusicColors(
    val darkGreenBg: Color,
    val cardGreenBg: Color,
    val accentGreen: Color,
    val secondaryText: Color,
    val primaryText: Color,
    val iconPlaceholder: Color
)

val DarkZombieMusicColors = ZombieMusicColors(
    darkGreenBg = Color(0xFF0D1410),
    cardGreenBg = Color(0xFF1B261F),
    accentGreen = Color(0xFFC1F153),
    secondaryText = Color(0xFF8BA08E),
    primaryText = Color.White,
    iconPlaceholder = Color(0xFF6B7E6F)
)

val LightZombieMusicColors = ZombieMusicColors(
    darkGreenBg = Color(0xFFEAF4EA),
    cardGreenBg = Color(0xFFF8FBF6),
    accentGreen = Color(0xFF4F8F2F),
    secondaryText = Color(0xFF5F7463),
    primaryText = Color(0xFF102617),
    iconPlaceholder = Color(0xFFD5E3D4)
)

val LocalZombieMusicColors = staticCompositionLocalOf { DarkZombieMusicColors }

val DarkGreenBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalZombieMusicColors.current.darkGreenBg

val CardGreenBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalZombieMusicColors.current.cardGreenBg

val AccentGreen: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalZombieMusicColors.current.accentGreen

val SecondaryText: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalZombieMusicColors.current.secondaryText

val PrimaryText: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalZombieMusicColors.current.primaryText

val IconPlaceholderColor: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalZombieMusicColors.current.iconPlaceholder
