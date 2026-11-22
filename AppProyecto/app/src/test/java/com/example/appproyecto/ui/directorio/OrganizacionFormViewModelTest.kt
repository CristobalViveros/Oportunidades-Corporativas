package com.example.appproyecto.ui.directorio

import androidx.lifecycle.SavedStateHandle
import com.example.appproyecto.data.model.Organizacion
import com.example.appproyecto.data.model.TipoOrganizacion
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.ui.EntornoViewModel
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.slot
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OrganizacionFormViewModelTest {

    private val entorno = EntornoViewModel()

    @After
    fun limpiar() = entorno.terminar()

    private val ana = Usuario(id = 1, nombre = "Ana (demo)", correo = "ana@ejemplo.cl", passwordHash = "x")

    private fun crearNueva(conSesion: Boolean = true): OrganizacionFormViewModel {
        entorno.iniciar(usuarioIdEnSesion = if (conSesion) 1L else null)
        coEvery { entorno.usuarioDao.obtenerPorId(1L) } returns ana
        return OrganizacionFormViewModel(entorno.application, SavedStateHandle())
    }

    private fun OrganizacionFormViewModel.llenarValido() {
        cambiarNombre("Feria Demo")
        cambiarRubro("Ferias y eventos")
        cambiarRegion("Metropolitana")
        cambiarComuna("Santiago")
    }

    // ---------- estado y campos ----------

    @Test
    fun organizacionNueva_empiezaVaciaYSinCargar() {
        val estado = crearNueva().uiState.value

        assertFalse(estado.esEdicion)
        assertFalse(estado.cargando)
        assertEquals("", estado.nombre)
        assertEquals(TipoOrganizacion.EMPRESA.name, estado.tipo)
    }

    @Test
    fun cambiarCampos_actualizaElEstado() {
        val vm = crearNueva()

        vm.cambiarNombre("Feria")
        vm.cambiarTipo(TipoOrganizacion.MUNICIPALIDAD.name)
        vm.cambiarRubro("Ferias")
        vm.cambiarRegion("Metropolitana")
        vm.cambiarComuna("Santiago")
        vm.cambiarSitioWeb("www.feria.cl")
        vm.cambiarPublico("Familias")
        vm.cambiarObservaciones("Nota")

        val e = vm.uiState.value
        assertEquals("Feria", e.nombre)
        assertEquals(TipoOrganizacion.MUNICIPALIDAD.name, e.tipo)
        assertEquals("Ferias", e.rubro)
        assertEquals("Metropolitana", e.region)
        assertEquals("Santiago", e.comuna)
        assertEquals("www.feria.cl", e.sitioWeb)
        assertEquals("Familias", e.publicoAsociado)
        assertEquals("Nota", e.observaciones)
    }

    @Test
    fun cambiarUnCampo_limpiaSuError() {
        val vm = crearNueva()
        vm.guardar() // deja errores por campos vacíos
        assertNotNull(vm.uiState.value.errorNombre)

        vm.cambiarNombre("Feria")

        assertNull(vm.uiState.value.errorNombre)
    }

    // ---------- guardar: validaciones ----------

    @Test
    fun guardar_sinSesion_muestraErrorGeneral() {
        val vm = crearNueva(conSesion = false)

        vm.guardar()

        assertEquals("No hay una sesión activa", vm.uiState.value.errorGeneral)
    }

    @Test
    fun guardar_camposVacios_muestraErroresYNoGuarda() {
        val vm = crearNueva()

        vm.guardar()

        val e = vm.uiState.value
        assertEquals("El nombre es obligatorio", e.errorNombre)
        assertEquals("El rubro es obligatorio", e.errorRubro)
        assertNotNull(e.errorRegion)
        assertNotNull(e.errorComuna)
        assertFalse(e.guardado)
        coVerify(exactly = 0) { entorno.organizacionDao.insertar(any()) }
    }

    @Test
    fun guardar_sitioWebInvalido_muestraError() {
        val vm = crearNueva()
        vm.llenarValido()
        vm.cambiarSitioWeb("esto no es un sitio")

        vm.guardar()

        assertEquals("Ingresa un sitio web válido (ej: www.empresa.cl)", vm.uiState.value.errorSitioWeb)
        assertFalse(vm.uiState.value.guardado)
    }

    @Test
    fun guardar_textosOpcionalesMuyLargos_muestraErrores() {
        val vm = crearNueva()
        vm.llenarValido()
        vm.cambiarPublico("a".repeat(101))
        vm.cambiarObservaciones("b".repeat(301))

        vm.guardar()

        assertEquals("El público asociado no puede superar 100 caracteres", vm.uiState.value.errorPublico)
        assertNotNull(vm.uiState.value.errorObservaciones)
    }

    // ---------- guardar: resultado ----------

    @Test
    fun guardar_datosValidos_creaLaOrganizacion() {
        val guardada = slot<Organizacion>()
        val vm = crearNueva()
        coEvery { entorno.organizacionDao.contarDuplicados("Feria Demo", 0L) } returns 0
        coEvery { entorno.organizacionDao.insertar(capture(guardada)) } returns 1L
        vm.llenarValido()
        vm.cambiarNombre("  Feria Demo  ")

        vm.guardar()

        assertTrue(vm.uiState.value.guardado)
        assertFalse(vm.uiState.value.guardando)
        assertEquals("Feria Demo", guardada.captured.nombre)
        assertEquals("Ferias y eventos", guardada.captured.rubro)
    }

    @Test
    fun guardar_nombreDuplicado_muestraElErrorDelRepositorio() {
        val vm = crearNueva()
        coEvery { entorno.organizacionDao.contarDuplicados("Feria Demo", 0L) } returns 1
        vm.llenarValido()

        vm.guardar()

        assertEquals("Ya existe una organización con ese nombre", vm.uiState.value.errorGeneral)
        assertFalse(vm.uiState.value.guardado)
        assertFalse(vm.uiState.value.guardando)
    }

    // ---------- edición ----------

    @Test
    fun edicion_cargaLosDatosDeLaOrganizacion() {
        val org = Organizacion(
            id = 5, nombre = "Feria Demo", tipo = TipoOrganizacion.MUNICIPALIDAD.name, rubro = "Ferias",
            region = "Metropolitana", comuna = "Santiago", sitioWeb = "www.feria.cl"
        )
        entorno.iniciar(usuarioIdEnSesion = 1L)
        coEvery { entorno.usuarioDao.obtenerPorId(1L) } returns ana
        coEvery { entorno.organizacionDao.obtenerPorId(5L) } returns org

        val estado = OrganizacionFormViewModel(entorno.application, SavedStateHandle(mapOf("id" to 5L))).uiState.value

        assertTrue(estado.esEdicion)
        assertFalse(estado.cargando)
        assertEquals("Feria Demo", estado.nombre)
        assertEquals(TipoOrganizacion.MUNICIPALIDAD.name, estado.tipo)
        assertEquals("www.feria.cl", estado.sitioWeb)
    }

    @Test
    fun edicion_organizacionInexistente_muestraError() {
        entorno.iniciar(usuarioIdEnSesion = 1L)
        coEvery { entorno.usuarioDao.obtenerPorId(1L) } returns ana
        coEvery { entorno.organizacionDao.obtenerPorId(5L) } returns null

        val estado = OrganizacionFormViewModel(entorno.application, SavedStateHandle(mapOf("id" to 5L))).uiState.value

        assertEquals("La organización no existe", estado.errorGeneral)
        assertFalse(estado.cargando)
    }

    @Test
    fun edicion_guardar_actualizaLaOrganizacion() {
        val org = Organizacion(id = 5, nombre = "Feria Demo", rubro = "Ferias", region = "Metropolitana", comuna = "Santiago")
        entorno.iniciar(usuarioIdEnSesion = 1L)
        coEvery { entorno.usuarioDao.obtenerPorId(1L) } returns ana
        coEvery { entorno.organizacionDao.obtenerPorId(5L) } returns org
        coEvery { entorno.organizacionDao.contarDuplicados("Feria Demo", 5L) } returns 0
        coJustRun { entorno.organizacionDao.actualizar(any()) }
        val vm = OrganizacionFormViewModel(entorno.application, SavedStateHandle(mapOf("id" to 5L)))

        vm.guardar()

        assertTrue(vm.uiState.value.guardado)
        coVerify { entorno.organizacionDao.actualizar(any()) }
    }
}