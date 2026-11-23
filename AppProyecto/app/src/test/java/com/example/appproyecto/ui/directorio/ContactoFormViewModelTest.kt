package com.example.appproyecto.ui.directorio

import androidx.lifecycle.SavedStateHandle
import com.example.appproyecto.data.model.Contacto
import com.example.appproyecto.data.model.Organizacion
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

class ContactoFormViewModelTest {

    private val entorno = EntornoViewModel()

    @After
    fun limpiar() = entorno.terminar()

    private val ana = Usuario(id = 1, nombre = "Ana (demo)", correo = "ana@ejemplo.cl", passwordHash = "x")
    private val organizacion = Organizacion(id = 3, nombre = "Feria Andina")

    /** [id] distinto de 0 = edición del contacto con ese id. */
    private fun crear(
        org: Organizacion? = organizacion,
        contacto: Contacto? = null,
        conSesion: Boolean = true,
        id: Long = 0L
    ): ContactoFormViewModel {
        entorno.iniciar(usuarioIdEnSesion = if (conSesion) 1L else null)
        coEvery { entorno.usuarioDao.obtenerPorId(1L) } returns ana
        coEvery { entorno.organizacionDao.obtenerPorId(3L) } returns org
        if (id != 0L) coEvery { entorno.contactoDao.obtenerPorId(id) } returns contacto
        val argumentos = if (id != 0L) mapOf("orgId" to 3L, "id" to id) else mapOf("orgId" to 3L)
        return ContactoFormViewModel(entorno.application, SavedStateHandle(argumentos))
    }

    private fun ContactoFormViewModel.llenarValido() {
        cambiarNombre("Marta Díaz")
        cambiarCargo("Jefa de eventos")
        cambiarCorreo("marta@ejemplo.cl")
        cambiarTelefono("912345678")
    }

    // ---------- carga inicial ----------

    @Test
    fun contactoNuevo_cargaElNombreDeLaOrganizacion() {
        val estado = crear().uiState.value

        assertFalse(estado.cargando)
        assertFalse(estado.esEdicion)
        assertEquals("Feria Andina", estado.organizacionNombre)
    }

    @Test
    fun organizacionInexistente_muestraError() {
        val estado = crear(org = null).uiState.value

        assertEquals("La organización no existe", estado.errorGeneral)
        assertFalse(estado.cargando)
    }

    @Test
    fun edicion_cargaLosDatosDelContacto() {
        val contacto = Contacto(
            id = 7, organizacionId = 3, nombre = "Marta Díaz", cargo = "Jefa", correo = "marta@ejemplo.cl",
            telefono = "912345678", medioPreferente = "WhatsApp", estado = "Inactivo", observaciones = "Nota"
        )

        val estado = crear(contacto = contacto, id = 7L).uiState.value

        assertTrue(estado.esEdicion)
        assertFalse(estado.cargando)
        assertEquals("Marta Díaz", estado.nombre)
        assertEquals("WhatsApp", estado.medioPreferente)
        assertEquals("Inactivo", estado.estado)
    }

    @Test
    fun edicion_contactoInexistente_muestraError() {
        val estado = crear(contacto = null, id = 7L).uiState.value

        assertEquals("El contacto no existe", estado.errorGeneral)
        assertFalse(estado.cargando)
    }

    @Test
    fun edicion_contactoDeOtraOrganizacion_muestraError() {
        val deOtraOrg = Contacto(id = 7, organizacionId = 9, nombre = "Pedro")

        val estado = crear(contacto = deOtraOrg, id = 7L).uiState.value

        assertEquals("El contacto no existe", estado.errorGeneral)
    }

    // ---------- campos ----------

    @Test
    fun cambiarCampos_actualizaElEstado() {
        val vm = crear()

        vm.cambiarNombre("Marta")
        vm.cambiarCargo("Jefa")
        vm.cambiarArea("MARKETING")
        vm.cambiarCorreo("marta@ejemplo.cl")
        vm.cambiarTelefono("912345678")
        vm.cambiarMedio("WhatsApp")
        vm.cambiarEstado("Inactivo")
        vm.cambiarObservaciones("Nota")

        val e = vm.uiState.value
        assertEquals("Marta", e.nombre)
        assertEquals("Jefa", e.cargo)
        assertEquals("MARKETING", e.area)
        assertEquals("marta@ejemplo.cl", e.correo)
        assertEquals("912345678", e.telefono)
        assertEquals("WhatsApp", e.medioPreferente)
        assertEquals("Inactivo", e.estado)
        assertEquals("Nota", e.observaciones)
    }

    @Test
    fun cambiarUnCampo_limpiaSuError() {
        val vm = crear()
        vm.guardar()
        assertNotNull(vm.uiState.value.errorNombre)
        assertNotNull(vm.uiState.value.errorCargo)
        assertNotNull(vm.uiState.value.errorCorreo)

        vm.cambiarNombre("Marta")
        vm.cambiarCargo("Jefa")
        vm.cambiarCorreo("marta@ejemplo.cl")

        assertNull(vm.uiState.value.errorNombre)
        assertNull(vm.uiState.value.errorCargo)
        assertNull(vm.uiState.value.errorCorreo)
    }

    // ---------- guardar: validaciones ----------

    @Test
    fun guardar_sinSesion_muestraErrorGeneral() {
        val vm = crear(conSesion = false)

        vm.guardar()

        assertEquals("No hay una sesión activa", vm.uiState.value.errorGeneral)
    }

    @Test
    fun guardar_camposVacios_muestraErroresYNoGuarda() {
        val vm = crear()

        vm.guardar()

        val e = vm.uiState.value
        assertEquals("El nombre es obligatorio", e.errorNombre)
        assertEquals("El cargo es obligatorio", e.errorCargo)
        assertEquals("El correo es obligatorio", e.errorCorreo)
        assertNull(e.errorTelefono)
        assertFalse(e.guardado)
        coVerify(exactly = 0) { entorno.contactoDao.insertar(any()) }
    }

    @Test
    fun guardar_telefonoConPocosDigitos_muestraError() {
        val vm = crear()
        vm.llenarValido()
        vm.cambiarTelefono("123")

        vm.guardar()

        assertEquals("El teléfono debe tener entre 9 y 11 dígitos", vm.uiState.value.errorTelefono)
    }

    @Test
    fun guardar_observacionesMuyLargas_muestraError() {
        val vm = crear()
        vm.llenarValido()
        vm.cambiarObservaciones("a".repeat(301))

        vm.guardar()

        assertEquals("Las observaciones no puede superar 300 caracteres", vm.uiState.value.errorObservaciones)
    }

    // ---------- guardar: resultado ----------

    @Test
    fun guardar_datosValidos_creaElContacto() {
        val guardado = slot<Contacto>()
        val vm = crear()
        coEvery { entorno.contactoDao.contarDuplicados(3L, "marta@ejemplo.cl", 0L) } returns 0
        coEvery { entorno.contactoDao.insertar(capture(guardado)) } returns 1L
        vm.llenarValido()

        vm.guardar()

        assertTrue(vm.uiState.value.guardado)
        assertFalse(vm.uiState.value.guardando)
        assertEquals("Marta Díaz", guardado.captured.nombre)
        assertEquals(3L, guardado.captured.organizacionId)
    }

    @Test
    fun guardar_correoDuplicado_muestraElErrorDelRepositorio() {
        val vm = crear()
        coEvery { entorno.contactoDao.contarDuplicados(3L, "marta@ejemplo.cl", 0L) } returns 1
        vm.llenarValido()

        vm.guardar()

        assertEquals(
            "Ya existe un contacto con ese correo en esta organización",
            vm.uiState.value.errorGeneral
        )
        assertFalse(vm.uiState.value.guardado)
    }

    @Test
    fun edicion_guardar_actualizaElContacto() {
        val existente = Contacto(
            id = 7, organizacionId = 3, nombre = "Marta Díaz", cargo = "Jefa", correo = "marta@ejemplo.cl"
        )
        val vm = crear(contacto = existente, id = 7L)
        coEvery { entorno.contactoDao.contarDuplicados(3L, "marta@ejemplo.cl", 7L) } returns 0
        coJustRun { entorno.contactoDao.actualizar(any()) }
        vm.cambiarNombre("Marta Díaz Soto")

        vm.guardar()

        assertTrue(vm.uiState.value.guardado)
        coVerify { entorno.contactoDao.actualizar(any()) }
    }
}