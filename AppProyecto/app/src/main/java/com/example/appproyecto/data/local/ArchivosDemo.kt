package com.example.appproyecto.data.local

import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.util.sha256

/** Carga datos 100% ficticios solo si la base está vacía. */
suspend fun sembrarDatosDemo(db: AppDatabase) {
    if (db.usuarioDao().contar() > 0) return

    db.usuarioDao().insertar(
        Usuario(nombre = "Ana Pérez (demo)", correo = "ana.ejecutiva@ejemplo.cl",
            passwordHash = sha256("Ana12345"), perfil = "EJECUTIVO", zona = "Región Metropolitana")
    )
    db.usuarioDao().insertar(
        Usuario(nombre = "Carlos Soto (demo)", correo = "carlos.ejecutivo@ejemplo.cl",
            passwordHash = sha256("Carlos123"), perfil = "EJECUTIVO", zona = "Valparaíso")
    )
    db.usuarioDao().insertar(
        Usuario(nombre = "Sofía Rojas (demo)", correo = "sofia.supervisora@ejemplo.cl",
            passwordHash = sha256("Sofia1234"), perfil = "SUPERVISOR", area = "Supervisión")
    )
}