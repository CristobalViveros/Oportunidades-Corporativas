package com.example.appproyecto.data.repository

import com.example.appproyecto.data.local.OportunidadDao
import com.example.appproyecto.data.local.SeguimientoDao
import com.example.appproyecto.data.local.UsuarioDao
import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.Oportunidad
import com.example.appproyecto.data.model.OportunidadDetalle
import com.example.appproyecto.data.model.OportunidadItem
import com.example.appproyecto.data.model.Perfil
import com.example.appproyecto.data.model.Prioridad
import com.example.appproyecto.data.model.Resultado
import com.example.appproyecto.data.model.SeguimientoItem
import com.example.appproyecto.data.model.SeguimientoPendiente
import com.example.appproyecto.data.model.Usuario
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OportunidadRepositoryConsultasTest {

    private val oportunidadDao = mockk<OportunidadDao>()
    private val seguimientoDao = mockk<SeguimientoDao>()
    private val usuarioDao = mockk<UsuarioDao>()
    private val repositorio = OportunidadRepository(oportunidadDao, seguimientoDao, usuarioDao)

    private val ejecutivo = Usuario(id = 1, nombre = "Ana (demo)", correo = "ana@ejemplo.cl", passwordHash = "x")
    private val otroEjecutivo = Usuario(id = 2, nombre = "Carlos (demo)", correo = "carlos@ejemplo.cl", passwordHash = "x")
    private val supervisor = Usuario(
        id = 10, nombre = "Sofía (demo)", correo = "sofia@ejemplo.cl", passwordHash = "x",
        perfil = Perfil.SUPERVISOR.name
    )

    private fun oportunidad(
        responsableId: Long = 1,
        estado: EstadoOportunidad = EstadoOportunidad.CONTACTO_PENDIENTE
    ) = Oportunidad(
        id = 5, nombre = "Feria Demo", eventoId = 1, responsableId = responsableId,
        estado = estado.name, prioridad = Prioridad.MEDIA.name
    )

    private fun item() = OportunidadItem(
        id = 5, nombre = "Feria Demo", estado = "CONTACTO_PENDIENTE", prioridad = "MEDIA", potencial = 3,
        eventoNombre = "Feria", eventoFecha = 2000L, comuna = "Santiago",
        organizacionNombre = "Org", responsableId = 1, responsableNombre = "Ana (demo)"
    )

    private fun pendiente() = SeguimientoPendiente(
        oportunidadId = 5, oportunidadNombre = "Feria Demo", estado = "CONTACTO_PENDIENTE",
        responsableId = 1, responsableNombre = "Ana (demo)", proximaAccion = "Llamar", fecha = 5000L
    )

    private fun detalle(responsableId: Long = 1) = OportunidadDetalle(
        id = 5, nombre = "Feria Demo", tipo = "OTRA", descripcion = "", fuente = "", fechaDeteccion = 0L,
        prioridad = "MEDIA", potencial = 3, afinidad = 3, estado = "CONTACTO_PENDIENTE",
        fechaLimitePostulacion = null, fechaActualizacion = 0L, observaciones = "",
        responsableId = responsableId, responsableNombre = "Ana (demo)", eventoId = 1,
        eventoNombre = "Feria", fechaInicio = 2000L, fechaTermino = 3000L, horario = "",
        direccion = "", comuna = "Santiago", region = "Metropolitana", publicoEsperado = 100,
        publicoObjetivo = "", condiciones = "", costoReferencial = 0, fotoUri = null,
        organizacionId = 1, organizacionNombre = "Org"
    )

    private fun historial() = SeguimientoItem(
        id = 1, tipoAccion = "SEGUIMIENTO", fechaHora = 1000L, resultado = "Llamada realizada",
        proximaAccion = "", fechaProximoSeguimiento = null, estadoNuevo = null, observaciones = "",
        usuarioNombre = "Ana (demo)", contactoNombre = null
    )

    // ---------- consultas por rol ----------

    @Test
    fun observarItemsPara_supervisor_veTodas() = runTest {
        every { oportunidadDao.observarItems() } returns flowOf(listOf(item(), item()))

        assertEquals(2, repositorio.observarItemsPara(supervisor).first().size)
    }

    @Test
    fun observarItemsPara_ejecutivo_veSoloLasSuyas() = runTest {
        every { oportunidadDao.observarItemsPorResponsable(1L) } returns flowOf(listOf(item()))

        assertEquals(1, repositorio.observarItemsPara(ejecutivo).first().size)
    }

    @Test
    fun observarPendientesPara_supervisor_pideTodas() = runTest {
        every { seguimientoDao.observarPendientes(0L) } returns flowOf(listOf(pendiente(), pendiente()))

        assertEquals(2, repositorio.observarPendientesPara(supervisor).first().size)
    }

    @Test
    fun observarPendientesPara_ejecutivo_pideSoloLasSuyas() = runTest {
        every { seguimientoDao.observarPendientes(1L) } returns flowOf(listOf(pendiente()))

        assertEquals(1, repositorio.observarPendientesPara(ejecutivo).first().size)
    }

    // ---------- obtener y detalle ----------

    @Test
    fun obtenerPara_duenio_devuelveLaOportunidad() = runTest {
        coEvery { oportunidadDao.obtenerPorId(5L) } returns oportunidad(responsableId = 1)

        assertNotNull(repositorio.obtenerPara(ejecutivo, 5L))
    }

    @Test
    fun obtenerPara_ejecutivoAjeno_devuelveNull() = runTest {
        coEvery { oportunidadDao.obtenerPorId(5L) } returns oportunidad(responsableId = 1)

        assertNull(repositorio.obtenerPara(otroEjecutivo, 5L))
    }

    @Test
    fun obtenerPara_inexistente_devuelveNull() = runTest {
        coEvery { oportunidadDao.obtenerPorId(5L) } returns null

        assertNull(repositorio.obtenerPara(supervisor, 5L))
    }

    @Test
    fun observarDetallePara_duenioYSupervisor_ven() = runTest {
        every { oportunidadDao.observarDetalle(5L) } returns flowOf(detalle(responsableId = 1))

        assertNotNull(repositorio.observarDetallePara(ejecutivo, 5L).first())
        assertNotNull(repositorio.observarDetallePara(supervisor, 5L).first())
    }

    @Test
    fun observarDetallePara_ejecutivoAjeno_emiteNull() = runTest {
        every { oportunidadDao.observarDetalle(5L) } returns flowOf(detalle(responsableId = 1))

        assertNull(repositorio.observarDetallePara(otroEjecutivo, 5L).first())
    }

    @Test
    fun observarDetallePara_cuentaInactiva_emiteNull() = runTest {
        every { oportunidadDao.observarDetalle(5L) } returns flowOf(detalle(responsableId = 1))

        assertNull(repositorio.observarDetallePara(ejecutivo.copy(activo = false), 5L).first())
    }

    @Test
    fun observarDetallePara_inexistente_emiteNull() = runTest {
        every { oportunidadDao.observarDetalle(5L) } returns flowOf(null)

        assertNull(repositorio.observarDetallePara(supervisor, 5L).first())
    }

    @Test
    fun observarHistorialPara_conPermiso_emiteElHistorial() = runTest {
        every { oportunidadDao.observarDetalle(5L) } returns flowOf(detalle(responsableId = 1))
        every { seguimientoDao.observarItemsPorOportunidad(5L) } returns flowOf(listOf(historial()))

        assertEquals(1, repositorio.observarHistorialPara(ejecutivo, 5L).first().size)
    }

    @Test
    fun observarHistorialPara_sinPermiso_emiteListaVacia() = runTest {
        every { oportunidadDao.observarDetalle(5L) } returns flowOf(detalle(responsableId = 1))

        assertTrue(repositorio.observarHistorialPara(otroEjecutivo, 5L).first().isEmpty())
    }

    // ---------- crear ----------

    @Test
    fun crear_cuentaInactiva_devuelveError() = runTest {
        val resultado = repositorio.crear(ejecutivo.copy(activo = false), oportunidad())

        assertEquals(Resultado.Error("Tu cuenta no está activa"), resultado)
    }

    @Test
    fun crear_potencialFueraDeRango_devuelveError() = runTest {
        val resultado = repositorio.crear(ejecutivo, oportunidad().copy(potencial = 0))

        assertEquals(Resultado.Error("El potencial y la afinidad deben estar entre 1 y 5"), resultado)
    }

    @Test
    fun crear_afinidadFueraDeRango_devuelveError() = runTest {
        val resultado = repositorio.crear(ejecutivo, oportunidad().copy(afinidad = 6))

        assertEquals(Resultado.Error("El potencial y la afinidad deben estar entre 1 y 5"), resultado)
    }

    @Test
    fun crear_ejecutivo_quedaASuNombreYEnEstadoInicial() = runTest {
        val guardada = slot<Oportunidad>()
        coEvery { oportunidadDao.insertar(capture(guardada)) } returns 9L

        val resultado = repositorio.crear(
            ejecutivo,
            oportunidad(responsableId = 2).copy(id = 99, estado = EstadoOportunidad.OPORTUNIDAD_CERRADA.name)
        )

        assertEquals(Resultado.Exito("Oportunidad creada"), resultado)
        assertEquals(0L, guardada.captured.id)
        assertEquals(1L, guardada.captured.responsableId)
        assertEquals(EstadoOportunidad.EVENTO_IDENTIFICADO.name, guardada.captured.estado)
    }

    @Test
    fun crear_supervisorConEjecutivoActivo_asignaAEseResponsable() = runTest {
        val guardada = slot<Oportunidad>()
        coEvery { usuarioDao.obtenerPorId(2L) } returns otroEjecutivo
        coEvery { oportunidadDao.insertar(capture(guardada)) } returns 9L

        val resultado = repositorio.crear(supervisor, oportunidad(responsableId = 2))

        assertEquals(Resultado.Exito("Oportunidad creada"), resultado)
        assertEquals(2L, guardada.captured.responsableId)
    }

    @Test
    fun crear_supervisorConResponsableInexistente_devuelveError() = runTest {
        coEvery { usuarioDao.obtenerPorId(2L) } returns null

        val resultado = repositorio.crear(supervisor, oportunidad(responsableId = 2))

        assertEquals(Resultado.Error("El responsable debe ser un ejecutivo activo"), resultado)
        coVerify(exactly = 0) { oportunidadDao.insertar(any()) }
    }

    @Test
    fun crear_supervisorConResponsableInactivo_devuelveError() = runTest {
        coEvery { usuarioDao.obtenerPorId(2L) } returns otroEjecutivo.copy(activo = false)

        val resultado = repositorio.crear(supervisor, oportunidad(responsableId = 2))

        assertEquals(Resultado.Error("El responsable debe ser un ejecutivo activo"), resultado)
    }

    @Test
    fun crear_supervisorConResponsableSupervisor_devuelveError() = runTest {
        coEvery { usuarioDao.obtenerPorId(11L) } returns supervisor.copy(id = 11)

        val resultado = repositorio.crear(supervisor, oportunidad(responsableId = 11))

        assertEquals(Resultado.Error("El responsable debe ser un ejecutivo activo"), resultado)
    }

    // ---------- actualizar ----------

    @Test
    fun actualizar_puntajesInvalidos_devuelveError() = runTest {
        val resultado = repositorio.actualizar(ejecutivo, oportunidad().copy(potencial = 9))

        assertEquals(Resultado.Error("El potencial y la afinidad deben estar entre 1 y 5"), resultado)
    }

    @Test
    fun actualizar_inexistente_devuelveError() = runTest {
        coEvery { oportunidadDao.obtenerPorId(5L) } returns null

        val resultado = repositorio.actualizar(ejecutivo, oportunidad())

        assertEquals(Resultado.Error("La oportunidad no existe"), resultado)
    }

    @Test
    fun actualizar_ejecutivoAjeno_devuelveError() = runTest {
        coEvery { oportunidadDao.obtenerPorId(5L) } returns oportunidad(responsableId = 1)

        val resultado = repositorio.actualizar(otroEjecutivo, oportunidad())

        assertEquals(Resultado.Error("No tienes acceso a esta oportunidad"), resultado)
        coVerify(exactly = 0) { oportunidadDao.actualizar(any()) }
    }

    @Test
    fun actualizar_ejecutivo_noCambiaEstadoResponsableNiPrioridad() = runTest {
        val guardada = slot<Oportunidad>()
        val actual = oportunidad(responsableId = 1)
        coEvery { oportunidadDao.obtenerPorId(5L) } returns actual
        coJustRun { oportunidadDao.actualizar(capture(guardada)) }

        val resultado = repositorio.actualizar(
            ejecutivo,
            actual.copy(
                nombre = "Nombre nuevo", responsableId = 2,
                estado = EstadoOportunidad.OPORTUNIDAD_CERRADA.name, prioridad = Prioridad.ALTA.name
            )
        )

        assertEquals(Resultado.Exito("Oportunidad actualizada"), resultado)
        assertEquals("Nombre nuevo", guardada.captured.nombre)
        assertEquals(1L, guardada.captured.responsableId)
        assertEquals(actual.estado, guardada.captured.estado)
        assertEquals(Prioridad.MEDIA.name, guardada.captured.prioridad)
    }

    @Test
    fun actualizar_supervisor_siPuedeCambiarLaPrioridad() = runTest {
        val guardada = slot<Oportunidad>()
        val actual = oportunidad(responsableId = 1)
        coEvery { oportunidadDao.obtenerPorId(5L) } returns actual
        coJustRun { oportunidadDao.actualizar(capture(guardada)) }

        val resultado = repositorio.actualizar(supervisor, actual.copy(prioridad = Prioridad.ALTA.name))

        assertEquals(Resultado.Exito("Oportunidad actualizada"), resultado)
        assertEquals(Prioridad.ALTA.name, guardada.captured.prioridad)
    }
}