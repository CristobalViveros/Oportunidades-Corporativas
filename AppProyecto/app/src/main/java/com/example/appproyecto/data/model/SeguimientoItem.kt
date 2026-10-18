package com.example.appproyecto.data.model

/** Una gestión del historial, con el nombre de quien la registró y del contacto (si hubo). */
data class SeguimientoItem(
    val id: Long,
    val tipoAccion: String,
    val fechaHora: Long,
    val resultado: String,
    val proximaAccion: String,
    val fechaProximoSeguimiento: Long?,
    val estadoNuevo: String?,
    val observaciones: String,
    val usuarioNombre: String,
    val contactoNombre: String?
)
