package com.example.appproyecto.`data`.local

import androidx.room3.EntityInsertAdapter
import androidx.room3.RoomDatabase
import androidx.room3.util.getColumnIndexOrThrow
import androidx.room3.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.example.appproyecto.`data`.model.Usuario
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.reflect.KClass

@Generated(value = ["androidx.room3.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL", "MemberExtensionConflict"])
internal class UsuarioDao_Impl(
  __db: RoomDatabase,
) : UsuarioDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfUsuario: EntityInsertAdapter<Usuario>
  init {
    this.__db = __db
    this.__insertAdapterOfUsuario = object : EntityInsertAdapter<Usuario>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `usuarios` (`id`,`nombre`,`correo`,`passwordHash`,`perfil`,`area`,`zona`,`activo`,`fechaCreacion`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Usuario) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.nombre)
        statement.bindText(3, entity.correo)
        statement.bindText(4, entity.passwordHash)
        statement.bindText(5, entity.perfil)
        statement.bindText(6, entity.area)
        statement.bindText(7, entity.zona)
        val _tmp: Int = if (entity.activo) 1 else 0
        statement.bindLong(8, _tmp.toLong())
        statement.bindLong(9, entity.fechaCreacion)
      }
    }
  }

  public override suspend fun insertar(usuario: Usuario): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfUsuario.insertAndReturnId(_connection, usuario)
    _result
  }

  public override suspend fun obtenerPorCorreo(correo: String): Usuario? {
    val _sql: String = "SELECT * FROM usuarios WHERE correo = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, correo)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfNombre: Int = getColumnIndexOrThrow(_stmt, "nombre")
        val _columnIndexOfCorreo: Int = getColumnIndexOrThrow(_stmt, "correo")
        val _columnIndexOfPasswordHash: Int = getColumnIndexOrThrow(_stmt, "passwordHash")
        val _columnIndexOfPerfil: Int = getColumnIndexOrThrow(_stmt, "perfil")
        val _columnIndexOfArea: Int = getColumnIndexOrThrow(_stmt, "area")
        val _columnIndexOfZona: Int = getColumnIndexOrThrow(_stmt, "zona")
        val _columnIndexOfActivo: Int = getColumnIndexOrThrow(_stmt, "activo")
        val _columnIndexOfFechaCreacion: Int = getColumnIndexOrThrow(_stmt, "fechaCreacion")
        val _result: Usuario?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpNombre: String
          _tmpNombre = _stmt.getText(_columnIndexOfNombre)
          val _tmpCorreo: String
          _tmpCorreo = _stmt.getText(_columnIndexOfCorreo)
          val _tmpPasswordHash: String
          _tmpPasswordHash = _stmt.getText(_columnIndexOfPasswordHash)
          val _tmpPerfil: String
          _tmpPerfil = _stmt.getText(_columnIndexOfPerfil)
          val _tmpArea: String
          _tmpArea = _stmt.getText(_columnIndexOfArea)
          val _tmpZona: String
          _tmpZona = _stmt.getText(_columnIndexOfZona)
          val _tmpActivo: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfActivo).toInt()
          _tmpActivo = _tmp != 0
          val _tmpFechaCreacion: Long
          _tmpFechaCreacion = _stmt.getLong(_columnIndexOfFechaCreacion)
          _result = Usuario(_tmpId,_tmpNombre,_tmpCorreo,_tmpPasswordHash,_tmpPerfil,_tmpArea,_tmpZona,_tmpActivo,_tmpFechaCreacion)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun obtenerPorId(id: Long): Usuario? {
    val _sql: String = "SELECT * FROM usuarios WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfNombre: Int = getColumnIndexOrThrow(_stmt, "nombre")
        val _columnIndexOfCorreo: Int = getColumnIndexOrThrow(_stmt, "correo")
        val _columnIndexOfPasswordHash: Int = getColumnIndexOrThrow(_stmt, "passwordHash")
        val _columnIndexOfPerfil: Int = getColumnIndexOrThrow(_stmt, "perfil")
        val _columnIndexOfArea: Int = getColumnIndexOrThrow(_stmt, "area")
        val _columnIndexOfZona: Int = getColumnIndexOrThrow(_stmt, "zona")
        val _columnIndexOfActivo: Int = getColumnIndexOrThrow(_stmt, "activo")
        val _columnIndexOfFechaCreacion: Int = getColumnIndexOrThrow(_stmt, "fechaCreacion")
        val _result: Usuario?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpNombre: String
          _tmpNombre = _stmt.getText(_columnIndexOfNombre)
          val _tmpCorreo: String
          _tmpCorreo = _stmt.getText(_columnIndexOfCorreo)
          val _tmpPasswordHash: String
          _tmpPasswordHash = _stmt.getText(_columnIndexOfPasswordHash)
          val _tmpPerfil: String
          _tmpPerfil = _stmt.getText(_columnIndexOfPerfil)
          val _tmpArea: String
          _tmpArea = _stmt.getText(_columnIndexOfArea)
          val _tmpZona: String
          _tmpZona = _stmt.getText(_columnIndexOfZona)
          val _tmpActivo: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfActivo).toInt()
          _tmpActivo = _tmp != 0
          val _tmpFechaCreacion: Long
          _tmpFechaCreacion = _stmt.getLong(_columnIndexOfFechaCreacion)
          _result = Usuario(_tmpId,_tmpNombre,_tmpCorreo,_tmpPasswordHash,_tmpPerfil,_tmpArea,_tmpZona,_tmpActivo,_tmpFechaCreacion)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun contar(): Int {
    val _sql: String = "SELECT COUNT(*) FROM usuarios"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredColumnConverters(): List<KClass<*>> = emptyList()

    public fun getRequiredDaoReturnTypeConverters(): List<KClass<*>> = emptyList()
  }
}
