package com.example.appproyecto.data.local

import com.example.appproyecto.data.model.*
import com.example.appproyecto.util.sha256

private const val DIA = 86_400_000L

/** Fecha relativa a hoy: dias(3) = en 3 días, dias(-2) = hace 2 días. */
private fun dias(n: Int): Long = System.currentTimeMillis() + n * DIA

/**
 * Datos 100 % ficticios para probar la app.
 * Cuentas: 3 ejecutivos + 1 supervisor. Se siembran solo si la base está vacía.
 */
suspend fun sembrarDatosDemo(db: AppDatabase) {
    if (db.usuarioDao().contar() > 0) return

    // ---------- Usuarios (el orden respeta las claves foráneas) ----------
    val ana = db.usuarioDao().insertar(
        Usuario(nombre = "Ana Pérez (demo)", correo = "ana.ejecutiva@ejemplo.cl",
            passwordHash = sha256("Ana12345"), perfil = Perfil.EJECUTIVO.name, zona = "Región Metropolitana")
    )
    val carlos = db.usuarioDao().insertar(
        Usuario(nombre = "Carlos Soto (demo)", correo = "carlos.ejecutivo@ejemplo.cl",
            passwordHash = sha256("Carlos123"), perfil = Perfil.EJECUTIVO.name, zona = "Valparaíso")
    )
    val diego = db.usuarioDao().insertar(
        Usuario(nombre = "Diego Fuentes (demo)", correo = "diego.ejecutivo@ejemplo.cl",
            passwordHash = sha256("Diego1234"), perfil = Perfil.EJECUTIVO.name, zona = "Biobío")
    )
    db.usuarioDao().insertar(
        Usuario(nombre = "Sofía Rojas (demo)", correo = "sofia.supervisora@ejemplo.cl",
            passwordHash = sha256("Sofia1234"), perfil = Perfil.SUPERVISOR.name, area = "Supervisión")
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
    val universidad = db.organizacionDao().insertar(Organizacion(
        nombre = "Instituto Demo del Sur", tipo = TipoOrganizacion.INSTITUCION_PRIVADA.name,
        rubro = "Educación", comuna = "Concepción", region = "Biobío",
        publicoAsociado = "Estudiantes y funcionarios"))

    // ---------- Contactos ----------
    val c1 = db.contactoDao().insertar(Contacto(organizacionId = muni, nombre = "Marta Lagos (demo)",
        cargo = "Encargada de Bienestar", area = AreaContacto.BIENESTAR.name,
        correo = "marta.lagos@ejemplo.cl", telefono = "+56 9 0000 0001", fechaUltimoContacto = dias(-4)))
    val c2 = db.contactoDao().insertar(Contacto(organizacionId = feria, nombre = "Rodrigo Vega (demo)",
        cargo = "Jefe de Producción", area = AreaContacto.EVENTOS.name,
        correo = "rodrigo.vega@ejemplo.cl", telefono = "+56 9 0000 0002", fechaUltimoContacto = dias(-8)))
    val c3 = db.contactoDao().insertar(Contacto(organizacionId = holding, nombre = "Paula Díaz (demo)",
        cargo = "Analista de Beneficios", area = AreaContacto.BENEFICIOS.name,
        correo = "paula.diaz@ejemplo.cl", telefono = "+56 9 0000 0003", fechaUltimoContacto = dias(-3)))
    db.contactoDao().insertar(Contacto(organizacionId = plaza, nombre = "Jorge Silva (demo)",
        cargo = "Coordinador de Marketing", area = AreaContacto.MARKETING.name,
        correo = "jorge.silva@ejemplo.cl", telefono = "+56 9 0000 0004", fechaUltimoContacto = dias(-1)))
    val c5 = db.contactoDao().insertar(Contacto(organizacionId = universidad, nombre = "Elena Mora (demo)",
        cargo = "Directora de Extensión", area = AreaContacto.ACTIVACIONES.name,
        correo = "elena.mora@ejemplo.cl", telefono = "+56 9 0000 0005", fechaUltimoContacto = dias(-2)))

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
    val e5 = db.eventoDao().insertar(Evento(nombre = "Feria de Beneficios Estudiantiles",
        tipo = TipoOportunidad.FERIA_INSTITUCIONAL.name,
        descripcion = "Feria anual de servicios para estudiantes y funcionarios.",
        fechaInicio = dias(12), fechaTermino = dias(12), horario = "10:00 - 16:00",
        comuna = "Concepción", region = "Biobío",
        organizacionId = universidad, publicoEsperado = 2500, publicoObjetivo = "Estudiantes",
        condiciones = "Stand de 2x2 m", costoReferencial = 90_000))

    // ---------- Oportunidades ----------
    val o1 = db.oportunidadDao().insertar(Oportunidad(nombre = "Stand en Feria de Bienestar",
        tipo = TipoOportunidad.ACTIVIDAD_MUNICIPAL.name, fuente = "Sitio web municipal",
        eventoId = e1, responsableId = ana, prioridad = Prioridad.ALTA.name,
        potencial = 4, afinidad = 5, estado = EstadoOportunidad.PROPUESTA_ENVIADA.name,
        fechaDeteccion = dias(-15), fechaLimitePostulacion = dias(2)))
    val o2 = db.oportunidadDao().insertar(Oportunidad(nombre = "Participación Expo Empresas",
        tipo = TipoOportunidad.FERIA_EMPRESARIAL.name, fuente = "Redes sociales",
        eventoId = e2, responsableId = ana, prioridad = Prioridad.ALTA.name,
        potencial = 5, afinidad = 4, estado = EstadoOportunidad.CONTACTO_REALIZADO.name,
        fechaDeteccion = dias(-12)))
    val o3 = db.oportunidadDao().insertar(Oportunidad(nombre = "Jornada interna Alfa",
        tipo = TipoOportunidad.JORNADA_BENEFICIOS.name, fuente = "Referencia",
        eventoId = e3, responsableId = carlos, prioridad = Prioridad.MEDIA.name,
        potencial = 3, afinidad = 4, estado = EstadoOportunidad.CONTACTO_PENDIENTE.name,
        fechaDeteccion = dias(-3)))
    val o4 = db.oportunidadDao().insertar(Oportunidad(nombre = "Activación Festival de Verano",
        tipo = TipoOportunidad.ACTIVACION_COMERCIAL.name, fuente = "Correo",
        eventoId = e4, responsableId = carlos, prioridad = Prioridad.MEDIA.name,
        potencial = 5, afinidad = 3, estado = EstadoOportunidad.EVENTO_IDENTIFICADO.name,
        fechaDeteccion = dias(-1)))
    val o5 = db.oportunidadDao().insertar(Oportunidad(nombre = "Stand en Feria Estudiantil",
        tipo = TipoOportunidad.FERIA_INSTITUCIONAL.name, fuente = "Contacto directo",
        eventoId = e5, responsableId = diego, prioridad = Prioridad.BAJA.name,
        potencial = 3, afinidad = 3, estado = EstadoOportunidad.EN_ESPERA_RESPUESTA.name,
        fechaDeteccion = dias(-9), fechaLimitePostulacion = dias(8)))

    // ---------- Seguimientos (historial) ----------
    val seg = db.seguimientoDao()

    // o1 (Ana): propuesta enviada y seguimiento ATRASADO (próxima fecha ya pasó)
    seg.insertar(Seguimiento(oportunidadId = o1, usuarioId = ana,
        tipoAccion = TipoAccion.INVESTIGACION_INICIAL.name, fechaHora = dias(-15),
        resultado = "Evento detectado en el sitio web municipal.",
        proximaAccion = "Buscar contacto en Bienestar", fechaProximoSeguimiento = dias(-13)))
    seg.insertar(Seguimiento(oportunidadId = o1, usuarioId = ana, contactoId = c1,
        tipoAccion = TipoAccion.PRIMER_CONTACTO.name, fechaHora = dias(-10),
        resultado = "Marta Lagos se muestra interesada.",
        proximaAccion = "Enviar propuesta", fechaProximoSeguimiento = dias(-5),
        estadoNuevo = EstadoOportunidad.CONTACTO_REALIZADO.name))
    seg.insertar(Seguimiento(oportunidadId = o1, usuarioId = ana, contactoId = c1,
        tipoAccion = TipoAccion.ENVIO_PROPUESTA.name, fechaHora = dias(-4),
        resultado = "Propuesta ficticia enviada.",
        proximaAccion = "Llamar para confirmar recepción", fechaProximoSeguimiento = dias(-1),
        estadoNuevo = EstadoOportunidad.PROPUESTA_ENVIADA.name))

    // o2 (Ana): seguimiento PRÓXIMO
    seg.insertar(Seguimiento(oportunidadId = o2, usuarioId = ana, contactoId = c2,
        tipoAccion = TipoAccion.PRIMER_CONTACTO.name, fechaHora = dias(-8),
        resultado = "Interesado, pide dossier de presentación.",
        proximaAccion = "Enviar presentación", fechaProximoSeguimiento = dias(2),
        estadoNuevo = EstadoOportunidad.CONTACTO_REALIZADO.name))

    // o3 (Carlos): seguimiento PRÓXIMO
    seg.insertar(Seguimiento(oportunidadId = o3, usuarioId = carlos, contactoId = c3,
        tipoAccion = TipoAccion.BUSQUEDA_CONTACTO.name, fechaHora = dias(-3),
        resultado = "Se identificó a la analista de beneficios.",
        proximaAccion = "Realizar primer contacto", fechaProximoSeguimiento = dias(1)))

    // o4 (Carlos): sin contacto todavía (solo investigación, sin próxima fecha)
    seg.insertar(Seguimiento(oportunidadId = o4, usuarioId = carlos,
        tipoAccion = TipoAccion.INVESTIGACION_INICIAL.name, fechaHora = dias(-1),
        resultado = "Evento detectado por correo. Falta buscar contacto."))

    // o5 (Diego): propuesta sin respuesta
    seg.insertar(Seguimiento(oportunidadId = o5, usuarioId = diego, contactoId = c5,
        tipoAccion = TipoAccion.ENVIO_PROPUESTA.name, fechaHora = dias(-6),
        resultado = "Propuesta ficticia enviada.",
        proximaAccion = "Consultar respuesta", fechaProximoSeguimiento = dias(-2),
        estadoNuevo = EstadoOportunidad.EN_ESPERA_RESPUESTA.name))
    seg.insertar(Seguimiento(oportunidadId = o5, usuarioId = diego, contactoId = c5,
        tipoAccion = TipoAccion.LLAMADA.name, fechaHora = dias(-2),
        resultado = "Sin respuesta aún; piden esperar unos días.",
        proximaAccion = "Volver a llamar", fechaProximoSeguimiento = dias(3)))
}
