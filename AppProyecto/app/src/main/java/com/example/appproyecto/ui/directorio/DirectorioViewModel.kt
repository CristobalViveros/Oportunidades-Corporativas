package com.example.appproyecto.ui.directorio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appproyecto.data.local.AppDatabase
import com.example.appproyecto.data.model.Organizacion
import com.example.appproyecto.data.repository.ContactoRepository
import com.example.appproyecto.data.repository.OrganizacionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class OrganizacionItem(val organizacion: Organizacion, val contactos: Int)

data class DirectorioUiState(
    val busqueda: String = "",
    val organizaciones: List<OrganizacionItem> = emptyList(),
    val cargando: Boolean = true
)

class DirectorioViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val organizaciones = OrganizacionRepository(db.organizacionDao())
    private val contactos = ContactoRepository(db.contactoDao(), db.organizacionDao())

    private val busqueda = MutableStateFlow("")

    val uiState: StateFlow<DirectorioUiState> = combine(
        organizaciones.observarTodas(), contactos.observarTodos(), busqueda
    ) { orgs, todosContactos, texto ->
        val porOrg = todosContactos.groupingBy { it.organizacionId }.eachCount()
        val q = texto.trim().lowercase()
        DirectorioUiState(
            busqueda = texto,
            organizaciones = orgs
                .filter { o ->
                    q.isEmpty() || listOf(o.nombre, o.rubro, o.comuna).any { it.lowercase().contains(q) }
                }
                .map { OrganizacionItem(it, porOrg[it.id] ?: 0) },
            cargando = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DirectorioUiState())

    fun buscar(texto: String) = busqueda.update { texto }
}
