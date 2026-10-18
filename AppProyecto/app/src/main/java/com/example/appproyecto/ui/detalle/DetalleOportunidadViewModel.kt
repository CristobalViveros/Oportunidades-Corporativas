package com.example.appproyecto.ui.detalle

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.appproyecto.data.local.AppDatabase
import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.OportunidadDetalle
import com.example.appproyecto.data.model.Perfil
import com.example.appproyecto.data.model.Resultado
import com.example.appproyecto.data.model.SeguimientoItem
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.data.model.aEnum
import com.example.appproyecto.data.repository.OportunidadRepository
import com.example.appproyecto.data.repository.UsuarioRepository
import com.example.appproyecto.util.Permisos
import com.example.appproyecto.util.SesionManager
import com.example.appproyecto.util.proximoSeguimiento
import com.example.appproyecto.util.rol
import com.example.appproyecto.util.validarTextoOpcional
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetalleUiState(
    val cargando: Boolean = true,
    /** La oportunidad no existe o el usuario no tiene permiso para verla. */
    val sinAcceso: Boolean = false,
    val rol: Perfil = Perfil.EJECUTIVO,
    val detalle: OportunidadDetalle? = null,
    val historial: List<SeguimientoItem> = emptyList(),
    val proximo: SeguimientoItem? = null,
    /** Estados a los que este usuario puede mover la oportunidad desde el estado actual. */
    val estadosDestino: List<EstadoOportunidad> = emptyList(),
    val puedeRegistrarGestion: Boolean = false,
    val dialogoEstado: Boolean = false,
    val resultado: Resultado? = null
)

private class Datos(val rol: Perfil, val detalle: OportunidadDetalle?, val historial: List<SeguimientoItem>)

class DetalleOportunidadViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository =
        OportunidadRepository(db.oportunidadDao(), db.seguimientoDao(), db.usuarioDao())
    private val usuarioRepository = UsuarioRepository(db.usuarioDao())
    private val sesion = SesionManager(application)

    private val oportunidadId: Long = savedStateHandle.get<Long>("id") ?: 0L

    private val actor = MutableStateFlow<Usuario?>(null)
    private val sesionInvalida = MutableStateFlow(false)
    private val dialogoEstado = MutableStateFlow(false)
    private val resultado = MutableStateFlow<Resultado?>(null)

    init {
        viewModelScope.launch {
            val usuario = sesion.usuarioId()?.let { usuarioRepository.obtenerPorId(it) }?.takeIf { it.activo }
            if (usuario == null) sesionInvalida.value = true else actor.value = usuario
        }
    }

    // Se actualiza sola: si cambia el estado o se registra una gestión, la pantalla se refresca.
    @OptIn(ExperimentalCoroutinesApi::class)
    private val datos = actor.flatMapLatest { a ->
        if (a == null) flowOf<Datos?>(null)
        else combine(
            repository.observarDetallePara(a, oportunidadId),
            repository.observarHistorialPara(a, oportunidadId)
        ) { detalle, historial -> Datos(a.rol(), detalle, historial) }
    }

    val uiState: StateFlow<DetalleUiState> = combine(
        datos, sesionInvalida, dialogoEstado, resultado
    ) { d, sinSesion, dialogo, ultimo ->
        when {
            sinSesion -> DetalleUiState(cargando = false, sinAcceso = true)
            d == null -> DetalleUiState(cargando = true)
            d.detalle == null -> DetalleUiState(cargando = false, sinAcceso = true, rol = d.rol)
            else -> {
                val estado = d.detalle.estado.aEnum(EstadoOportunidad.EVENTO_IDENTIFICADO)
                DetalleUiState(
                    cargando = false,
                    rol = d.rol,
                    detalle = d.detalle,
                    historial = d.historial,
                    proximo = proximoSeguimiento(d.historial),
                    estadosDestino = Permisos.estadosDestino(d.rol, estado),
                    puedeRegistrarGestion = !Permisos.esFinal(estado),
                    dialogoEstado = dialogo,
                    resultado = ultimo
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DetalleUiState())

    fun abrirDialogoEstado() { dialogoEstado.value = true }
    fun cerrarDialogoEstado() { dialogoEstado.value = false }
    fun consumirResultado() { resultado.value = null }

    fun cambiarEstado(nuevo: EstadoOportunidad, observacion: String) {
        val usuario = actor.value ?: return
        val error = validarTextoOpcional(observacion, "La observación")
        if (error != null) {
            resultado.value = Resultado.Error(error)
            return
        }
        viewModelScope.launch {
            // El repositorio vuelve a validar permisos y transiciones y deja constancia en el historial.
            resultado.value = repository.cambiarEstado(usuario, oportunidadId, nuevo, observacion.trim())
            dialogoEstado.value = false
        }
    }
}
