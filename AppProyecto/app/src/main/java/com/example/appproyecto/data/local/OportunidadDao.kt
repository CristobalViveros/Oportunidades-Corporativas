package com.example.appproyecto.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.example.appproyecto.data.model.Oportunidad
import kotlinx.coroutines.flow.Flow

@Dao
interface OportunidadDao {
    @Insert suspend fun insertar(oportunidad: Oportunidad): Long
    @Update suspend fun actualizar(oportunidad: Oportunidad)

    @Query("SELECT * FROM oportunidades ORDER BY fechaActualizacion DESC")
    fun observarTodas(): Flow<List<Oportunidad>>

    @Query("SELECT * FROM oportunidades WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): Oportunidad?

    @Query("SELECT * FROM oportunidades WHERE responsableId = :usuarioId ORDER BY fechaActualizacion DESC")
    fun observarPorResponsable(usuarioId: Long): Flow<List<Oportunidad>>
}