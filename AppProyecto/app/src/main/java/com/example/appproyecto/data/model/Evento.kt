package com.example.appproyecto.data.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "eventos")
data class Evento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val tipo: String = TipoOportunidad.EVENTO_CORPORATIVO.name,
    val descripcion: String = "",
    val fechaInicio: Long,
    val fechaTermino: Long,
    val horario: String = "",
    val direccion: String = "",
    val comuna: String = "",
    val region: String = "",
    val organizacionId: Long,
    val publicoEsperado: Int = 0,
    val publicoObjetivo: String = "",
    val condiciones: String = "",
    val costoReferencial: Int = 0,
    val observaciones: String = "",
    val fotoUri: String? = null
)