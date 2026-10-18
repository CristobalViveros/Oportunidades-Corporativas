package com.example.appproyecto.data.repository

import com.example.appproyecto.data.local.ContactoDao
import com.example.appproyecto.data.local.OrganizacionDao
import com.example.appproyecto.data.model.Contacto
import com.example.appproyecto.data.model.Resultado
import com.example.appproyecto.data.model.Usuario
import kotlinx.coroutines.flow.Flow

class ContactoRepository(
    private val contactoDao: ContactoDao,
    private val organizacionDao: OrganizacionDao
) {
    fun observarTodos(): Flow<List<Contacto>> = contactoDao.observarTodos()

    fun observarPorOrganizacion(organizacionId: Long): Flow<List<Contacto>> =
        contactoDao.observarPorOrganizacion(organizacionId)

    fun observarPorId(id: Long): Flow<Contacto?> = contactoDao.observarPorId(id)

    suspend fun obtenerPorId(id: Long): Contacto? = contactoDao.obtenerPorId(id)

    suspend fun crear(actor: Usuario, nuevo: Contacto): Resultado {
        if (!actor.activo) return Resultado.Error("Tu cuenta no está activa")
        if (organizacionDao.obtenerPorId(nuevo.organizacionId) == null) {
            return Resultado.Error("La organización no existe")
        }
        val correo = nuevo.correo.trim().lowercase()
        if (contactoDao.contarDuplicados(nuevo.organizacionId, correo, excluirId = 0) > 0) {
            return Resultado.Error("Ya existe un contacto con ese correo en esta organización")
        }
        contactoDao.insertar(nuevo.copy(id = 0, nombre = nuevo.nombre.trim(), correo = correo))
        return Resultado.Exito("Contacto guardado")
    }

    suspend fun actualizar(actor: Usuario, cambios: Contacto): Resultado {
        if (!actor.activo) return Resultado.Error("Tu cuenta no está activa")
        val actual = contactoDao.obtenerPorId(cambios.id) ?: return Resultado.Error("El contacto no existe")
        val correo = cambios.correo.trim().lowercase()
        if (contactoDao.contarDuplicados(actual.organizacionId, correo, excluirId = cambios.id) > 0) {
            return Resultado.Error("Ya existe otro contacto con ese correo en esta organización")
        }
        // La organización y la fecha del último contacto no se cambian desde el formulario.
        contactoDao.actualizar(
            cambios.copy(
                organizacionId = actual.organizacionId,
                fechaUltimoContacto = actual.fechaUltimoContacto,
                nombre = cambios.nombre.trim(),
                correo = correo
            )
        )
        return Resultado.Exito("Contacto actualizado")
    }
}
