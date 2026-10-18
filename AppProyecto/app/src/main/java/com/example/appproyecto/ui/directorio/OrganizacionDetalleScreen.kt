package com.example.appproyecto.ui.directorio

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appproyecto.data.model.AreaContacto
import com.example.appproyecto.data.model.Contacto
import com.example.appproyecto.data.model.Organizacion
import com.example.appproyecto.data.model.TipoOrganizacion
import com.example.appproyecto.data.model.aEnum

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizacionDetalleScreen(
    onVolver: () -> Unit,
    onEditar: (Long) -> Unit,
    onNuevoContacto: (Long) -> Unit,
    onEditarContacto: (organizacionId: Long, contactoId: Long) -> Unit,
    viewModel: OrganizacionDetalleViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val org = estado.organizacion

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(org?.nombre ?: "Organización") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (org != null) {
                        IconButton(onClick = { onEditar(org.id) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar organización")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (org != null) {
                FloatingActionButton(onClick = { onNuevoContacto(org.id) }) {
                    Icon(Icons.Filled.Add, contentDescription = "Nuevo contacto")
                }
            }
        }
    ) { padding ->
        when {
            estado.cargando -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            org == null -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("La organización no existe", style = MaterialTheme.typography.bodyLarge)
            }

            else -> LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { DatosOrganizacion(org) }
                item {
                    Text(
                        "Contactos (${estado.contactos.size})",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                if (estado.contactos.isEmpty()) {
                    item {
                        Text(
                            "Esta organización aún no tiene contactos. Toca + para agregar uno.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(estado.contactos, key = { it.id }) { c ->
                        TarjetaContacto(c, onClick = { onEditarContacto(org.id, c.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun DatosOrganizacion(o: Organizacion) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(o.tipo.aEnum(TipoOrganizacion.OTRA).etiqueta, style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary)
            Text("Rubro: ${o.rubro}", style = MaterialTheme.typography.bodyMedium)
            Text("Ubicación: ${o.comuna}, ${o.region}", style = MaterialTheme.typography.bodyMedium)
            if (o.sitioWeb.isNotBlank()) Text("Sitio web: ${o.sitioWeb}", style = MaterialTheme.typography.bodyMedium)
            if (o.publicoAsociado.isNotBlank()) {
                Text("Público asociado: ${o.publicoAsociado}", style = MaterialTheme.typography.bodyMedium)
            }
            if (o.observaciones.isNotBlank()) {
                Text(o.observaciones, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun TarjetaContacto(c: Contacto, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(c.nombre, style = MaterialTheme.typography.titleMedium)
            Text(
                "${c.cargo} · ${c.area.aEnum(AreaContacto.EVENTOS).etiqueta}",
                style = MaterialTheme.typography.bodyMedium
            )
            FilaIcono(Icons.Filled.Email, c.correo)
            if (c.telefono.isNotBlank()) FilaIcono(Icons.Filled.Phone, c.telefono)
            if (c.estado != "Activo") {
                Text(c.estado, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun FilaIcono(icono: ImageVector, texto: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(texto, style = MaterialTheme.typography.bodySmall)
    }
}
