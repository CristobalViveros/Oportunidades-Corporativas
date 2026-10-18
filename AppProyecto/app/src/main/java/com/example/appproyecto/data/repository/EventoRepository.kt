package com.example.appproyecto.data.repository

import com.example.appproyecto.data.local.EventoDao
import com.example.appproyecto.data.model.Evento
import com.example.appproyecto.data.model.Resultado
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.util.COSTO_MAXIMO
import kotlinx.coroutines.flow.Flow

class EventoRepository(private val eventoDao: EventoDao) {
    fun observarTodos(): Flow<List<Evento>> = eventoDao.observarTodos()

    suspend fun insertar(evento: Evento) = eventoDao.insertar(evento)

    /**
     * Registra un evento nuevo. Las reglas de negocio se vuelven a comprobar aquí (además del
     * formulario) para que ninguna pantalla pueda guardar datos incoherentes.
     */
    suspend fun crear(actor: Usuario, nuevo: Evento): Resultado {
        if (!actor.activo) return Resultado.Error("Tu cuenta no está activa")
        if (nuevo.fechaTermino < nuevo.fechaInicio) {
            return Resultado.Error("La fecha de término no puede ser anterior al inicio")
        }
        if (nuevo.costoReferencial < 0 || nuevo.costoReferencial > COSTO_MAXIMO) {
            return Resultado.Error("El costo referencial no es válido")
        }
        if (nuevo.publicoEsperado < 1) {
            return Resultado.Error("El público esperado debe ser al menos 1 persona")
        }
        val nombre = nuevo.nombre.trim()
        if (eventoDao.contarDuplicados(nombre, nuevo.fechaInicio, nuevo.organizacionId) > 0) {
            return Resultado.Error("Ya existe un evento con ese nombre, fecha y organización")
        }
        eventoDao.insertar(nuevo.copy(id = 0, nombre = nombre))
        return Resultado.Exito("Evento guardado")
    }
}
