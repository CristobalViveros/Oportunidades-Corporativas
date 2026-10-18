package com.example.appproyecto.ui.detalle

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.appproyecto.data.local.AppDatabase
import com.example.appproyecto.data.model.EvidenciaItem
import com.example.appproyecto.data.model.Resultado
import com.example.appproyecto.data.model.Usuario
import com.example.appproyecto.data.repository.EvidenciaRepository
import com.example.appproyecto.data.repository.UsuarioRepository
import com.example.appproyecto.util.SesionManager
import com.example.appproyecto.util.validarTextoOpcional
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class EvidenciasUiState(
    val evidencias: List<EvidenciaItem> = emptyList(),
    /** Foto recién tomada que aún no se guarda: se muestra una vista previa para confirmar. */
    val fotoPendiente: String? = null,
    val descripcion: String = "",
    val errorDescripcion: String? = null,
    val guardando: Boolean = false,
    val resultado: Resultado? = null
)

private class Borrador(
    val fotoPendiente: String? = null,
    val descripcion: String = "",
    val errorDescripcion: String? = null,
    val guardando: Boolean = false,
    val resultado: Resultado? = null
)

class EvidenciasViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = EvidenciaRepository(db.evidenciaDao(), db.oportunidadDao())
    private val usuarioRepository = UsuarioRepository(db.usuarioDao())
    private val sesion = SesionManager(application)

    private val oportunidadId: Long = savedStateHandle.get<Long>("id") ?: 0L

    private val actor = MutableStateFlow<Usuario?>(null)
    private val borrador = MutableStateFlow(Borrador())

    init {
        viewModelScope.launch {
            actor.value = sesion.usuarioId()?.let { usuarioRepository.obtenerPorId(it) }?.takeIf { it.activo }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val evidencias = actor.flatMapLatest { a ->
        if (a == null) flowOf(emptyList()) else repository.observarPara(a, oportunidadId)
    }

    val uiState: StateFlow<EvidenciasUiState> = combine(evidencias, borrador) { lista, b ->
        EvidenciasUiState(
            evidencias = lista,
            fotoPendiente = b.fotoPendiente,
            descripcion = b.descripcion,
            errorDescripcion = b.errorDescripcion,
            guardando = b.guardando,
            resultado = b.resultado
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EvidenciasUiState())

    private fun cambiar(transformar: (Borrador) -> Borrador) = borrador.update(transformar)

    /** Llega una foto nueva (cámara o galería): queda pendiente hasta que el usuario la confirme. */
    fun prepararFoto(ruta: String) {
        borrador.value.fotoPendiente?.let { File(it).delete() } // si había otra sin guardar, se descarta
        borrador.value = Borrador(fotoPendiente = ruta)
    }

    fun cambiarDescripcion(texto: String) = cambiar {
        Borrador(it.fotoPendiente, texto, null, it.guardando, it.resultado)
    }

    /** El usuario se arrepiente: se borra el archivo para no dejar fotos huérfanas. */
    fun cancelarFoto() {
        borrador.value.fotoPendiente?.let { File(it).delete() }
        borrador.value = Borrador()
    }

    fun guardar() {
        val b = borrador.value
        val usuario = actor.value
        val ruta = b.fotoPendiente
        if (b.guardando || ruta == null) return
        if (usuario == null) {
            cambiar { Borrador(it.fotoPendiente, it.descripcion, null, false, Resultado.Error("No hay una sesión activa")) }
            return
        }
        val error = validarTextoOpcional(b.descripcion, "La descripción", EvidenciaRepository.DESCRIPCION_MAXIMA)
        if (error != null) {
            // La foto y lo escrito se conservan; solo aparece el mensaje.
            cambiar { Borrador(it.fotoPendiente, it.descripcion, error, false, it.resultado) }
            return
        }
        viewModelScope.launch {
            cambiar { Borrador(it.fotoPendiente, it.descripcion, null, true, null) }
            val resultado = repository.agregar(usuario, oportunidadId, ruta, b.descripcion)
            when (resultado) {
                is Resultado.Exito -> borrador.value = Borrador(resultado = resultado)
                is Resultado.Error -> cambiar { Borrador(it.fotoPendiente, it.descripcion, null, false, resultado) }
            }
        }
    }

    fun consumirResultado() = cambiar {
        Borrador(it.fotoPendiente, it.descripcion, it.errorDescripcion, it.guardando, null)
    }
}
