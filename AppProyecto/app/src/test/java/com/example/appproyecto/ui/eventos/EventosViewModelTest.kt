package com.example.appproyecto.ui.eventos

import com.example.appproyecto.data.model.Evento
import com.example.appproyecto.ui.EntornoViewModel
import io.mockk.every
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventosViewModelTest {

    private val entorno = EntornoViewModel()

    @After
    fun limpiar() = entorno.terminar()

    private val evento = Evento(nombre = "Feria Demo", fechaInicio = 2000L, fechaTermino = 3000L, organizacionId = 1)

    @Test
    fun antesDeObservar_laListaEstaVacia() = runTest {
        entorno.iniciar()
        every { entorno.eventoDao.observarTodos() } returns flowOf(listOf(evento))

        val vm = EventosViewModel(entorno.application)

        assertTrue(vm.eventos.value.isEmpty())
    }

    @Test
    fun alObservar_emiteLosEventosDelRepositorio() = runTest {
        entorno.iniciar()
        every { entorno.eventoDao.observarTodos() } returns flowOf(listOf(evento, evento.copy(nombre = "Otra")))

        val vm = EventosViewModel(entorno.application)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.eventos.collect {} }

        assertEquals(2, vm.eventos.value.size)
    }
}