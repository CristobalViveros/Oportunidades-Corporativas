package com.example.appproyecto.ui.oportunidades

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.appproyecto.data.local.AppDatabase
import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.Evento
import com.example.appproyecto.data.model.Oportunidad
import com.example.appproyecto.data.model.Prioridad
import com.example.appproyecto.data.model.TipoOportunidad
import com.example.appproyecto.data.model.aEnum
import com.example.appproyecto.data.repository.EventoRepository
import com.example.appproyecto.data.repository.OportunidadRepository
import com.example.appproyecto.util.SesionManager
import com.example.appproyecto.util.validarSeleccion
import com.example.appproyecto.util.validarTextoObligatorio
import com.example.appproyecto.util.validarTextoOpcional
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

data class FormUiState(
    val nombre: String = "",
    val descripcion: String = "",
    val observaciones: String = "",
    val eventoId: Long? = null,
    val prioridad: Prioridad = Prioridad.MEDIA,
    val estado: EstadoOportunidad = EstadoOportunidad.EVENTO_IDENTIFICADO,
    val eventos: List<Evento> = emptyList(),
    val esEdicion: Boolean = false,
    val errorNombre: String? = null,
    val errorEvento: String? = null,
    val errorDescripcion: String? = null,
    val errorObservaciones: String? = null,
    val guardado: Boolean = false
)

class OportunidadFormViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = OportunidadRepository(db.oportunidadDao())
    private val sesion = SesionManager(application)

    private val idEditar: Long = savedStateHandle.get<Long>("id") ?: 0L
    private var original: Oportunidad? = null

    private val _uiState = MutableStateFlow(FormUiState(esEdicion = idEditar != 0L))
    val uiState: StateFlow<FormUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Cargamos la lista de eventos (una sola vez) para el selector.
            val eventos = EventoRepository(db.eventoDao()).observarTodos().first()
            _uiState.update { it.copy(eventos = eventos) }

            if (idEditar != 0L) {
                repository.obtenerPorId(idEditar)?.let { op ->
                    original = op
                    _uiState.update {
                        it.copy(
                            nombre = op.nombre,
                            descripcion = op.descripcion,
                            observaciones = op.observaciones,
                            eventoId = op.eventoId,
                            prioridad = op.prioridad.aEnum(Prioridad.MEDIA),
                            estado = op.estado.aEnum(EstadoOportunidad.EVENTO_IDENTIFICADO)
                        )
                    }
                }
            }
        }
    }

    // Cada cambio limpia el error de ese campo: el usuario ve que lo está corrigiendo.
    fun cambiarNombre(v: String) = _uiState.update { it.copy(nombre = v, errorNombre = null) }
    fun cambiarDescripcion(v: String) = _uiState.update { it.copy(descripcion = v, errorDescripcion = null) }
    fun cambiarObservaciones(v: String) = _uiState.update { it.copy(observaciones = v, errorObservaciones = null) }
    fun elegirEvento(id: Long) = _uiState.update { it.copy(eventoId = id, errorEvento = null) }
    fun elegirPrioridad(p: Prioridad) = _uiState.update { it.copy(prioridad = p) }
    fun elegirEstado(e: EstadoOportunidad) = _uiState.update { it.copy(estado = e) }

    fun guardar() {
        val s = _uiState.value
        val errNombre = validarTextoObligatorio(s.nombre, "El nombre", 5, 80)
        val errEvento = validarSeleccion(s.eventoId, "un evento")
        val errDesc = validarTextoOpcional(s.descripcion, "La descripción")
        val errObs = validarTextoOpcional(s.observaciones, "Las observaciones")

        if (listOf(errNombre, errEvento, errDesc, errObs).any { it != null }) {
            _uiState.update {
                it.copy(
                    errorNombre = errNombre, errorEvento = errEvento,
                    errorDescripcion = errDesc, errorObservaciones = errObs
                )
            }
            return
        }

        viewModelScope.launch {
            val ahora = System.currentTimeMillis()
            val base = original
            if (base != null) {
                repository.actualizar(
                    base.copy(
                        nombre = s.nombre.trim(),
                        descripcion = s.descripcion.trim(),
                        observaciones = s.observaciones.trim(),
                        eventoId = s.eventoId!!,
                        prioridad = s.prioridad.name,
                        estado = s.estado.name,
                        fechaActualizacion = ahora
                    )
                )
            } else {
                repository.insertar(
                    Oportunidad(
                        id = 0,
                        nombre = s.nombre.trim(),
                        tipo = TipoOportunidad.entries.first().name,
                        descripcion = s.descripcion.trim(),
                        fuente = "Registro manual",
                        fechaDeteccion = ahora,
                        eventoId = s.eventoId!!,
                        responsableId = sesion.usuarioId() ?: 0L,
                        prioridad = s.prioridad.name,
                        potencial = 3,
                        afinidad = 3,
                        estado = s.estado.name,
                        fechaLimitePostulacion = null,
                        fechaActualizacion = ahora,
                        observaciones = s.observaciones.trim()
                    )
                )
            }
            _uiState.update { it.copy(guardado = true) }
        }
    }
}