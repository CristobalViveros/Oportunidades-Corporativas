package com.example.appproyecto.util

import android.content.Context
import java.security.MessageDigest

/** Convierte un texto en su "huella" SHA-256. Así no guardamos contraseñas en texto plano. */
fun sha256(texto: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(texto.toByteArray())
        .joinToString("") { "%02x".format(it) }

/** Recuerda qué usuario inició sesión (control de sesión). */
class SesionManager(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("sesion", Context.MODE_PRIVATE)

    fun guardar(usuarioId: Long) = prefs.edit().putLong("usuario_id", usuarioId).apply()

    fun usuarioId(): Long? = prefs.getLong("usuario_id", -1L).takeIf { it != -1L }

    fun cerrar() = prefs.edit().remove("usuario_id").apply()
}