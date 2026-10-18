package com.example.appproyecto.ui.seguimiento

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
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.NextPlan
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TaskAlt
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
import com.example.appproyecto.data.model.TipoAccion
import com.example.appproyecto.data.model.requiereContacto
import com.example.appproyecto.ui.componentes.CampoFecha
import com.example.appproyecto.ui.oportunidades.CampoDesplegable
import com.example.appproyecto.ui.oportunidades.CampoTexto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeguimientoFormScreen(
    onVolver: () -> Unit,
    viewModel: SeguimientoFormViewModel = viewModel()
) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(s.guardado) {
        if (s.guardado) {
            Toast.makeText(context, "Gestión registrada", Toast.LENGTH_SHORT).show()
            onVolver()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar gestión") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        when {
            s.cargando -> Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            s.bloqueado -> Box(
                modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    s.errorGeneral ?: "No tienes acceso a esta oportunidad",
                    color = MaterialTheme.colorScheme.error
                )
            }

            else -> Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(s.nombreOportunidad, style = MaterialTheme.typography.titleMedium)

                CampoDesplegable(
                    etiqueta = "Tipo de acción",
                    icono = Icons.Filled.Category,
                    textoActual = s.tipo.etiqueta,
                    opciones = TipoAccion.entries.map { it to it.etiqueta },
                    error = null,
                    onElegir = viewModel::cambiarTipo
                )

                // El contacto solo se pide en las acciones donde se habla con alguien.
                AnimatedVisibility(visible = s.tipo.requiereContacto) {
                    CampoDesplegable(
                        etiqueta = "Contacto",
                        icono = Icons.Filled.Contacts,
                        textoActual = s.contactos.firstOrNull { it.id == s.contactoId }?.let { "${it.nombre} · ${it.cargo}" } ?: "",
                        opciones = s.contactos.map { it.id to "${it.nombre} · ${it.cargo}" },
                        error = s.errorContacto,
                        onElegir = viewModel::elegirContacto
                    )
                }

                CampoTexto(
                    valor = s.resultado, etiqueta = "Resultado de la gestión",
                    icono = Icons.Filled.TaskAlt, error = s.errorResultado,
                    maximo = 300, lineas = 3, onCambio = viewModel::cambiarResultado
                )

                CampoTexto(
                    valor = s.proximaAccion, etiqueta = "Próxima acción (opcional)",
                    icono = Icons.Filled.NextPlan, error = s.errorProximaAccion,
                    onCambio = viewModel::cambiarProximaAccion
                )

                CampoFecha(
                    etiqueta = "Fecha del próximo seguimiento",
                    fecha = s.fechaProximo,
                    error = s.errorFecha,
                    onElegir = viewModel::fijarFecha,
                    onQuitar = viewModel::quitarFecha
                )

                CampoTexto(
                    valor = s.observaciones, etiqueta = "Observaciones (opcional)",
                    icono = Icons.Filled.Notes, error = s.errorObservaciones,
                    maximo = 300, lineas = 3, onCambio = viewModel::cambiarObservaciones
                )

                AnimatedVisibility(visible = s.errorGeneral != null) {
                    Text(
                        text = s.errorGeneral.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Button(
                    onClick = viewModel::guardar,
                    enabled = !s.guardando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (s.guardando) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Save, contentDescription = null)
                        Text("  Guardar gestión")
                    }
                }
            }
        }
    }
}
