package com.example.appproyecto.util

import com.example.appproyecto.data.model.SeguimientoItem

/**
 * Próximo seguimiento pendiente: la gestión más reciente que dejó una fecha programada.
 * [historial] viene ordenado de la más reciente a la más antigua.
 */
fun proximoSeguimiento(historial: List<SeguimientoItem>): SeguimientoItem? =
    historial.firstOrNull { it.fechaProximoSeguimiento != null }

/** Un seguimiento está atrasado cuando su fecha es anterior a hoy. */
fun estaAtrasado(fecha: Long, ahora: Long = System.currentTimeMillis()): Boolean =
    fecha < inicioDelDia(ahora)
