package com.example.appproyecto.ui.eventos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appproyecto.data.local.AppDatabase
import com.example.appproyecto.data.model.Evento
import com.example.appproyecto.data.model.Organizacion
import com.example.appproyecto.data.model.Resultado
import com.example.appproyecto.data.model.TipoOportunidad
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.data.repository.EventoRepository
import com.example.appproyecto.data.repository.UsuarioRepository
import com.example.appproyecto.util.SesionManager
import com.example.appproyecto.util.validarCosto
import com.example.appproyecto.util.validarEntero
import com.example.appproyecto.util.validarFechaEvento
import com.example.appproyecto.util.validarFechaTermino
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
    val tipo: String = TipoOportunidad.EVENTO_CORPORATIVO.name,
    val region: String = "Región Metropolitana",
    val comuna: String = "",
    val direccion: String = "",
    val publicoEsperado: String = "",
    val costo: String = "",
    val fecha: Long? = null,
    val fechaTermino: Long? = null,
    val organizacionId: Long? = null,
    val fotoRuta: String? = null,
    val organizaciones: List<Organizacion> = emptyList(),
    val guardando: Boolean = false,
    val errorNombre: String? = null,
    val errorRegion: String? = null,
    val errorComuna: String? = null,
    val errorDireccion: String? = null,
    val errorPublico: String? = null,
    val errorCosto: String? = null,
    val errorFecha: String? = null,
    val errorFechaTermino: String? = null,
    val errorOrganizacion: String? = null,
    val errorGeneral: String? = null,
    val guardado: Boolean = false
)

class EventoFormViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = EventoRepository(db.eventoDao())
    private val usuarioRepository = UsuarioRepository(db.usuarioDao())
    private val sesion = SesionManager(application)
    private var actor: Usuario? = null

    private val _uiState = MutableStateFlow(EventoFormUiState())
    val uiState: StateFlow<EventoFormUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            actor = sesion.usuarioId()?.let { usuarioRepository.obtenerPorId(it) }?.takeIf { it.activo }
            val organizaciones = db.organizacionDao().observarTodas().first()
            _uiState.update { it.copy(organizaciones = organizaciones) }
        }
    }

    // Cada cambio limpia el error de ese campo; los valores ingresados nunca se pierden.
    fun cambiarNombre(v: String) = _uiState.update { it.copy(nombre = v, errorNombre = null, errorGeneral = null) }
    fun cambiarTipo(v: String) = _uiState.update { it.copy(tipo = v) }
    fun cambiarRegion(v: String) = _uiState.update { it.copy(region = v, errorRegion = null) }
    fun cambiarComuna(v: String) = _uiState.update { it.copy(comuna = v, errorComuna = null) }
    fun cambiarDireccion(v: String) = _uiState.update { it.copy(direccion = v, errorDireccion = null) }
    fun cambiarPublico(v: String) =
        _uiState.update { it.copy(publicoEsperado = v.filter { c -> c.isDigit() }.take(6), errorPublico = null) }

    /** Se permite escribir "-" a propósito: así el usuario ve el mensaje "no puede ser negativo". */
    fun cambiarCosto(v: String) =
        _uiState.update { it.copy(costo = v.filter { c -> c.isDigit() || c == '-' }.take(10), errorCosto = null) }

    fun elegirOrganizacion(id: Long) = _uiState.update { it.copy(organizacionId = id, errorOrganizacion = null, errorGeneral = null) }

    // Al cambiar el inicio se revalida el término (y viceversa), por eso se limpian ambos errores.
    fun fijarFecha(millis: Long) =
        _uiState.update { it.copy(fecha = millis, errorFecha = null, errorFechaTermino = null, errorGeneral = null) }

    fun fijarFechaTermino(millis: Long) =
        _uiState.update { it.copy(fechaTermino = millis, errorFechaTermino = null) }

    fun quitarFechaTermino() = _uiState.update { it.copy(fechaTermino = null, errorFechaTermino = null) }

    fun fijarFoto(ruta: String) = _uiState.update { it.copy(fotoRuta = ruta) }

    fun guardar() {
        val s = _uiState.value
        val usuario = actor
        if (s.guardando) return
        if (usuario == null) {
            _uiState.update { it.copy(errorGeneral = "No hay una sesión activa") }
            return
        }

        val errNombre = validarTextoObligatorio(s.nombre, "El nombre", 5, 80)
        val errOrg = validarSeleccion(s.organizacionId, "una organización")
        val errFecha = validarFechaEvento(s.fecha)
        val errTermino = validarFechaTermino(s.fecha, s.fechaTermino)
        val errRegion = validarTextoObligatorio(s.region, "La región", 3, 40)
        val errComuna = validarTextoObligatorio(s.comuna, "La comuna", 3, 40)
        val errDireccion = validarTextoObligatorio(s.direccion, "La dirección", 5, 120)
        val errPublico = validarEntero(s.publicoEsperado, "El público esperado", 1, 100000)
        val errCosto = validarCosto(s.costo)

        val errores = listOf(errNombre, errOrg, errFecha, errTermino, errRegion, errComuna, errDireccion, errPublico, errCosto)
        if (errores.any { it != null }) {
            // Los valores ingresados se conservan: solo se agregan los mensajes de error.
            _uiState.update {
                it.copy(
                    errorNombre = errNombre, errorOrganizacion = errOrg, errorFecha = errFecha,
                    errorFechaTermino = errTermino, errorRegion = errRegion, errorComuna = errComuna,
                    errorDireccion = errDireccion, errorPublico = errPublico, errorCosto = errCosto
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, errorGeneral = null) }
            val inicio = s.fecha!!
            val resultado = repository.crear(
                usuario,
                Evento(
                    nombre = s.nombre.trim(),
                    tipo = s.tipo,
                    fechaInicio = inicio,
                    fechaTermino = s.fechaTermino ?: inicio,
                    horario = "Por definir",
                    direccion = s.direccion.trim(),
                    comuna = s.comuna.trim(),
                    region = s.region.trim(),
                    organizacionId = s.organizacionId!!,
                    publicoEsperado = s.publicoEsperado.toInt(),
                    publicoObjetivo = "General",
                    condiciones = "Por definir",
                    costoReferencial = s.costo.trim().ifBlank { "0" }.toInt(),
                    fotoUri = s.fotoRuta
                )
            )
            when (resultado) {
                is Resultado.Exito -> _uiState.update { it.copy(guardando = false, guardado = true) }
                is Resultado.Error -> _uiState.update { it.copy(guardando = false, errorGeneral = resultado.mensaje) }
            }
        }
    }
}
