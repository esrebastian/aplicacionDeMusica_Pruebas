package com.example.proyectopruebaappmusia1.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.proyectopruebaappmusia1.R
import com.example.proyectopruebaappmusia1.ui.BottomTab
import com.example.proyectopruebaappmusia1.ui.theme.AccentGreen
import com.example.proyectopruebaappmusia1.ui.theme.DarkGreenBg
import com.example.proyectopruebaappmusia1.ui.theme.SecondaryText

@Composable
fun MusicBottomNavigation(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit
) {
    NavigationBar(
        containerColor = DarkGreenBg,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == BottomTab.HOME,
            onClick = { onTabSelected(BottomTab.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.home)) },
            label = { Text(stringResource(R.string.home)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentGreen,
                selectedTextColor = AccentGreen,
                unselectedIconColor = SecondaryText,
                unselectedTextColor = SecondaryText,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selectedTab == BottomTab.EXPLORE,
            onClick = { onTabSelected(BottomTab.EXPLORE) },
            icon = { Icon(Icons.Default.Explore, contentDescription = stringResource(R.string.explore)) },
            label = { Text(stringResource(R.string.explore)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentGreen,
                selectedTextColor = AccentGreen,
                unselectedIconColor = SecondaryText,
                unselectedTextColor = SecondaryText,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selectedTab == BottomTab.LIBRARY,
            onClick = { onTabSelected(BottomTab.LIBRARY) },
            icon = { Icon(Icons.Default.LibraryMusic, contentDescription = stringResource(R.string.library)) },
            label = { Text(stringResource(R.string.library)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentGreen,
                selectedTextColor = AccentGreen,
                unselectedIconColor = SecondaryText,
                unselectedTextColor = SecondaryText,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selectedTab == BottomTab.FAVORITES,
            onClick = { onTabSelected(BottomTab.FAVORITES) },
            icon = { Icon(Icons.Default.Favorite, contentDescription = stringResource(R.string.favorites)) },
            label = { Text(stringResource(R.string.favorites)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentGreen,
                selectedTextColor = AccentGreen,
                unselectedIconColor = SecondaryText,
                unselectedTextColor = SecondaryText,
                indicatorColor = Color.Transparent
            )
        )
    }
}
