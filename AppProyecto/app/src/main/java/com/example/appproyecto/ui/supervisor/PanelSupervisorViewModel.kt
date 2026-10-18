package com.example.appproyecto.ui.supervisor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appproyecto.data.local.AppDatabase
import com.example.appproyecto.data.model.OportunidadItem
import com.example.appproyecto.data.model.Resultado
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.data.repository.OportunidadRepository
import com.example.appproyecto.data.repository.UsuarioRepository
import com.example.appproyecto.util.SesionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PanelUiState(
    val oportunidades: List<OportunidadItem> = emptyList(),
    val ejecutivos: List<Usuario> = emptyList(),
    val cargando: Boolean = true,
    /** Oportunidad que se está reasignando (si hay un diálogo abierto). */
    val reasignando: OportunidadItem? = null,
    /** Resultado de la última operación, para mostrarlo en un Snackbar. */
    val resultado: Resultado? = null
)

class PanelSupervisorViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository =
        OportunidadRepository(db.oportunidadDao(), db.seguimientoDao(), db.usuarioDao())
    private val usuarioRepository = UsuarioRepository(db.usuarioDao())
    private val sesion = SesionManager(application)

    private val actor = MutableStateFlow<Usuario?>(null)
    private val reasignando = MutableStateFlow<OportunidadItem?>(null)
    private val resultado = MutableStateFlow<Resultado?>(null)

    init {
        viewModelScope.launch {
            actor.value = sesion.usuarioId()?.let { usuarioRepository.obtenerPorId(it) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val oportunidades =
        actor.filterNotNull().flatMapLatest { repository.observarItemsPara(it) }

    val uiState: StateFlow<PanelUiState> = combine(
        oportunidades, usuarioRepository.observarEjecutivos(), reasignando, resultado
    ) { lista, ejecutivos, dialogo, ultimoResultado ->
        PanelUiState(
            oportunidades = lista,
            ejecutivos = ejecutivos,
            cargando = false,
            reasignando = dialogo,
            resultado = ultimoResultado
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PanelUiState())

    fun abrirReasignar(item: OportunidadItem) { reasignando.value = item }

    fun cerrarReasignar() { reasignando.value = null }

    fun confirmarReasignar(nuevoResponsableId: Long) {
        val item = reasignando.value ?: return
        viewModelScope.launch {
            val usuario = actor.value
            resultado.value = if (usuario == null) {
                Resultado.Error("No hay una sesión activa")
            } else {
                // El repositorio vuelve a validar que el usuario sea supervisor.
                repository.asignar(usuario, item.id, nuevoResponsableId)
            }
            reasignando.value = null
        }
    }

    fun consumirResultado() { resultado.value = null }
}
