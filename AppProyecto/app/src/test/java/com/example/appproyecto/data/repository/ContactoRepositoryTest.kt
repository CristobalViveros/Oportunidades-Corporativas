package com.example.appproyecto.data.repository

import com.example.appproyecto.data.local.ContactoDao
import com.example.appproyecto.data.local.OrganizacionDao
import com.example.appproyecto.data.model.Contacto
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
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactoRepositoryTest {

    private val contactoDao = mockk<ContactoDao>()
    private val organizacionDao = mockk<OrganizacionDao>()
    private val repositorio = ContactoRepository(contactoDao, organizacionDao)

    private val actor = Usuario(id = 1, nombre = "Ana (demo)", correo = "ana@ejemplo.cl", passwordHash = "x")
    private val organizacion = Organizacion(id = 1, nombre = "Feria Demo")

    // ---------- crear ----------

    @Test
    fun crear_cuentaInactiva_devuelveError() = runTest {
        val inactivo = actor.copy(activo = false)

        val resultado = repositorio.crear(inactivo, Contacto(organizacionId = 1, nombre = "Marta"))

        assertEquals(Resultado.Error("Tu cuenta no está activa"), resultado)
        coVerify(exactly = 0) { contactoDao.insertar(any()) }
    }

    @Test
    fun crear_organizacionInexistente_devuelveError() = runTest {
        coEvery { organizacionDao.obtenerPorId(1L) } returns null

        val resultado = repositorio.crear(actor, Contacto(organizacionId = 1, nombre = "Marta"))

        assertEquals(Resultado.Error("La organización no existe"), resultado)
        coVerify(exactly = 0) { contactoDao.insertar(any()) }
    }

    @Test
    fun crear_correoDuplicadoEnLaOrganizacion_devuelveError() = runTest {
        coEvery { organizacionDao.obtenerPorId(1L) } returns organizacion
        coEvery { contactoDao.contarDuplicados(1L, "marta@ejemplo.cl", 0L) } returns 1

        val resultado = repositorio.crear(
            actor, Contacto(organizacionId = 1, nombre = "Marta", correo = "marta@ejemplo.cl")
        )

        assertEquals(
            Resultado.Error("Ya existe un contacto con ese correo en esta organización"),
            resultado
        )
        coVerify(exactly = 0) { contactoDao.insertar(any()) }
    }

    @Test
    fun crear_datosValidos_guardaConCorreoNormalizadoYIdCero() = runTest {
        val guardado = slot<Contacto>()
        coEvery { organizacionDao.obtenerPorId(1L) } returns organizacion
        coEvery { contactoDao.contarDuplicados(1L, "marta@ejemplo.cl", 0L) } returns 0
        coEvery { contactoDao.insertar(capture(guardado)) } returns 7L

        val resultado = repositorio.crear(
            actor,
            Contacto(id = 99, organizacionId = 1, nombre = "  Marta Díaz  ", correo = "  MARTA@Ejemplo.CL ")
        )

        assertEquals(Resultado.Exito("Contacto guardado"), resultado)
        assertEquals(0L, guardado.captured.id)
        assertEquals("Marta Díaz", guardado.captured.nombre)
        assertEquals("marta@ejemplo.cl", guardado.captured.correo)
    }

    // ---------- actualizar ----------

    @Test
    fun actualizar_cuentaInactiva_devuelveError() = runTest {
        val resultado = repositorio.actualizar(actor.copy(activo = false), Contacto(id = 5, organizacionId = 1, nombre = "Marta"))

        assertEquals(Resultado.Error("Tu cuenta no está activa"), resultado)
    }

    @Test
    fun actualizar_contactoInexistente_devuelveError() = runTest {
        coEvery { contactoDao.obtenerPorId(5L) } returns null

        val resultado = repositorio.actualizar(actor, Contacto(id = 5, organizacionId = 1, nombre = "Marta"))

        assertEquals(Resultado.Error("El contacto no existe"), resultado)
    }

    @Test
    fun actualizar_correoDuplicadoEnOtroContacto_devuelveError() = runTest {
        val actual = Contacto(id = 5, organizacionId = 1, nombre = "Marta", correo = "marta@ejemplo.cl")
        coEvery { contactoDao.obtenerPorId(5L) } returns actual
        coEvery { contactoDao.contarDuplicados(1L, "otro@ejemplo.cl", 5L) } returns 1

        val resultado = repositorio.actualizar(
            actor, actual.copy(correo = "otro@ejemplo.cl")
        )

        assertEquals(
            Resultado.Error("Ya existe otro contacto con ese correo en esta organización"),
            resultado
        )
        coVerify(exactly = 0) { contactoDao.actualizar(any()) }
    }

    @Test
    fun actualizar_datosValidos_noCambiaOrganizacionNiFechaUltimoContacto() = runTest {
        val actual = Contacto(
            id = 5, organizacionId = 1, nombre = "Marta", correo = "marta@ejemplo.cl",
            fechaUltimoContacto = 1000L
        )
        val guardado = slot<Contacto>()
        coEvery { contactoDao.obtenerPorId(5L) } returns actual
        coEvery { contactoDao.contarDuplicados(1L, "nuevo@ejemplo.cl", 5L) } returns 0
        coJustRun { contactoDao.actualizar(capture(guardado)) }

        val resultado = repositorio.actualizar(
            actor,
            Contacto(
                id = 5, organizacionId = 99, nombre = "  Marta D  ", correo = " NUEVO@Ejemplo.cl ",
                fechaUltimoContacto = null
            )
        )

        assertEquals(Resultado.Exito("Contacto actualizado"), resultado)
        assertEquals(1L, guardado.captured.organizacionId)
        assertEquals(1000L, guardado.captured.fechaUltimoContacto)
        assertEquals("Marta D", guardado.captured.nombre)
        assertEquals("nuevo@ejemplo.cl", guardado.captured.correo)
    }

    // ---------- consultas ----------

    @Test
    fun consultas_delegaEnElDao() = runTest {
        val contacto = Contacto(id = 5, organizacionId = 1, nombre = "Marta")
        every { contactoDao.observarTodos() } returns flowOf(listOf(contacto))
        every { contactoDao.observarPorOrganizacion(1L) } returns flowOf(listOf(contacto))
        every { contactoDao.observarPorId(5L) } returns flowOf(contacto)
        coEvery { contactoDao.obtenerPorId(5L) } returns contacto

        assertEquals(1, repositorio.observarTodos().first().size)
        assertEquals(1, repositorio.observarPorOrganizacion(1L).first().size)
        assertEquals(contacto, repositorio.observarPorId(5L).first())
        assertTrue(repositorio.obtenerPorId(5L) == contacto)
    }
}