package com.example.appproyecto.data.repository

import com.example.appproyecto.data.local.EventoDao
import com.example.appproyecto.data.model.Evento
import kotlinx.coroutines.flow.Flow

class EventoRepository(private val eventoDao: EventoDao) {
    fun observarTodos(): Flow<List<Evento>> = eventoDao.observarTodos()
}