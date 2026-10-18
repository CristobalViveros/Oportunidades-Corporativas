package com.example.appproyecto.ui.oportunidades

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appproyecto.data.local.AppDatabase
import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.OportunidadItem
import com.example.appproyecto.data.model.Perfil
import com.example.appproyecto.data.model.Prioridad
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.data.repository.OportunidadRepository
import com.example.appproyecto.data.repository.UsuarioRepository
import com.example.appproyecto.util.SesionManager
import com.example.appproyecto.util.filtrarOportunidades
import com.example.appproyecto.util.rol
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OportunidadesUiState(
    val perfil: Perfil = Perfil.EJECUTIVO,
    val busqueda: String = "",
    val estadoFiltro: EstadoOportunidad? = null,
    val prioridadFiltro: Prioridad? = null,
    val oportunidades: List<OportunidadItem> = emptyList(),
    val cargando: Boolean = true
)

class OportunidadesViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository =
        OportunidadRepository(db.oportunidadDao(), db.seguimientoDao(), db.usuarioDao())
    private val usuarioRepository = UsuarioRepository(db.usuarioDao())
    private val sesion = SesionManager(application)

    /** Usuario que inició sesión; de su rol depende qué oportunidades se cargan. */
    private val usuario = MutableStateFlow<Usuario?>(null)

    private val busqueda = MutableStateFlow("")
    private val estadoFiltro = MutableStateFlow<EstadoOportunidad?>(null)
    private val prioridadFiltro = MutableStateFlow<Prioridad?>(null)

    init {
        viewModelScope.launch {
            usuario.value = sesion.usuarioId()?.let { usuarioRepository.obtenerPorId(it) }
        }
    }

    // Supervisor: todas. Ejecutivo: solo las suyas (lo decide el repositorio).
    @OptIn(ExperimentalCoroutinesApi::class)
    private val items = usuario.filterNotNull().flatMapLatest { repository.observarItemsPara(it) }

    // Se recalcula sola cuando cambian los datos de la base, el usuario O cualquier filtro.
    val uiState: StateFlow<OportunidadesUiState> = combine(
        items, usuario, busqueda, estadoFiltro, prioridadFiltro
    ) { lista, u, texto, estado, prioridad ->
        OportunidadesUiState(
            perfil = u?.rol() ?: Perfil.EJECUTIVO,
            busqueda = texto,
            estadoFiltro = estado,
            prioridadFiltro = prioridad,
            oportunidades = filtrarOportunidades(lista, texto, estado, prioridad),
            cargando = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OportunidadesUiState())

    fun cambiarBusqueda(texto: String) { busqueda.value = texto }

    /** Si tocas el filtro ya activo, se desactiva. */
    fun elegirEstado(estado: EstadoOportunidad) {
        estadoFiltro.value = if (estadoFiltro.value == estado) null else estado
    }

    fun elegirPrioridad(prioridad: Prioridad) {
        prioridadFiltro.value = if (prioridadFiltro.value == prioridad) null else prioridad
    }
}
