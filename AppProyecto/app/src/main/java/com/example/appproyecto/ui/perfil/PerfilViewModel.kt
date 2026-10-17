package com.example.appproyecto.ui.perfil

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

class PerfilViewModel(application: Application) : AndroidViewModel(application) {

    private val repository =
        UsuarioRepository(AppDatabase.getInstance(application).usuarioDao())
    private val sesion = SesionManager(application)

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario.asStateFlow()

    init {
        viewModelScope.launch {
            _usuario.value = sesion.usuarioId()?.let { repository.obtenerPorId(it) }
        }
    }

    fun cerrarSesion() = sesion.cerrar()
}