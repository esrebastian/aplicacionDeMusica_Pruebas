package com.example.proyectopruebaappmusia1.util

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun rememberPermissionState(permission: String): Boolean {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }
    
    LaunchedEffect(permission) {
        hasPermission = ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    return hasPermission
}

@Composable
fun MusicPermissionLauncher(onPermissionGranted: () -> Unit) {
    val context = LocalContext.current
    val readAudioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        }
    }
    
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                readAudioPermission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            launcher.launch(readAudioPermission)
        } else {
            onPermissionGranted()
        }
    }
}
