package com.example.appproyecto.ui.login

import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.ui.EntornoViewModel
import com.example.appproyecto.util.sha256
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoginViewModelTest {

    private val entorno = EntornoViewModel()

    @Before
    fun preparar() = entorno.iniciar()

    @After
    fun limpiar() = entorno.terminar()

    private fun crearViewModel() = LoginViewModel(entorno.application)

    private fun usuario() = Usuario(
        id = 1, nombre = "Ana Pérez (demo)", correo = "ana.ejecutiva@ejemplo.cl",
        passwordHash = sha256("Ana12345")
    )

    @Test
    fun estadoInicial_estaVacio() {
        assertEquals(LoginUiState(), crearViewModel().uiState.value)
    }

    @Test
    fun cambiarCorreo_actualizaElValorYLimpiaLosErrores() {
        val vm = crearViewModel()
        vm.iniciarSesion() // con campos vacíos deja errores
        assertNotNull(vm.uiState.value.errorCorreo)

        vm.cambiarCorreo("ana@ejemplo.cl")

        assertEquals("ana@ejemplo.cl", vm.uiState.value.correo)
        assertNull(vm.uiState.value.errorCorreo)
        assertNull(vm.uiState.value.errorGeneral)
    }

    @Test
    fun cambiarPassword_actualizaElValorYLimpiaLosErrores() {
        val vm = crearViewModel()
        vm.iniciarSesion()
        assertNotNull(vm.uiState.value.errorPassword)

        vm.cambiarPassword("Ana12345")

        assertEquals("Ana12345", vm.uiState.value.password)
        assertNull(vm.uiState.value.errorPassword)
    }

    @Test
    fun iniciarSesion_camposVacios_muestraErroresYNoConsultaLaBase() {
        val vm = crearViewModel()

        vm.iniciarSesion()

        assertEquals("El correo es obligatorio", vm.uiState.value.errorCorreo)
        assertEquals("La contraseña es obligatoria", vm.uiState.value.errorPassword)
        assertFalse(vm.uiState.value.loginExitoso)
        coVerify(exactly = 0) { entorno.usuarioDao.obtenerPorCorreo(any()) }
    }

    @Test
    fun iniciarSesion_credencialesCorrectas_guardaLaSesionYMarcaExito() {
        coEvery { entorno.usuarioDao.obtenerPorCorreo("ana.ejecutiva@ejemplo.cl") } returns usuario()
        val vm = crearViewModel()
        vm.cambiarCorreo("ana.ejecutiva@ejemplo.cl")
        vm.cambiarPassword("Ana12345")

        vm.iniciarSesion()

        assertTrue(vm.uiState.value.loginExitoso)
        assertFalse(vm.uiState.value.cargando)
        verify { entorno.editor.putLong("usuario_id", 1L) }
    }

    @Test
    fun iniciarSesion_credencialesIncorrectas_muestraErrorGeneral() {
        coEvery { entorno.usuarioDao.obtenerPorCorreo("ana.ejecutiva@ejemplo.cl") } returns usuario()
        val vm = crearViewModel()
        vm.cambiarCorreo("ana.ejecutiva@ejemplo.cl")
        vm.cambiarPassword("Clave99999")

        vm.iniciarSesion()

        assertEquals("Correo o contraseña incorrectos", vm.uiState.value.errorGeneral)
        assertFalse(vm.uiState.value.loginExitoso)
        assertFalse(vm.uiState.value.cargando)
        verify(exactly = 0) { entorno.editor.putLong(any(), any()) }
    }
}