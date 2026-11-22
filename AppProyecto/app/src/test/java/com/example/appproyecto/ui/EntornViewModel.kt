package com.example.appproyecto.ui

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.example.appproyecto.data.local.AppDatabase
import com.example.appproyecto.data.local.ContactoDao
import com.example.appproyecto.data.local.EventoDao
import com.example.appproyecto.data.local.EvidenciaDao
import com.example.appproyecto.data.local.OportunidadDao
import com.example.appproyecto.data.local.OrganizacionDao
import com.example.appproyecto.data.local.SeguimientoDao
import com.example.appproyecto.data.local.UsuarioDao
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/**
 * Entorno falso para probar ViewModels sin emulador: una base de datos cuyos DAOs son mocks,
 * una Application con SharedPreferences simuladas y el dispatcher Main reemplazado.
 * Se usa con @Before (iniciar) y @After (terminar).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EntornoViewModel {

    val application = mockk<Application>(relaxed = true)

    val usuarioDao = mockk<UsuarioDao>()
    val oportunidadDao = mockk<OportunidadDao>()
    val seguimientoDao = mockk<SeguimientoDao>()
    val eventoDao = mockk<EventoDao>()
    val organizacionDao = mockk<OrganizacionDao>()
    val contactoDao = mockk<ContactoDao>()
    val evidenciaDao = mockk<EvidenciaDao>()

    /** Permite verificar qué se guardó o borró en la sesión. */
    val editor = mockk<SharedPreferences.Editor>(relaxed = true)

    private val db = mockk<AppDatabase>()
    private val preferencias = mockk<SharedPreferences>(relaxed = true)

    /** [usuarioIdEnSesion] = null simula que no hay sesión iniciada. */
    fun iniciar(usuarioIdEnSesion: Long? = 1L) {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        mockkObject(AppDatabase.Companion)
        every { AppDatabase.getInstance(any()) } returns db
        every { db.usuarioDao() } returns usuarioDao
        every { db.oportunidadDao() } returns oportunidadDao
        every { db.seguimientoDao() } returns seguimientoDao
        every { db.eventoDao() } returns eventoDao
        every { db.organizacionDao() } returns organizacionDao
        every { db.contactoDao() } returns contactoDao
        every { db.evidenciaDao() } returns evidenciaDao

        every { application.applicationContext } returns application
        every { application.getSharedPreferences("sesion", Context.MODE_PRIVATE) } returns preferencias
        every { preferencias.getLong("usuario_id", -1L) } returns (usuarioIdEnSesion ?: -1L)
        every { preferencias.edit() } returns editor
        every { editor.putLong(any(), any()) } returns editor
        every { editor.remove(any()) } returns editor
    }

    fun terminar() {
        Dispatchers.resetMain()
        unmockkAll()
    }
}