package com.example.appproyecto.data.repository

import com.example.appproyecto.data.local.SeguimientoDao
import com.example.appproyecto.data.model.Seguimiento
import kotlinx.coroutines.flow.Flow

class SeguimientoRepository(private val seguimientoDao: SeguimientoDao) {

    fun observarTodos(): Flow<List<Seguimiento>> = seguimientoDao.observarTodos()

    fun observarPorOportunidad(oportunidadId: Long): Flow<List<Seguimiento>> =
        seguimientoDao.observarPorOportunidad(oportunidadId)

    fun observarPorContacto(contactoId: Long): Flow<List<Seguimiento>> =
        seguimientoDao.observarPorContacto(contactoId)

    suspend fun registrar(seguimiento: Seguimiento): Long = seguimientoDao.insertar(seguimiento)
}
