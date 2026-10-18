package com.example.appproyecto.ui.directorio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.appproyecto.data.local.AppDatabase
import com.example.appproyecto.data.model.Contacto
import com.example.appproyecto.data.model.Organizacion
import com.example.appproyecto.data.repository.ContactoRepository
import com.example.appproyecto.data.repository.OrganizacionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class OrganizacionDetalleUiState(
    val cargando: Boolean = true,
    val organizacion: Organizacion? = null,
    val contactos: List<Contacto> = emptyList()
)

class OrganizacionDetalleViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val organizaciones = OrganizacionRepository(db.organizacionDao())
    private val contactos = ContactoRepository(db.contactoDao(), db.organizacionDao())

    val organizacionId: Long = savedStateHandle["id"] ?: 0L

    val uiState: StateFlow<OrganizacionDetalleUiState> = combine(
        organizaciones.observarPorId(organizacionId),
        contactos.observarPorOrganizacion(organizacionId)
    ) { org, lista ->
        OrganizacionDetalleUiState(cargando = false, organizacion = org, contactos = lista)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OrganizacionDetalleUiState())
}
