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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ToggleOn
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
import com.example.appproyecto.data.model.AreaContacto
import com.example.appproyecto.data.model.aEnum
import com.example.appproyecto.ui.oportunidades.CampoDesplegable
import com.example.appproyecto.ui.oportunidades.CampoTexto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactoFormScreen(
    onVolver: () -> Unit,
    viewModel: ContactoFormViewModel = viewModel()
) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(s.guardado) {
        if (s.guardado) {
            Toast.makeText(
                context,
                if (s.esEdicion) "Contacto actualizado" else "Contacto guardado",
                Toast.LENGTH_SHORT
            ).show()
            onVolver()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (s.esEdicion) "Editar contacto" else "Nuevo contacto") },
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
            if (s.organizacionNombre.isNotBlank()) {
                Text(
                    "Organización: ${s.organizacionNombre}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            CampoTexto(
                valor = s.nombre, etiqueta = "Nombre completo", icono = Icons.Filled.Person,
                error = s.errorNombre, maximo = 80, onCambio = viewModel::cambiarNombre
            )
            CampoTexto(
                valor = s.cargo, etiqueta = "Cargo", icono = Icons.Filled.Badge,
                error = s.errorCargo, maximo = 60, onCambio = viewModel::cambiarCargo
            )
            CampoDesplegable(
                etiqueta = "Área", icono = Icons.Filled.Groups,
                textoActual = s.area.aEnum(AreaContacto.EVENTOS).etiqueta,
                opciones = AreaContacto.entries.map { it.name to it.etiqueta },
                error = null, onElegir = viewModel::cambiarArea
            )
            CampoTexto(
                valor = s.correo, etiqueta = "Correo", icono = Icons.Filled.Email,
                error = s.errorCorreo, onCambio = viewModel::cambiarCorreo
            )
            CampoTexto(
                valor = s.telefono, etiqueta = "Teléfono (opcional)", icono = Icons.Filled.Phone,
                error = s.errorTelefono, onCambio = viewModel::cambiarTelefono
            )
            CampoDesplegable(
                etiqueta = "Medio preferente", icono = Icons.Filled.RecordVoiceOver,
                textoActual = s.medioPreferente,
                opciones = MEDIOS_PREFERENTES.map { it to it },
                error = null, onElegir = viewModel::cambiarMedio
            )
            CampoDesplegable(
                etiqueta = "Estado", icono = Icons.Filled.ToggleOn,
                textoActual = s.estado,
                opciones = ESTADOS_CONTACTO.map { it to it },
                error = null, onElegir = viewModel::cambiarEstado
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
                    Text("  Guardar contacto")
                }
            }
        }
    }
}
