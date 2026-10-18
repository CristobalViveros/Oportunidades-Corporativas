package com.example.appproyecto

import com.example.appproyecto.data.model.EstadoOportunidad
import com.example.appproyecto.data.model.OportunidadItem
import com.example.appproyecto.data.model.Prioridad
import com.example.appproyecto.util.filtrarOportunidades
import com.example.appproyecto.util.validarCorreo
import com.example.appproyecto.util.validarEntero
import com.example.appproyecto.util.validarPassword
import com.example.appproyecto.util.validarTextoObligatorio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class LogicaTest {

    private fun item(id: Long, nombre: String, estado: EstadoOportunidad, prioridad: Prioridad) =
        OportunidadItem(
            id = id, nombre = nombre, estado = estado.name, prioridad = prioridad.name,
            potencial = 3, eventoNombre = "Feria Demo", eventoFecha = 0L, comuna = "Valle Verde",
            organizacionNombre = "Org Demo", responsableNombre = "Ana"
        )

    private val lista = listOf(
        item(1, "Stand de bienestar", EstadoOportunidad.EVENTO_IDENTIFICADO, Prioridad.ALTA),
        item(2, "Charla de beneficios", EstadoOportunidad.PROPUESTA_ENVIADA, Prioridad.BAJA)
    )

    @Test fun correoValido_noDevuelveError() = assertNull(validarCorreo("ana@ejemplo.cl"))
    @Test fun correoSinArroba_devuelveError() = assertNotNull(validarCorreo("anaejemplo.cl"))
    @Test fun correoVacio_devuelveError() = assertNotNull(validarCorreo(""))

    @Test fun passwordCorta_devuelveError() = assertNotNull(validarPassword("abc"))
    @Test fun passwordValida_noDevuelveError() = assertNull(validarPassword("Ana12345"))

    @Test fun textoCorto_devuelveError() = assertNotNull(validarTextoObligatorio("ab", "El nombre", 5, 80))
    @Test fun textoValido_noDevuelveError() = assertNull(validarTextoObligatorio("Feria andina", "El nombre", 5, 80))

    @Test fun enteroNoNumerico_devuelveError() = assertNotNull(validarEntero("abc", "El público", 1, 100))
    @Test fun enteroFueraDeRango_devuelveError() = assertNotNull(validarEntero("500", "El público", 1, 100))
    @Test fun enteroValido_noDevuelveError() = assertNull(validarEntero("50", "El público", 1, 100))

    @Test
    fun filtroPorTexto_encuentraCoincidencia() {
        val r = filtrarOportunidades(lista, "stand", null, null)
        assertEquals(1, r.size)
        assertEquals(1L, r[0].id)
    }

    @Test
    fun filtroPorEstadoYPrioridad_combinaCriterios() {
        val r = filtrarOportunidades(lista, "", EstadoOportunidad.PROPUESTA_ENVIADA, Prioridad.BAJA)
        assertEquals(1, r.size)
        assertEquals(2L, r[0].id)
    }

    @Test
    fun sinFiltros_devuelveTodo() {
        assertEquals(2, filtrarOportunidades(lista, "", null, null).size)
    }

}