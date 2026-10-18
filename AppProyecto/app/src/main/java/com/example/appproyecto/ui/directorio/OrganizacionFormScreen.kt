package com.example.appproyecto.ui.directorio

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appproyecto.data.model.TipoOrganizacion
import com.example.appproyecto.data.model.aEnum
import com.example.appproyecto.ui.oportunidades.CampoDesplegable
import com.example.appproyecto.ui.oportunidades.CampoTexto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizacionFormScreen(
    onVolver: () -> Unit,
    viewModel: OrganizacionFormViewModel = viewModel()
) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(s.guardado) {
        if (s.guardado) {
            Toast.makeText(
                context,
                if (s.esEdicion) "Organización actualizada" else "Organización guardada",
                Toast.LENGTH_SHORT
            ).show()
            onVolver()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (s.esEdicion) "Editar organización" else "Nueva organización") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (s.cargando) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CampoTexto(
                valor = s.nombre, etiqueta = "Nombre de la organización", icono = Icons.Filled.Business,
                error = s.errorNombre, maximo = 80, onCambio = viewModel::cambiarNombre
            )
            CampoDesplegable(
                etiqueta = "Tipo de organización", icono = Icons.Filled.Category,
                textoActual = s.tipo.aEnum(TipoOrganizacion.OTRA).etiqueta,
                opciones = TipoOrganizacion.entries.map { it.name to it.etiqueta },
                error = null, onElegir = viewModel::cambiarTipo
            )
            CampoTexto(
                valor = s.rubro, etiqueta = "Rubro", icono = Icons.Filled.Work,
                error = s.errorRubro, maximo = 60, onCambio = viewModel::cambiarRubro
            )
            CampoTexto(
                valor = s.region, etiqueta = "Región", icono = Icons.Filled.Map,
                error = s.errorRegion, onCambio = viewModel::cambiarRegion
            )
            CampoTexto(
                valor = s.comuna, etiqueta = "Comuna", icono = Icons.Filled.Place,
                error = s.errorComuna, onCambio = viewModel::cambiarComuna
            )
            CampoTexto(
                valor = s.sitioWeb, etiqueta = "Sitio web (opcional)", icono = Icons.Filled.Language,
                error = s.errorSitioWeb, onCambio = viewModel::cambiarSitioWeb
            )
            CampoTexto(
                valor = s.publicoAsociado, etiqueta = "Público asociado (opcional)", icono = Icons.Filled.Group,
                error = s.errorPublico, maximo = 100, onCambio = viewModel::cambiarPublico
            )
            CampoTexto(
                valor = s.observaciones, etiqueta = "Observaciones (opcional)", icono = Icons.Filled.Chat,
                error = s.errorObservaciones, maximo = 300, lineas = 3, onCambio = viewModel::cambiarObservaciones
            )

            AnimatedVisibility(visible = s.errorGeneral != null) {
                Text(
                    text = s.errorGeneral.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(onClick = viewModel::guardar, enabled = !s.guardando, modifier = Modifier.fillMaxWidth()) {
                if (s.guardando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.Save, contentDescription = null)
                    Text("  Guardar organización")
                }
            }
        }
    }
}
