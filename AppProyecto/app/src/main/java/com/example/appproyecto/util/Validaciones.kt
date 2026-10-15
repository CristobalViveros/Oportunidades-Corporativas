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