package com.example.appproyecto.util

// Devuelven el mensaje de error, o null si el dato es válido.

fun validarCorreo(correo: String): String? = when {
    correo.isBlank() -> "El correo es obligatorio"
    !correo.contains("@") -> "El correo debe contener @"
    !correo.trim().endsWith(".cl") -> "El correo debe terminar en .cl"
    else -> null
}

fun validarPassword(password: String): String? = when {
    password.isBlank() -> "La contraseña es obligatoria"
    password.length < 8 -> "Mínimo 8 caracteres"
    else -> null
}

fun validarTextoObligatorio(valor: String, campo: String, minimo: Int = 3, maximo: Int = 80): String? = when {
    valor.isBlank() -> "$campo es obligatorio"
    valor.trim().length < minimo -> "$campo debe tener al menos $minimo caracteres"
    valor.trim().length > maximo -> "$campo no puede superar $maximo caracteres"
    else -> null
}

fun validarTextoOpcional(valor: String, campo: String, maximo: Int = 300): String? =
    if (valor.length > maximo) "$campo no puede superar $maximo caracteres" else null

fun validarSeleccion(valor: Any?, campo: String): String? =
    if (valor == null) "Selecciona $campo" else null

fun validarEntero(valor: String, campo: String, minimo: Int, maximo: Int): String? {
    val numero = valor.trim().toIntOrNull() ?: return "$campo debe ser un número"
    return if (numero !in minimo..maximo) "$campo debe estar entre $minimo y $maximo" else null
}

fun validarFecha(fecha: Long?): String? =
    if (fecha == null) "Selecciona una fecha" else null