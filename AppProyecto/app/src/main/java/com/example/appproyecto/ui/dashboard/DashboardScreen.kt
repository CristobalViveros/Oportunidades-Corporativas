package com.example.appproyecto.ui.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.OportunidadItem
import com.example.appproyecto.data.model.Perfil
import com.example.appproyecto.data.model.SeguimientoPendiente
import com.example.appproyecto.data.model.aEnum
import com.example.appproyecto.util.CargaEjecutivo
import com.example.appproyecto.util.DIAS_PROXIMOS
import com.example.appproyecto.util.diasEntre
import com.example.appproyecto.util.formatearFecha
import com.example.appproyecto.util.rol

/** Máximo de filas por sección: el Dashboard es un resumen, la lista completa está en Oportunidades. */
private const val MAXIMO_FILAS = 5

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAbrirOportunidad: (Long) -> Unit,
    onAbrirPerfil: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()
    val usuario = s.usuario
    val r = s.resumen
    val esSupervisor = usuario?.rol() == Perfil.SUPERVISOR

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Inicio") },
            actions = {
                IconButton(onClick = onAbrirPerfil) {
                    Icon(Icons.Filled.AccountCircle, contentDescription = "Perfil y cerrar sesión")
                }
            }
        )

        if (s.cargando || usuario == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Text("Hola, ${usuario.nombre}", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        if (esSupervisor) "Resumen de todo el equipo comercial"
                        else "Resumen de tus oportunidades",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Indicador("Oportunidades activas", r.activas, Icons.Filled.Work, Modifier.weight(1f))
                    Indicador("Gestiones atrasadas", r.atrasados.size, Icons.Filled.Warning, Modifier.weight(1f), alerta = r.atrasados.isNotEmpty())
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Indicador("Seguimientos pendientes", r.pendientes.size, Icons.Filled.Schedule, Modifier.weight(1f))
                    Indicador("Eventos en $DIAS_PROXIMOS días", r.proximas.size, Icons.Filled.Event, Modifier.weight(1f))
                }
            }

            // ---------- Solo supervisor ----------
            if (esSupervisor) {
                item { Seccion("Esperando tu decisión (${r.esperandoDecision.size})") }
                if (r.esperandoDecision.isEmpty()) {
                    item { Vacio("No hay oportunidades en evaluación.") }
                } else {
                    items(r.esperandoDecision.take(MAXIMO_FILAS), key = { "dec-${it.id}" }) { op ->
                        FilaOportunidad(op, "${op.responsableNombre} · ${op.organizacionNombre}", onClick = { onAbrirOportunidad(op.id) })
                    }
                }
            }

            // ---------- Gestiones atrasadas ----------
            item { Seccion("Gestiones atrasadas (${r.atrasados.size})") }
            if (r.atrasados.isEmpty()) {
                item { Vacio("¡Al día! No hay gestiones atrasadas.") }
            } else {
                items(r.atrasados.take(MAXIMO_FILAS), key = { "atr-${it.oportunidadId}" }) { p ->
                    val dias = diasEntre(p.fecha, System.currentTimeMillis())
                    FilaSeguimiento(
                        p, esSupervisor, atrasado = true,
                        detalle = "Atrasada ${if (dias == 1) "1 día" else "$dias días"} · ${formatearFecha(p.fecha)}",
                        onClick = { onAbrirOportunidad(p.oportunidadId) }
                    )
                }
                masFilas(r.atrasados.size)
            }

            // ---------- Seguimientos pendientes ----------
            item { Seccion("Seguimientos pendientes (${r.pendientes.size})") }
            if (r.pendientes.isEmpty()) {
                item { Vacio("No hay seguimientos programados.") }
            } else {
                items(r.pendientes.take(MAXIMO_FILAS), key = { "pen-${it.oportunidadId}" }) { p ->
                    FilaSeguimiento(
                        p, esSupervisor, atrasado = false,
                        detalle = formatearFecha(p.fecha),
                        onClick = { onAbrirOportunidad(p.oportunidadId) }
                    )
                }
                masFilas(r.pendientes.size)
            }

            // ---------- Próximos eventos ----------
            item { Seccion("Próximos eventos (${r.proximas.size})") }
            if (r.proximas.isEmpty()) {
                item { Vacio("No hay eventos en los próximos $DIAS_PROXIMOS días.") }
            } else {
                items(r.proximas.take(MAXIMO_FILAS), key = { "prox-${it.id}" }) { op ->
                    FilaOportunidad(
                        op,
                        "${formatearFecha(op.eventoFecha)} · ${op.eventoNombre} · ${op.comuna}",
                        onClick = { onAbrirOportunidad(op.id) }
                    )
                }
                masFilas(r.proximas.size)
            }

            // ---------- Solo supervisor: carga del equipo ----------
            if (esSupervisor) {
                item { Seccion("Carga del equipo") }
                if (r.cargaEquipo.isEmpty()) {
                    item { Vacio("Ningún ejecutivo tiene oportunidades abiertas.") }
                } else {
                    items(r.cargaEquipo, key = { "eq-${it.id}" }) { FilaCarga(it) }
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.masFilas(total: Int) {
    if (total > MAXIMO_FILAS) {
        item {
            Text(
                "y ${total - MAXIMO_FILAS} más en la pestaña Oportunidades",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Seccion(titulo: String) {
    Text(titulo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun Vacio(texto: String) {
    Text(texto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun Indicador(
    titulo: String,
    valor: Int,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    alerta: Boolean = false
) {
    // Animación: la tarjeta cambia de color de forma gradual cuando aparecen o se resuelven atrasos.
    val colorTarjeta by animateColorAsState(
        targetValue = if (alerta) MaterialTheme.colorScheme.errorContainer
        else MaterialTheme.colorScheme.secondaryContainer,
        animationSpec = tween(durationMillis = 500),
        label = "Color del indicador"
    )
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = colorTarjeta)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icono, contentDescription = null)
            Text(valor.toString(), style = MaterialTheme.typography.headlineMedium)
            Text(titulo, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun FilaOportunidad(op: OportunidadItem, detalle: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(op.nombre, style = MaterialTheme.typography.titleSmall)
            Text(op.estado.aEnum(EstadoOportunidad.EVENTO_IDENTIFICADO).etiqueta, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(detalle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun FilaSeguimiento(
    p: SeguimientoPendiente,
    mostrarResponsable: Boolean,
    atrasado: Boolean,
    detalle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = if (atrasado) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        else CardDefaults.cardColors()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(p.oportunidadNombre, style = MaterialTheme.typography.titleSmall)
            Text(p.proximaAccion.ifBlank { "Sin detalle" }, style = MaterialTheme.typography.bodyMedium)
            Text(
                if (mostrarResponsable) "$detalle · ${p.responsableNombre}" else detalle,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun FilaCarga(c: CargaEjecutivo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(c.nombre, style = MaterialTheme.typography.titleSmall)
            Text(
                "${c.activas} activas · ${c.atrasadas} atrasadas",
                style = MaterialTheme.typography.bodyMedium,
                color = if (c.atrasadas > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
