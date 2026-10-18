package com.example.appproyecto.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import com.example.appproyecto.data.model.Seguimiento
import com.example.appproyecto.data.model.SeguimientoItem
import com.example.appproyecto.data.model.SeguimientoPendiente
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

    /** Historial de una oportunidad (más reciente primero) con nombres de usuario y contacto. */
    @Query("""
        SELECT s.id AS id, s.tipoAccion AS tipoAccion, s.fechaHora AS fechaHora,
               s.resultado AS resultado, s.proximaAccion AS proximaAccion,
               s.fechaProximoSeguimiento AS fechaProximoSeguimiento,
               s.estadoNuevo AS estadoNuevo, s.observaciones AS observaciones,
               u.nombre AS usuarioNombre, c.nombre AS contactoNombre
        FROM seguimientos s
        INNER JOIN usuarios u ON u.id = s.usuarioId
        LEFT JOIN contactos c ON c.id = s.contactoId
        WHERE s.oportunidadId = :oportunidadId
        ORDER BY s.fechaHora DESC, s.id DESC
    """)
    fun observarItemsPorOportunidad(oportunidadId: Long): Flow<List<SeguimientoItem>>

    /**
     * Próxima gestión programada de cada oportunidad abierta: la de la última gestión que dejó una
     * fecha (el mismo criterio que usa la pantalla de detalle). [responsableId] = 0 trae todas
     * (solo para el supervisor; el repositorio lo controla).
     */
    @Query("""
        SELECT o.id AS oportunidadId, o.nombre AS oportunidadNombre, o.estado AS estado,
               u.id AS responsableId, u.nombre AS responsableNombre,
               s.proximaAccion AS proximaAccion, s.fechaProximoSeguimiento AS fecha
        FROM seguimientos s
        INNER JOIN oportunidades o ON o.id = s.oportunidadId
        INNER JOIN usuarios u ON u.id = o.responsableId
        WHERE s.fechaProximoSeguimiento IS NOT NULL
          AND o.estado NOT IN ('OPORTUNIDAD_DESCARTADA', 'OPORTUNIDAD_CERRADA')
          AND (:responsableId = 0 OR o.responsableId = :responsableId)
          AND s.id = (
              SELECT s2.id FROM seguimientos s2
              WHERE s2.oportunidadId = s.oportunidadId AND s2.fechaProximoSeguimiento IS NOT NULL
              ORDER BY s2.fechaHora DESC, s2.id DESC
              LIMIT 1
          )
        ORDER BY s.fechaProximoSeguimiento
    """)
    fun observarPendientes(responsableId: Long): Flow<List<SeguimientoPendiente>>
}
