package com.example.appproyecto

import android.app.Application
import com.example.appproyecto.data.local.AppDatabase
import com.example.appproyecto.data.local.sembrarDatosDemo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppProyectoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            sembrarDatosDemo(AppDatabase.getInstance(this@AppProyectoApp))
        }
    }
}