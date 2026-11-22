package com.example.appproyecto.data.repository

import com.example.appproyecto.data.local.EvidenciaDao
import com.example.appproyecto.data.local.OportunidadDao
import com.example.appproyecto.data.model.Evidencia
import com.example.appproyecto.data.model.EvidenciaItem
import com.example.appproyecto.data.model.Oportunidad
import com.example.appproyecto.data.model.Resultado
import com.example.appproyecto.data.model.Usuario
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class EvidenciaRepositoryTest {

    @get:Rule
    val carpeta = TemporaryFolder()

    private val evidenciaDao = mockk<EvidenciaDao>()
    private val oportunidadDao = mockk<OportunidadDao>()
    private val repositorio = EvidenciaRepository(evidenciaDao, oportunidadDao)

    private val ana = Usuario(id = 1, nombre = "Ana (demo)", correo = "ana@ejemplo.cl", passwordHash = "x")
    private val carlos = Usuario(id = 2, nombre = "Carlos (demo)", correo = "carlos@ejemplo.cl", passwordHash = "x")

    private val oportunidad = Oportunidad(id = 5, nombre = "Feria Demo", eventoId = 1, responsableId = 1)

    private fun evidencia() = EvidenciaItem(
        id = 1, rutaFoto = "/tmp/foto.jpg", fecha = 1000L, descripcion = "Stand", usuarioNombre = "Ana (demo)"
    )

    // ---------- observarPara ----------

    @Test
    fun observarPara_conPermiso_emiteLasEvidencias() = runTest {
        coEvery { oportunidadDao.obtenerPorId(5L) } returns oportunidad
        every { evidenciaDao.observarItemsPorOportunidad(5L) } returns flowOf(listOf(evidencia()))

        assertEquals(1, repositorio.observarPara(ana, 5L).first().size)
    }

    @Test
    fun observarPara_sinPermiso_emiteListaVacia() = runTest {
        coEvery { oportunidadDao.obtenerPorId(5L) } returns oportunidad

        assertTrue(repositorio.observarPara(carlos, 5L).first().isEmpty())
    }

    @Test
    fun observarPara_oportunidadInexistente_emiteListaVacia() = runTest {
        coEvery { oportunidadDao.obtenerPorId(5L) } returns null

        assertTrue(repositorio.observarPara(ana, 5L).first().isEmpty())
    }

    // ---------- agregar ----------

    @Test
    fun agregar_oportunidadInexistente_devuelveError() = runTest {
        coEvery { oportunidadDao.obtenerPorId(5L) } returns null

        val resultado = repositorio.agregar(ana, 5L, "/ruta/foto.jpg", "Stand")

        assertEquals(Resultado.Error("La oportunidad no existe"), resultado)
    }

    @Test
    fun agregar_ejecutivoAjeno_devuelveError() = runTest {
        coEvery { oportunidadDao.obtenerPorId(5L) } returns oportunidad

        val resultado = repositorio.agregar(carlos, 5L, "/ruta/foto.jpg", "Stand")

        assertEquals(Resultado.Error("No tienes acceso a esta oportunidad"), resultado)
    }

    @Test
    fun agregar_descripcionMuyLarga_devuelveError() = runTest {
        coEvery { oportunidadDao.obtenerPorId(5L) } returns oportunidad
        val foto = carpeta.newFile("foto.jpg")

        val resultado = repositorio.agregar(ana, 5L, foto.absolutePath, "a".repeat(101))

        assertEquals(
            Resultado.Error("La descripción no puede superar ${EvidenciaRepository.DESCRIPCION_MAXIMA} caracteres"),
            resultado
        )
        coVerify(exactly = 0) { evidenciaDao.insertar(any()) }
    }

    @Test
    fun agregar_archivoInexistente_devuelveError() = runTest {
        coEvery { oportunidadDao.obtenerPorId(5L) } returns oportunidad

        val resultado = repositorio.agregar(ana, 5L, "/ruta/que/no/existe.jpg", "Stand")

        assertEquals(Resultado.Error("No se encontró la fotografía, intenta tomarla de nuevo"), resultado)
        coVerify(exactly = 0) { evidenciaDao.insertar(any()) }
    }

    @Test
    fun agregar_datosValidos_guardaConDescripcionSinEspacios() = runTest {
        val guardada = slot<Evidencia>()
        val foto = carpeta.newFile("foto.jpg")
        coEvery { oportunidadDao.obtenerPorId(5L) } returns oportunidad
        coEvery { evidenciaDao.insertar(capture(guardada)) } returns 1L

        val resultado = repositorio.agregar(ana, 5L, foto.absolutePath, "  Stand principal  ")

        assertEquals(Resultado.Exito("Evidencia guardada"), resultado)
        assertEquals(5L, guardada.captured.oportunidadId)
        assertEquals(1L, guardada.captured.usuarioId)
        assertEquals(foto.absolutePath, guardada.captured.rutaFoto)
        assertEquals("Stand principal", guardada.captured.descripcion)
    }

    @Test
    fun agregar_descripcionEnElLimite_seAcepta() = runTest {
        val foto = carpeta.newFile("foto.jpg")
        coEvery { oportunidadDao.obtenerPorId(5L) } returns oportunidad
        coEvery { evidenciaDao.insertar(any()) } returns 1L

        val resultado = repositorio.agregar(ana, 5L, foto.absolutePath, "a".repeat(100))

        assertEquals(Resultado.Exito("Evidencia guardada"), resultado)
    }
}