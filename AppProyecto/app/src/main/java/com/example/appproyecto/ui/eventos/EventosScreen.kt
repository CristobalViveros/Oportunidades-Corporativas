package com.example.appproyecto.ui.eventos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appproyecto.util.formatearFecha

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventosScreen(viewModel: EventosViewModel = viewModel()) {
    val eventos by viewModel.eventos.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Eventos") })

        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(eventos, key = { it.id }) { evento ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        if (evento.fotoUri != null) {
                            FotoEvento(
                                ruta = evento.fotoUri,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                            )
                        }
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(evento.nombre, style = MaterialTheme.typography.titleMedium)
                            Text("${evento.tipo} · ${formatearFecha(evento.fechaInicio)}")
                            Text("${evento.comuna} · ${evento.publicoEsperado} personas esperadas")
                        }
                    }
                }
            }
        }
    }
}