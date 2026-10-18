package com.example.appproyecto.util

import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.OportunidadItem
import com.example.appproyecto.data.model.Prioridad
import org.junit.Assert.assertEquals
import org.junit.Test

class FiltrosTest {

    private fun item(id: Long, nombre: String, estado: EstadoOportunidad, prioridad: Prioridad, comuna: String) =
        OportunidadItem(
            id = id, nombre = nombre, estado = estado.name, prioridad = prioridad.name, potencial = 3,
            eventoNombre = "Evento $id", eventoFecha = 0, comuna = comuna, organizacionNombre = "Org $id",
            responsableId = 1, responsableNombre = "Ana"
        )

    private val lista = listOf(
        item(1, "Stand Feria", EstadoOportunidad.PROPUESTA_ENVIADA, Prioridad.ALTA, "Puente Alto"),
        item(2, "Activación Festival", EstadoOportunidad.CONTACTO_PENDIENTE, Prioridad.MEDIA, "Viña del Mar"),
        item(3, "Jornada Alfa", EstadoOportunidad.PROPUESTA_ENVIADA, Prioridad.BAJA, "Las Condes")
    )

    @Test fun sinFiltrosDevuelveTodo() {
        assertEquals(3, filtrarOportunidades(lista, "", null, null).size)
    }

    @Test fun buscaPorTextoSinDistinguirMayusculas() {
        assertEquals(listOf(1L), filtrarOportunidades(lista, "  FERIA ", null, null).map { it.id })
        assertEquals(listOf(2L), filtrarOportunidades(lista, "viña", null, null).map { it.id })
    }

    @Test fun filtraPorEstadoYPrioridad() {
        val porEstado = filtrarOportunidades(lista, "", EstadoOportunidad.PROPUESTA_ENVIADA, null)
        assertEquals(listOf(1L, 3L), porEstado.map { it.id })
        val combinado = filtrarOportunidades(lista, "", EstadoOportunidad.PROPUESTA_ENVIADA, Prioridad.BAJA)
        assertEquals(listOf(3L), combinado.map { it.id })
    }
}
