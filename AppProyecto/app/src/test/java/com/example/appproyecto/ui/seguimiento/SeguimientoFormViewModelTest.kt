package com.example.appproyecto.ui.seguimiento

import androidx.lifecycle.SavedStateHandle
import com.example.appproyecto.data.model.Contacto
import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.Oportunidad
import com.example.appproyecto.data.model.OportunidadDetalle
import com.example.appproyecto.data.model.Seguimiento
import com.example.appproyecto.data.model.TipoAccion
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.ui.EntornoViewModel
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SeguimientoFormViewModelTest {

    private val entorno = EntornoViewModel()

    @After
    fun limpiar() = entorno.terminar()

    private val ana = Usuario(id = 1, nombre = "Ana (demo)", correo = "ana@ejemplo.cl", passwordHash = "x")
    private val contacto = Contacto(id = 4, organizacionId = 3, nombre = "Marta Díaz")
    private val manana = System.currentTimeMillis() + 86_400_000L

    private val detalle = OportunidadDetalle(
        id = 10, nombre = "Feria Andina", tipo = "EVENTO_CORPORATIVO", descripcion = "", fuente = "",
        fechaDeteccion = 1000L, prioridad = "MEDIA", potencial = 3, afinidad = 3,
        estado = EstadoOportunidad.CONTACTO_PENDIENTE.name, fechaLimitePostulacion = null,
        fechaActualizacion = 1000L, observaciones = "", responsableId = 1, responsableNombre = "Ana (demo)",
        eventoId = 1, eventoNombre = "Feria 2026", fechaInicio = 2000L, fechaTermino = 3000L,
        horario = "10:00", direccion = "Calle 1", comuna = "Santiago", region = "Metropolitana",
        publicoEsperado = 100, publicoObjetivo = "", condiciones = "", costoReferencial = 0, fotoUri = null,
        organizacionId = 3, organizacionNombre = "Org"
    )

    private fun oportunidadEnBd(estado: EstadoOportunidad) = Oportunidad(
        id = 10, nombre = "Feria Andina", eventoId = 1, responsableId = 1, estado = estado.name
    )

    private fun crear(
        conSesion: Boolean = true,
        detalleVisible: OportunidadDetalle? = detalle,
        contactos: List<Contacto> = listOf(contacto)
    ): SeguimientoFormViewModel {
        entorno.iniciar(usuarioIdEnSesion = if (conSesion) 1L else null)
        coEvery { entorno.usuarioDao.obtenerPorId(1L) } returns ana
        every { entorno.oportunidadDao.observarDetalle(10L) } returns flowOf(detalleVisible)
        every { entorno.contactoDao.observarPorOrganizacion(3L) } returns flowOf(contactos)
        return SeguimientoFormViewModel(entorno.application, SavedStateHandle(mapOf("id" to 10L)))
    }

    /** Deja la BD lista para que un guardado válido termine bien. */
    private fun SeguimientoFormViewModelTest.prepararGuardado(estado: EstadoOportunidad = EstadoOportunidad.CONTACTO_PENDIENTE) {
        coEvery { entorno.oportunidadDao.obtenerPorId(10L) } returns oportunidadEnBd(estado)
        coJustRun { entorno.oportunidadDao.actualizar(any()) }
    }

    // ---------- carga ----------

    @Test
    fun alIniciar_cargaElNombreYLosContactosDeLaOrganizacion() {
        val estado = crear().uiState.value

        assertFalse(estado.cargando)
        assertFalse(estado.bloqueado)
        assertEquals("Feria Andina", estado.nombreOportunidad)
        assertEquals(listOf(contacto), estado.contactos)
    }

    @Test
    fun sinSesion_quedaBloqueado() {
        val estado = crear(conSesion = false).uiState.value

        assertTrue(estado.bloqueado)
        assertFalse(estado.cargando)
        assertEquals("No tienes acceso a esta oportunidad", estado.errorGeneral)
    }

    @Test
    fun oportunidadSinAcceso_quedaBloqueado() {
        val estado = crear(detalleVisible = null).uiState.value

        assertTrue(estado.bloqueado)
        assertEquals("No tienes acceso a esta oportunidad", estado.errorGeneral)
    }

    @Test
    fun bloqueado_guardarNoHaceNada() {
        val vm = crear(conSesion = false)

        vm.guardar()

        coVerify(exactly = 0) { entorno.seguimientoDao.insertar(any()) }
    }

    // ---------- campos ----------

    @Test
    fun cambiarCampos_actualizaElEstado() {
        val vm = crear()

        vm.cambiarTipo(TipoAccion.LLAMADA)
        vm.elegirContacto(4L)
        vm.cambiarResultado("Habló con el contacto")
        vm.cambiarProximaAccion("Enviar propuesta")
        vm.fijarFecha(manana)
        vm.cambiarObservaciones("Nota")

        val e = vm.uiState.value
        assertEquals(TipoAccion.LLAMADA, e.tipo)
        assertEquals(4L, e.contactoId)
        assertEquals("Habló con el contacto", e.resultado)
        assertEquals("Enviar propuesta", e.proximaAccion)
        assertEquals(manana, e.fechaProximo)
        assertEquals("Nota", e.observaciones)
    }

    @Test
    fun quitarFecha_dejaLaFechaEnNulo() {
        val vm = crear()
        vm.fijarFecha(manana)

        vm.quitarFecha()

        assertNull(vm.uiState.value.fechaProximo)
    }

    @Test
    fun cambiarUnCampo_limpiaSuError() {
        val vm = crear()
        vm.cambiarTipo(TipoAccion.LLAMADA)
        vm.guardar()
        assertNotNull(vm.uiState.value.errorContacto)
        assertNotNull(vm.uiState.value.errorResultado)

        vm.elegirContacto(4L)
        vm.cambiarResultado("Habló con el contacto")

        assertNull(vm.uiState.value.errorContacto)
        assertNull(vm.uiState.value.errorResultado)
    }

    // ---------- guardar: validaciones ----------

    @Test
    fun guardar_sinResultado_muestraErrorYNoGuarda() {
        val vm = crear()

        vm.guardar()

        assertEquals("El resultado es obligatorio", vm.uiState.value.errorResultado)
        assertNull(vm.uiState.value.errorContacto)
        coVerify(exactly = 0) { entorno.seguimientoDao.insertar(any()) }
    }

    @Test
    fun guardar_accionConContactoSinElegirlo_muestraError() {
        val vm = crear()
        vm.cambiarTipo(TipoAccion.LLAMADA)
        vm.cambiarResultado("Habló con el contacto")

        vm.guardar()

        assertEquals("Selecciona un contacto", vm.uiState.value.errorContacto)
    }

    @Test
    fun guardar_organizacionSinContactos_muestraErrorEspecifico() {
        val vm = crear(contactos = emptyList())
        vm.cambiarTipo(TipoAccion.REUNION)
        vm.cambiarResultado("Reunión realizada")

        vm.guardar()

        assertEquals("La organización aún no tiene contactos registrados", vm.uiState.value.errorContacto)
    }

    @Test
    fun guardar_fechaSinProximaAccion_pideLaAccion() {
        val vm = crear()
        vm.cambiarResultado("Habló con el contacto")
        vm.fijarFecha(manana)

        vm.guardar()

        assertEquals("Indica cuál será la próxima acción", vm.uiState.value.errorProximaAccion)
    }

    @Test
    fun guardar_accionSinFecha_pideLaFecha() {
        val vm = crear()
        vm.cambiarResultado("Habló con el contacto")
        vm.cambiarProximaAccion("Enviar propuesta")

        vm.guardar()

        assertEquals("Selecciona la fecha del próximo seguimiento", vm.uiState.value.errorFecha)
    }

    @Test
    fun guardar_fechaPasada_muestraError() {
        val vm = crear()
        vm.cambiarResultado("Habló con el contacto")
        vm.cambiarProximaAccion("Enviar propuesta")
        vm.fijarFecha(1000L)

        vm.guardar()

        assertEquals("La fecha no puede ser anterior a hoy", vm.uiState.value.errorFecha)
    }

    @Test
    fun guardar_observacionesMuyLargas_muestraError() {
        val vm = crear()
        vm.cambiarResultado("Habló con el contacto")
        vm.cambiarObservaciones("a".repeat(301))

        vm.guardar()

        assertEquals("Las observaciones no puede superar 300 caracteres", vm.uiState.value.errorObservaciones)
    }

    // ---------- guardar: resultado ----------

    @Test
    fun guardar_accionSinContacto_ignoraElContactoElegido() {
        val guardado = slot<Seguimiento>()
        val vm = crear()
        prepararGuardado()
        coEvery { entorno.seguimientoDao.insertar(capture(guardado)) } returns 1L
        vm.elegirContacto(4L) // el tipo por defecto (SEGUIMIENTO) no usa contacto
        vm.cambiarResultado("  Revisé el estado  ")

        vm.guardar()

        assertTrue(vm.uiState.value.guardado)
        assertFalse(vm.uiState.value.guardando)
        assertNull(guardado.captured.contactoId)
        assertEquals("Revisé el estado", guardado.captured.resultado)
        assertEquals(1L, guardado.captured.usuarioId)
        assertEquals(TipoAccion.SEGUIMIENTO.name, guardado.captured.tipoAccion)
    }

    @Test
    fun guardar_llamadaConContactoYProximaAccion_losGuarda() {
        val guardado = slot<Seguimiento>()
        val vm = crear()
        prepararGuardado()
        coEvery { entorno.seguimientoDao.insertar(capture(guardado)) } returns 1L
        vm.cambiarTipo(TipoAccion.LLAMADA)
        vm.elegirContacto(4L)
        vm.cambiarResultado("Habló con el contacto")
        vm.cambiarProximaAccion(" Enviar propuesta ")
        vm.fijarFecha(manana)

        vm.guardar()

        assertTrue(vm.uiState.value.guardado)
        assertEquals(4L, guardado.captured.contactoId)
        assertEquals("Enviar propuesta", guardado.captured.proximaAccion)
        assertEquals(manana, guardado.captured.fechaProximoSeguimiento)
        assertEquals(TipoAccion.LLAMADA.name, guardado.captured.tipoAccion)
    }

    @Test
    fun guardar_oportunidadCerrada_muestraElErrorDelRepositorio() {
        val vm = crear()
        prepararGuardado(EstadoOportunidad.OPORTUNIDAD_CERRADA)
        vm.cambiarResultado("Habló con el contacto")

        vm.guardar()

        assertEquals(
            "La oportunidad está cerrada o descartada y no admite nuevas gestiones",
            vm.uiState.value.errorGeneral
        )
        assertFalse(vm.uiState.value.guardado)
        assertFalse(vm.uiState.value.guardando)
    }
}