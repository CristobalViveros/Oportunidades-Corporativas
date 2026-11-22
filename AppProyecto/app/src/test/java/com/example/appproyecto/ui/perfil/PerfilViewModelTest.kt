package com.example.appproyecto.ui.perfil

import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.ui.EntornoViewModel
import io.mockk.coEvery
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PerfilViewModelTest {

    private val entorno = EntornoViewModel()

    @After
    fun limpiar() = entorno.terminar()

    private val usuario = Usuario(id = 1, nombre = "Ana Pérez (demo)", correo = "ana@ejemplo.cl", passwordHash = "x")

    @Test
    fun conSesionIniciada_cargaElUsuario() {
        entorno.iniciar(usuarioIdEnSesion = 1L)
        coEvery { entorno.usuarioDao.obtenerPorId(1L) } returns usuario

        val vm = PerfilViewModel(entorno.application)

        assertEquals(usuario, vm.usuario.value)
    }

    @Test
    fun sinSesion_elUsuarioEsNull() {
        entorno.iniciar(usuarioIdEnSesion = null)

        val vm = PerfilViewModel(entorno.application)

        assertNull(vm.usuario.value)
    }

    @Test
    fun cerrarSesion_borraElUsuarioGuardado() {
        entorno.iniciar(usuarioIdEnSesion = 1L)
        coEvery { entorno.usuarioDao.obtenerPorId(1L) } returns usuario
        val vm = PerfilViewModel(entorno.application)

        vm.cerrarSesion()

        verify { entorno.editor.remove("usuario_id") }
    }
}