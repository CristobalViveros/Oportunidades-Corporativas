package com.example.appproyecto.ui.detalle

import androidx.lifecycle.SavedStateHandle
import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.Oportunidad
import com.example.appproyecto.data.model.OportunidadDetalle
import com.example.appproyecto.data.model.Perfil
import com.example.appproyecto.data.model.Resultado
import com.example.appproyecto.data.model.Seguimiento
import com.example.appproyecto.data.model.SeguimientoItem
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.ui.EntornoViewModel
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetalleOportunidadViewModelTest {

    private val entorno = EntornoViewModel()

    @After
    fun limpiar() = entorno.terminar()

    private val ana = Usuario(id = 1, nombre = "Ana (demo)", correo = "ana@ejemplo.cl", passwordHash = "x")
    private val sofia = Usuario(
        id = 10, nombre = "Sofía (demo)", correo = "sofia@ejemplo.cl", passwordHash = "x",
        perfil = Perfil.SUPERVISOR.name
    )

    private fun detalle(estado: EstadoOportunidad, responsableId: Long = 1L) = OportunidadDetalle(
        id = 10, nombre = "Feria Andina", tipo = "EVENTO_CORPORATIVO", descripcion = "", fuente = "",
        fechaDeteccion = 1000L, prioridad = "MEDIA", potencial = 3, afinidad = 3, estado = estado.name,
        fechaLimitePostulacion = null, fechaActualizacion = 1000L, observaciones = "",
        responsableId = responsableId, responsableNombre = "Ana (demo)", eventoId = 1,
        eventoNombre = "Feria 2026", fechaInicio = 2000L, fechaTermino = 3000L, horario = "10:00",
        direccion = "Calle 1", comuna = "Santiago", region = "Metropolitana", publicoEsperado = 100,
        publicoObjetivo = "", condiciones = "", costoReferencial = 0, fotoUri = null,
        organizacionId = 3, organizacionNombre = "Org"
    )

    private fun gestion(id: Long, fechaProximo: Long?) = SeguimientoItem(
        id = id, tipoAccion = "LLAMADA", fechaHora = 1000L, resultado = "Habló con el contacto",
        proximaAccion = "", fechaProximoSeguimiento = fechaProximo, estadoNuevo = null,
        observaciones = "", usuarioNombre = "Ana (demo)", contactoNombre = null
    )

    private fun TestScope.crear(
        usuario: Usuario? = ana,
        detalle: OportunidadDetalle? = detalle(EstadoOportunidad.CONTACTO_PENDIENTE),
        historial: List<SeguimientoItem> = emptyList()
    ): DetalleOportunidadViewModel {
        entorno.iniciar(usuarioIdEnSesion = usuario?.id)
        if (usuario != null) coEvery { entorno.usuarioDao.obtenerPorId(usuario.id) } returns usuario
        every { entorno.oportunidadDao.observarDetalle(10L) } returns flowOf(detalle)
        every { entorno.seguimientoDao.observarItemsPorOportunidad(10L) } returns flowOf(historial)
        val vm = DetalleOportunidadViewModel(entorno.application, SavedStateHandle(mapOf("id" to 10L)))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        return vm
    }

    private fun oportunidadEnBd(estado: EstadoOportunidad) = Oportunidad(
        id = 10, nombre = "Feria Andina", eventoId = 1, responsableId = 1, estado = estado.name
    )

    // ---------- carga y permisos de vista ----------

    @Test
    fun sinSesion_muestraSinAcceso() = runTest {
        val estado = crear(usuario = null).uiState.value

        assertTrue(estado.sinAcceso)
        assertFalse(estado.cargando)
    }

    @Test
    fun usuarioInactivo_muestraSinAcceso() = runTest {
        val estado = crear(usuario = ana.copy(activo = false)).uiState.value

        assertTrue(estado.sinAcceso)
        assertFalse(estado.cargando)
    }

    @Test
    fun oportunidadInexistente_muestraSinAcceso() = runTest {
        val estado = crear(detalle = null).uiState.value

        assertTrue(estado.sinAcceso)
        assertFalse(estado.cargando)
        assertNull(estado.detalle)
    }

    @Test
    fun ejecutivo_noVeLaOportunidadDeOtro() = runTest {
        val estado = crear(detalle = detalle(EstadoOportunidad.CONTACTO_PENDIENTE, responsableId = 2L)).uiState.value

        assertTrue(estado.sinAcceso)
        assertNull(estado.detalle)
    }

    @Test
    fun ejecutivo_veSuOportunidadConSusEstadosDestino() = runTest {
        val estado = crear().uiState.value

        assertFalse(estado.cargando)
        assertFalse(estado.sinAcceso)
        assertEquals(Perfil.EJECUTIVO, estado.rol)
        assertEquals("Feria Andina", estado.detalle?.nombre)
        assertEquals(listOf(EstadoOportunidad.CONTACTO_REALIZADO), estado.estadosDestino)
        assertTrue(estado.puedeRegistrarGestion)
        assertNull(estado.proximo)
    }

    @Test
    fun supervisor_veOportunidadesAjenasYPuedeDecidir() = runTest {
        val estado = crear(
            usuario = sofia,
            detalle = detalle(EstadoOportunidad.EN_EVALUACION, responsableId = 2L)
        ).uiState.value

        assertEquals(Perfil.SUPERVISOR, estado.rol)
        assertTrue(EstadoOportunidad.PARTICIPACION_CONFIRMADA in estado.estadosDestino)
        assertTrue(EstadoOportunidad.OPORTUNIDAD_DESCARTADA in estado.estadosDestino)
    }

    @Test
    fun supervisor_puedeReabrirUnaOportunidadDescartada() = runTest {
        val estado = crear(
            usuario = sofia,
            detalle = detalle(EstadoOportunidad.OPORTUNIDAD_DESCARTADA)
        ).uiState.value

        assertEquals(listOf(EstadoOportunidad.EN_EVALUACION), estado.estadosDestino)
        assertFalse(estado.puedeRegistrarGestion)
    }

    @Test
    fun oportunidadCerrada_noAdmiteGestionesNiCambiosParaEjecutivo() = runTest {
        val estado = crear(detalle = detalle(EstadoOportunidad.OPORTUNIDAD_CERRADA)).uiState.value

        assertFalse(estado.puedeRegistrarGestion)
        assertTrue(estado.estadosDestino.isEmpty())
    }

    @Test
    fun proximo_esLaGestionMasRecienteConFechaProgramada() = runTest {
        val historial = listOf(gestion(3, null), gestion(2, 5000L), gestion(1, 4000L))

        val estado = crear(historial = historial).uiState.value

        assertEquals(3, estado.historial.size)
        assertEquals(2L, estado.proximo?.id)
    }

    // ---------- diálogo y resultado ----------

    @Test
    fun abrirYCerrarDialogoEstado_actualizaElEstado() = runTest {
        val vm = crear()

        vm.abrirDialogoEstado()
        assertTrue(vm.uiState.value.dialogoEstado)

        vm.cerrarDialogoEstado()
        assertFalse(vm.uiState.value.dialogoEstado)
    }

    // ---------- cambiarEstado ----------

    @Test
    fun cambiarEstado_valido_actualizaYRegistraEnElHistorial() = runTest {
        val guardado = slot<Seguimiento>()
        val vm = crear()
        coEvery { entorno.oportunidadDao.obtenerPorId(10L) } returns oportunidadEnBd(EstadoOportunidad.CONTACTO_PENDIENTE)
        coJustRun { entorno.oportunidadDao.actualizar(any()) }
        coEvery { entorno.seguimientoDao.insertar(capture(guardado)) } returns 1L
        vm.abrirDialogoEstado()

        vm.cambiarEstado(EstadoOportunidad.CONTACTO_REALIZADO, "  Ya hablamos  ")

        val estado = vm.uiState.value
        assertTrue(estado.resultado is Resultado.Exito)
        assertFalse(estado.dialogoEstado)
        assertEquals("Ya hablamos", guardado.captured.observaciones)
        assertEquals(EstadoOportunidad.CONTACTO_REALIZADO.name, guardado.captured.estadoNuevo)
    }

    @Test
    fun cambiarEstado_observacionMuyLarga_muestraErrorYNoLlamaALaBd() = runTest {
        val vm = crear()

        vm.cambiarEstado(EstadoOportunidad.CONTACTO_REALIZADO, "a".repeat(301))

        val resultado = vm.uiState.value.resultado
        assertTrue(resultado is Resultado.Error)
        assertEquals("La observación no puede superar 300 caracteres", resultado?.mensaje)
        coVerify(exactly = 0) { entorno.oportunidadDao.actualizar(any()) }
    }

    @Test
    fun cambiarEstado_transicionNoPermitida_muestraErrorDelRepositorio() = runTest {
        val vm = crear()
        coEvery { entorno.oportunidadDao.obtenerPorId(10L) } returns oportunidadEnBd(EstadoOportunidad.CONTACTO_PENDIENTE)

        vm.cambiarEstado(EstadoOportunidad.PROPUESTA_ENVIADA, "")

        assertTrue(vm.uiState.value.resultado is Resultado.Error)
        coVerify(exactly = 0) { entorno.seguimientoDao.insertar(any()) }
    }

    @Test
    fun cambiarEstado_ejecutivoQuiereConfirmar_exigeSupervisor() = runTest {
        val vm = crear(detalle = detalle(EstadoOportunidad.EN_EVALUACION))
        coEvery { entorno.oportunidadDao.obtenerPorId(10L) } returns oportunidadEnBd(EstadoOportunidad.EN_EVALUACION)

        vm.cambiarEstado(EstadoOportunidad.PARTICIPACION_CONFIRMADA, "")

        assertEquals(
            "Solo un supervisor puede confirmar o descartar una oportunidad",
            vm.uiState.value.resultado?.mensaje
        )
    }

    @Test
    fun cambiarEstado_sinSesion_noHaceNada() = runTest {
        val vm = crear(usuario = null)

        vm.cambiarEstado(EstadoOportunidad.CONTACTO_REALIZADO, "")

        assertNull(vm.uiState.value.resultado)
        coVerify(exactly = 0) { entorno.oportunidadDao.obtenerPorId(any()) }
    }

    @Test
    fun consumirResultado_limpiaElMensaje() = runTest {
        val vm = crear()
        vm.cambiarEstado(EstadoOportunidad.CONTACTO_REALIZADO, "a".repeat(301))
        assertTrue(vm.uiState.value.resultado != null)

        vm.consumirResultado()

        assertNull(vm.uiState.value.resultado)
    }
}