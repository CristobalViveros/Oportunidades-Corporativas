package com.example.appproyecto.ui.directorio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.appproyecto.data.local.AppDatabase
import com.example.appproyecto.data.model.Organizacion
import com.example.appproyecto.data.model.Resultado
import com.example.appproyecto.data.model.TipoOrganizacion
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.data.repository.OrganizacionRepository
import com.example.appproyecto.data.repository.UsuarioRepository
import com.example.appproyecto.util.SesionManager
import com.example.appproyecto.util.validarSitioWebOpcional
import com.example.appproyecto.util.validarTextoObligatorio
import com.example.appproyecto.util.validarTextoOpcional
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrganizacionFormUiState(
    val nombre: String = "",
    val tipo: String = TipoOrganizacion.EMPRESA.name,
    val rubro: String = "",
    val region: String = "",
    val comuna: String = "",
    val sitioWeb: String = "",
    val publicoAsociado: String = "",
    val observaciones: String = "",
    val esEdicion: Boolean = false,
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val errorNombre: String? = null,
    val errorRubro: String? = null,
    val errorRegion: String? = null,
    val errorComuna: String? = null,
    val errorSitioWeb: String? = null,
    val errorPublico: String? = null,
    val errorObservaciones: String? = null,
    val errorGeneral: String? = null,
    val guardado: Boolean = false
)

class OrganizacionFormViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = OrganizacionRepository(db.organizacionDao())
    private val usuarioRepository = UsuarioRepository(db.usuarioDao())
    private val sesion = SesionManager(application)
    private var actor: Usuario? = null

    /** 0 = organización nueva. */
    private val organizacionId: Long = savedStateHandle["id"] ?: 0L

    private val _uiState = MutableStateFlow(
        OrganizacionFormUiState(esEdicion = organizacionId != 0L, cargando = organizacionId != 0L)
    )
    val uiState: StateFlow<OrganizacionFormUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            actor = sesion.usuarioId()?.let { usuarioRepository.obtenerPorId(it) }?.takeIf { it.activo }
            if (organizacionId != 0L) {
                val o = repository.obtenerPorId(organizacionId)
                if (o == null) {
                    _uiState.update { it.copy(cargando = false, errorGeneral = "La organización no existe") }
                } else {
                    _uiState.update {
                        it.copy(
                            nombre = o.nombre, tipo = o.tipo, rubro = o.rubro, region = o.region,
                            comuna = o.comuna, sitioWeb = o.sitioWeb, publicoAsociado = o.publicoAsociado,
                            observaciones = o.observaciones, cargando = false
                        )
                    }
                }
            }
        }
    }

    // Cada cambio limpia el error de ese campo; los valores ingresados nunca se pierden.
    fun cambiarNombre(v: String) = _uiState.update { it.copy(nombre = v, errorNombre = null, errorGeneral = null) }
    fun cambiarTipo(v: String) = _uiState.update { it.copy(tipo = v) }
    fun cambiarRubro(v: String) = _uiState.update { it.copy(rubro = v, errorRubro = null) }
    fun cambiarRegion(v: String) = _uiState.update { it.copy(region = v, errorRegion = null) }
    fun cambiarComuna(v: String) = _uiState.update { it.copy(comuna = v, errorComuna = null) }
    fun cambiarSitioWeb(v: String) = _uiState.update { it.copy(sitioWeb = v, errorSitioWeb = null) }
    fun cambiarPublico(v: String) = _uiState.update { it.copy(publicoAsociado = v, errorPublico = null) }
    fun cambiarObservaciones(v: String) = _uiState.update { it.copy(observaciones = v, errorObservaciones = null) }

    fun guardar() {
        val s = _uiState.value
        val usuario = actor
        if (s.guardando) return
        if (usuario == null) {
            _uiState.update { it.copy(errorGeneral = "No hay una sesión activa") }
            return
        }

        val errNombre = validarTextoObligatorio(s.nombre, "El nombre", 3, 80)
        val errRubro = validarTextoObligatorio(s.rubro, "El rubro", 3, 60)
        val errRegion = validarTextoObligatorio(s.region, "La región", 3, 40)
        val errComuna = validarTextoObligatorio(s.comuna, "La comuna", 3, 40)
        val errWeb = validarSitioWebOpcional(s.sitioWeb)
        val errPublico = validarTextoOpcional(s.publicoAsociado, "El público asociado", 100)
        val errObs = validarTextoOpcional(s.observaciones, "Las observaciones", 300)

        if (listOf(errNombre, errRubro, errRegion, errComuna, errWeb, errPublico, errObs).any { it != null }) {
            _uiState.update {
                it.copy(
                    errorNombre = errNombre, errorRubro = errRubro, errorRegion = errRegion,
                    errorComuna = errComuna, errorSitioWeb = errWeb, errorPublico = errPublico,
                    errorObservaciones = errObs
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, errorGeneral = null) }
            val organizacion = Organizacion(
                id = organizacionId,
                nombre = s.nombre.trim(),
                tipo = s.tipo,
                rubro = s.rubro.trim(),
                comuna = s.comuna.trim(),
                region = s.region.trim(),
                sitioWeb = s.sitioWeb.trim(),
                publicoAsociado = s.publicoAsociado.trim(),
                observaciones = s.observaciones.trim()
            )
            val resultado = if (s.esEdicion) repository.actualizar(usuario, organizacion)
            else repository.crear(usuario, organizacion)
            when (resultado) {
                is Resultado.Exito -> _uiState.update { it.copy(guardando = false, guardado = true) }
                is Resultado.Error -> _uiState.update { it.copy(guardando = false, errorGeneral = resultado.mensaje) }
            }
        }
    }
}
