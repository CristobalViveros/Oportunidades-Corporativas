package com.example.appproyecto.data.model

enum class Prioridad(val etiqueta: String) {
    ALTA("Alta"), MEDIA("Media"), BAJA("Baja")
}

enum class EstadoOportunidad(val etiqueta: String) {
    EVENTO_IDENTIFICADO("Evento identificado"),
    CONTACTO_PENDIENTE("Contacto pendiente"),
    CONTACTO_REALIZADO("Contacto realizado"),
    PROPUESTA_ENVIADA("Propuesta enviada"),
    EN_ESPERA_RESPUESTA("En espera de respuesta"),
    EN_EVALUACION("En evaluación"),
    REQUIERE_SEGUIMIENTO("Requiere seguimiento"),
    PARTICIPACION_CONFIRMADA("Participación confirmada"),
    ACTIVACION_REALIZADA("Activación realizada"),
    ACTIVACION_CANCELADA("Activación cancelada"),
    OPORTUNIDAD_DESCARTADA("Oportunidad descartada"),
    OPORTUNIDAD_CERRADA("Oportunidad cerrada")
}

enum class TipoOportunidad(val etiqueta: String) {
    EVENTO_CORPORATIVO("Evento corporativo"),
    FERIA_EMPRESARIAL("Feria empresarial"),
    ACTIVIDAD_MUNICIPAL("Actividad municipal"),
    JORNADA_BENEFICIOS("Jornada de beneficios"),
    FERIA_INSTITUCIONAL("Feria institucional"),
    ACTIVIDAD_TRABAJADORES("Actividad de trabajadores"),
    ACTIVACION_COMERCIAL("Activación comercial"),
    OTRA("Otra instancia relevante")
}

enum class TipoOrganizacion(val etiqueta: String) {
    EMPRESA("Empresa"),
    MUNICIPALIDAD("Municipalidad"),
    INSTITUCION_PUBLICA("Institución pública"),
    INSTITUCION_PRIVADA("Institución privada"),
    ORGANIZADOR_EVENTOS("Organizador de eventos"),
    CENTRO_COMERCIAL("Centro comercial"),
    ASOCIACION("Asociación"),
    OTRA("Otra entidad")
}

enum class AreaContacto(val etiqueta: String) {
    EVENTOS("Eventos"),
    ACTIVACIONES("Activaciones"),
    MARKETING("Marketing"),
    COMUNICACIONES("Comunicaciones"),
    RECURSOS_HUMANOS("Recursos Humanos"),
    BIENESTAR("Bienestar"),
    BENEFICIOS("Beneficios"),
    ADMINISTRACION("Administración")
}

/** Convierte un texto guardado en la base de datos de vuelta a su enum. */
inline fun <reified T : Enum<T>> String.aEnum(porDefecto: T): T =
    enumValues<T>().firstOrNull { it.name == this } ?: porDefecto