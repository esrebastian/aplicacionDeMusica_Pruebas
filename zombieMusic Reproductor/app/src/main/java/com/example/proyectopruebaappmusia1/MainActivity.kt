package com.example.proyectopruebaappmusia1

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.proyectopruebaappmusia1.ui.theme.ProyectoPruebaAppMusia1Theme

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Si el permiso es denegado, podrías mostrar un aviso informando
        // que la notificación de música no aparecerá.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Solicitar el permiso de notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            ProyectoPruebaAppMusia1Theme {
                MusicPlayerScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MusicPreview() {
    ProyectoPruebaAppMusia1Theme {
        MusicPlayerScreen()
    }
}
