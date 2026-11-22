package com.example.appproyecto.ui.supervisor

import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.Oportunidad
import com.example.appproyecto.data.model.OportunidadItem
import com.example.appproyecto.data.model.Perfil
import com.example.appproyecto.data.model.Resultado
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.ui.EntornoViewModel
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
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
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PanelSupervisorViewModelTest {

    private val entorno = EntornoViewModel()

    @After
    fun limpiar() = entorno.terminar()

    private val ana = Usuario(id = 1, nombre = "Ana (demo)", correo = "ana@ejemplo.cl", passwordHash = "x")
    private val carlos = Usuario(id = 2, nombre = "Carlos (demo)", correo = "carlos@ejemplo.cl", passwordHash = "x")
    private val sofia = Usuario(
        id = 10, nombre = "Sofía (demo)", correo = "sofia@ejemplo.cl", passwordHash = "x",
        perfil = Perfil.SUPERVISOR.name
    )

    private val item = OportunidadItem(
        id = 5, nombre = "Feria Demo", estado = "CONTACTO_PENDIENTE", prioridad = "MEDIA", potencial = 3,
        eventoNombre = "Feria", eventoFecha = 2000L, comuna = "Santiago",
        organizacionNombre = "Org", responsableId = 1, responsableNombre = "Ana (demo)"
    )

    private fun oportunidad(responsableId: Long) = Oportunidad(
        id = 5, nombre = "Feria Demo", eventoId = 1, responsableId = responsableId,
        estado = EstadoOportunidad.CONTACTO_PENDIENTE.name
    )

    /** Prepara la sesión del supervisor y los datos que la pantalla observa. */
    private fun prepararSupervisor() {
        entorno.iniciar(usuarioIdEnSesion = 10L)
        coEvery { entorno.usuarioDao.obtenerPorId(10L) } returns sofia
        every { entorno.oportunidadDao.observarItems() } returns flowOf(listOf(item))
        every { entorno.usuarioDao.observarEjecutivos() } returns flowOf(listOf(ana, carlos))
    }

    private fun TestScope.crearYObservar(): PanelSupervisorViewModel {
        val vm = PanelSupervisorViewModel(entorno.application)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        return vm
    }

    @Test
    fun alCargar_muestraOportunidadesYEjecutivos() = runTest {
        prepararSupervisor()

        val estado = crearYObservar().uiState.value

        assertFalse(estado.cargando)
        assertEquals(1, estado.oportunidades.size)
        assertEquals(2, estado.ejecutivos.size)
        assertNull(estado.reasignando)
        assertNull(estado.resultado)
    }

    @Test
    fun abrirYCerrarReasignar_actualizanElDialogo() = runTest {
        prepararSupervisor()
        val vm = crearYObservar()

        vm.abrirReasignar(item)
        assertEquals(item, vm.uiState.value.reasignando)

        vm.cerrarReasignar()
        assertNull(vm.uiState.value.reasignando)
    }

    @Test
    fun confirmarReasignar_valido_asignaYMuestraResultado() = runTest {
        prepararSupervisor()
        coEvery { entorno.oportunidadDao.obtenerPorId(5L) } returns oportunidad(responsableId = 1)
        coEvery { entorno.usuarioDao.obtenerPorId(2L) } returns carlos
        coEvery { entorno.usuarioDao.obtenerPorId(1L) } returns ana
        coJustRun { entorno.oportunidadDao.actualizar(any()) }
        coEvery { entorno.seguimientoDao.insertar(any()) } returns 1L
        val vm = crearYObservar()

        vm.abrirReasignar(item)
        vm.confirmarReasignar(2L)

        assertEquals(Resultado.Exito("Asignada a Carlos (demo)"), vm.uiState.value.resultado)
        assertNull(vm.uiState.value.reasignando)
    }

    @Test
    fun confirmarReasignar_alMismoResponsable_muestraError() = runTest {
        prepararSupervisor()
        coEvery { entorno.oportunidadDao.obtenerPorId(5L) } returns oportunidad(responsableId = 2)
        val vm = crearYObservar()

        vm.abrirReasignar(item)
        vm.confirmarReasignar(2L)

        assertEquals(
            Resultado.Error("La oportunidad ya está asignada a ese ejecutivo"),
            vm.uiState.value.resultado
        )
    }

    @Test
    fun confirmarReasignar_sinDialogoAbierto_noHaceNada() = runTest {
        prepararSupervisor()
        val vm = crearYObservar()

        vm.confirmarReasignar(2L)

        assertNull(vm.uiState.value.resultado)
        coVerify(exactly = 0) { entorno.oportunidadDao.obtenerPorId(any()) }
    }

    @Test
    fun consumirResultado_limpiaElMensaje() = runTest {
        prepararSupervisor()
        coEvery { entorno.oportunidadDao.obtenerPorId(5L) } returns oportunidad(responsableId = 2)
        val vm = crearYObservar()
        vm.abrirReasignar(item)
        vm.confirmarReasignar(2L)

        vm.consumirResultado()

        assertNull(vm.uiState.value.resultado)
    }
}