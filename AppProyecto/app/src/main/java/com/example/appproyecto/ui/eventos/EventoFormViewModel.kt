package com.example.appproyecto.ui.eventos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appproyecto.data.local.AppDatabase
import com.example.appproyecto.data.model.Evento
import com.example.appproyecto.data.model.Organizacion
import com.example.appproyecto.data.repository.EventoRepository
import com.example.appproyecto.util.validarEntero
import com.example.appproyecto.util.validarFecha
import com.example.appproyecto.util.validarSeleccion
import com.example.appproyecto.util.validarTextoObligatorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventoFormUiState(
    val nombre: String = "",
    val tipo: String = "Feria",
    val comuna: String = "",
    val direccion: String = "",
    val publicoEsperado: String = "",
    val fecha: Long? = null,
    val organizacionId: Long? = null,
    val fotoRuta: String? = null,
    val organizaciones: List<Organizacion> = emptyList(),
    val errorNombre: String? = null,
    val errorComuna: String? = null,
    val errorPublico: String? = null,
    val errorFecha: String? = null,
    val errorOrganizacion: String? = null,
    val guardado: Boolean = false
)

class EventoFormViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = EventoRepository(db.eventoDao())

    private val _uiState = MutableStateFlow(EventoFormUiState())
    val uiState: StateFlow<EventoFormUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val organizaciones = db.organizacionDao().observarTodas().first()
            _uiState.update { it.copy(organizaciones = organizaciones) }
        }
    }

    fun cambiarNombre(v: String) = _uiState.update { it.copy(nombre = v, errorNombre = null) }
    fun cambiarTipo(v: String) = _uiState.update { it.copy(tipo = v) }
    fun cambiarComuna(v: String) = _uiState.update { it.copy(comuna = v, errorComuna = null) }
    fun cambiarDireccion(v: String) = _uiState.update { it.copy(direccion = v.take(120)) }
    fun cambiarPublico(v: String) =
        _uiState.update { it.copy(publicoEsperado = v.filter { c -> c.isDigit() }.take(6), errorPublico = null) }
    fun elegirOrganizacion(id: Long) = _uiState.update { it.copy(organizacionId = id, errorOrganizacion = null) }
    fun fijarFecha(millis: Long) = _uiState.update { it.copy(fecha = millis, errorFecha = null) }
    fun fijarFoto(ruta: String) = _uiState.update { it.copy(fotoRuta = ruta) }

    fun guardar() {
        val s = _uiState.value
        val errNombre = validarTextoObligatorio(s.nombre, "El nombre", 5, 80)
        val errComuna = validarTextoObligatorio(s.comuna, "La comuna", 3, 40)
        val errPublico = validarEntero(s.publicoEsperado, "El público esperado", 1, 100000)
        val errFecha = validarFecha(s.fecha)
        val errOrg = validarSeleccion(s.organizacionId, "una organización")

        if (listOf(errNombre, errComuna, errPublico, errFecha, errOrg).any { it != null }) {
            _uiState.update {
                it.copy(
                    errorNombre = errNombre, errorComuna = errComuna, errorPublico = errPublico,
                    errorFecha = errFecha, errorOrganizacion = errOrg
                )
            }
            return
        }

        viewModelScope.launch {
            repository.insertar(
                Evento(
                    id = 0,
                    nombre = s.nombre.trim(),
                    tipo = s.tipo,
                    descripcion = "",
                    fechaInicio = s.fecha!!,
                    fechaTermino = s.fecha,
                    horario = "Por definir",
                    direccion = s.direccion.trim(),
                    comuna = s.comuna.trim(),
                    region = "Región Metropolitana",
                    organizacionId = s.organizacionId!!,
                    publicoEsperado = s.publicoEsperado.toInt(),
                    publicoObjetivo = "General",
                    condiciones = "Por definir",
                    costoReferencial = 0,
                    observaciones = "",
                    fotoUri = s.fotoRuta
                )
            )
            _uiState.update { it.copy(guardado = true) }
        }
    }
}