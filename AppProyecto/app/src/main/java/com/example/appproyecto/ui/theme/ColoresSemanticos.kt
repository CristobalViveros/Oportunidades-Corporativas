package com.example.appproyecto.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.Prioridad

/** Un color de fondo con el color legible que va encima. */
data class ParColor(val fondo: Color, val contenido: Color)

/**
 * Colores con significado propio que Material 3 no trae: éxito, advertencia e información.
 * Con ellos se pintan los estados y prioridades de las oportunidades.
 */
data class ColoresApp(
    val exito: ParColor,
    val advertencia: ParColor,
    val informacion: ParColor,
    val peligro: ParColor,
    val neutro: ParColor
) {
    /** Color de la insignia según el avance de la oportunidad. */
    fun paraEstado(estado: EstadoOportunidad): ParColor = when (estado) {
        EstadoOportunidad.EVENTO_IDENTIFICADO,
        EstadoOportunidad.CONTACTO_PENDIENTE -> neutro

        EstadoOportunidad.CONTACTO_REALIZADO,
        EstadoOportunidad.PROPUESTA_ENVIADA,
        EstadoOportunidad.EN_ESPERA_RESPUESTA,
        EstadoOportunidad.EN_EVALUACION -> informacion

        EstadoOportunidad.REQUIERE_SEGUIMIENTO -> advertencia

        EstadoOportunidad.PARTICIPACION_CONFIRMADA,
        EstadoOportunidad.ACTIVACION_REALIZADA -> exito

        EstadoOportunidad.ACTIVACION_CANCELADA,
        EstadoOportunidad.OPORTUNIDAD_DESCARTADA -> peligro

        EstadoOportunidad.OPORTUNIDAD_CERRADA -> neutro
    }

    /** Color de la insignia de prioridad. */
    fun paraPrioridad(prioridad: Prioridad): ParColor = when (prioridad) {
        Prioridad.ALTA -> peligro
        Prioridad.MEDIA -> advertencia
        Prioridad.BAJA -> neutro
    }
}

internal val ColoresAppClaro = ColoresApp(
    exito = ParColor(Verde90, Verde10),
    advertencia = ParColor(Ambar90, Ambar10),
    informacion = ParColor(Celeste90, Celeste10),
    peligro = ParColor(Rojo90, Rojo10),
    neutro = ParColor(NeutroVar90, Neutro10)
)

internal val ColoresAppOscuro = ColoresApp(
    exito = ParColor(Verde40, Verde90),
    advertencia = ParColor(Ambar40, Ambar90),
    informacion = ParColor(Celeste40, Celeste90),
    peligro = ParColor(Rojo40, Rojo90),
    neutro = ParColor(NeutroVar30, NeutroVar90)
)

val LocalColoresApp = staticCompositionLocalOf { ColoresAppClaro }

/** Uso: `MaterialTheme.coloresApp.exito.fondo`. */
val MaterialTheme.coloresApp: ColoresApp
    @Composable
    @ReadOnlyComposable
    get() = LocalColoresApp.current
