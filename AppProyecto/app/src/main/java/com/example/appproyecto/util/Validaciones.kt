package com.example.appproyecto.util

// Devuelven el mensaje de error, o null si el dato es válido.

private val REGEX_CORREO =
    Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")

private val REGEX_TELEFONO = Regex("^[+0-9 ()-]+$")

private val REGEX_SITIO_WEB = Regex("^(https?://)?([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}(/\\S*)?$")

fun validarCorreo(correo: String): String? = when {
    correo.isBlank() -> "El correo es obligatorio"
    !correo.contains("@") -> "El correo debe contener @"
    !REGEX_CORREO.matches(correo.trim()) -> "Ingresa un correo válido (ej: nombre@empresa.cl)"
    else -> null
}

/** Acepta formatos como +56 9 1234 5678, 912345678 o (2) 2345 6789: entre 9 y 11 dígitos. */
fun validarTelefono(telefono: String): String? = when {
    telefono.isBlank() -> "El teléfono es obligatorio"
    !REGEX_TELEFONO.matches(telefono.trim()) -> "Solo números, espacios, + , ( ) y guiones"
    telefono.count { it.isDigit() } !in 9..11 -> "El teléfono debe tener entre 9 y 11 dígitos"
    else -> null
}

fun validarSitioWebOpcional(url: String): String? = when {
    url.isBlank() -> null
    url.trim().length > 100 -> "El sitio web no puede superar 100 caracteres"
    !REGEX_SITIO_WEB.matches(url.trim()) -> "Ingresa un sitio web válido (ej: www.empresa.cl)"
    else -> null
}

fun validarPassword(password: String): String? = when {
    password.isBlank() -> "La contraseña es obligatoria"
    password.length < 8 -> "Mínimo 8 caracteres"
    else -> null
}

fun validarTextoObligatorio(valor: String, campo: String, minimo: Int = 3, maximo: Int = 80): String? = when {
    valor.isBlank() -> "$campo es obligatorio"
    valor.trim().length < minimo -> "$campo debe tener al menos $minimo caracteres"
    valor.trim().length > maximo -> "$campo no puede superar $maximo caracteres"
    else -> null
}

fun validarTextoOpcional(valor: String, campo: String, maximo: Int = 300): String? =
    if (valor.length > maximo) "$campo no puede superar $maximo caracteres" else null

fun validarSeleccion(valor: Any?, campo: String): String? =
    if (valor == null) "Selecciona $campo" else null

fun validarEntero(valor: String, campo: String, minimo: Int, maximo: Int): String? {
    val numero = valor.trim().toIntOrNull() ?: return "$campo debe ser un número"
    return if (numero !in minimo..maximo) "$campo debe estar entre $minimo y $maximo" else null
}

fun validarFecha(fecha: Long?): String? =
    if (fecha == null) "Selecciona una fecha" else null

/** La próxima acción es obligatoria si se programó fecha. */
fun validarProximaAccion(accion: String, fecha: Long?): String? = when {
    accion.trim().length > 100 -> "La próxima acción no puede superar 100 caracteres"
    fecha != null && accion.isBlank() -> "Indica cuál será la próxima acción"
    else -> null
}

/** La fecha es obligatoria si hay próxima acción, y no puede estar en el pasado. */
fun validarFechaProximoSeguimiento(fecha: Long?, accion: String, hoy: Long = inicioDelDia()): String? = when {
    fecha == null && accion.isNotBlank() -> "Selecciona la fecha del próximo seguimiento"
    fecha != null && fecha < hoy -> "La fecha no puede ser anterior a hoy"
    else -> null
}

// ---------- Reglas de negocio de eventos y oportunidades ----------

const val COSTO_MAXIMO = 100_000_000L

/** La fecha de un evento nuevo es obligatoria y no puede estar en el pasado. */
fun validarFechaEvento(fecha: Long?, hoy: Long = inicioDelDia()): String? = when {
    fecha == null -> "Selecciona la fecha del evento"
    fecha < hoy -> "La fecha del evento no puede ser anterior a hoy"
    else -> null
}

/** El término es opcional (si falta, el evento dura un día); si existe no puede ser antes del inicio. */
fun validarFechaTermino(inicio: Long?, termino: Long?): String? = when {
    termino == null -> null
    inicio == null -> "Primero elige la fecha de inicio"
    termino < inicio -> "La fecha de término no puede ser anterior al inicio"
    else -> null
}

/** Costo referencial en pesos: opcional, entero, nunca negativo y con un tope razonable. */
fun validarCosto(texto: String): String? {
    if (texto.isBlank()) return null
    val valor = texto.trim().toLongOrNull() ?: return "El costo debe ser un número entero"
    return when {
        valor < 0 -> "El costo no puede ser negativo"
        valor > COSTO_MAXIMO -> "El costo no puede superar $${"%,d".format(COSTO_MAXIMO).replace(',', '.')}"
        else -> null
    }
}

/**
 * Fecha límite de postulación (opcional). No puede ser anterior a hoy ni posterior al evento.
 * [permitirPasado] sirve al editar: si la fecha ya estaba guardada y no se cambió, no se vuelve a exigir.
 */
fun validarFechaLimite(
    fecha: Long?,
    fechaEvento: Long?,
    permitirPasado: Boolean = false,
    hoy: Long = inicioDelDia()
): String? = when {
    fecha == null -> null
    !permitirPasado && fecha < hoy -> "La fecha límite no puede ser anterior a hoy"
    fechaEvento != null && fecha > fechaEvento -> "La fecha límite no puede ser posterior al evento"
    else -> null
}


// ---------- Directorio: organizaciones y contactos ----------

/** Correo del contacto: obligatorio y con formato válido. */
fun validarCorreoContacto(correo: String): String? = validarCorreo(correo)

/** Teléfono del contacto: opcional, pero si se escribe debe tener un formato válido. */
fun validarTelefonoOpcional(telefono: String): String? =
    if (telefono.isBlank()) null else validarTelefono(telefono)
