package com.example.appproyecto.ui.navigation

import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.ui.EntornoViewModel
import io.mockk.coEvery
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PrincipalViewModelTest {

    private val entorno = EntornoViewModel()

    @After
    fun limpiar() = entorno.terminar()

    private val usuario = Usuario(id = 1, nombre = "Ana Pérez (demo)", correo = "ana@ejemplo.cl", passwordHash = "x")

    @Test
    fun sesionValida_cargaElUsuarioYTerminaDeCargar() {
        entorno.iniciar(usuarioIdEnSesion = 1L)
        coEvery { entorno.usuarioDao.obtenerPorId(1L) } returns usuario

        val estado = PrincipalViewModel(entorno.application).uiState.value

        assertFalse(estado.cargando)
        assertEquals(usuario, estado.usuario)
        assertFalse(estado.sesionInvalida)
    }

    @Test
    fun sinSesion_marcaSesionInvalidaYCierraLaSesion() {
        entorno.iniciar(usuarioIdEnSesion = null)

        val estado = PrincipalViewModel(entorno.application).uiState.value

        assertFalse(estado.cargando)
        assertNull(estado.usuario)
        assertTrue(estado.sesionInvalida)
        verify { entorno.editor.remove("usuario_id") }
    }

    @Test
    fun usuarioInactivo_marcaSesionInvalida() {
        entorno.iniciar(usuarioIdEnSesion = 1L)
        coEvery { entorno.usuarioDao.obtenerPorId(1L) } returns usuario.copy(activo = false)

        val estado = PrincipalViewModel(entorno.application).uiState.value

        assertTrue(estado.sesionInvalida)
        assertNull(estado.usuario)
    }

    @Test
    fun sesionDeUsuarioInexistente_marcaSesionInvalida() {
        entorno.iniciar(usuarioIdEnSesion = 1L)
        coEvery { entorno.usuarioDao.obtenerPorId(1L) } returns null

        assertTrue(PrincipalViewModel(entorno.application).uiState.value.sesionInvalida)
    }
}