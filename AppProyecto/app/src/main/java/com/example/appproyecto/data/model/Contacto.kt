package com.example.appproyecto.data.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "contactos")
data class Contacto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val organizacionId: Long,
    val nombre: String,
    val cargo: String = "",
    val area: String = AreaContacto.EVENTOS.name,
    val correo: String = "",
    val telefono: String = "",
    val medioPreferente: String = "Correo",
    val estado: String = "Activo",
    val fechaUltimoContacto: Long? = null,
    val observaciones: String = ""
)