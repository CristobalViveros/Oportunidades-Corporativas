package com.example.appproyecto.ui.oportunidades

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.Prioridad

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OportunidadFormScreen(
    onVolver: () -> Unit,
    viewModel: OportunidadFormViewModel = viewModel()
) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Feedback: al guardar, mostramos un mensaje y volvemos a la lista.
    LaunchedEffect(s.guardado) {
        if (s.guardado) {
            Toast.makeText(context, "Oportunidad guardada", Toast.LENGTH_SHORT).show()
            onVolver()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (s.esEdicion) "Editar oportunidad" else "Nueva oportunidad") },
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
            CampoTexto(
                valor = s.nombre, etiqueta = "Nombre de la oportunidad",
                icono = Icons.Filled.Title, error = s.errorNombre,
                onCambio = viewModel::cambiarNombre
            )

            CampoDesplegable(
                etiqueta = "Evento",
                icono = Icons.Filled.Event,
                textoActual = s.eventos.firstOrNull { it.id == s.eventoId }?.nombre ?: "",
                opciones = s.eventos.map { it.id to it.nombre },
                error = s.errorEvento,
                onElegir = viewModel::elegirEvento
            )

            CampoDesplegable(
                etiqueta = "Prioridad",
                icono = Icons.Filled.Flag,
                textoActual = s.prioridad.etiqueta,
                opciones = Prioridad.entries.map { it to it.etiqueta },
                error = null,
                onElegir = viewModel::elegirPrioridad
            )

            CampoDesplegable(
                etiqueta = "Estado",
                icono = Icons.Filled.Timeline,
                textoActual = s.estado.etiqueta,
                opciones = EstadoOportunidad.entries.map { it to it.etiqueta },
                error = null,
                onElegir = viewModel::elegirEstado
            )

            CampoTexto(
                valor = s.descripcion, etiqueta = "Descripción (opcional)",
                icono = Icons.Filled.Description, error = s.errorDescripcion,
                maximo = 300, lineas = 3, onCambio = viewModel::cambiarDescripcion
            )

            CampoTexto(
                valor = s.observaciones, etiqueta = "Observaciones (opcional)",
                icono = Icons.Filled.Notes, error = s.errorObservaciones,
                maximo = 300, lineas = 3, onCambio = viewModel::cambiarObservaciones
            )

            Button(onClick = viewModel::guardar, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Save, contentDescription = null)
                Text("  Guardar")
            }
        }
    }
}

@Composable
fun CampoTexto(
    valor: String,
    etiqueta: String,
    icono: ImageVector,
    error: String?,
    onCambio: (String) -> Unit,
    maximo: Int? = null,
    lineas: Int = 1
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(icono, contentDescription = null) },
        trailingIcon = {
            AnimatedVisibility(visible = error != null) {
                Icon(Icons.Filled.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
            }
        },
        isError = error != null,
        supportingText = {
            when {
                error != null -> Text(error)
                maximo != null -> Text("${valor.length}/$maximo")
            }
        },
        minLines = lineas,
        singleLine = lineas == 1,
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> CampoDesplegable(
    etiqueta: String,
    icono: ImageVector,
    textoActual: String,
    opciones: List<Pair<T, String>>,
    error: String?,
    onElegir: (T) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = abierto, onExpandedChange = { abierto = it }) {
        OutlinedTextField(
            value = textoActual,
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            leadingIcon = { Icon(icono, contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = abierto) },
            isError = error != null,
            supportingText = { if (error != null) Text(error) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            opciones.forEach { (valor, texto) ->
                DropdownMenuItem(
                    text = { Text(texto) },
                    onClick = {
                        onElegir(valor)
                        abierto = false
                    }
                )
            }
        }
    }
}

