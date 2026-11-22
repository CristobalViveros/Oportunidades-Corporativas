package com.example.appproyecto.data.repository

import com.example.appproyecto.data.local.EventoDao
import com.example.appproyecto.data.model.Evento
import com.example.appproyecto.data.model.Resultado
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.util.COSTO_MAXIMO
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class EventoRepositoryTest {

    private val dao = mockk<EventoDao>()
    private val repositorio = EventoRepository(dao)

    private val actor = Usuario(id = 1, nombre = "Ana (demo)", correo = "ana@ejemplo.cl", passwordHash = "x")

    private fun evento() = Evento(
        nombre = "Feria Demo",
        fechaInicio = 2000L,
        fechaTermino = 3000L,
        organizacionId = 1,
        publicoEsperado = 100,
        costoReferencial = 50_000
    )

    @Test
    fun crear_cuentaInactiva_devuelveError() = runTest {
        val resultado = repositorio.crear(actor.copy(activo = false), evento())

        assertEquals(Resultado.Error("Tu cuenta no está activa"), resultado)
        coVerify(exactly = 0) { dao.insertar(any()) }
    }

    @Test
    fun crear_terminoAnteriorAlInicio_devuelveError() = runTest {
        val resultado = repositorio.crear(actor, evento().copy(fechaTermino = 1000L))

        assertEquals(Resultado.Error("La fecha de término no puede ser anterior al inicio"), resultado)
    }

    @Test
    fun crear_costoNegativo_devuelveError() = runTest {
        val resultado = repositorio.crear(actor, evento().copy(costoReferencial = -1))

        assertEquals(Resultado.Error("El costo referencial no es válido"), resultado)
    }

    @Test
    fun crear_costoSobreElMaximo_devuelveError() = runTest {
        val resultado = repositorio.crear(actor, evento().copy(costoReferencial = (COSTO_MAXIMO + 1).toInt()))

        assertEquals(Resultado.Error("El costo referencial no es válido"), resultado)
    }

    @Test
    fun crear_publicoEsperadoCero_devuelveError() = runTest {
        val resultado = repositorio.crear(actor, evento().copy(publicoEsperado = 0))

        assertEquals(Resultado.Error("El público esperado debe ser al menos 1 persona"), resultado)
    }

    @Test
    fun crear_eventoDuplicado_devuelveError() = runTest {
        coEvery { dao.contarDuplicados("Feria Demo", 2000L, 1L) } returns 1

        val resultado = repositorio.crear(actor, evento())

        assertEquals(Resultado.Error("Ya existe un evento con ese nombre, fecha y organización"), resultado)
        coVerify(exactly = 0) { dao.insertar(any()) }
    }

    @Test
    fun crear_datosValidos_guardaConNombreSinEspaciosEIdCero() = runTest {
        val guardado = slot<Evento>()
        coEvery { dao.contarDuplicados("Feria Demo", 2000L, 1L) } returns 0
        coEvery { dao.insertar(capture(guardado)) } returns 8L

        val resultado = repositorio.crear(actor, evento().copy(id = 99, nombre = "  Feria Demo  "))

        assertEquals(Resultado.Exito("Evento guardado"), resultado)
        assertEquals(0L, guardado.captured.id)
        assertEquals("Feria Demo", guardado.captured.nombre)
    }

    @Test
    fun crear_costoEnElLimite_seAcepta() = runTest {
        coEvery { dao.contarDuplicados("Feria Demo", 2000L, 1L) } returns 0
        coEvery { dao.insertar(any()) } returns 8L

        val resultado = repositorio.crear(actor, evento().copy(costoReferencial = COSTO_MAXIMO.toInt()))

        assertEquals(Resultado.Exito("Evento guardado"), resultado)
    }

    @Test
    fun insertar_delegaEnElDaoYDevuelveElId() = runTest {
        val e = evento()
        coEvery { dao.insertar(e) } returns 3L

        assertEquals(3L, repositorio.insertar(e))
    }

    @Test
    fun observarTodos_emiteLaListaDelDao() = runTest {
        every { dao.observarTodos() } returns flowOf(listOf(evento()))

        assertEquals(1, repositorio.observarTodos().first().size)
    }
}