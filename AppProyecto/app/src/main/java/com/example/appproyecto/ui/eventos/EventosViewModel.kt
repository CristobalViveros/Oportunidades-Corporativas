package com.example.appproyecto.ui.eventos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appproyecto.data.local.AppDatabase
import com.example.appproyecto.data.model.Evento
import com.example.appproyecto.data.repository.EventoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class EventosViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = EventoRepository(AppDatabase.getInstance(application).eventoDao())

    val eventos: StateFlow<List<Evento>> = repository.observarTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}