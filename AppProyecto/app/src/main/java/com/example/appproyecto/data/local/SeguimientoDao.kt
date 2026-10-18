package com.example.appproyecto.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import com.example.appproyecto.data.model.Seguimiento
import kotlinx.coroutines.flow.Flow

@Dao
interface SeguimientoDao {
    @Insert suspend fun insertar(seguimiento: Seguimiento): Long

    @Query("SELECT * FROM seguimientos ORDER BY fechaHora DESC")
    fun observarTodos(): Flow<List<Seguimiento>>

    @Query("SELECT * FROM seguimientos WHERE oportunidadId = :oportunidadId ORDER BY fechaHora DESC")
    fun observarPorOportunidad(oportunidadId: Long): Flow<List<Seguimiento>>

    @Query("SELECT * FROM seguimientos WHERE contactoId = :contactoId ORDER BY fechaHora DESC")
    fun observarPorContacto(contactoId: Long): Flow<List<Seguimiento>>
}
