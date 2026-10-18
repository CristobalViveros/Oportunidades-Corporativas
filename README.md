# AppProyecto – Gestión de Oportunidades Comerciales

Aplicación Android nativa (Kotlin + Jetpack Compose) para que un equipo comercial identifique, gestione y dé seguimiento a oportunidades de participación en eventos, ferias y activaciones. Funciona sin conexión: todos los datos se guardan localmente en el dispositivo.

> **Asignatura / Curso:** _(Desarrollo de Aplicaciones Móviles)_
> **Profesor:** _(CHRISTIAN EDUARDO ACUNA SANDOVAL)_

## Integrantes

| Nombre | Rol en el proyecto |
|---|---|
| _(Ignacio Espinoza)_ | _(Desarrollador)_ |
| _(Cristobal Viveros )_ | _(Desarrollador)_ |

## Descripción

AppProyecto centraliza el trabajo de un equipo comercial en torno a **eventos** (ferias, actividades municipales, jornadas de beneficios, activaciones, etc.). Cada evento puede convertirse en una **oportunidad** asignada a un ejecutivo, que registra sus gestiones (llamadas, reuniones, propuestas) hasta lograr la participación o descartarla. Un supervisor tiene visión global del equipo y toma las decisiones clave.

## Funcionalidades

**Acceso y sesión**
- Inicio de sesión con correo y contraseña (la contraseña se guarda como hash SHA-256, no en texto plano).
- La sesión se recuerda al cerrar la app y se puede cerrar desde el perfil.

**Oportunidades**
- Listado, filtros, creación y edición, con prioridad, potencial, afinidad y fecha límite de postulación.
- Flujo de estados controlado: Evento identificado → Contacto pendiente → Contacto realizado → Propuesta enviada → En espera de respuesta → En evaluación → Participación confirmada → Activación realizada → Oportunidad cerrada (también Requiere seguimiento, Activación cancelada y Oportunidad descartada).
- Detalle completo con historial de gestiones.

**Seguimientos**
- Registro de acciones (primer contacto, llamada, reunión, envío de propuesta, etc.), con resultado, próxima acción y fecha del próximo seguimiento.
- Las acciones que implican hablar con alguien exigen indicar con qué contacto.
- Alertas de seguimientos atrasados en el inicio.

**Eventos**
- Registro de eventos con fecha, horario, dirección, público esperado, costo referencial y foto.
- Opción para agregar el evento o un recordatorio de seguimiento al **calendario del dispositivo** (sin necesitar permisos de calendario: abre la app de calendario y el usuario confirma).

**Directorio**
- Organizaciones (empresas, municipalidades, centros comerciales, etc.) y sus contactos, con detalle, creación y edición.

**Evidencias fotográficas**
- Cada oportunidad permite tomar o elegir fotos desde la galería, ver una vista previa antes de guardar y consultar las anteriores.

**Inicio (dashboard)**
- Resumen de oportunidades, pendientes y atrasos según el rol.

**Validaciones**
- Correo, teléfono, sitio web, contraseña, fechas (no pasadas), rangos numéricos y largos máximos de texto, con mensajes de error claros.

## Roles y permisos

| | Ejecutivo comercial | Supervisor comercial |
|---|---|---|
| Ver oportunidades | Solo las suyas | Todas |
| Crear y editar | Sí (quedan a su nombre) | Sí |
| Registrar seguimientos | Sí | Sí |
| Cambiar prioridad al editar | No | Sí |
| Asignar o reasignar responsable | No | Sí |
| Confirmar participación | No | Sí |
| Descartar o reabrir oportunidad | No | Sí |
| Panel de supervisión del equipo | No | Sí |

Las reglas están centralizadas en `util/Permisos.kt`.

## Usuarios de prueba

Al abrir la app por primera vez con la base vacía se cargan datos ficticios:

| Rol | Correo | Contraseña |
|---|---|---|
| Ejecutivo | `ana.ejecutiva@ejemplo.cl` | `Ana12345` |
| Ejecutivo | `carlos.ejecutivo@ejemplo.cl` | `Carlos123` |
| Ejecutivo | `diego.ejecutivo@ejemplo.cl` | `Diego1234` |
| Supervisor | `sofia.supervisora@ejemplo.cl` | `Sofia1234` |

También se crean organizaciones, contactos, eventos, oportunidades y seguimientos de ejemplo (`data/local/DatosDemo.kt`).

## Arquitectura

La app sigue el patrón **MVVM** con una capa de repositorios, y la interfaz está construida íntegramente con **Jetpack Compose**.

```
UI (Compose Screens)  →  ViewModel  →  Repository  →  DAO (Room)  →  SQLite
```

- **UI (`ui/`)**: pantallas Compose organizadas por módulo (login, dashboard, oportunidades, detalle, directorio, eventos, seguimiento, supervisor, perfil, navegación y tema).
- **ViewModel**: mantiene el estado de cada pantalla y expone datos reactivos con `StateFlow` y corrutinas.
- **Repository (`data/repository/`)**: aplica las reglas de negocio y de permisos antes de leer o escribir.
- **Persistencia (`data/local/`)**: base de datos local con **Room** y el driver **BundledSQLiteDriver**, con un DAO por entidad.
- **Modelo (`data/model/`)**: entidades, enumeraciones (estados, tipos, perfiles) y objetos de apoyo.
- **Utilidades (`util/`)**: permisos, validaciones, manejo de sesión, fechas, calendario, fotos y cálculos del dashboard.

### Estructura del proyecto

```
appproyecto/
├── AppProyectoApp.kt        # Application: siembra los datos demo
├── MainActivity.kt
├── data/
│   ├── local/               # AppDatabase, DAOs y datos demo
│   ├── model/               # Entidades y enumeraciones
│   └── repository/          # Repositorios
├── ui/
│   ├── componentes/         # Componentes reutilizables
│   ├── dashboard/  detalle/  directorio/  eventos/
│   ├── login/  navigation/  oportunidades/
│   ├── perfil/  seguimiento/  supervisor/  theme/
└── util/                    # Permisos, validaciones, sesión, calendario, fotos, etc.
```

### Modelo de datos

Entidades principales: `Usuario`, `Organizacion`, `Contacto`, `Evento`, `Oportunidad`, `Seguimiento` y `Evidencia`. Una oportunidad pertenece a un evento y a un ejecutivo responsable; los seguimientos y las evidencias cuelgan de la oportunidad.

## Tecnologías

- Kotlin
- Jetpack Compose y Navigation Compose
- Room (con BundledSQLiteDriver)
- Corrutinas y Flow
- FileProvider, cámara y galería para las evidencias
- SharedPreferences para la sesión

## Instrucciones de ejecución

**Requisitos**
- Android Studio (versión reciente) con el SDK de Android instalado.
- Un emulador o un dispositivo físico con Android.

**Pasos**
1. Clona el repositorio:
   ```bash
   git clone <URL-DEL-REPOSITORIO>
   ```
2. Abre la carpeta del proyecto en Android Studio.
3. Espera a que termine la sincronización de Gradle (*Sync Project with Gradle Files*).
4. Selecciona un emulador o conecta un dispositivo.
5. Presiona **Run ▶** (o `Shift + F10`).
6. Inicia sesión con alguno de los [usuarios de prueba](#usuarios-de-prueba).

**Si la app se cierra al abrir**
Si el dispositivo tiene instalada una versión anterior de la app, desinstálala y vuelve a ejecutar (o usa *Wipe Data* en el Device Manager). El proyecto ya incluye `fallbackToDestructiveMigration` para recrear la base cuando cambia la versión; esto borra los datos locales y se vuelven a cargar los datos demo.

> **Nota:** el borrado automático de la base es adecuado mientras solo existan datos de prueba. Antes de publicar la app a usuarios reales deben escribirse migraciones de Room.
