package com.example.appproyecto.util

import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.OportunidadItem
import com.example.appproyecto.data.model.Perfil
import com.example.appproyecto.data.model.Prioridad
import com.example.appproyecto.data.model.SeguimientoPendiente
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardTest {

    private val dia = 86_400_000L
    private val ahora = 1_800_000_000_000L

    private fun item(
        id: Long, estado: EstadoOportunidad, prioridad: Prioridad = Prioridad.MEDIA,
        diasAlEvento: Int = 5, responsable: Long = 1
    ) = OportunidadItem(
        id = id, nombre = "Op$id", estado = estado.name, prioridad = prioridad.name, potencial = 3,
        eventoNombre = "Ev$id", eventoFecha = ahora + diasAlEvento * dia, comuna = "Santiago",
        organizacionNombre = "Org", responsableId = responsable, responsableNombre = "R$responsable"
    )

    private fun pendiente(id: Long, diasDesdeHoy: Int, responsable: Long = 1) = SeguimientoPendiente(
        oportunidadId = id, oportunidadNombre = "Op$id", estado = EstadoOportunidad.CONTACTO_REALIZADO.name,
        responsableId = responsable, responsableNombre = "R$responsable", proximaAccion = "Llamar",
        fecha = ahora + diasDesdeHoy * dia
    )

    @Test fun cuentaSoloOportunidadesAbiertas() {
        val items = listOf(
            item(1, EstadoOportunidad.PROPUESTA_ENVIADA, Prioridad.ALTA),
            item(2, EstadoOportunidad.OPORTUNIDAD_CERRADA, Prioridad.ALTA),
            item(3, EstadoOportunidad.OPORTUNIDAD_DESCARTADA),
            item(4, EstadoOportunidad.CONTACTO_PENDIENTE)
        )
        val r = calcularDashboard(Perfil.EJECUTIVO, items, emptyList(), ahora)
        assertEquals(2, r.activas)
        assertEquals(1, r.altaPrioridad)
    }

    @Test fun proximasSoloDentroDeLosProximos30Dias() {
        val items = listOf(
            item(1, EstadoOportunidad.CONTACTO_PENDIENTE, diasAlEvento = 5),
            item(2, EstadoOportunidad.CONTACTO_PENDIENTE, diasAlEvento = 40),
            item(3, EstadoOportunidad.CONTACTO_PENDIENTE, diasAlEvento = -3)
        )
        val r = calcularDashboard(Perfil.EJECUTIVO, items, emptyList(), ahora)
        assertEquals(listOf(1L), r.proximas.map { it.id })
    }

    @Test fun separaSeguimientosAtrasadosDePendientes() {
        val segs = listOf(pendiente(1, -2), pendiente(2, 0), pendiente(3, 4))
        val r = calcularDashboard(Perfil.EJECUTIVO, emptyList(), segs, ahora)
        assertEquals(listOf(1L), r.atrasados.map { it.oportunidadId })
        assertEquals(listOf(2L, 3L), r.pendientes.map { it.oportunidadId })
    }

    @Test fun soloElSupervisorVeColaDeDecisionYCargaDelEquipo() {
        val items = listOf(
            item(1, EstadoOportunidad.EN_EVALUACION, responsable = 1),
            item(2, EstadoOportunidad.CONTACTO_PENDIENTE, responsable = 2)
        )
        val segs = listOf(pendiente(2, -1, responsable = 2))

        val sup = calcularDashboard(Perfil.SUPERVISOR, items, segs, ahora)
        assertEquals(listOf(1L), sup.esperandoDecision.map { it.id })
        assertEquals(2, sup.cargaEquipo.size)
        assertEquals(2L, sup.cargaEquipo.first().id) // el que tiene atrasos va primero
        assertEquals(1, sup.cargaEquipo.first().atrasadas)

        val eje = calcularDashboard(Perfil.EJECUTIVO, items, segs, ahora)
        assertTrue(eje.esperandoDecision.isEmpty())
        assertTrue(eje.cargaEquipo.isEmpty())
    }

    @Test fun atrasoSeCalculaPorDiaNoPorHora() {
        assertFalse(estaAtrasado(ahora, ahora))
        assertTrue(estaAtrasado(ahora - 2 * dia, ahora))
    }
}
