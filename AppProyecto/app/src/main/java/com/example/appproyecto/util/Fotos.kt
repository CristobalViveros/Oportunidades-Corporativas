package com.example.appproyecto.util

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.FileProvider
import java.io.File

/** Crea un archivo vacío donde se guardará la foto. */
fun crearArchivoFoto(context: Context): File {
    val carpeta = File(context.filesDir, "fotos").apply { mkdirs() }
    return File(carpeta, "evento_${System.currentTimeMillis()}.jpg")
}

/** Uri segura (content://) que la cámara usa para escribir en nuestro archivo. */
fun uriDeArchivo(context: Context, archivo: File): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)

/** La Uri de la galería es temporal: copiamos la imagen a nuestra carpeta. */
fun copiarDesdeGaleria(context: Context, uri: Uri): String? = try {
    val destino = crearArchivoFoto(context)
    context.contentResolver.openInputStream(uri)?.use { entrada ->
        destino.outputStream().use { salida -> entrada.copyTo(salida) }
    }
    destino.absolutePath
} catch (e: Exception) {
    null
}

/** Carga la foto reducida para no gastar memoria con imágenes de cámara muy grandes. */
fun cargarBitmap(ruta: String, anchoMaximo: Int = 800): ImageBitmap? {
    val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(ruta, medidas)
    var reduccion = 1
    while (medidas.outWidth / reduccion > anchoMaximo * 2) reduccion *= 2
    val opciones = BitmapFactory.Options().apply { inSampleSize = reduccion }
    return BitmapFactory.decodeFile(ruta, opciones)?.asImageBitmap()
}
