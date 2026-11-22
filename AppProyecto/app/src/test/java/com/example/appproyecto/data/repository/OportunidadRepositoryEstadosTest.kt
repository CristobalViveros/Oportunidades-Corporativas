package com.example.appproyecto.data.repository

import com.example.appproyecto.data.local.OportunidadDao
import com.example.appproyecto.data.local.SeguimientoDao
import com.example.appproyecto.data.local.UsuarioDao
import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.Oportunidad
import com.example.appproyecto.data.model.Perfil
import com.example.appproyecto.data.model.Resultado
import com.example.appproyecto.data.model.Seguimiento
import com.example.appproyecto.data.model.TipoAccion
import com.example.appproyecto.data.model.Usuario
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OportunidadRepositoryEstadosTest {

    private val oportunidadDao = mockk<OportunidadDao>()
    private val seguimientoDao = mockk<SeguimientoDao>()
    private val usuarioDao = mockk<UsuarioDao>()
    private val repositorio = OportunidadRepository(oportunidadDao, seguimientoDao, usuarioDao)

    private val ana = Usuario(id = 1, nombre = "Ana (demo)", correo = "ana@ejemplo.cl", passwordHash = "x")
    private val carlos = Usuario(id = 2, nombre = "Carlos (demo)", correo = "carlos@ejemplo.cl", passwordHash = "x")
    private val sofia = Usuario(
        id = 10, nombre = "Sofía (demo)", correo = "sofia@ejemplo.cl", passwordHash = "x",
        perfil = Perfil.SUPERVISOR.name
    )

    private fun oportunidad(
        responsableId: Long = 1,
        estado: EstadoOportunidad = EstadoOportunidad.EVENTO_IDENTIFICADO
    ) = Oportunidad(id = 5, nombre = "Feria Demo", eventoId = 1, responsableId = responsableId, estado = estado.name)

    // ---------- cambiarEstado ----------

    @Test
    fun cambiarEstado_inexistente_devuelveError() = runTest {
        coEvery { oportunidadDao.obtenerPorId(5L) } returns null

        val resultado = repositorio.cambiarEstado(ana, 5L, EstadoOportunidad.CONTACTO_PENDIENTE)

        assertEquals(Resultado.Error("La oportunidad no existe"), resultado)
    }

    @Test
    fun cambiarEstado_ejecutivoAjeno_devuelveError() = runTest {
        coEvery { oportunidadDao.obtenerPorId(5L) } returns oportunidad(responsableId = 1)

        val resultado = repositorio.cambiarEstado(carlos, 5L, EstadoOportunidad.CONTACTO_PENDIENTE)

        assertEquals(Resultado.Error("No tienes acceso a esta oportunidad"), resultado)
    }

    @Test
    fun cambiarEstado_ejecutivoIntentaConfirmar_devuelveErrorDeSupervisor() = runTest {
        coEvery { oportunidadDao.obtenerPorId(5L) } returns oportunidad(estado = EstadoOportunidad.EN_EVALUACION)

        val resultado = repositorio.cambiarEstado(ana, 5L, EstadoOportunidad.PARTICIPACION_CONFIRMADA)

        assertEquals(
            Resultado.Error("Solo un supervisor puede confirmar o descartar una oportunidad"),
            resultado
        )
        coVerify(exactly = 0) { oportunidadDao.actualizar(any()) }
    }

    @Test
    fun cambiarEstado_saltoNoPermitido_devuelveErrorConEtiquetas() = runTest {
        coEvery { oportunidadDao.obtenerPorId(5L) } returns oportunidad(estado = EstadoOportunidad.EVENTO_IDENTIFICADO)

        val resultado = repositorio.cambiarEstado(ana, 5L, EstadoOportunidad.PROPUESTA_ENVIADA)

        assertEquals(
            Resultado.Error("No se puede pasar de «Evento identificado» a «Propuesta enviada»"),
            resultado
        )
    }

    @Test
    fun cambiarEstado_transicionValida_actualizaYDejaConstanciaEnElHistorial() = runTest {
        val guardada = slot<Oportunidad>()
        val historial = slot<Seguimiento>()
        coEvery { oportunidadDao.obtenerPorId(5L) } returns oportunidad()
        coJustRun { oportunidadDao.actualizar(capture(guardada)) }
        coEvery { seguimientoDao.insertar(capture(historial)) } returns 1L

        val resultado = repositorio.cambiarEstado(ana, 5L, EstadoOportunidad.CONTACTO_PENDIENTE, "Primer paso")

        assertEquals(Resultado.Exito("Estado actualizado a «Contacto pendiente»"), resultado)
        assertEquals(EstadoOportunidad.CONTACTO_PENDIENTE.name, guardada.captured.estado)
        assertEquals(TipoAccion.SEGUIMIENTO.name, historial.captured.tipoAccion)
        assertEquals(EstadoOportunidad.CONTACTO_PENDIENTE.name, historial.captured.estadoNuevo)
        assertEquals(
            "Cambio de estado: Evento identificado → Contacto pendiente",
            historial.captured.resultado
        )
        assertEquals("Primer paso", historial.captured.observaciones)
        assertEquals(1L, historial.captured.usuarioId)
    }

    @Test
    fun cambiarEstado_confirmacionDelSupervisor_registraAccionConfirmacion() = runTest {
        val historial = slot<Seguimiento>()
        coEvery { oportunidadDao.obtenerPorId(5L) } returns oportunidad(estado = EstadoOportunidad.EN_EVALUACION)
        coJustRun { oportunidadDao.actualizar(any()) }
        coEvery { seguimientoDao.insertar(capture(historial)) } returns 1L

        val resultado = repositorio.cambiarEstado(sofia, 5L, EstadoOportunidad.PARTICIPACION_CONFIRMADA)

        assertEquals(Resultado.Exito("Estado actualizado a «Participación confirmada»"), resultado)
        assertEquals(TipoAccion.CONFIRMACION.name, historial.captured.tipoAccion)
    }

    @Test
    fun cambiarEstado_cierre_registraAccionCierre() = runTest {
        val historial = slot<Seguimiento>()
        coEvery { oportunidadDao.obtenerPorId(5L) } returns oportunidad(estado = EstadoOportunidad.ACTIVACION_REALIZADA)
        coJustRun { oportunidadDao.actualizar(any()) }
        coEvery { seguimientoDao.insertar(capture(historial)) } returns 1L

        val resultado = repositorio.cambiarEstado(ana, 5L, EstadoOportunidad.OPORTUNIDAD_CERRADA)

        assertEquals(Resultado.Exito("Estado actualizado a «Oportunidad cerrada»"), resultado)
        assertEquals(TipoAccion.CIERRE.name, historial.captured.tipoAccion)
    }

    // ---------- registrarSeguimiento ----------

    private fun gestion() = Seguimiento(
        id = 77, oportunidadId = 5, usuarioId = 999, resultado = "Llamada realizada",
        estadoNuevo = EstadoOportunidad.OPORTUNIDAD_CERRADA.name
    )

    @Test
    fun registrarSeguimiento_inexistente_devuelveError() = runTest {
        coEvery { oportunidadDao.obtenerPorId(5L) } returns null

        assertEquals(Resultado.Error("La oportunidad no existe"), repositorio.registrarSeguimiento(ana, gestion()))
    }

    @Test
    fun registrarSeguimiento_ejecutivoAjeno_devuelveError() = runTest {
        coEvery { oportunidadDao.obtenerPorId(5L) } returns oportunidad(responsableId = 1)

        assertEquals(
            Resultado.Error("No tienes acceso a esta oportunidad"),
            repositorio.registrarSeguimiento(carlos, gestion())
        )
    }

    @Test
    fun registrarSeguimiento_oportunidadCerrada_devuelveError() = runTest {
        coEvery { oportunidadDao.obtenerPorId(5L) } returns oportunidad(estado = EstadoOportunidad.OPORTUNIDAD_CERRADA)

        assertEquals(
            Resultado.Error("La oportunidad está cerrada o descartada y no admite nuevas gestiones"),
            repositorio.registrarSeguimiento(ana, gestion())
        )
        coVerify(exactly = 0) { seguimientoDao.insertar(any()) }
    }

    @Test
    fun registrarSeguimiento_valido_usaAlActorYNoPermiteCambiarEstado() = runTest {
        val guardado = slot<Seguimiento>()
        coEvery { oportunidadDao.obtenerPorId(5L) } returns oportunidad()
        coEvery { seguimientoDao.insertar(capture(guardado)) } returns 1L
        coJustRun { oportunidadDao.actualizar(any()) }

        val resultado = repositorio.registrarSeguimiento(ana, gestion())

        assertEquals(Resultado.Exito("Gestión registrada"), resultado)
        assertEquals(0L, guardado.captured.id)
        assertEquals(1L, guardado.captured.usuarioId)
        assertNull(guardado.captured.estadoNuevo)
        coVerify { oportunidadDao.actualizar(any()) }
    }

    // ---------- asignar ----------

    @Test
    fun asignar_ejecutivo_devuelveError() = runTest {
        assertEquals(
            Resultado.Error("Solo un supervisor puede asignar oportunidades"),
            repositorio.asignar(ana, 5L, 2L)
        )
    }

    @Test
    fun asignar_inexistente_devuelveError() = runTest {
        coEvery { oportunidadDao.obtenerPorId(5L) } returns null

        assertEquals(Resultado.Error("La oportunidad no existe"), repositorio.asignar(sofia, 5L, 2L))
    }

    @Test
    fun asignar_oportunidadDescartada_devuelveError() = runTest {
        coEvery { oportunidadDao.obtenerPorId(5L) } returns oportunidad(estado = EstadoOportunidad.OPORTUNIDAD_DESCARTADA)

        assertEquals(
            Resultado.Error("No se puede reasignar una oportunidad cerrada o descartada"),
            repositorio.asignar(sofia, 5L, 2L)
        )
    }

    @Test
    fun asignar_mismoResponsable_devuelveError() = runTest {
        coEvery { oportunidadDao.obtenerPorId(5L) } returns oportunidad(responsableId = 2)

        assertEquals(
            Resultado.Error("La oportunidad ya está asignada a ese ejecutivo"),
            repositorio.asignar(sofia, 5L, 2L)
        )
    }

    @Test
    fun asignar_nuevoResponsableInexistente_devuelveError() = runTest {
        coEvery { oportunidadDao.obtenerPorId(5L) } returns oportunidad(responsableId = 1)
        coEvery { usuarioDao.obtenerPorId(2L) } returns null

        assertEquals(
            Resultado.Error("Solo se puede asignar a un ejecutivo activo"),
            repositorio.asignar(sofia, 5L, 2L)
        )
    }

    @Test
    fun asignar_nuevoResponsableInactivo_devuelveError() = runTest {
        coEvery { oportunidadDao.obtenerPorId(5L) } returns oportunidad(responsableId = 1)
        coEvery { usuarioDao.obtenerPorId(2L) } returns carlos.copy(activo = false)

        assertEquals(
            Resultado.Error("Solo se puede asignar a un ejecutivo activo"),
            repositorio.asignar(sofia, 5L, 2L)
        )
    }

    @Test
    fun asignar_nuevoResponsableSupervisor_devuelveError() = runTest {
        coEvery { oportunidadDao.obtenerPorId(5L) } returns oportunidad(responsableId = 1)
        coEvery { usuarioDao.obtenerPorId(11L) } returns sofia.copy(id = 11)

        assertEquals(
            Resultado.Error("Solo se puede asignar a un ejecutivo activo"),
            repositorio.asignar(sofia, 5L, 11L)
        )
    }

    @Test
    fun asignar_valido_cambiaElResponsableYDejaConstancia() = runTest {
        val guardada = slot<Oportunidad>()
        val historial = slot<Seguimiento>()
        coEvery { oportunidadDao.obtenerPorId(5L) } returns oportunidad(responsableId = 1)
        coEvery { usuarioDao.obtenerPorId(2L) } returns carlos
        coEvery { usuarioDao.obtenerPorId(1L) } returns ana
        coJustRun { oportunidadDao.actualizar(capture(guardada)) }
        coEvery { seguimientoDao.insertar(capture(historial)) } returns 1L

        val resultado = repositorio.asignar(sofia, 5L, 2L)

        assertEquals(Resultado.Exito("Asignada a Carlos (demo)"), resultado)
        assertEquals(2L, guardada.captured.responsableId)
        assertEquals("Reasignada de Ana (demo) a Carlos (demo)", historial.captured.resultado)
        assertEquals(10L, historial.captured.usuarioId)
    }

    @Test
    fun asignar_sinResponsableAnterior_indicaSinResponsable() = runTest {
        val historial = slot<Seguimiento>()
        coEvery { oportunidadDao.obtenerPorId(5L) } returns oportunidad(responsableId = 1)
        coEvery { usuarioDao.obtenerPorId(2L) } returns carlos
        coEvery { usuarioDao.obtenerPorId(1L) } returns null
        coJustRun { oportunidadDao.actualizar(any()) }
        coEvery { seguimientoDao.insertar(capture(historial)) } returns 1L

        repositorio.asignar(sofia, 5L, 2L)

        assertEquals("Reasignada de sin responsable a Carlos (demo)", historial.captured.resultado)
    }
}