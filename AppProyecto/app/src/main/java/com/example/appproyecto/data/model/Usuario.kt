package com.example.appproyecto.data.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey



@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val correo: String,
    val passwordHash: String,
    val perfil: String = "EJECUTIVO",
    val area: String = "Comercial",
    val zona: String = "Región Metropolitana",
    val activo: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis()
)
