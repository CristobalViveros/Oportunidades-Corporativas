package com.example.appproyecto.data.repository

import com.example.appproyecto.data.local.SeguimientoDao
import com.example.appproyecto.data.model.Seguimiento
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SeguimientoRepositoryTest {

    private val dao = mockk<SeguimientoDao>()
    private val repositorio = SeguimientoRepository(dao)

    private val seguimiento = Seguimiento(oportunidadId = 1, usuarioId = 2, resultado = "Se envió la propuesta")

    @Test
    fun registrar_delegaEnElDaoYDevuelveElId() = runTest {
        coEvery { dao.insertar(seguimiento) } returns 6L

        assertEquals(6L, repositorio.registrar(seguimiento))
    }

    @Test
    fun consultas_delegaEnElDao() = runTest {
        every { dao.observarTodos() } returns flowOf(listOf(seguimiento))
        every { dao.observarPorOportunidad(1L) } returns flowOf(listOf(seguimiento))
        every { dao.observarPorContacto(3L) } returns flowOf(listOf(seguimiento))

        assertEquals(1, repositorio.observarTodos().first().size)
        assertEquals(1, repositorio.observarPorOportunidad(1L).first().size)
        assertEquals(1, repositorio.observarPorContacto(3L).first().size)
    }
}