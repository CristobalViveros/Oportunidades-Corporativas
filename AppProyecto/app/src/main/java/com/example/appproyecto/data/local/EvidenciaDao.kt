package com.example.appproyecto.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import com.example.appproyecto.data.model.Evidencia
import com.example.appproyecto.data.model.EvidenciaItem
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenciaDao {
    @Insert suspend fun insertar(evidencia: Evidencia): Long

    /** Evidencias de una oportunidad, la más reciente primero. */
    @Query("""
        SELECT v.id AS id, v.rutaFoto AS rutaFoto, v.fecha AS fecha,
               v.descripcion AS descripcion, u.nombre AS usuarioNombre
        FROM evidencias v
        INNER JOIN usuarios u ON u.id = v.usuarioId
        WHERE v.oportunidadId = :oportunidadId
        ORDER BY v.fecha DESC, v.id DESC
    """)
    fun observarItemsPorOportunidad(oportunidadId: Long): Flow<List<EvidenciaItem>>
}
