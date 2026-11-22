package com.example.appproyecto.data.repository

import com.example.appproyecto.data.local.UsuarioDao
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.util.sha256
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class UsuarioRepositoryTest {

    private val dao = mockk<UsuarioDao>()
    private val repositorio = UsuarioRepository(dao)

    private fun usuario(activo: Boolean = true) = Usuario(
        id = 1,
        nombre = "Ana Pérez (demo)",
        correo = "ana.ejecutiva@ejemplo.cl",
        passwordHash = sha256("Ana12345"),
        activo = activo
    )

    @Test
    fun autenticar_credencialesCorrectas_devuelveElUsuario() = runTest {
        coEvery { dao.obtenerPorCorreo("ana.ejecutiva@ejemplo.cl") } returns usuario()

        val resultado = repositorio.autenticar("ana.ejecutiva@ejemplo.cl", "Ana12345")

        assertNotNull(resultado)
        assertEquals(1L, resultado!!.id)
    }

    @Test
    fun autenticar_correoConEspaciosYMayusculas_seNormaliza() = runTest {
        coEvery { dao.obtenerPorCorreo("ana.ejecutiva@ejemplo.cl") } returns usuario()

        val resultado = repositorio.autenticar("  ANA.Ejecutiva@Ejemplo.cl ", "Ana12345")

        assertNotNull(resultado)
    }

    @Test
    fun autenticar_passwordIncorrecta_devuelveNull() = runTest {
        coEvery { dao.obtenerPorCorreo("ana.ejecutiva@ejemplo.cl") } returns usuario()

        assertNull(repositorio.autenticar("ana.ejecutiva@ejemplo.cl", "Clave99999"))
    }

    @Test
    fun autenticar_correoInexistente_devuelveNull() = runTest {
        coEvery { dao.obtenerPorCorreo("nadie@ejemplo.cl") } returns null

        assertNull(repositorio.autenticar("nadie@ejemplo.cl", "Ana12345"))
    }

    @Test
    fun autenticar_usuarioInactivo_devuelveNull() = runTest {
        coEvery { dao.obtenerPorCorreo("ana.ejecutiva@ejemplo.cl") } returns usuario(activo = false)

        assertNull(repositorio.autenticar("ana.ejecutiva@ejemplo.cl", "Ana12345"))
    }

    @Test
    fun obtenerPorId_delegaEnElDao() = runTest {
        coEvery { dao.obtenerPorId(1L) } returns usuario()

        assertEquals("Ana Pérez (demo)", repositorio.obtenerPorId(1L)?.nombre)
    }

    @Test
    fun observarEjecutivos_emiteLaListaDelDao() = runTest {
        every { dao.observarEjecutivos() } returns flowOf(listOf(usuario()))

        assertEquals(1, repositorio.observarEjecutivos().first().size)
    }
}