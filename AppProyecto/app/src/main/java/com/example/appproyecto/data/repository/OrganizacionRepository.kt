package com.example.appproyecto.data.repository

import com.example.appproyecto.data.local.OrganizacionDao
import com.example.appproyecto.data.model.Organizacion
import com.example.appproyecto.data.model.Resultado
import com.example.appproyecto.data.model.Usuario
import kotlinx.coroutines.flow.Flow

/** Las reglas se comprueban aquí además del formulario, para que ninguna pantalla guarde datos repetidos. */
class OrganizacionRepository(private val organizacionDao: OrganizacionDao) {

    fun observarTodas(): Flow<List<Organizacion>> = organizacionDao.observarTodas()

    fun observarPorId(id: Long): Flow<Organizacion?> = organizacionDao.observarPorId(id)

    suspend fun obtenerPorId(id: Long): Organizacion? = organizacionDao.obtenerPorId(id)

    suspend fun crear(actor: Usuario, nueva: Organizacion): Resultado {
        if (!actor.activo) return Resultado.Error("Tu cuenta no está activa")
        val nombre = nueva.nombre.trim()
        if (organizacionDao.contarDuplicados(nombre, excluirId = 0) > 0) {
            return Resultado.Error("Ya existe una organización con ese nombre")
        }
        organizacionDao.insertar(nueva.copy(id = 0, nombre = nombre))
        return Resultado.Exito("Organización guardada")
    }

    suspend fun actualizar(actor: Usuario, cambios: Organizacion): Resultado {
        if (!actor.activo) return Resultado.Error("Tu cuenta no está activa")
        organizacionDao.obtenerPorId(cambios.id) ?: return Resultado.Error("La organización no existe")
        val nombre = cambios.nombre.trim()
        if (organizacionDao.contarDuplicados(nombre, excluirId = cambios.id) > 0) {
            return Resultado.Error("Ya existe otra organización con ese nombre")
        }
        organizacionDao.actualizar(cambios.copy(nombre = nombre))
        return Resultado.Exito("Organización actualizada")
    }
}
