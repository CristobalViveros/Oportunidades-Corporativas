package com.example.appproyecto.ui.dashboard

import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.OportunidadItem
import com.example.appproyecto.data.model.Perfil
import com.example.appproyecto.data.model.SeguimientoPendiente
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.ui.EntornoViewModel
import io.mockk.coEvery
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
class DashboardViewModelTest {

    private val entorno = EntornoViewModel()

    @After
    fun limpiar() = entorno.terminar()

    private val ana = Usuario(id = 1, nombre = "Ana (demo)", correo = "ana@ejemplo.cl", passwordHash = "x")
    private val sofia = Usuario(
        id = 10, nombre = "Sofía (demo)", correo = "sofia@ejemplo.cl", passwordHash = "x",
        perfil = Perfil.SUPERVISOR.name
    )

    private fun item(
        id: Long = 5,
        estado: EstadoOportunidad = EstadoOportunidad.CONTACTO_PENDIENTE,
        fechaEvento: Long = System.currentTimeMillis() + 86_400_000L,
        responsableId: Long = 1
    ) = OportunidadItem(
        id = id, nombre = "Feria $id", estado = estado.name, prioridad = "ALTA", potencial = 3,
        eventoNombre = "Feria", eventoFecha = fechaEvento, comuna = "Santiago",
        organizacionNombre = "Org", responsableId = responsableId, responsableNombre = "Ana (demo)"
    )

    private fun pendiente(fecha: Long) = SeguimientoPendiente(
        oportunidadId = 5, oportunidadNombre = "Feria 5", estado = "CONTACTO_PENDIENTE",
        responsableId = 1, responsableNombre = "Ana (demo)", proximaAccion = "Llamar", fecha = fecha
    )

    @Test
    fun estadoInicial_estaCargando() = runTest {
        entorno.iniciar(usuarioIdEnSesion = null)

        val vm = DashboardViewModel(entorno.application)

        assertTrue(vm.uiState.value.cargando)
        assertNull(vm.uiState.value.usuario)
    }

    @Test
    fun ejecutivo_veSoloSusOportunidadesAbiertas() = runTest {
        entorno.iniciar(usuarioIdEnSesion = 1L)
        coEvery { entorno.usuarioDao.obtenerPorId(1L) } returns ana
        every { entorno.oportunidadDao.observarItemsPorResponsable(1L) } returns flowOf(
            listOf(item(id = 5), item(id = 6, estado = EstadoOportunidad.OPORTUNIDAD_CERRADA))
        )
        every { entorno.seguimientoDao.observarPendientes(1L) } returns flowOf(emptyList())

        val vm = DashboardViewModel(entorno.application)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        val estado = vm.uiState.value
        assertFalse(estado.cargando)
        assertEquals(ana, estado.usuario)
        assertEquals(1, estado.resumen.activas)
        assertEquals(1, estado.resumen.proximas.size)
        assertTrue(estado.resumen.cargaEquipo.isEmpty())
    }

    @Test
    fun supervisor_veTodoYSuCargaDeEquipo() = runTest {
        entorno.iniciar(usuarioIdEnSesion = 10L)
        coEvery { entorno.usuarioDao.obtenerPorId(10L) } returns sofia
        every { entorno.oportunidadDao.observarItems() } returns flowOf(
            listOf(item(id = 5), item(id = 6, estado = EstadoOportunidad.EN_EVALUACION))
        )
        every { entorno.seguimientoDao.observarPendientes(0L) } returns
                flowOf(listOf(pendiente(fecha = System.currentTimeMillis() - 3 * 86_400_000L)))

        val vm = DashboardViewModel(entorno.application)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        val resumen = vm.uiState.value.resumen
        assertEquals(2, resumen.activas)
        assertEquals(1, resumen.esperandoDecision.size)
        assertEquals(1, resumen.atrasados.size)
        assertEquals(1, resumen.cargaEquipo.size)
        assertEquals(1, resumen.cargaEquipo.first().atrasadas)
    }
}