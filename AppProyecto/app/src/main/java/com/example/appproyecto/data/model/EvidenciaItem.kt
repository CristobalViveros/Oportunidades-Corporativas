package com.example.appproyecto.data.model

/** Una evidencia lista para mostrar, con el nombre de quien la registró. */
data class EvidenciaItem(
    val id: Long,
    val rutaFoto: String,
    val fecha: Long,
    val descripcion: String,
    val usuarioNombre: String
)
