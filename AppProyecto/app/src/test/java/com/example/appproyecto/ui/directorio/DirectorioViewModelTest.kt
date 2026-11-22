package com.example.appproyecto.ui.directorio

import com.example.appproyecto.data.model.Contacto
import com.example.appproyecto.data.model.Organizacion
import com.example.appproyecto.ui.EntornoViewModel
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
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DirectorioViewModelTest {

    private val entorno = EntornoViewModel()

    @After
    fun limpiar() = entorno.terminar()

    private val feria = Organizacion(id = 1, nombre = "Feria Andina", rubro = "Ferias y eventos", comuna = "Santiago")
    private val muni = Organizacion(id = 2, nombre = "Municipalidad Valle Verde", rubro = "Gobierno local", comuna = "Puente Alto")

    private fun TestScope.crearYObservar(): DirectorioViewModel {
        entorno.iniciar()
        every { entorno.organizacionDao.observarTodas() } returns flowOf(listOf(feria, muni))
        every { entorno.contactoDao.observarTodos() } returns flowOf(
            listOf(
                Contacto(id = 1, organizacionId = 1, nombre = "Marta"),
                Contacto(id = 2, organizacionId = 1, nombre = "Pedro")
            )
        )
        val vm = DirectorioViewModel(entorno.application)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        return vm
    }

    @Test
    fun sinBusqueda_listaTodasConSuCantidadDeContactos() = runTest {
        val estado = crearYObservar().uiState.value

        assertFalse(estado.cargando)
        assertEquals(2, estado.organizaciones.size)
        assertEquals(2, estado.organizaciones[0].contactos)
        assertEquals(0, estado.organizaciones[1].contactos)
    }

    @Test
    fun buscar_porNombre_ignoraMayusculas() = runTest {
        val vm = crearYObservar()

        vm.buscar("FERIA")

        assertEquals("FERIA", vm.uiState.value.busqueda)
        assertEquals(listOf("Feria Andina"), vm.uiState.value.organizaciones.map { it.organizacion.nombre })
    }

    @Test
    fun buscar_porRubro_filtra() = runTest {
        val vm = crearYObservar()

        vm.buscar("gobierno")

        assertEquals(listOf("Municipalidad Valle Verde"), vm.uiState.value.organizaciones.map { it.organizacion.nombre })
    }

    @Test
    fun buscar_porComuna_filtra() = runTest {
        val vm = crearYObservar()

        vm.buscar("puente")

        assertEquals(1, vm.uiState.value.organizaciones.size)
        assertEquals(2L, vm.uiState.value.organizaciones.first().organizacion.id)
    }

    @Test
    fun buscar_soloEspacios_noFiltra() = runTest {
        val vm = crearYObservar()

        vm.buscar("   ")

        assertEquals(2, vm.uiState.value.organizaciones.size)
    }

    @Test
    fun buscar_sinCoincidencias_devuelveListaVacia() = runTest {
        val vm = crearYObservar()

        vm.buscar("zzz")

        assertTrue(vm.uiState.value.organizaciones.isEmpty())
    }
}