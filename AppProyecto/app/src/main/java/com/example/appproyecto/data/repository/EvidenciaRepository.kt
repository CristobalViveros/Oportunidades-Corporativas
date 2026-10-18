package com.example.appproyecto.data.repository

import com.example.appproyecto.data.local.EvidenciaDao
import com.example.appproyecto.data.local.OportunidadDao
import com.example.appproyecto.data.model.Evidencia
import com.example.appproyecto.data.model.EvidenciaItem
import com.example.appproyecto.data.model.Resultado
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.util.Permisos
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import java.io.File

/** Las evidencias siguen las mismas reglas de acceso que la oportunidad a la que pertenecen. */
class EvidenciaRepository(
    private val evidenciaDao: EvidenciaDao,
    private val oportunidadDao: OportunidadDao
) {

    /** Vacío si la oportunidad no existe o el usuario no tiene permiso para verla. */
    fun observarPara(actor: Usuario, oportunidadId: Long): Flow<List<EvidenciaItem>> = flow {
        val oportunidad = oportunidadDao.obtenerPorId(oportunidadId)
        if (oportunidad != null && Permisos.puedeVer(actor, oportunidad)) {
            emitAll(evidenciaDao.observarItemsPorOportunidad(oportunidadId))
        } else {
            emit(emptyList())
        }
    }

    suspend fun agregar(
        actor: Usuario,
        oportunidadId: Long,
        rutaFoto: String,
        descripcion: String
    ): Resultado {
        val oportunidad = oportunidadDao.obtenerPorId(oportunidadId)
            ?: return Resultado.Error("La oportunidad no existe")
        if (!Permisos.puedeGestionar(actor, oportunidad)) {
            return Resultado.Error("No tienes acceso a esta oportunidad")
        }
        if (descripcion.trim().length > DESCRIPCION_MAXIMA) {
            return Resultado.Error("La descripción no puede superar $DESCRIPCION_MAXIMA caracteres")
        }
        if (!File(rutaFoto).exists()) {
            return Resultado.Error("No se encontró la fotografía, intenta tomarla de nuevo")
        }
        evidenciaDao.insertar(
            Evidencia(
                oportunidadId = oportunidadId,
                usuarioId = actor.id,
                rutaFoto = rutaFoto,
                fecha = System.currentTimeMillis(),
                descripcion = descripcion.trim()
            )
        )
        return Resultado.Exito("Evidencia guardada")
    }

    companion object {
        const val DESCRIPCION_MAXIMA = 100
    }
}
