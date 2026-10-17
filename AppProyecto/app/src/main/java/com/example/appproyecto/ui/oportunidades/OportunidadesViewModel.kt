package com.example.appproyecto.ui.oportunidades

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appproyecto.data.local.AppDatabase
import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.OportunidadItem
import com.example.appproyecto.data.model.Prioridad
import com.example.appproyecto.data.repository.OportunidadRepository
import com.example.appproyecto.util.filtrarOportunidades
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class OportunidadesUiState(
    val busqueda: String = "",
    val estadoFiltro: EstadoOportunidad? = null,
    val prioridadFiltro: Prioridad? = null,
    val oportunidades: List<OportunidadItem> = emptyList(),
    val cargando: Boolean = true
)

class OportunidadesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository =
        OportunidadRepository(AppDatabase.getInstance(application).oportunidadDao())

    private val busqueda = MutableStateFlow("")
    private val estadoFiltro = MutableStateFlow<EstadoOportunidad?>(null)
    private val prioridadFiltro = MutableStateFlow<Prioridad?>(null)

    // Se recalcula sola cuando cambian los datos de la base O cualquier filtro.
    val uiState: StateFlow<OportunidadesUiState> = combine(
        repository.observarItems(), busqueda, estadoFiltro, prioridadFiltro
    ) { items, texto, estado, prioridad ->
        OportunidadesUiState(
            busqueda = texto,
            estadoFiltro = estado,
            prioridadFiltro = prioridad,
            oportunidades = filtrarOportunidades(items, texto, estado, prioridad),
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