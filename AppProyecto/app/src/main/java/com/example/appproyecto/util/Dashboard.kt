package com.example.appproyecto.util

import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.OportunidadItem
import com.example.appproyecto.data.model.Perfil
import com.example.appproyecto.data.model.Prioridad
import com.example.appproyecto.data.model.SeguimientoPendiente
import com.example.appproyecto.data.model.aEnum
import kotlin.math.roundToInt

private const val MILIS_DIA = 24L * 60 * 60 * 1000

/** Cuántos días hacia adelante se consideran "próximos" para los eventos. */
const val DIAS_PROXIMOS = 30

data class CargaEjecutivo(val id: Long, val nombre: String, val activas: Int, val atrasadas: Int)

/** Todo lo que muestra el Dashboard, ya calculado. Las listas vienen ordenadas por fecha. */
data class ResumenDashboard(
    val activas: Int = 0,
    val altaPrioridad: Int = 0,
    /** Eventos de oportunidades abiertas que ocurren en los próximos [DIAS_PROXIMOS] días. */
    val proximas: List<OportunidadItem> = emptyList(),
    /** Gestiones programadas para hoy o más adelante. */
    val pendientes: List<SeguimientoPendiente> = emptyList(),
    /** Gestiones cuya fecha ya pasó (la más atrasada primero). */
    val atrasados: List<SeguimientoPendiente> = emptyList(),
    /** Solo supervisor: oportunidades "En evaluación", esperando su decisión. */
    val esperandoDecision: List<OportunidadItem> = emptyList(),
    /** Solo supervisor: carga de trabajo por ejecutivo. */
    val cargaEquipo: List<CargaEjecutivo> = emptyList()
)

private fun OportunidadItem.estaAbierta(): Boolean =
    !Permisos.esFinal(estado.aEnum(EstadoOportunidad.EVENTO_IDENTIFICADO))

/** Días completos entre dos fechas (puede ser negativo). Redondea para no fallar con cambios de hora. */
fun diasEntre(desde: Long, hasta: Long): Int =
    ((inicioDelDia(hasta) - inicioDelDia(desde)) / MILIS_DIA.toDouble()).roundToInt()

/**
 * Calcula el resumen del Dashboard. Es una función pura (sin Android ni base de datos): recibe las
 * oportunidades y gestiones que el repositorio ya filtró según el rol, así se puede probar con tests.
 */
fun calcularDashboard(
    rol: Perfil,
    items: List<OportunidadItem>,
    seguimientos: List<SeguimientoPendiente>,
    ahora: Long = System.currentTimeMillis()
): ResumenDashboard {
    val hoy = inicioDelDia(ahora)
    val limite = hoy + DIAS_PROXIMOS * MILIS_DIA
    val abiertas = items.filter { it.estaAbierta() }

    val atrasados = seguimientos.filter { estaAtrasado(it.fecha, ahora) }.sortedBy { it.fecha }
    val pendientes = seguimientos.filter { !estaAtrasado(it.fecha, ahora) }.sortedBy { it.fecha }

    val esSupervisor = rol == Perfil.SUPERVISOR
    return ResumenDashboard(
        activas = abiertas.size,
        altaPrioridad = abiertas.count { it.prioridad == Prioridad.ALTA.name },
        proximas = abiertas
            .filter { it.eventoFecha in hoy..limite }
            .sortedBy { it.eventoFecha },
        pendientes = pendientes,
        atrasados = atrasados,
        esperandoDecision = if (esSupervisor) {
            abiertas.filter { it.estado == EstadoOportunidad.EN_EVALUACION.name }
        } else emptyList(),
        cargaEquipo = if (esSupervisor) {
            abiertas.groupBy { it.responsableId }.map { (id, lista) ->
                CargaEjecutivo(
                    id = id,
                    nombre = lista.first().responsableNombre,
                    activas = lista.size,
                    atrasadas = atrasados.count { it.responsableId == id }
                )
            }.sortedByDescending { it.atrasadas * 1000 + it.activas }
        } else emptyList()
    )
}
