package com.example.proyectopruebaappmusia1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.proyectopruebaappmusia1.ui.theme.ProyectoPruebaAppMusia1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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