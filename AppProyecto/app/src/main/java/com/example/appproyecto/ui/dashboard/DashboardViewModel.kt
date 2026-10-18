package com.example.appproyecto.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appproyecto.data.local.AppDatabase
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.data.repository.OportunidadRepository
import com.example.appproyecto.data.repository.UsuarioRepository
import com.example.appproyecto.util.ResumenDashboard
import com.example.appproyecto.util.SesionManager
import com.example.appproyecto.util.calcularDashboard
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

data class DashboardUiState(
    val usuario: Usuario? = null,
    val resumen: ResumenDashboard = ResumenDashboard(),
    val cargando: Boolean = true
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository =
        OportunidadRepository(db.oportunidadDao(), db.seguimientoDao(), db.usuarioDao())
    private val usuarioRepository = UsuarioRepository(db.usuarioDao())
    private val sesion = SesionManager(application)

    private val usuario = MutableStateFlow<Usuario?>(null)

    init {
        viewModelScope.launch {
            usuario.value = sesion.usuarioId()?.let { usuarioRepository.obtenerPorId(it) }
        }
    }

    // El repositorio decide qué ve cada rol: el supervisor todo, el ejecutivo solo lo suyo.
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DashboardUiState> = usuario.filterNotNull().flatMapLatest { u ->
        combine(
            repository.observarItemsPara(u),
            repository.observarPendientesPara(u)
        ) { items, pendientes ->
            DashboardUiState(
                usuario = u,
                resumen = calcularDashboard(u.rol(), items, pendientes),
                cargando = false
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())
}
