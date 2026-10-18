package com.example.appproyecto.data.local

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.appproyecto.data.model.Contacto
import com.example.appproyecto.data.model.Evento
import com.example.appproyecto.data.model.Evidencia
import com.example.appproyecto.data.model.Oportunidad
import com.example.appproyecto.data.model.Organizacion
import com.example.appproyecto.data.model.Seguimiento
import com.example.appproyecto.data.model.Usuario

@Database(
    entities = [
        Usuario::class,
        Organizacion::class,
        Contacto::class,
        Evento::class,
        Oportunidad::class,
        Seguimiento::class,
        Evidencia::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun organizacionDao(): OrganizacionDao
    abstract fun contactoDao(): ContactoDao
    abstract fun eventoDao(): EventoDao
    abstract fun oportunidadDao(): OportunidadDao
    abstract fun seguimientoDao(): SeguimientoDao
    abstract fun evidenciaDao(): EvidenciaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder<AppDatabase>(
                    context.applicationContext,
                    "oportunidades.db"
                )
                    .setDriver(BundledSQLiteDriver())
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
