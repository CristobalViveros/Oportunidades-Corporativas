package com.example.appproyecto.ui.componentes

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.appproyecto.util.formatearFecha
import java.util.Calendar

/** Botón que abre el selector de fecha nativo y muestra el error debajo, como los demás campos. */
@Composable
fun CampoFecha(
    etiqueta: String,
    fecha: Long?,
    error: String?,
    onElegir: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onQuitar: (() -> Unit)? = null
) {
    val context = LocalContext.current
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = {
                val base = Calendar.getInstance().apply { fecha?.let { timeInMillis = it } }
                DatePickerDialog(
                    context,
                    { _, anio, mes, dia ->
                        val elegido = Calendar.getInstance().apply {
                            set(anio, mes, dia, 0, 0, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        onElegir(elegido.timeInMillis)
                    },
                    base.get(Calendar.YEAR), base.get(Calendar.MONTH), base.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.CalendarMonth, contentDescription = null)
            Text("  " + (fecha?.let { "$etiqueta: ${formatearFecha(it)}" } ?: etiqueta))
        }
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
        if (fecha != null && onQuitar != null) {
            TextButton(onClick = onQuitar) { Text("Quitar fecha") }
        }
    }
}
