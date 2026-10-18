package com.example.appproyecto.ui.eventos

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.example.appproyecto.util.cargarBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun FotoEvento(ruta: String?, modifier: Modifier = Modifier) {
    // La imagen se decodifica en un hilo de fondo para no congelar la pantalla.
    val imagen by produceState<ImageBitmap?>(initialValue = null, key1 = ruta) {
        value = if (ruta == null) null else withContext(Dispatchers.IO) { cargarBitmap(ruta) }
    }

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = imagen
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Foto del evento",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        } else {
            Icon(Icons.Filled.Photo, contentDescription = "Sin foto")
        }
    }
}