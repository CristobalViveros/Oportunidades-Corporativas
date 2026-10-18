package com.example.appproyecto.ui.componentes

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.ui.theme.coloresApp

/** Etiqueta redondeada con el estado de la oportunidad; su color sale del tema (ver ColoresApp). */
@Composable
fun InsigniaEstado(estado: EstadoOportunidad, modifier: Modifier = Modifier) {
    val color = MaterialTheme.coloresApp.paraEstado(estado)
    Surface(
        color = color.fondo,
        contentColor = color.contenido,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = estado.etiqueta,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
