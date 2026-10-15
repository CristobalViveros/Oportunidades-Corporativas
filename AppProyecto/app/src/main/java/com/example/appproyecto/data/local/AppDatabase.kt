package com.example.appproyecto.data.local

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.appproyecto.data.model.Usuario

@Database(
    entities = [Usuario::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao

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