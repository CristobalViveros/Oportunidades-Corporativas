package com.example.appproyecto.util

import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.OportunidadItem
import com.example.appproyecto.data.model.Prioridad

fun filtrarOportunidades(
    items: List<OportunidadItem>,
    texto: String,
    estado: EstadoOportunidad?,
    prioridad: Prioridad?
): List<OportunidadItem> {
    val busqueda = texto.trim().lowercase()
    return items.filter { item ->
        val coincideTexto = busqueda.isEmpty() || listOf(
            item.nombre, item.eventoNombre, item.organizacionNombre, item.comuna
        ).any { it.lowercase().contains(busqueda) }
        val coincideEstado = estado == null || item.estado == estado.name
        val coincidePrioridad = prioridad == null || item.prioridad == prioridad.name
        coincideTexto && coincideEstado && coincidePrioridad
    }
}