package com.example.appproyecto.data.model

/** Fila lista para mostrar en pantalla: une oportunidad, evento, organización y responsable. */
data class OportunidadItem(
    val id: Long,
    val nombre: String,
    val estado: String,
    val prioridad: String,
    val potencial: Int,
    val eventoNombre: String,
    val eventoFecha: Long,
    val comuna: String,
    val organizacionNombre: String,
    val responsableId: Long,
    val responsableNombre: String
)
