package com.example.appproyecto.data.model

/** Resultado de una operación: siempre trae un mensaje claro para mostrar al usuario. */
sealed interface Resultado {
    val mensaje: String

    data class Exito(override val mensaje: String) : Resultado
    data class Error(override val mensaje: String) : Resultado
}
