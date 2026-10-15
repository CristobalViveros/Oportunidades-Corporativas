package com.example.appproyecto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.appproyecto.ui.navigation.Navegacion
import com.example.appproyecto.ui.theme.AppProyectoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppProyectoTheme {
                Navegacion()
            }
        }
    }
}