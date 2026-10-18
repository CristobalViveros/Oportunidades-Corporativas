package com.example.appproyecto.ui.detalle

import androidx.compose.animation.animateContentSize
import com.example.appproyecto.ui.componentes.InsigniaEstado
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.EventAvailable
import com.example.appproyecto.ui.componentes.rememberLanzadorCalendario
import com.example.appproyecto.util.Calendario
import kotlinx.coroutines.launch
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.OportunidadDetalle
import com.example.appproyecto.data.model.Perfil
import com.example.appproyecto.data.model.Prioridad
import com.example.appproyecto.data.model.SeguimientoItem
import com.example.appproyecto.data.model.TipoAccion
import com.example.appproyecto.data.model.TipoOportunidad
import com.example.appproyecto.data.model.aEnum
import com.example.appproyecto.ui.eventos.FotoEvento
import com.example.appproyecto.util.estaAtrasado
import com.example.appproyecto.util.formatearFecha
import com.example.appproyecto.util.formatearFechaHora

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleOportunidadScreen(
    onVolver: () -> Unit,
    onEditar: (Long) -> Unit,
    onRegistrarGestion: (Long) -> Unit,
    viewModel: DetalleOportunidadViewModel = viewModel()
) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val detalle = s.detalle
    val scope = rememberCoroutineScope()
    val abrirCalendario = rememberLanzadorCalendario { mensaje ->
        scope.launch { snackbarHostState.showSnackbar(mensaje) }
    }

    // Confirmación de éxito o mensaje de error de la última operación.
    LaunchedEffect(s.resultado) {
        s.resultado?.let {
            snackbarHostState.showSnackbar(it.mensaje)
            viewModel.consumirResultado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de oportunidad") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (detalle != null) {
                        IconButton(onClick = { onEditar(detalle.id) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar oportunidad")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            s.cargando -> Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            detalle == null -> Box(
                modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No tienes acceso a esta oportunidad o ya no existe.",
                    color = MaterialTheme.colorScheme.error
                )
            }

            else -> LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Encabezado(detalle) }

                s.proximo?.fechaProximoSeguimiento?.let { fecha ->
                    item {
                        val accion = s.proximo?.proximaAccion.orEmpty()
                        Column {
                            AvisoProximo(fecha = fecha, accion = accion)
                            // Un recordatorio en el pasado no sirve: solo se ofrece si aún no vence.
                            if (!estaAtrasado(fecha)) {
                                TextButton(onClick = {
                                    abrirCalendario(
                                        Calendario.intentSeguimiento(
                                            titulo = "Seguimiento: ${accion.ifBlank { detalle.nombre }}",
                                            descripcion = "Oportunidad: ${detalle.nombre}\n" +
                                                "Organización: ${detalle.organizacionNombre}\n" +
                                                "Responsable: ${detalle.responsableNombre}",
                                            fechaLocal = fecha
                                        )
                                    )
                                }) {
                                    Icon(Icons.Filled.EditCalendar, contentDescription = null)
                                    Text("  Agregar recordatorio al calendario")
                                }
                            }
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = viewModel::abrirDialogoEstado,
                            enabled = s.estadosDestino.isNotEmpty(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.SwapHoriz, contentDescription = null)
                            Text("  Cambiar estado")
                        }
                        OutlinedButton(
                            onClick = { onRegistrarGestion(detalle.id) },
                            enabled = s.puedeRegistrarGestion,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.AddComment, contentDescription = null)
                            Text("  Registrar gestión")
                        }
                    }
                }

                if (s.estadosDestino.isEmpty()) {
                    item {
                        Text(
                            "Esta oportunidad está en un estado final: no admite más cambios.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (s.rol == Perfil.EJECUTIVO) {
                    item {
                        Text(
                            "Confirmar o descartar una oportunidad lo decide el supervisor.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                item { TarjetaEvento(detalle) }
                item {
                    OutlinedButton(
                        onClick = {
                            abrirCalendario(
                                Calendario.intentEvento(
                                    titulo = detalle.eventoNombre,
                                    descripcion = "Oportunidad: ${detalle.nombre}\n" +
                                        "Organización: ${detalle.organizacionNombre}" +
                                        if (detalle.horario.isNotBlank()) "\nHorario: ${detalle.horario}" else "",
                                    ubicacion = listOf(detalle.direccion, detalle.comuna, detalle.region)
                                        .filter { it.isNotBlank() }.joinToString(", "),
                                    inicioLocal = detalle.fechaInicio,
                                    terminoLocal = detalle.fechaTermino
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.EventAvailable, contentDescription = null)
                        Text("  Agregar evento al calendario")
                    }
                }
                item { TarjetaOrganizacion(detalle) }

                detalle.fotoUri?.let { ruta ->
                    item {
                        FotoEvento(
                            ruta = ruta,
                            modifier = Modifier.fillMaxWidth().height(180.dp)
                        )
                    }
                }

                item {
                    SeccionEvidencias(
                        onMensaje = { mensaje -> scope.launch { snackbarHostState.showSnackbar(mensaje) } }
                    )
                }

                item {
                    Text(
                        "Historial de gestiones (${s.historial.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                if (s.historial.isEmpty()) {
                    item {
                        Text(
                            "Aún no hay gestiones registradas.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(s.historial, key = { it.id }) { TarjetaGestion(it) }
                }
            }
        }
    }

    if (s.dialogoEstado && detalle != null) {
        DialogoCambiarEstado(
            estadoActual = detalle.estado.aEnum(EstadoOportunidad.EVENTO_IDENTIFICADO),
            destinos = s.estadosDestino,
            onConfirmar = viewModel::cambiarEstado,
            onCancelar = viewModel::cerrarDialogoEstado
        )
    }
}

@Composable
private fun Encabezado(d: OportunidadDetalle) {
    val estado = d.estado.aEnum(EstadoOportunidad.EVENTO_IDENTIFICADO)
    val prioridad = d.prioridad.aEnum(Prioridad.MEDIA)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(d.nombre, style = MaterialTheme.typography.titleLarge)
            InsigniaEstado(estado)
            Text(
                "Prioridad ${prioridad.etiqueta} · Potencial ${d.potencial}/5 · Afinidad ${d.afinidad}/5",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Detectada el ${formatearFecha(d.fechaDeteccion)} · Fuente: ${d.fuente.ifBlank { "sin registrar" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            d.fechaLimitePostulacion?.let {
                Text(
                    "Fecha límite de postulación: ${formatearFecha(it)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (d.descripcion.isNotBlank()) Text(d.descripcion, style = MaterialTheme.typography.bodyMedium)
            if (d.observaciones.isNotBlank()) {
                Text(
                    "Observaciones: ${d.observaciones}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AvisoProximo(fecha: Long, accion: String) {
    val atrasado = estaAtrasado(fecha)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (atrasado) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (atrasado) Icons.Filled.Warning else Icons.Filled.Schedule,
                contentDescription = null
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    if (atrasado) "Seguimiento atrasado" else "Próximo seguimiento",
                    style = MaterialTheme.typography.titleSmall
                )
                Text("${formatearFecha(fecha)} · ${accion.ifBlank { "Sin detalle" }}")
            }
        }
    }
}

@Composable
private fun FilaDato(icono: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun TarjetaEvento(d: OportunidadDetalle) {
    val fechas = if (d.fechaTermino == d.fechaInicio) formatearFecha(d.fechaInicio)
    else "${formatearFecha(d.fechaInicio)} al ${formatearFecha(d.fechaTermino)}"
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Evento", style = MaterialTheme.typography.titleMedium)
            Text(d.eventoNombre, style = MaterialTheme.typography.titleSmall)
            FilaDato(Icons.Filled.Event, "${d.tipo.aEnum(TipoOportunidad.OTRA).etiqueta} · $fechas")
            if (d.horario.isNotBlank()) FilaDato(Icons.Filled.Schedule, d.horario)
            FilaDato(
                Icons.Filled.Place,
                listOf(d.direccion, d.comuna, d.region).filter { it.isNotBlank() }.joinToString(", ")
            )
            FilaDato(Icons.Filled.Group, "${d.publicoEsperado} personas esperadas · ${d.publicoObjetivo.ifBlank { "público no definido" }}")
            FilaDato(Icons.Filled.Payments, "Costo referencial: $${d.costoReferencial}")
            if (d.condiciones.isNotBlank()) {
                Text(
                    "Condiciones: ${d.condiciones}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TarjetaOrganizacion(d: OportunidadDetalle) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Organización y responsable", style = MaterialTheme.typography.titleMedium)
            FilaDato(Icons.Filled.Business, d.organizacionNombre)
            FilaDato(Icons.Filled.Person, "Responsable: ${d.responsableNombre}")
        }
    }
}

@Composable
private fun TarjetaGestion(g: SeguimientoItem) {
    var expandida by remember { mutableStateOf(false) }
    val tipo = g.tipoAccion.aEnum(TipoAccion.SEGUIMIENTO)

    // Animación: al tocar la gestión, la tarjeta se expande con suavidad para mostrar el detalle.
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expandida = !expandida }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(tipo.etiqueta, style = MaterialTheme.typography.titleSmall)
            Text(
                "${formatearFechaHora(g.fechaHora)} · ${g.usuarioNombre}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(g.resultado, style = MaterialTheme.typography.bodyMedium, maxLines = if (expandida) Int.MAX_VALUE else 2)
            if (expandida) {
                g.contactoNombre?.let { Text("Contacto: $it", style = MaterialTheme.typography.bodySmall) }
                if (g.proximaAccion.isNotBlank()) {
                    val fecha = g.fechaProximoSeguimiento?.let { " (${formatearFecha(it)})" }.orEmpty()
                    Text("Próxima acción: ${g.proximaAccion}$fecha", style = MaterialTheme.typography.bodySmall)
                }
                g.estadoNuevo?.let {
                    Text(
                        "Nuevo estado: ${it.aEnum(EstadoOportunidad.EVENTO_IDENTIFICADO).etiqueta}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (g.observaciones.isNotBlank()) {
                    Text("Observaciones: ${g.observaciones}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun DialogoCambiarEstado(
    estadoActual: EstadoOportunidad,
    destinos: List<EstadoOportunidad>,
    onConfirmar: (EstadoOportunidad, String) -> Unit,
    onCancelar: () -> Unit
) {
    var elegido by remember { mutableStateOf<EstadoOportunidad?>(null) }
    var observacion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Cambiar estado") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "Estado actual: ${estadoActual.etiqueta}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                destinos.forEach { destino ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = elegido == destino,
                                role = Role.RadioButton,
                                onClick = { elegido = destino }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = elegido == destino, onClick = null)
                        Spacer(Modifier.width(8.dp))
                        Text(destino.etiqueta)
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = observacion,
                    onValueChange = { if (it.length <= 300) observacion = it },
                    label = { Text("Observación (opcional)") },
                    supportingText = { Text("${observacion.length}/300") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { elegido?.let { onConfirmar(it, observacion) } },
                enabled = elegido != null
            ) { Text("Confirmar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}
