package com.example.appproyecto.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.example.appproyecto.data.model.Contacto
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactoDao {
    @Insert suspend fun insertar(contacto: Contacto): Long
    @Update suspend fun actualizar(contacto: Contacto)

    @Query("SELECT * FROM contactos ORDER BY nombre")
    fun observarTodos(): Flow<List<Contacto>>

    @Query("SELECT * FROM contactos WHERE organizacionId = :organizacionId ORDER BY nombre")
    fun observarPorOrganizacion(organizacionId: Long): Flow<List<Contacto>>

    @Query("SELECT * FROM contactos WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): Contacto?

    @Query("SELECT * FROM contactos WHERE id = :id LIMIT 1")
    fun observarPorId(id: Long): Flow<Contacto?>

    /** Mismo correo en la misma organización = contacto repetido. [excluirId] sirve al editar. */
    @Query(
        "SELECT COUNT(*) FROM contactos WHERE organizacionId = :organizacionId " +
            "AND LOWER(correo) = LOWER(:correo) AND id != :excluirId"
    )
    suspend fun contarDuplicados(organizacionId: Long, correo: String, excluirId: Long): Int
}
