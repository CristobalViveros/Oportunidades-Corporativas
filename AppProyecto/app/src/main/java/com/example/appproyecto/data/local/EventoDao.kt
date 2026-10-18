package com.example.appproyecto.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.example.appproyecto.data.model.Evento
import kotlinx.coroutines.flow.Flow

@Dao
interface EventoDao {
    @Insert suspend fun insertar(evento: Evento): Long
    @Update suspend fun actualizar(evento: Evento)

    @Query("SELECT * FROM eventos ORDER BY fechaInicio")
    fun observarTodos(): Flow<List<Evento>>

    @Query("SELECT * FROM eventos WHERE fechaInicio >= :desde ORDER BY fechaInicio")
    fun observarProximos(desde: Long): Flow<List<Evento>>

    @Query("SELECT * FROM eventos WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): Evento?

    /** Mismo nombre (sin distinguir mayúsculas), misma fecha y misma organización = evento repetido. */
    @Query(
        "SELECT COUNT(*) FROM eventos WHERE LOWER(nombre) = LOWER(:nombre) " +
            "AND fechaInicio = :fechaInicio AND organizacionId = :organizacionId"
    )
    suspend fun contarDuplicados(nombre: String, fechaInicio: Long, organizacionId: Long): Int
}
