package com.example.appproyecto.data.repository

import com.example.appproyecto.data.local.OrganizacionDao
import com.example.appproyecto.data.model.Organizacion
import com.example.appproyecto.data.model.Resultado
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
import org.junit.Test

class OrganizacionRepositoryTest {

    private val dao = mockk<OrganizacionDao>()
    private val repositorio = OrganizacionRepository(dao)

    private val actor = Usuario(id = 1, nombre = "Ana (demo)", correo = "ana@ejemplo.cl", passwordHash = "x")

    // ---------- crear ----------

    @Test
    fun crear_cuentaInactiva_devuelveError() = runTest {
        val resultado = repositorio.crear(actor.copy(activo = false), Organizacion(nombre = "Feria Demo"))

        assertEquals(Resultado.Error("Tu cuenta no está activa"), resultado)
        coVerify(exactly = 0) { dao.insertar(any()) }
    }

    @Test
    fun crear_nombreDuplicado_devuelveError() = runTest {
        coEvery { dao.contarDuplicados("Feria Demo", 0L) } returns 1

        val resultado = repositorio.crear(actor, Organizacion(nombre = "Feria Demo"))

        assertEquals(Resultado.Error("Ya existe una organización con ese nombre"), resultado)
        coVerify(exactly = 0) { dao.insertar(any()) }
    }

    @Test
    fun crear_datosValidos_guardaConNombreSinEspaciosEIdCero() = runTest {
        val guardada = slot<Organizacion>()
        coEvery { dao.contarDuplicados("Feria Demo", 0L) } returns 0
        coEvery { dao.insertar(capture(guardada)) } returns 4L

        val resultado = repositorio.crear(actor, Organizacion(id = 99, nombre = "  Feria Demo  "))

        assertEquals(Resultado.Exito("Organización guardada"), resultado)
        assertEquals(0L, guardada.captured.id)
        assertEquals("Feria Demo", guardada.captured.nombre)
    }

    // ---------- actualizar ----------

    @Test
    fun actualizar_cuentaInactiva_devuelveError() = runTest {
        val resultado = repositorio.actualizar(actor.copy(activo = false), Organizacion(id = 5, nombre = "Feria"))

        assertEquals(Resultado.Error("Tu cuenta no está activa"), resultado)
    }

    @Test
    fun actualizar_organizacionInexistente_devuelveError() = runTest {
        coEvery { dao.obtenerPorId(5L) } returns null

        val resultado = repositorio.actualizar(actor, Organizacion(id = 5, nombre = "Feria"))

        assertEquals(Resultado.Error("La organización no existe"), resultado)
    }

    @Test
    fun actualizar_nombreDuplicadoEnOtra_devuelveError() = runTest {
        val actual = Organizacion(id = 5, nombre = "Feria")
        coEvery { dao.obtenerPorId(5L) } returns actual
        coEvery { dao.contarDuplicados("Otra", 5L) } returns 1

        val resultado = repositorio.actualizar(actor, actual.copy(nombre = "Otra"))

        assertEquals(Resultado.Error("Ya existe otra organización con ese nombre"), resultado)
        coVerify(exactly = 0) { dao.actualizar(any()) }
    }

    @Test
    fun actualizar_datosValidos_guardaConNombreSinEspacios() = runTest {
        val actual = Organizacion(id = 5, nombre = "Feria")
        val guardada = slot<Organizacion>()
        coEvery { dao.obtenerPorId(5L) } returns actual
        coEvery { dao.contarDuplicados("Nueva", 5L) } returns 0
        coJustRun { dao.actualizar(capture(guardada)) }

        val resultado = repositorio.actualizar(actor, actual.copy(nombre = "  Nueva  "))

        assertEquals(Resultado.Exito("Organización actualizada"), resultado)
        assertEquals("Nueva", guardada.captured.nombre)
    }

    // ---------- consultas ----------

    @Test
    fun consultas_delegaEnElDao() = runTest {
        val organizacion = Organizacion(id = 5, nombre = "Feria")
        every { dao.observarTodas() } returns flowOf(listOf(organizacion))
        every { dao.observarPorId(5L) } returns flowOf(organizacion)
        coEvery { dao.obtenerPorId(5L) } returns organizacion

        assertEquals(1, repositorio.observarTodas().first().size)
        assertEquals(organizacion, repositorio.observarPorId(5L).first())
        assertEquals(organizacion, repositorio.obtenerPorId(5L))
    }
}