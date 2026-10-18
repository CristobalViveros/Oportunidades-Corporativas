package com.example.appproyecto.util

import com.example.appproyecto.data.model.EstadoOportunidad.*
import com.example.appproyecto.data.model.Oportunidad
import com.example.appproyecto.data.model.Perfil
import com.example.appproyecto.data.model.Usuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermisosTest {

    private fun usuario(id: Long, perfil: Perfil, activo: Boolean = true) = Usuario(
        id = id, nombre = "U$id", correo = "u$id@ejemplo.cl", passwordHash = "x",
        perfil = perfil.name, activo = activo
    )

    private fun oportunidad(responsableId: Long, estado: String = EVENTO_IDENTIFICADO.name) =
        Oportunidad(id = 1, nombre = "Op", eventoId = 1, responsableId = responsableId, estado = estado)

    private val ana = usuario(1, Perfil.EJECUTIVO)
    private val carlos = usuario(2, Perfil.EJECUTIVO)
    private val sofia = usuario(9, Perfil.SUPERVISOR)

    @Test fun supervisorVeTodo_ejecutivoSoloLoSuyo() {
        val deAna = oportunidad(responsableId = 1)
        assertTrue(Permisos.puedeVer(sofia, deAna))
        assertTrue(Permisos.puedeVer(ana, deAna))
        assertFalse(Permisos.puedeVer(carlos, deAna))
    }

    @Test fun usuarioInactivoNoPuedeNada() {
        val inactiva = usuario(9, Perfil.SUPERVISOR, activo = false)
        assertFalse(Permisos.esSupervisor(inactiva))
        assertFalse(Permisos.puedeVer(inactiva, oportunidad(1)))
    }

    @Test fun soloElSupervisorAsignaYCambiaPrioridad() {
        assertTrue(Permisos.puedeAsignar(sofia))
        assertFalse(Permisos.puedeAsignar(ana))
        assertTrue(Permisos.puedeCambiarPrioridad(sofia))
        assertFalse(Permisos.puedeCambiarPrioridad(ana))
    }

    @Test fun ejecutivoNoPuedeConfirmarNiDescartar() {
        val enEvaluacion = oportunidad(1, EN_EVALUACION.name)
        assertFalse(Permisos.puedeCambiarEstado(ana, enEvaluacion, PARTICIPACION_CONFIRMADA))
        assertFalse(Permisos.puedeCambiarEstado(ana, enEvaluacion, OPORTUNIDAD_DESCARTADA))
        assertTrue(Permisos.puedeCambiarEstado(ana, enEvaluacion, REQUIERE_SEGUIMIENTO))
    }

    @Test fun supervisorSiPuedeConfirmarYDescartar() {
        val enEvaluacion = oportunidad(1, EN_EVALUACION.name)
        assertTrue(Permisos.puedeCambiarEstado(sofia, enEvaluacion, PARTICIPACION_CONFIRMADA))
        assertTrue(Permisos.puedeCambiarEstado(sofia, enEvaluacion, OPORTUNIDAD_DESCARTADA))
    }

    @Test fun ejecutivoAjenoNoPuedeCambiarEstado() {
        val deAna = oportunidad(1)
        assertFalse(Permisos.puedeCambiarEstado(carlos, deAna, CONTACTO_PENDIENTE))
    }

    @Test fun noSePermitenSaltosDeEstado() {
        val nueva = oportunidad(1, EVENTO_IDENTIFICADO.name)
        assertTrue(Permisos.puedeCambiarEstado(ana, nueva, CONTACTO_PENDIENTE))
        assertFalse(Permisos.puedeCambiarEstado(ana, nueva, PROPUESTA_ENVIADA))
    }

    @Test fun estadosFinalesNoTienenSalidaParaElEjecutivo() {
        assertTrue(Permisos.estadosDestino(Perfil.EJECUTIVO, OPORTUNIDAD_CERRADA).isEmpty())
        assertTrue(Permisos.estadosDestino(Perfil.EJECUTIVO, OPORTUNIDAD_DESCARTADA).isEmpty())
    }

    @Test fun supervisorPuedeReabrirUnaDescartada() {
        assertEquals(listOf(EN_EVALUACION), Permisos.estadosDestino(Perfil.SUPERVISOR, OPORTUNIDAD_DESCARTADA))
    }

    @Test fun rolDesconocidoSeTrataComoEjecutivo() {
        val raro = usuario(5, Perfil.EJECUTIVO).copy(perfil = "ADMIN")
        assertEquals(Perfil.EJECUTIVO, raro.rol())
        assertFalse(Permisos.esSupervisor(raro))
    }
}
