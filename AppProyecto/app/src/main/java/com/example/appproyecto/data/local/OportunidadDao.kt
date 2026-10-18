package com.example.appproyecto.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.example.appproyecto.data.model.Oportunidad
import kotlinx.coroutines.flow.Flow
import com.example.appproyecto.data.model.OportunidadDetalle
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

    /** Todas las oportunidades (solo para el supervisor; el repositorio lo controla). */
    @Query("""
        SELECT o.id AS id, o.nombre AS nombre, o.estado AS estado, o.prioridad AS prioridad,
               o.potencial AS potencial,
               e.nombre AS eventoNombre, e.fechaInicio AS eventoFecha, e.comuna AS comuna,
               g.nombre AS organizacionNombre,
               u.id AS responsableId, u.nombre AS responsableNombre
        FROM oportunidades o
        INNER JOIN eventos e ON e.id = o.eventoId
        INNER JOIN organizaciones g ON g.id = e.organizacionId
        INNER JOIN usuarios u ON u.id = o.responsableId
        ORDER BY e.fechaInicio
    """)
    fun observarItems(): Flow<List<OportunidadItem>>

    /** Solo las oportunidades de un ejecutivo. */
    @Query("""
        SELECT o.id AS id, o.nombre AS nombre, o.estado AS estado, o.prioridad AS prioridad,
               o.potencial AS potencial,
               e.nombre AS eventoNombre, e.fechaInicio AS eventoFecha, e.comuna AS comuna,
               g.nombre AS organizacionNombre,
               u.id AS responsableId, u.nombre AS responsableNombre
        FROM oportunidades o
        INNER JOIN eventos e ON e.id = o.eventoId
        INNER JOIN organizaciones g ON g.id = e.organizacionId
        INNER JOIN usuarios u ON u.id = o.responsableId
        WHERE o.responsableId = :usuarioId
        ORDER BY e.fechaInicio
    """)
    fun observarItemsPorResponsable(usuarioId: Long): Flow<List<OportunidadItem>>

    /** Detalle completo de una oportunidad. El repositorio comprueba el permiso de lectura. */
    @Query("""
        SELECT o.id AS id, o.nombre AS nombre, o.tipo AS tipo, o.descripcion AS descripcion,
               o.fuente AS fuente, o.fechaDeteccion AS fechaDeteccion, o.prioridad AS prioridad,
               o.potencial AS potencial, o.afinidad AS afinidad, o.estado AS estado,
               o.fechaLimitePostulacion AS fechaLimitePostulacion,
               o.fechaActualizacion AS fechaActualizacion, o.observaciones AS observaciones,
               u.id AS responsableId, u.nombre AS responsableNombre,
               e.id AS eventoId, e.nombre AS eventoNombre, e.fechaInicio AS fechaInicio,
               e.fechaTermino AS fechaTermino, e.horario AS horario, e.direccion AS direccion,
               e.comuna AS comuna, e.region AS region, e.publicoEsperado AS publicoEsperado,
               e.publicoObjetivo AS publicoObjetivo, e.condiciones AS condiciones,
               e.costoReferencial AS costoReferencial, e.fotoUri AS fotoUri,
               g.id AS organizacionId, g.nombre AS organizacionNombre
        FROM oportunidades o
        INNER JOIN eventos e ON e.id = o.eventoId
        INNER JOIN organizaciones g ON g.id = e.organizacionId
        INNER JOIN usuarios u ON u.id = o.responsableId
        WHERE o.id = :id
    """)
    fun observarDetalle(id: Long): Flow<OportunidadDetalle?>
}
