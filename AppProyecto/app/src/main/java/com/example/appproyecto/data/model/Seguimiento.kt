package com.example.appproyecto.data.model

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * Una gestión realizada sobre una oportunidad (llamada, envío de propuesta, reunión...).
 * Los registros nunca se borran: forman el historial de la oportunidad.
 */
@Entity(
    tableName = "seguimientos",
    foreignKeys = [
        ForeignKey(
            entity = Oportunidad::class,
            parentColumns = ["id"],
            childColumns = ["oportunidadId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Contacto::class,
            parentColumns = ["id"],
            childColumns = ["contactoId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("oportunidadId"),
        Index("usuarioId"),
        Index("contactoId"),
        Index("fechaProximoSeguimiento")
    ]
)
data class Seguimiento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val oportunidadId: Long,
    /** Quién registró la gestión. */
    val usuarioId: Long,
    /** Contacto con el que se habló (opcional). */
    val contactoId: Long? = null,
    val tipoAccion: String = TipoAccion.SEGUIMIENTO.name,
    val fechaHora: Long = System.currentTimeMillis(),
    val resultado: String = "",
    val proximaAccion: String = "",
    val fechaProximoSeguimiento: Long? = null,
    /** Si esta gestión cambió el estado de la oportunidad, queda registrado aquí. */
    val estadoNuevo: String? = null,
    val observaciones: String = ""
)
