package com.example.appproyecto.ui.directorio

import androidx.lifecycle.SavedStateHandle
import com.example.appproyecto.data.model.Contacto
import com.example.appproyecto.data.model.Organizacion
import com.example.appproyecto.ui.EntornoViewModel
import io.mockk.every
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrganizacionDetalleViewModelTest {

    private val entorno = EntornoViewModel()

    @After
    fun limpiar() = entorno.terminar()

    private val organizacion = Organizacion(id = 5, nombre = "Feria Andina")

    @Test
    fun alObservar_emiteLaOrganizacionYSusContactos() = runTest {
        entorno.iniciar()
        every { entorno.organizacionDao.observarPorId(5L) } returns flowOf(organizacion)
        every { entorno.contactoDao.observarPorOrganizacion(5L) } returns
                flowOf(listOf(Contacto(id = 1, organizacionId = 5, nombre = "Marta")))

        val vm = OrganizacionDetalleViewModel(entorno.application, SavedStateHandle(mapOf("id" to 5L)))
        assertTrue(vm.uiState.value.cargando)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        assertEquals(5L, vm.organizacionId)
        assertFalse(vm.uiState.value.cargando)
        assertEquals(organizacion, vm.uiState.value.organizacion)
        assertEquals(1, vm.uiState.value.contactos.size)
    }

    @Test
    fun sinIdEnLaNavegacion_usaCeroYNoEncuentraOrganizacion() = runTest {
        entorno.iniciar()
        every { entorno.organizacionDao.observarPorId(0L) } returns flowOf(null)
        every { entorno.contactoDao.observarPorOrganizacion(0L) } returns flowOf(emptyList())

        val vm = OrganizacionDetalleViewModel(entorno.application, SavedStateHandle())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        assertEquals(0L, vm.organizacionId)
        assertNull(vm.uiState.value.organizacion)
        assertFalse(vm.uiState.value.cargando)
    }
}