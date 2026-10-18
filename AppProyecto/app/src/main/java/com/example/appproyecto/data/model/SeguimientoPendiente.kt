package com.example.appproyecto.data.model

/** Próxima gestión programada de una oportunidad abierta (la que dejó la última gestión con fecha). */
data class SeguimientoPendiente(
    val oportunidadId: Long,
    val oportunidadNombre: String,
    val estado: String,
    val responsableId: Long,
    val responsableNombre: String,
    val proximaAccion: String,
    val fecha: Long
)
