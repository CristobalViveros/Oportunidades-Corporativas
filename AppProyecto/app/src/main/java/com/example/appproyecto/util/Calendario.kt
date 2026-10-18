package com.example.appproyecto.util

import android.content.Intent
import android.provider.CalendarContract
import java.util.Calendar
import java.util.TimeZone

/**
 * Crea las intenciones para guardar eventos en el calendario del dispositivo.
 *
 * Se usa ACTION_INSERT: abre la app de calendario con el evento ya rellenado y es el usuario quien
 * lo confirma. Así la app no necesita permisos de calendario (READ/WRITE_CALENDAR) y el usuario
 * siempre puede cancelar.
 */
object Calendario {

    /** Hora a la que se agenda un recordatorio de seguimiento (el modelo solo guarda la fecha). */
    const val HORA_SEGUIMIENTO = 9
    const val DURACION_SEGUIMIENTO_MIN = 30

    /**
     * Evento de día completo (o de varios días). Los eventos de día completo se expresan en UTC a
     * medianoche; si se enviara la medianoche local, algunos calendarios lo mostrarían un día antes.
     * El término es exclusivo: termina a la medianoche del día siguiente al último día.
     */
    fun rangoDiaCompletoUtc(inicioLocal: Long, terminoLocal: Long): Pair<Long, Long> {
        val inicio = medianocheUtc(inicioLocal)
        val termino = medianocheUtc(maxOf(inicioLocal, terminoLocal)) + MILIS_DIA
        return inicio to termino
    }

    /** Inicio y fin (hora local del dispositivo) de un recordatorio de seguimiento en la fecha dada. */
    fun rangoSeguimiento(fechaLocal: Long): Pair<Long, Long> {
        val inicio = Calendar.getInstance().apply {
            timeInMillis = fechaLocal
            set(Calendar.HOUR_OF_DAY, HORA_SEGUIMIENTO)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return inicio to inicio + DURACION_SEGUIMIENTO_MIN * 60_000L
    }

    fun intentEvento(
        titulo: String,
        descripcion: String,
        ubicacion: String,
        inicioLocal: Long,
        terminoLocal: Long
    ): Intent {
        val (inicio, termino) = rangoDiaCompletoUtc(inicioLocal, terminoLocal)
        return base(titulo, descripcion, ubicacion, inicio, termino, diaCompleto = true)
    }

    fun intentSeguimiento(
        titulo: String,
        descripcion: String,
        fechaLocal: Long
    ): Intent {
        val (inicio, termino) = rangoSeguimiento(fechaLocal)
        return base(titulo, descripcion, "", inicio, termino, diaCompleto = false)
    }

    private fun base(
        titulo: String,
        descripcion: String,
        ubicacion: String,
        inicio: Long,
        termino: Long,
        diaCompleto: Boolean
    ): Intent = Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE, titulo)
        putExtra(CalendarContract.Events.DESCRIPTION, descripcion)
        if (ubicacion.isNotBlank()) putExtra(CalendarContract.Events.EVENT_LOCATION, ubicacion)
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, inicio)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, termino)
        putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, diaCompleto)
    }

    private const val MILIS_DIA = 24L * 60 * 60 * 1000

    private fun medianocheUtc(millisLocal: Long): Long {
        val local = Calendar.getInstance().apply { timeInMillis = millisLocal }
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis
    }
}
