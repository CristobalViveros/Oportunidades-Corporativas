package com.example.appproyecto.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidacionesTest {

    private val dia = 86_400_000L
    private val hoy = 1_800_000_000_000L // un instante fijo cualquiera, para no depender del reloj

    // ---------- Correo y teléfono ----------

    @Test fun correo() {
        assertEquals("El correo es obligatorio", validarCorreo(""))
        assertEquals("El correo debe contener @", validarCorreo("sin-arroba"))
        assertNotNull(validarCorreo("a@b"))
        assertNull(validarCorreo("nombre@empresa.cl"))
    }

    @Test fun telefono() {
        assertEquals("El teléfono es obligatorio", validarTelefono(""))
        assertNotNull(validarTelefono("abc"))
        assertNotNull(validarTelefono("12345"))
        assertNull(validarTelefono("+56 9 1234 5678"))
        assertNull(validarTelefono("912345678"))
    }

    @Test fun telefonoOpcional() {
        assertNull(validarTelefonoOpcional(""))
        assertNotNull(validarTelefonoOpcional("123"))
    }

    @Test fun sitioWebOpcional() {
        assertNull(validarSitioWebOpcional(""))
        assertNull(validarSitioWebOpcional("www.empresa.cl"))
        assertNotNull(validarSitioWebOpcional("no es un sitio"))
    }

    // ---------- Textos y números ----------

    @Test fun textoObligatorio() {
        assertEquals("El nombre es obligatorio", validarTextoObligatorio("   ", "El nombre"))
        assertNotNull(validarTextoObligatorio("ab", "El nombre", minimo = 3))
        assertNotNull(validarTextoObligatorio("x".repeat(81), "El nombre", maximo = 80))
        assertNull(validarTextoObligatorio("Feria Andina", "El nombre"))
    }

    @Test fun enteroEnRango() {
        assertEquals("El público debe ser un número", validarEntero("abc", "El público", 1, 100))
        assertEquals("El público debe estar entre 1 y 100", validarEntero("0", "El público", 1, 100))
        assertNull(validarEntero("50", "El público", 1, 100))
    }

    @Test fun costoNuncaNegativo() {
        assertNull(validarCosto(""))
        assertNull(validarCosto("150000"))
        assertEquals("El costo no puede ser negativo", validarCosto("-5"))
        assertNotNull(validarCosto("abc"))
        assertNotNull(validarCosto((COSTO_MAXIMO + 1).toString()))
    }

    // ---------- Fechas coherentes ----------

    @Test fun fechaDelEventoNoPuedeSerPasada() {
        assertEquals("Selecciona la fecha del evento", validarFechaEvento(null, hoy))
        assertNotNull(validarFechaEvento(hoy - dia, hoy))
        assertNull(validarFechaEvento(hoy, hoy))
    }

    @Test fun terminoNoPuedeSerAnteriorAlInicio() {
        assertNull(validarFechaTermino(hoy, null))
        assertNotNull(validarFechaTermino(hoy, hoy - dia))
        assertNull(validarFechaTermino(hoy, hoy + dia))
    }

    @Test fun fechaLimiteNoPuedeSerPosteriorAlEvento() {
        assertNull(validarFechaLimite(null, hoy + 10 * dia, hoy = hoy))
        assertNotNull(validarFechaLimite(hoy + 11 * dia, hoy + 10 * dia, hoy = hoy))
        assertNotNull(validarFechaLimite(hoy - dia, hoy + 10 * dia, hoy = hoy))
        // Al editar, una fecha pasada ya guardada no se vuelve a exigir.
        assertNull(validarFechaLimite(hoy - dia, hoy + 10 * dia, permitirPasado = true, hoy = hoy))
    }

    @Test fun proximoSeguimientoNecesitaAccionYFecha() {
        assertNotNull(validarProximaAccion("", hoy))
        assertNull(validarProximaAccion("", null))
        assertNotNull(validarFechaProximoSeguimiento(null, "Llamar", hoy))
        assertNotNull(validarFechaProximoSeguimiento(hoy - dia, "Llamar", hoy))
        assertNull(validarFechaProximoSeguimiento(hoy + dia, "Llamar", hoy))
    }
}
