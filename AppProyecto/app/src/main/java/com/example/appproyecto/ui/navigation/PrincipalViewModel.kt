package com.example.appproyecto.ui.navigation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appproyecto.data.local.AppDatabase
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.data.repository.UsuarioRepository
import com.example.appproyecto.util.SesionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PrincipalUiState(
    val cargando: Boolean = true,
    val usuario: Usuario? = null,
    /** La sesión guardada ya no corresponde a un usuario activo: hay que volver al login. */
    val sesionInvalida: Boolean = false
)

/** Carga al usuario de la sesión para saber su rol y armar la navegación. */
class PrincipalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository =
        UsuarioRepository(AppDatabase.getInstance(application).usuarioDao())
    private val sesion = SesionManager(application)

    private val _uiState = MutableStateFlow(PrincipalUiState())
    val uiState: StateFlow<PrincipalUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val usuario = sesion.usuarioId()
                ?.let { repository.obtenerPorId(it) }
                ?.takeIf { it.activo }
            if (usuario == null) {
                sesion.cerrar()
                _uiState.value = PrincipalUiState(cargando = false, sesionInvalida = true)
            } else {
                _uiState.value = PrincipalUiState(cargando = false, usuario = usuario)
            }
        }
    }
}
