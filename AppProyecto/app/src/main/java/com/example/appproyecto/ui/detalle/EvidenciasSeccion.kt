package com.example.appproyecto.ui.detalle

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appproyecto.data.model.EvidenciaItem
import com.example.appproyecto.ui.eventos.FotoEvento
import com.example.appproyecto.ui.oportunidades.CampoTexto
import com.example.appproyecto.util.copiarDesdeGaleria
import com.example.appproyecto.util.crearArchivoFoto
import com.example.appproyecto.util.formatearFechaHora
import com.example.appproyecto.util.uriDeArchivo
import java.io.File

/**
 * Evidencias fotográficas de la oportunidad: tomar o elegir una foto, ver una vista previa antes de
 * guardarla y consultar las anteriores. Cada foto queda asociada a la oportunidad en la base de datos.
 * Usa su propio ViewModel (comparte el argumento "id" de la pantalla de detalle).
 */
@Composable
fun SeccionEvidencias(
    onMensaje: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EvidenciasViewModel = viewModel()
) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var rutaCamara by rememberSaveable { mutableStateOf<String?>(null) }
    var ampliada by remember { mutableStateOf<EvidenciaItem?>(null) }

    LaunchedEffect(s.resultado) {
        s.resultado?.let {
            onMensaje(it.mensaje)
            viewModel.consumirResultado()
        }
    }

    // Cámara: TakePicture escribe en la Uri de nuestro archivo; si el usuario cancela, se borra el archivo vacío.
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val ruta = rutaCamara
        rutaCamara = null
        if (exito && ruta != null) viewModel.prepararFoto(ruta)
        else ruta?.let { File(it).delete() }
    }

    // Galería: selector del sistema (no pide permisos); la imagen se copia a la carpeta de la app.
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val ruta = copiarDesdeGaleria(context, uri)
            if (ruta != null) viewModel.prepararFoto(ruta) else onMensaje("No se pudo leer la imagen")
        }
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Evidencias fotográficas (${s.evidencias.size})",
            style = MaterialTheme.typography.titleMedium
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    val archivo = crearArchivoFoto(context)
                    rutaCamara = archivo.absolutePath
                    try {
                        camara.launch(uriDeArchivo(context, archivo))
                    } catch (e: ActivityNotFoundException) {
                        archivo.delete()
                        rutaCamara = null
                        onMensaje("No hay una app de cámara disponible")
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null)
                Text("  Cámara")
            }
            OutlinedButton(
                onClick = {
                    galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                Text("  Galería")
            }
        }

        if (s.evidencias.isEmpty()) {
            Text(
                "Aún no hay fotografías. Toma una para dejar respaldo de esta oportunidad.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(s.evidencias, key = { it.id }) { evidencia ->
                    Card(
                        modifier = Modifier
                            .width(160.dp)
                            .clickable { ampliada = evidencia }
                    ) {
                        FotoEvento(
                            ruta = evidencia.rutaFoto,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                        )
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                formatearFechaHora(evidencia.fecha),
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                evidencia.descripcion.ifBlank { evidencia.usuarioNombre },
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Vista previa de la foto recién tomada: se confirma o se descarta.
    s.fotoPendiente?.let { ruta ->
        AlertDialog(
            onDismissRequest = { if (!s.guardando) viewModel.cancelarFoto() },
            title = { Text("Nueva evidencia") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FotoEvento(
                        ruta = ruta,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    CampoTexto(
                        valor = s.descripcion,
                        etiqueta = "Descripción (opcional)",
                        icono = Icons.Filled.Chat,
                        error = s.errorDescripcion,
                        maximo = 100,
                        onCambio = viewModel::cambiarDescripcion
                    )
                }
            },
            confirmButton = {
                Button(onClick = viewModel::guardar, enabled = !s.guardando) {
                    if (s.guardando) CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.height(20.dp))
                    else Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelarFoto, enabled = !s.guardando) { Text("Descartar") }
            }
        )
    }

    // Foto ampliada.
    ampliada?.let { evidencia ->
        AlertDialog(
            onDismissRequest = { ampliada = null },
            title = { Text(formatearFechaHora(evidencia.fecha)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FotoEvento(
                        ruta = evidencia.rutaFoto,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    if (evidencia.descripcion.isNotBlank()) Text(evidencia.descripcion)
                    Text(
                        "Registrada por ${evidencia.usuarioNombre}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = { TextButton(onClick = { ampliada = null }) { Text("Cerrar") } }
        )
    }
}
