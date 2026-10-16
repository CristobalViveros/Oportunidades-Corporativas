package com.example.appproyecto.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.example.appproyecto.data.model.Organizacion
import kotlinx.coroutines.flow.Flow

@Dao
interface OrganizacionDao {
    @Insert suspend fun insertar(organizacion: Organizacion): Long
    @Update suspend fun actualizar(organizacion: Organizacion)

    @Query("SELECT * FROM organizaciones ORDER BY nombre")
    fun observarTodas(): Flow<List<Organizacion>>

    @Query("SELECT * FROM organizaciones WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): Organizacion?
}