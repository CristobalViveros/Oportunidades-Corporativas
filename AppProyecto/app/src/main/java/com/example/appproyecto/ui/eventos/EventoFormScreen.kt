package com.example.appproyecto.ui.eventos

import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appproyecto.ui.oportunidades.CampoDesplegable
import com.example.appproyecto.ui.oportunidades.CampoTexto
import com.example.appproyecto.util.copiarDesdeGaleria
import com.example.appproyecto.util.crearArchivoFoto
import com.example.appproyecto.util.formatearFecha
import com.example.appproyecto.util.uriDeArchivo
import java.io.File
import java.util.Calendar

private val TIPOS_EVENTO = listOf("Feria", "Seminario", "Aniversario", "Evento deportivo", "Otro")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventoFormScreen(
    onVolver: () -> Unit,
    viewModel: EventoFormViewModel = viewModel()
) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var rutaCamara by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(s.guardado) {
        if (s.guardado) {
            Toast.makeText(context, "Evento guardado", Toast.LENGTH_SHORT).show()
            onVolver()
        }
    }

    // RECURSO NATIVO 1: cámara (TakePicture escribe en la Uri que le entregamos).
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val ruta = rutaCamara
        if (exito && ruta != null) viewModel.fijarFoto(ruta) else ruta?.let { File(it).delete() }
    }

    // RECURSO NATIVO 2: galería (selector del sistema, no pide permisos).
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val ruta = copiarDesdeGaleria(context, uri)
            if (ruta != null) viewModel.fijarFoto(ruta)
            else Toast.makeText(context, "No se pudo leer la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo evento") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FotoEvento(
                ruta = s.fotoRuta,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val archivo = crearArchivoFoto(context)
                        rutaCamara = archivo.absolutePath
                        try {
                            camara.launch(uriDeArchivo(context, archivo))
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(context, "No hay una app de cámara disponible", Toast.LENGTH_SHORT).show()
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

            CampoTexto(
                valor = s.nombre, etiqueta = "Nombre del evento", icono = Icons.Filled.Title,
                error = s.errorNombre, onCambio = viewModel::cambiarNombre
            )

            CampoDesplegable(
                etiqueta = "Tipo de evento", icono = Icons.Filled.Category,
                textoActual = s.tipo, opciones = TIPOS_EVENTO.map { it to it },
                error = null, onElegir = viewModel::cambiarTipo
            )

            CampoDesplegable(
                etiqueta = "Organización", icono = Icons.Filled.Business,
                textoActual = s.organizaciones.firstOrNull { it.id == s.organizacionId }?.nombre ?: "",
                opciones = s.organizaciones.map { it.id to it.nombre },
                error = s.errorOrganizacion, onElegir = viewModel::elegirOrganizacion
            )

            // Selector de fecha nativo de Android.
            OutlinedButton(
                onClick = {
                    val c = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, anio, mes, dia ->
                            val elegido = Calendar.getInstance().apply {
                                set(anio, mes, dia, 0, 0, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            viewModel.fijarFecha(elegido.timeInMillis)
                        },
                        c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                Text("  " + (s.fecha?.let { formatearFecha(it) } ?: "Elegir fecha del evento"))
            }
            s.errorFecha?.let {
                Text(
                    it, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            CampoTexto(
                valor = s.comuna, etiqueta = "Comuna", icono = Icons.Filled.Place,
                error = s.errorComuna, onCambio = viewModel::cambiarComuna
            )

            CampoTexto(
                valor = s.publicoEsperado, etiqueta = "Público esperado", icono = Icons.Filled.Group,
                error = s.errorPublico, onCambio = viewModel::cambiarPublico
            )

            Button(onClick = viewModel::guardar, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Save, contentDescription = null)
                Text("  Guardar evento")
            }
        }
    }
}