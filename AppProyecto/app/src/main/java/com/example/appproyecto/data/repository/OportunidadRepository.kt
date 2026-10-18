package com.example.appproyecto.data.repository

import com.example.appproyecto.data.local.OportunidadDao
import com.example.appproyecto.data.local.SeguimientoDao
import com.example.appproyecto.data.local.UsuarioDao
import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.Oportunidad
import com.example.appproyecto.data.model.OportunidadItem
import com.example.appproyecto.data.model.Perfil
import com.example.appproyecto.data.model.Resultado
import com.example.appproyecto.data.model.Seguimiento
import com.example.appproyecto.data.model.TipoAccion
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.data.model.aEnum
import com.example.appproyecto.util.Permisos
import com.example.appproyecto.util.rol
import kotlinx.coroutines.flow.Flow

/**
 * Las reglas de permisos se aplican AQUÍ (y no solo en la pantalla), así ninguna pantalla
 * puede saltárselas por error. Cada operación recibe al usuario que la realiza ([actor]).
 */
class OportunidadRepository(
    private val oportunidadDao: OportunidadDao,
    private val seguimientoDao: SeguimientoDao,
    private val usuarioDao: UsuarioDao
) {

    // ---------- Consultas ----------

    /** Supervisor: todas las oportunidades. Ejecutivo: solo las suyas. */
    fun observarItemsPara(actor: Usuario): Flow<List<OportunidadItem>> =
        if (Permisos.esSupervisor(actor)) oportunidadDao.observarItems()
        else oportunidadDao.observarItemsPorResponsable(actor.id)

    /** Devuelve la oportunidad solo si el usuario tiene permiso para verla. */
    suspend fun obtenerPara(actor: Usuario, id: Long): Oportunidad? =
        oportunidadDao.obtenerPorId(id)?.takeIf { Permisos.puedeVer(actor, it) }

    // ---------- Crear y editar ----------

    suspend fun crear(actor: Usuario, nueva: Oportunidad): Resultado {
        if (!actor.activo) return Resultado.Error("Tu cuenta no está activa")

        val responsableId = if (Permisos.puedeAsignar(actor)) {
            val responsable = usuarioDao.obtenerPorId(nueva.responsableId)
            if (responsable == null || !responsable.activo || responsable.rol() != Perfil.EJECUTIVO) {
                return Resultado.Error("El responsable debe ser un ejecutivo activo")
            }
            responsable.id
        } else {
            actor.id // un ejecutivo siempre crea oportunidades a su nombre
        }

        oportunidadDao.insertar(
            nueva.copy(
                id = 0,
                responsableId = responsableId,
                estado = EstadoOportunidad.EVENTO_IDENTIFICADO.name,
                fechaActualizacion = System.currentTimeMillis()
            )
        )
        return Resultado.Exito("Oportunidad creada")
    }

    /**
     * Edita los datos de la oportunidad. El estado y el responsable NO se pueden cambiar desde
     * aquí (para eso están [cambiarEstado] y [asignar]); la prioridad solo la cambia el supervisor.
     */
    suspend fun actualizar(actor: Usuario, cambios: Oportunidad): Resultado {
        val actual = oportunidadDao.obtenerPorId(cambios.id)
            ?: return Resultado.Error("La oportunidad no existe")
        if (!Permisos.puedeGestionar(actor, actual)) {
            return Resultado.Error("No tienes acceso a esta oportunidad")
        }
        oportunidadDao.actualizar(
            cambios.copy(
                estado = actual.estado,
                responsableId = actual.responsableId,
                prioridad = if (Permisos.puedeCambiarPrioridad(actor)) cambios.prioridad else actual.prioridad,
                fechaActualizacion = System.currentTimeMillis()
            )
        )
        return Resultado.Exito("Oportunidad actualizada")
    }

    // ---------- Estados ----------

    /** Cambia el estado respetando permisos y transiciones, y deja constancia en el historial. */
    suspend fun cambiarEstado(
        actor: Usuario,
        oportunidadId: Long,
        nuevo: EstadoOportunidad,
        observacion: String = ""
    ): Resultado {
        val oportunidad = oportunidadDao.obtenerPorId(oportunidadId)
            ?: return Resultado.Error("La oportunidad no existe")
        if (!Permisos.puedeGestionar(actor, oportunidad)) {
            return Resultado.Error("No tienes acceso a esta oportunidad")
        }

        val actual = oportunidad.estado.aEnum(EstadoOportunidad.EVENTO_IDENTIFICADO)
        if (!Permisos.puedeCambiarEstado(actor, oportunidad, nuevo)) {
            val mensaje = if (nuevo in Permisos.estadosSoloSupervisor && !Permisos.esSupervisor(actor)) {
                "Solo un supervisor puede confirmar o descartar una oportunidad"
            } else {
                "No se puede pasar de «${actual.etiqueta}» a «${nuevo.etiqueta}»"
            }
            return Resultado.Error(mensaje)
        }

        val ahora = System.currentTimeMillis()
        oportunidadDao.actualizar(oportunidad.copy(estado = nuevo.name, fechaActualizacion = ahora))
        seguimientoDao.insertar(
            Seguimiento(
                oportunidadId = oportunidad.id,
                usuarioId = actor.id,
                tipoAccion = when (nuevo) {
                    EstadoOportunidad.PARTICIPACION_CONFIRMADA -> TipoAccion.CONFIRMACION.name
                    EstadoOportunidad.OPORTUNIDAD_CERRADA -> TipoAccion.CIERRE.name
                    else -> TipoAccion.SEGUIMIENTO.name
                },
                fechaHora = ahora,
                resultado = "Cambio de estado: ${actual.etiqueta} → ${nuevo.etiqueta}",
                estadoNuevo = nuevo.name,
                observaciones = observacion
            )
        )
        return Resultado.Exito("Estado actualizado a «${nuevo.etiqueta}»")
    }

    // ---------- Asignación (solo supervisor) ----------

    suspend fun asignar(actor: Usuario, oportunidadId: Long, nuevoResponsableId: Long): Resultado {
        if (!Permisos.puedeAsignar(actor)) {
            return Resultado.Error("Solo un supervisor puede asignar oportunidades")
        }
        val oportunidad = oportunidadDao.obtenerPorId(oportunidadId)
            ?: return Resultado.Error("La oportunidad no existe")

        val estado = oportunidad.estado.aEnum(EstadoOportunidad.EVENTO_IDENTIFICADO)
        if (Permisos.esFinal(estado)) {
            return Resultado.Error("No se puede reasignar una oportunidad cerrada o descartada")
        }
        if (oportunidad.responsableId == nuevoResponsableId) {
            return Resultado.Error("La oportunidad ya está asignada a ese ejecutivo")
        }

        val nuevo = usuarioDao.obtenerPorId(nuevoResponsableId)
        if (nuevo == null || !nuevo.activo || nuevo.rol() != Perfil.EJECUTIVO) {
            return Resultado.Error("Solo se puede asignar a un ejecutivo activo")
        }
        val anterior = usuarioDao.obtenerPorId(oportunidad.responsableId)

        val ahora = System.currentTimeMillis()
        oportunidadDao.actualizar(oportunidad.copy(responsableId = nuevo.id, fechaActualizacion = ahora))
        seguimientoDao.insertar(
            Seguimiento(
                oportunidadId = oportunidad.id,
                usuarioId = actor.id,
                tipoAccion = TipoAccion.SEGUIMIENTO.name,
                fechaHora = ahora,
                resultado = "Reasignada de ${anterior?.nombre ?: "sin responsable"} a ${nuevo.nombre}"
            )
        )
        return Resultado.Exito("Asignada a ${nuevo.nombre}")
    }
}
