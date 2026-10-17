package com.example.appproyecto.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.example.appproyecto.data.model.Oportunidad
import kotlinx.coroutines.flow.Flow
import com.example.appproyecto.data.model.OportunidadItem

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

    @Query("""
        SELECT o.id AS id, o.nombre AS nombre, o.estado AS estado, o.prioridad AS prioridad,
               o.potencial AS potencial,
               e.nombre AS eventoNombre, e.fechaInicio AS eventoFecha, e.comuna AS comuna,
               g.nombre AS organizacionNombre, u.nombre AS responsableNombre
        FROM oportunidades o
        INNER JOIN eventos e ON e.id = o.eventoId
        INNER JOIN organizaciones g ON g.id = e.organizacionId
        INNER JOIN usuarios u ON u.id = o.responsableId
        ORDER BY e.fechaInicio
    """)
    fun observarItems(): Flow<List<OportunidadItem>>
}