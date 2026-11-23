package com.example.appproyecto.ui.oportunidades

import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.OportunidadItem
import com.example.appproyecto.data.model.Perfil
import com.example.appproyecto.data.model.Prioridad
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.ui.EntornoViewModel
import io.mockk.coEvery
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
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OportunidadesViewModelTest {

    private val entorno = EntornoViewModel()

    @After
    fun limpiar() = entorno.terminar()

    private val ana = Usuario(id = 1, nombre = "Ana (demo)", correo = "ana@ejemplo.cl", passwordHash = "x")
    private val sofia = Usuario(
        id = 10, nombre = "Sofía (demo)", correo = "sofia@ejemplo.cl", passwordHash = "x",
        perfil = Perfil.SUPERVISOR.name
    )

    private fun item(id: Long, nombre: String, estado: EstadoOportunidad, prioridad: Prioridad) = OportunidadItem(
        id = id, nombre = nombre, estado = estado.name, prioridad = prioridad.name, potencial = 3,
        eventoNombre = "Evento $id", eventoFecha = 2000L, comuna = "Santiago",
        organizacionNombre = "Org $id", responsableId = 1, responsableNombre = "Ana (demo)"
    )

    private val lista = listOf(
        item(1, "Feria Andina", EstadoOportunidad.CONTACTO_PENDIENTE, Prioridad.ALTA),
        item(2, "Expo Verde", EstadoOportunidad.EN_EVALUACION, Prioridad.MEDIA),
        item(3, "Congreso Salud", EstadoOportunidad.CONTACTO_PENDIENTE, Prioridad.BAJA)
    )

    private fun TestScope.crearEjecutivo(): OportunidadesViewModel {
        entorno.iniciar(usuarioIdEnSesion = 1L)
        coEvery { entorno.usuarioDao.obtenerPorId(1L) } returns ana
        every { entorno.oportunidadDao.observarItemsPorResponsable(1L) } returns flowOf(lista)
        val vm = OportunidadesViewModel(entorno.application)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        return vm
    }

    @Test
    fun antesDeObservar_estaCargando() = runTest {
        entorno.iniciar(usuarioIdEnSesion = null)

        val vm = OportunidadesViewModel(entorno.application)

        assertTrue(vm.uiState.value.cargando)
        assertTrue(vm.uiState.value.oportunidades.isEmpty())
    }

    @Test
    fun ejecutivo_veSusOportunidades() = runTest {
        val estado = crearEjecutivo().uiState.value

        assertFalse(estado.cargando)
        assertEquals(Perfil.EJECUTIVO, estado.perfil)
        assertEquals(3, estado.oportunidades.size)
    }

    @Test
    fun supervisor_veTodasYSuPerfilEsSupervisor() = runTest {
        entorno.iniciar(usuarioIdEnSesion = 10L)
        coEvery { entorno.usuarioDao.obtenerPorId(10L) } returns sofia
        every { entorno.oportunidadDao.observarItems() } returns flowOf(lista)
        val vm = OportunidadesViewModel(entorno.application)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        assertEquals(Perfil.SUPERVISOR, vm.uiState.value.perfil)
        assertEquals(3, vm.uiState.value.oportunidades.size)
    }

    @Test
    fun cambiarBusqueda_filtraPorTexto() = runTest {
        val vm = crearEjecutivo()

        vm.cambiarBusqueda("expo")

        assertEquals("expo", vm.uiState.value.busqueda)
        assertEquals(listOf("Expo Verde"), vm.uiState.value.oportunidades.map { it.nombre })
    }

    @Test
    fun elegirEstado_filtraYAlTocarDeNuevoSeDesactiva() = runTest {
        val vm = crearEjecutivo()

        vm.elegirEstado(EstadoOportunidad.CONTACTO_PENDIENTE)
        assertEquals(EstadoOportunidad.CONTACTO_PENDIENTE, vm.uiState.value.estadoFiltro)
        assertEquals(2, vm.uiState.value.oportunidades.size)

        vm.elegirEstado(EstadoOportunidad.CONTACTO_PENDIENTE)
        assertNull(vm.uiState.value.estadoFiltro)
        assertEquals(3, vm.uiState.value.oportunidades.size)
    }

    @Test
    fun elegirPrioridad_filtraYAlTocarDeNuevoSeDesactiva() = runTest {
        val vm = crearEjecutivo()

        vm.elegirPrioridad(Prioridad.ALTA)
        assertEquals(Prioridad.ALTA, vm.uiState.value.prioridadFiltro)
        assertEquals(listOf("Feria Andina"), vm.uiState.value.oportunidades.map { it.nombre })

        vm.elegirPrioridad(Prioridad.ALTA)
        assertNull(vm.uiState.value.prioridadFiltro)
    }

    @Test
    fun filtrosCombinados_seAplicanJuntos() = runTest {
        val vm = crearEjecutivo()

        vm.elegirEstado(EstadoOportunidad.CONTACTO_PENDIENTE)
        vm.elegirPrioridad(Prioridad.BAJA)
        vm.cambiarBusqueda("congreso")

        assertEquals(listOf("Congreso Salud"), vm.uiState.value.oportunidades.map { it.nombre })
    }
}