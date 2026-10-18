package com.example.appproyecto.ui.supervisor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.OportunidadItem
import com.example.appproyecto.data.model.Prioridad
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.data.model.aEnum
import com.example.appproyecto.util.formatearFecha

/** Panel del supervisor: visión global del equipo y asignación de responsables. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelSupervisorScreen(viewModel: PanelSupervisorViewModel = viewModel()) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Confirmación de éxito o mensaje de error de la última operación.
    LaunchedEffect(estado.resultado) {
        estado.resultado?.let { resultado ->
            snackbarHostState.showSnackbar(resultado.mensaje)
            viewModel.consumirResultado()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(title = { Text("Equipo y asignaciones") })

            when {
                estado.cargando -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(24.dp)
                    )
                }

                estado.oportunidades.isEmpty() -> {
                    Text(
                        text = "Aún no hay oportunidades registradas.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            ResumenEquipo(
                                ejecutivos = estado.ejecutivos,
                                oportunidades = estado.oportunidades
                            )
                        }
                        items(estado.oportunidades, key = { it.id }) { item ->
                            TarjetaAsignacion(
                                item = item,
                                onReasignar = { viewModel.abrirReasignar(item) }
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }

    estado.reasignando?.let { item ->
        DialogoReasignar(
            item = item,
            ejecutivos = estado.ejecutivos,
            onConfirmar = viewModel::confirmarReasignar,
            onCancelar = viewModel::cerrarReasignar
        )
    }
}

@Composable
private fun ResumenEquipo(ejecutivos: List<Usuario>, oportunidades: List<OportunidadItem>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Carga por ejecutivo", style = MaterialTheme.typography.titleMedium)
            ejecutivos.forEach { ejecutivo ->
                val cantidad = oportunidades.count { it.responsableId == ejecutivo.id }
                Text(
                    text = "${ejecutivo.nombre} · $cantidad oportunidades",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun TarjetaAsignacion(item: OportunidadItem, onReasignar: () -> Unit) {
    val estadoOp = item.estado.aEnum(EstadoOportunidad.EVENTO_IDENTIFICADO)
    val prioridad = item.prioridad.aEnum(Prioridad.MEDIA)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(item.nombre, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${estadoOp.etiqueta} · Prioridad ${prioridad.etiqueta}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${item.eventoNombre} · ${formatearFecha(item.eventoFecha)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Responsable: ${item.responsableNombre}",
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedButton(onClick = onReasignar) {
                Icon(Icons.Filled.SwapHoriz, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Asignar / reasignar")
            }
        }
    }
}

@Composable
private fun DialogoReasignar(
    item: OportunidadItem,
    ejecutivos: List<Usuario>,
    onConfirmar: (Long) -> Unit,
    onCancelar: () -> Unit
) {
    var seleccionado by remember(item.id) { mutableStateOf<Long?>(null) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Asignar oportunidad") },
        text = {
            Column {
                Text(item.nombre, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.padding(4.dp))
                ejecutivos.forEach { ejecutivo ->
                    val esActual = ejecutivo.id == item.responsableId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = seleccionado == ejecutivo.id,
                                enabled = !esActual,
                                role = Role.RadioButton,
                                onClick = { seleccionado = ejecutivo.id }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = seleccionado == ejecutivo.id,
                            onClick = null,
                            enabled = !esActual
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (esActual) "${ejecutivo.nombre} (actual)"
                            else "${ejecutivo.nombre} · ${ejecutivo.zona}"
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { seleccionado?.let(onConfirmar) },
                enabled = seleccionado != null
            ) { Text("Asignar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}
