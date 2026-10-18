package com.example.appproyecto.ui.componentes

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

/**
 * Devuelve una función que abre el calendario del dispositivo con el [Intent] dado y avisa el
 * resultado mediante [onMensaje] (para mostrarlo, por ejemplo, en un Snackbar).
 *
 * Cubre los casos de la checklist: sin app de calendario instalada, usuario que cancela y
 * confirmación cuando se guarda. Algunas apps de calendario devuelven "cancelado" aunque guarden el
 * evento, por eso el mensaje de cancelación no afirma que no se guardó.
 */
@Composable
fun rememberLanzadorCalendario(onMensaje: (String) -> Unit): (Intent) -> Unit {
    val mensaje = rememberUpdatedState(onMensaje)
    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
        if (resultado.resultCode == Activity.RESULT_OK) {
            mensaje.value("Evento agregado al calendario")
        } else {
            mensaje.value("Volviste del calendario. Si no guardaste el evento, puedes intentarlo de nuevo")
        }
    }
    return remember(lanzador) {
        { intent: Intent ->
            try {
                lanzador.launch(intent)
            } catch (e: ActivityNotFoundException) {
                mensaje.value("No hay una aplicación de calendario instalada en este dispositivo")
            }
        }
    }
}
