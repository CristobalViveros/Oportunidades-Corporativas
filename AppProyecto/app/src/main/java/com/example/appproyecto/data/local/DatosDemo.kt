package com.example.appproyecto.data.local

import com.example.appproyecto.data.model.*
import com.example.appproyecto.util.sha256

private const val DIA = 86_400_000L

/** Fecha relativa a hoy: dias(3) = en 3 días, dias(-2) = hace 2 días. */
private fun dias(n: Int): Long = System.currentTimeMillis() + n * DIA

suspend fun sembrarDatosDemo(db: AppDatabase) {
    if (db.usuarioDao().contar() > 0) return

    // ---------- Usuarios ----------
    val ana = db.usuarioDao().insertar(
        Usuario(nombre = "Ana Pérez (demo)", correo = "ana.ejecutiva@ejemplo.cl",
            passwordHash = sha256("Ana12345"), perfil = "EJECUTIVO", zona = "Región Metropolitana")
    )
    val carlos = db.usuarioDao().insertar(
        Usuario(nombre = "Carlos Soto (demo)", correo = "carlos.ejecutivo@ejemplo.cl",
            passwordHash = sha256("Carlos123"), perfil = "EJECUTIVO", zona = "Valparaíso")
    )
    db.usuarioDao().insertar(
        Usuario(nombre = "Sofía Rojas (demo)", correo = "sofia.supervisora@ejemplo.cl",
            passwordHash = sha256("Sofia1234"), perfil = "SUPERVISOR", area = "Supervisión")
    )

    // ---------- Organizaciones ----------
    val muni = db.organizacionDao().insertar(Organizacion(
        nombre = "Municipalidad de Valle Verde", tipo = TipoOrganizacion.MUNICIPALIDAD.name,
        rubro = "Gobierno local", comuna = "Puente Alto", region = "Metropolitana",
        publicoAsociado = "Vecinos y funcionarios municipales"))
    val feria = db.organizacionDao().insertar(Organizacion(
        nombre = "Corporación Feria Andina", tipo = TipoOrganizacion.ORGANIZADOR_EVENTOS.name,
        rubro = "Ferias y eventos", comuna = "Santiago", region = "Metropolitana",
        publicoAsociado = "Empresas y familias"))
    val holding = db.organizacionDao().insertar(Organizacion(
        nombre = "Holding Alfa Demo S.A.", tipo = TipoOrganizacion.EMPRESA.name,
        rubro = "Retail", comuna = "Las Condes", region = "Metropolitana",
        publicoAsociado = "Trabajadores y sus familias"))
    val plaza = db.organizacionDao().insertar(Organizacion(
        nombre = "Centro Comercial Plaza Demo", tipo = TipoOrganizacion.CENTRO_COMERCIAL.name,
        rubro = "Comercio", comuna = "Viña del Mar", region = "Valparaíso",
        publicoAsociado = "Visitantes y clientes"))

    // ---------- Contactos ----------
    db.contactoDao().insertar(Contacto(organizacionId = muni, nombre = "Marta Lagos (demo)",
        cargo = "Encargada de Bienestar", area = AreaContacto.BIENESTAR.name,
        correo = "marta.lagos@ejemplo.cl", telefono = "+56 9 0000 0001", fechaUltimoContacto = dias(-4)))
    db.contactoDao().insertar(Contacto(organizacionId = feria, nombre = "Rodrigo Vega (demo)",
        cargo = "Jefe de Producción", area = AreaContacto.EVENTOS.name,
        correo = "rodrigo.vega@ejemplo.cl", telefono = "+56 9 0000 0002", fechaUltimoContacto = dias(-10)))
    db.contactoDao().insertar(Contacto(organizacionId = holding, nombre = "Paula Díaz (demo)",
        cargo = "Analista de Beneficios", area = AreaContacto.BENEFICIOS.name,
        correo = "paula.diaz@ejemplo.cl", telefono = "+56 9 0000 0003"))
    db.contactoDao().insertar(Contacto(organizacionId = plaza, nombre = "Jorge Silva (demo)",
        cargo = "Coordinador de Marketing", area = AreaContacto.MARKETING.name,
        correo = "jorge.silva@ejemplo.cl", telefono = "+56 9 0000 0004", fechaUltimoContacto = dias(-1)))

    // ---------- Eventos ----------
    val e1 = db.eventoDao().insertar(Evento(nombre = "Feria de Bienestar Municipal",
        tipo = TipoOportunidad.ACTIVIDAD_MUNICIPAL.name,
        descripcion = "Feria anual de salud y beneficios para vecinos.",
        fechaInicio = dias(6), fechaTermino = dias(6), horario = "10:00 - 17:00",
        direccion = "Plaza de Armas (demo)", comuna = "Puente Alto", region = "Metropolitana",
        organizacionId = muni, publicoEsperado = 1500, publicoObjetivo = "Familias",
        condiciones = "Stand de 3x3 m", costoReferencial = 150_000))
    val e2 = db.eventoDao().insertar(Evento(nombre = "Expo Empresas Andina",
        tipo = TipoOportunidad.FERIA_EMPRESARIAL.name,
        descripcion = "Feria de proveedores y beneficios corporativos.",
        fechaInicio = dias(20), fechaTermino = dias(22), horario = "09:00 - 18:00",
        comuna = "Santiago", region = "Metropolitana",
        organizacionId = feria, publicoEsperado = 4000, publicoObjetivo = "Empresas y trabajadores",
        costoReferencial = 400_000))
    val e3 = db.eventoDao().insertar(Evento(nombre = "Jornada de Beneficios Alfa",
        tipo = TipoOportunidad.JORNADA_BENEFICIOS.name,
        fechaInicio = dias(3), fechaTermino = dias(3), horario = "12:00 - 16:00",
        comuna = "Las Condes", region = "Metropolitana",
        organizacionId = holding, publicoEsperado = 600, publicoObjetivo = "Trabajadores"))
    val e4 = db.eventoDao().insertar(Evento(nombre = "Festival de Verano Plaza",
        tipo = TipoOportunidad.ACTIVACION_COMERCIAL.name,
        fechaInicio = dias(35), fechaTermino = dias(37), horario = "11:00 - 21:00",
        comuna = "Viña del Mar", region = "Valparaíso",
        organizacionId = plaza, publicoEsperado = 8000, publicoObjetivo = "Visitantes y turistas",
        costoReferencial = 600_000))

    // ---------- Oportunidades ----------
    db.oportunidadDao().insertar(Oportunidad(nombre = "Stand en Feria de Bienestar",
        tipo = TipoOportunidad.ACTIVIDAD_MUNICIPAL.name, fuente = "Sitio web municipal",
        eventoId = e1, responsableId = ana, prioridad = Prioridad.ALTA.name,
        potencial = 4, afinidad = 5, estado = EstadoOportunidad.PROPUESTA_ENVIADA.name,
        fechaDeteccion = dias(-15), fechaLimitePostulacion = dias(2)))
    db.oportunidadDao().insertar(Oportunidad(nombre = "Participación Expo Empresas",
        tipo = TipoOportunidad.FERIA_EMPRESARIAL.name, fuente = "Redes sociales",
        eventoId = e2, responsableId = ana, prioridad = Prioridad.ALTA.name,
        potencial = 5, afinidad = 4, estado = EstadoOportunidad.CONTACTO_REALIZADO.name,
        fechaDeteccion = dias(-12)))
    db.oportunidadDao().insertar(Oportunidad(nombre = "Jornada interna Alfa",
        tipo = TipoOportunidad.JORNADA_BENEFICIOS.name, fuente = "Referencia",
        eventoId = e3, responsableId = carlos, prioridad = Prioridad.MEDIA.name,
        potencial = 3, afinidad = 4, estado = EstadoOportunidad.CONTACTO_PENDIENTE.name,
        fechaDeteccion = dias(-3)))
    db.oportunidadDao().insertar(Oportunidad(nombre = "Activación Festival de Verano",
        tipo = TipoOportunidad.ACTIVACION_COMERCIAL.name, fuente = "Correo",
        eventoId = e4, responsableId = carlos, prioridad = Prioridad.MEDIA.name,
        potencial = 5, afinidad = 3, estado = EstadoOportunidad.EVENTO_IDENTIFICADO.name,
        fechaDeteccion = dias(-1)))
}