package com.example.appproyecto.data.model

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * Fotografía de respaldo de una oportunidad (el stand, el lugar, la activación...).
 * Una oportunidad puede tener varias; nunca se borran, igual que el historial de gestiones.
 */
@Entity(
    tableName = "evidencias",
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
        )
    ],
    indices = [Index("oportunidadId"), Index("usuarioId")]
)
data class Evidencia(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val oportunidadId: Long,
    /** Quién tomó la foto. */
    val usuarioId: Long,
    /** Ruta del archivo en el almacenamiento interno de la app. */
    val rutaFoto: String,
    val fecha: Long = System.currentTimeMillis(),
    val descripcion: String = ""
)
