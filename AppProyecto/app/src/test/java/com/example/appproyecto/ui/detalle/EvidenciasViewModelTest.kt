package com.example.appproyecto.ui.detalle

import androidx.lifecycle.SavedStateHandle
import com.example.appproyecto.data.model.Evidencia
import com.example.appproyecto.data.model.EvidenciaItem
import com.example.appproyecto.data.model.Oportunidad
import com.example.appproyecto.data.model.Resultado
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.ui.EntornoViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class EvidenciasViewModelTest {

    @get:Rule
    val carpeta = TemporaryFolder()

    private val entorno = EntornoViewModel()

    @After
    fun limpiar() = entorno.terminar()

    private val ana = Usuario(id = 1, nombre = "Ana (demo)", correo = "ana@ejemplo.cl", passwordHash = "x")
    private val oportunidad = Oportunidad(id = 10, nombre = "Feria Andina", eventoId = 1, responsableId = 1)
    private val existente = EvidenciaItem(
        id = 1, rutaFoto = "/fotos/1.jpg", fecha = 1000L, descripcion = "Stand", usuarioNombre = "Ana (demo)"
    )

    private fun TestScope.crear(conSesion: Boolean = true): EvidenciasViewModel {
        entorno.iniciar(usuarioIdEnSesion = if (conSesion) 1L else null)
        coEvery { entorno.usuarioDao.obtenerPorId(1L) } returns ana
        coEvery { entorno.oportunidadDao.obtenerPorId(10L) } returns oportunidad
        every { entorno.evidenciaDao.observarItemsPorOportunidad(10L) } returns flowOf(listOf(existente))
        val vm = EvidenciasViewModel(entorno.application, SavedStateHandle(mapOf("id" to 10L)))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        return vm
    }

    // ---------- lista ----------

    @Test
    fun conSesion_cargaLasEvidenciasDeLaOportunidad() = runTest {
        assertEquals(listOf(existente), crear().uiState.value.evidencias)
    }

    @Test
    fun sinSesion_noMuestraEvidencias() = runTest {
        assertTrue(crear(conSesion = false).uiState.value.evidencias.isEmpty())
    }

    // ---------- foto pendiente ----------

    @Test
    fun prepararFoto_dejaLaFotoPendienteYLimpiaLaDescripcion() = runTest {
        val vm = crear()
        vm.cambiarDescripcion("Texto viejo")
        val foto = carpeta.newFile("a.jpg")

        vm.prepararFoto(foto.absolutePath)

        assertEquals(foto.absolutePath, vm.uiState.value.fotoPendiente)
        assertEquals("", vm.uiState.value.descripcion)
    }

    @Test
    fun prepararOtraFoto_borraElArchivoDeLaAnterior() = runTest {
        val vm = crear()
        val primera = carpeta.newFile("a.jpg")
        val segunda = carpeta.newFile("b.jpg")

        vm.prepararFoto(primera.absolutePath)
        vm.prepararFoto(segunda.absolutePath)

        assertFalse(primera.exists())
        assertEquals(segunda.absolutePath, vm.uiState.value.fotoPendiente)
    }

    @Test
    fun cancelarFoto_borraElArchivoYReiniciaElBorrador() = runTest {
        val vm = crear()
        val foto = carpeta.newFile("a.jpg")
        vm.prepararFoto(foto.absolutePath)
        vm.cambiarDescripcion("Stand")

        vm.cancelarFoto()

        assertFalse(foto.exists())
        assertNull(vm.uiState.value.fotoPendiente)
        assertEquals("", vm.uiState.value.descripcion)
    }

    @Test
    fun cambiarDescripcion_actualizaElTextoYLimpiaElError() = runTest {
        val vm = crear()
        vm.prepararFoto(carpeta.newFile("a.jpg").absolutePath)
        vm.cambiarDescripcion("a".repeat(101))
        vm.guardar()
        assertTrue(vm.uiState.value.errorDescripcion != null)

        vm.cambiarDescripcion("Stand principal")

        assertEquals("Stand principal", vm.uiState.value.descripcion)
        assertNull(vm.uiState.value.errorDescripcion)
    }

    // ---------- guardar ----------

    @Test
    fun guardar_sinFoto_noHaceNada() = runTest {
        val vm = crear()

        vm.guardar()

        assertNull(vm.uiState.value.resultado)
        coVerify(exactly = 0) { entorno.evidenciaDao.insertar(any()) }
    }

    @Test
    fun guardar_sinSesion_muestraError() = runTest {
        val vm = crear(conSesion = false)
        vm.prepararFoto(carpeta.newFile("a.jpg").absolutePath)

        vm.guardar()

        assertEquals("No hay una sesión activa", vm.uiState.value.resultado?.mensaje)
        assertTrue(vm.uiState.value.fotoPendiente != null)
    }

    @Test
    fun guardar_descripcionMuyLarga_muestraErrorYConservaLaFoto() = runTest {
        val vm = crear()
        val foto = carpeta.newFile("a.jpg")
        vm.prepararFoto(foto.absolutePath)
        vm.cambiarDescripcion("a".repeat(101))

        vm.guardar()

        assertEquals("La descripción no puede superar 100 caracteres", vm.uiState.value.errorDescripcion)
        assertEquals(foto.absolutePath, vm.uiState.value.fotoPendiente)
        coVerify(exactly = 0) { entorno.evidenciaDao.insertar(any()) }
    }

    @Test
    fun guardar_datosValidos_guardaYReiniciaElBorrador() = runTest {
        val guardada = slot<Evidencia>()
        val vm = crear()
        coEvery { entorno.evidenciaDao.insertar(capture(guardada)) } returns 1L
        val foto = carpeta.newFile("a.jpg")
        vm.prepararFoto(foto.absolutePath)
        vm.cambiarDescripcion("  Stand principal  ")

        vm.guardar()

        val estado = vm.uiState.value
        assertTrue(estado.resultado is Resultado.Exito)
        assertNull(estado.fotoPendiente)
        assertFalse(estado.guardando)
        assertEquals("Stand principal", guardada.captured.descripcion)
        assertEquals(10L, guardada.captured.oportunidadId)
    }

    @Test
    fun guardar_archivoInexistente_muestraErrorYConservaLaFoto() = runTest {
        val vm = crear()
        vm.prepararFoto("/ruta/que/no/existe.jpg")

        vm.guardar()

        val estado = vm.uiState.value
        assertEquals("No se encontró la fotografía, intenta tomarla de nuevo", estado.resultado?.mensaje)
        assertEquals("/ruta/que/no/existe.jpg", estado.fotoPendiente)
        assertFalse(estado.guardando)
    }

    @Test
    fun consumirResultado_limpiaElMensaje() = runTest {
        val vm = crear(conSesion = false)
        vm.prepararFoto(carpeta.newFile("a.jpg").absolutePath)
        vm.guardar()
        assertTrue(vm.uiState.value.resultado != null)

        vm.consumirResultado()

        assertNull(vm.uiState.value.resultado)
    }
}