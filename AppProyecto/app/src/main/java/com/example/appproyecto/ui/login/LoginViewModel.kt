package com.example.appproyecto.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appproyecto.data.local.AppDatabase
import com.example.appproyecto.data.repository.UsuarioRepository
import com.example.appproyecto.util.SesionManager
import com.example.appproyecto.util.validarCorreo
import com.example.appproyecto.util.validarPassword
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** "Fotografía" de lo que la pantalla de login muestra en cada momento. */
data class LoginUiState(
    val correo: String = "",
    val password: String = "",
    val errorCorreo: String? = null,
    val errorPassword: String? = null,
    val errorGeneral: String? = null,
    val cargando: Boolean = false,
    val loginExitoso: Boolean = false
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository =
        UsuarioRepository(AppDatabase.getInstance(application).usuarioDao())
    private val sesion = SesionManager(application)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun cambiarCorreo(valor: String) =
        _uiState.update { it.copy(correo = valor, errorCorreo = null, errorGeneral = null) }

    fun cambiarPassword(valor: String) =
        _uiState.update { it.copy(password = valor, errorPassword = null, errorGeneral = null) }

    fun iniciarSesion() {
        val estado = _uiState.value
        val errorCorreo = validarCorreo(estado.correo)
        val errorPassword = validarPassword(estado.password)

        if (errorCorreo != null || errorPassword != null) {
            _uiState.update { it.copy(errorCorreo = errorCorreo, errorPassword = errorPassword) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            val usuario = repository.autenticar(estado.correo, estado.password)
            if (usuario == null) {
                _uiState.update {
                    it.copy(cargando = false, errorGeneral = "Correo o contraseña incorrectos")
                }
            } else {
                sesion.guardar(usuario.id)
                _uiState.update { it.copy(cargando = false, loginExitoso = true) }
            }
        }
    }
}