package com.example.appproyecto.data.repository

import com.example.appproyecto.data.local.UsuarioDao
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.util.sha256
import kotlinx.coroutines.flow.Flow

class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    /** Devuelve el usuario si correo y contraseña son correctos; si no, null. */
    suspend fun autenticar(correo: String, password: String): Usuario? {
        val usuario = usuarioDao.obtenerPorCorreo(correo.trim().lowercase()) ?: return null
        return usuario.takeIf { it.activo && it.passwordHash == sha256(password) }
    }

    suspend fun obtenerPorId(id: Long): Usuario? = usuarioDao.obtenerPorId(id)

    fun observarEjecutivos(): Flow<List<Usuario>> = usuarioDao.observarEjecutivos()
}