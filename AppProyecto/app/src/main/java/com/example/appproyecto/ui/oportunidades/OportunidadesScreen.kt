package com.example.appproyecto.ui.oportunidades

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.OportunidadItem
import com.example.appproyecto.data.model.Prioridad
import com.example.appproyecto.data.model.aEnum
import com.example.appproyecto.util.formatearFecha

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OportunidadesScreen(onEditar: (Long) -> Unit, viewModel: OportunidadesViewModel = viewModel()) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Oportunidades") })

        OutlinedTextField(
            value = estado.busqueda,
            onValueChange = viewModel::cambiarBusqueda,
            label = { Text("Buscar por nombre, evento u organización") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (estado.busqueda.isNotEmpty()) {
                    IconButton(onClick = { viewModel.cambiarBusqueda("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Limpiar búsqueda")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        // Filtro por prioridad
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(Prioridad.entries) { prioridad ->
                FilterChip(
                    selected = estado.prioridadFiltro == prioridad,
                    onClick = { viewModel.elegirPrioridad(prioridad) },
                    label = { Text("Prioridad ${prioridad.etiqueta}") }
                )
            }
        }

        // Filtro por estado
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(EstadoOportunidad.entries) { e ->
                FilterChip(
                    selected = estado.estadoFiltro == e,
                    onClick = { viewModel.elegirEstado(e) },
                    label = { Text(e.etiqueta) }
                )
            }
        }

        Text(
            text = "${estado.oportunidades.size} oportunidades",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Animación: el mensaje de "sin resultados" aparece y desaparece suavemente.
        AnimatedVisibility(visible = estado.oportunidades.isEmpty() && !estado.cargando) {
            Text(
                text = "No hay oportunidades que coincidan con los filtros.",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(estado.oportunidades, key = { it.id }) { item ->
                // animateItem(): las tarjetas se mueven suavemente cuando cambia la lista.
                TarjetaOportunidad(item, modifier = Modifier.animateItem())
            }
        }
    }
}

@Composable
private fun TarjetaOportunidad(item: OportunidadItem, modifier: Modifier = Modifier) {
    val prioridad = item.prioridad.aEnum(Prioridad.MEDIA)
    val estadoOp = item.estado.aEnum(EstadoOportunidad.EVENTO_IDENTIFICADO)

    val colorObjetivo = when (prioridad) {
        Prioridad.ALTA -> MaterialTheme.colorScheme.errorContainer
        Prioridad.MEDIA -> MaterialTheme.colorScheme.tertiaryContainer
        Prioridad.BAJA -> MaterialTheme.colorScheme.surfaceVariant
    }
    // Animación: si cambia la prioridad, el color de la tarjeta cambia de forma gradual.
    val colorTarjeta by animateColorAsState(
        targetValue = colorObjetivo,
        animationSpec = tween(durationMillis = 500),
        label = "Color de prioridad"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = colorTarjeta)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(item.nombre, style = MaterialTheme.typography.titleMedium)
            Text(
                text = estadoOp.etiqueta,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Event, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("${item.eventoNombre} · ${formatearFecha(item.eventoFecha)}")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Place, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("${item.comuna} · ${item.organizacionNombre}")
            }
            Text(
                text = "Responsable: ${item.responsableNombre} · Prioridad ${prioridad.etiqueta}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}