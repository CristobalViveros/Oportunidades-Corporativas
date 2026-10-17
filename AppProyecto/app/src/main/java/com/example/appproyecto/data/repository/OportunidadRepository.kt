package com.example.appproyecto.data.repository

import com.example.appproyecto.data.local.OportunidadDao
import com.example.appproyecto.data.model.OportunidadItem
import kotlinx.coroutines.flow.Flow

class OportunidadRepository(private val oportunidadDao: OportunidadDao) {
    fun observarItems(): Flow<List<OportunidadItem>> = oportunidadDao.observarItems()
}