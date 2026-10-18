# AppProyecto – Gestión de Oportunidades Comerciales

Aplicación Android nativa (Kotlin + Jetpack Compose) para que un equipo comercial identifique, gestione y dé seguimiento a oportunidades de participación en eventos, ferias y activaciones. Funciona sin conexión: todos los datos se guardan localmente en el dispositivo.

> **Asignatura:** DSY1105 – Desarrollo de Aplicaciones Móviles (Duoc UC) · Evaluación Parcial 2
> **Profesor:** Christian Eduardo Acuña Sandoval

## Integrantes

| Nombre | Rol en el proyecto | GitHub |
|---|---|---|
| Ignacio Espinoza | Desarrollador | [usuario-github] |
| Cristóbal Viveros | Desarrollador | [CristobalViveros](https://github.com/CristobalViveros) |

## Descripción

AppProyecto centraliza el trabajo de un equipo comercial en torno a **eventos** (ferias, actividades municipales, jornadas de beneficios, activaciones, etc.). Cada evento puede convertirse en una **oportunidad** asignada a un ejecutivo, que registra sus gestiones (llamadas, reuniones, propuestas) hasta lograr la participación o descartarla. Un supervisor tiene visión global del equipo y toma las decisiones clave.

Es un proyecto académico (MVP): **todos los datos son ficticios** y no existen integraciones reales con otros sistemas.

## Funcionalidades

**Acceso y sesión**
- Inicio de sesión con correo y contraseña (la contraseña se guarda como hash SHA-256, no en texto plano).
- La sesión se recuerda al cerrar la app y se puede cerrar desde el perfil.

**Oportunidades**
- Listado con búsqueda y filtros por estado y prioridad, creación y edición, con prioridad, potencial, afinidad y fecha límite de postulación.
- Flujo de estados controlado: Evento identificado → Contacto pendiente → Contacto realizado → Propuesta enviada → En espera de respuesta → En evaluación → Participación confirmada → Activación realizada → Oportunidad cerrada (también Requiere seguimiento, Activación cancelada y Oportunidad descartada).
- Detalle completo con historial de gestiones.

**Seguimientos**
- Registro de acciones (primer contacto, llamada, reunión, envío de propuesta, etc.), con resultado, próxima acción y fecha del próximo seguimiento.
- Las acciones que implican hablar con alguien exigen indicar con qué contacto.
- Cada cambio de estado queda registrado en el historial, que nunca se borra.
- Alertas de seguimientos pendientes y atrasados en el inicio.

**Eventos**
- Registro de eventos con fecha, horario, ubicación (dirección y comuna), tipo, público esperado, costo referencial y foto.
- Opción para agregar el evento o un recordatorio de seguimiento al **calendario del dispositivo**.

**Directorio**
- Organizaciones (empresas, municipalidades, centros comerciales, etc.) y sus contactos, con detalle, creación y edición.

**Evidencias fotográficas**
- Cada oportunidad permite tomar o elegir fotos desde la galería, ver una vista previa antes de guardar y consultar las anteriores.

**Inicio (dashboard)**
- Resumen de oportunidades, próximas gestiones y atrasos, distinto según el rol.

**Panel del supervisor**
- Consulta global del equipo y asignación o reasignación de responsables.

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

Las reglas están centralizadas en `util/Permisos.kt` y se aplican también en los repositorios, no solo en las pantallas.

## Usuarios de prueba

Al abrir la app por primera vez con la base vacía se cargan datos ficticios:

| Rol | Correo | Contraseña |
|---|---|---|
| Ejecutivo | `ana.ejecutiva@ejemplo.cl` | `Ana12345` |
| Ejecutivo | `carlos.ejecutivo@ejemplo.cl` | `Carlos123` |
| Ejecutivo | `diego.ejecutivo@ejemplo.cl` | `Diego1234` |
| Supervisor | `sofia.supervisora@ejemplo.cl` | `Sofia1234` |

También se crean organizaciones, contactos, eventos, oportunidades y seguimientos de ejemplo (`data/local/DatosDemo.kt`).

## Recursos nativos del dispositivo

- **Cámara:** captura de fotografías de evidencia con `ActivityResultContracts.TakePicture`. Usa `FileProvider` para entregar una URI segura a la app de cámara, por lo que no requiere el permiso `CAMERA`. Si el usuario cancela, el archivo vacío se elimina; si no hay app de cámara, se muestra un aviso.
- **Galería:** selección de imágenes con el selector de fotos del sistema (`PickVisualMedia`), sin permisos de almacenamiento. La imagen se copia al almacenamiento interno de la app.
- **Calendario:** el evento o el recordatorio de seguimiento se envía al calendario del dispositivo con `Intent.ACTION_INSERT`. No requiere permisos de calendario: el usuario confirma o cancela en la app de calendario. Se manejan fechas de día completo (UTC) y horas locales, y los casos sin app de calendario o cancelación.
- **Selector de fecha nativo** en los formularios.

Las fotos se guardan en el almacenamiento interno de la app y quedan asociadas a su oportunidad en la base de datos.

## Validaciones

La lógica vive en `util/Validaciones.kt`, separada de las pantallas, y cada función devuelve el mensaje de error (o `null` si el dato es válido). Cubre correo, teléfono, sitio web, contraseña, longitudes, rangos numéricos, costos no negativos y reglas de fechas (por ejemplo, el término del evento no puede ser anterior al inicio). Los formularios muestran el error debajo de cada campo con `supportingText` e íconos, conservan lo que el usuario ya escribió y no permiten guardar datos inválidos.

## Interfaz y animaciones

- Jetpack Compose y Material Design 3.
- Colores y tipografías centralizados en `ui/theme`; los colores semánticos (éxito, advertencia, información, peligro) se usan para estados y prioridades, sin hexadecimales repetidos en las pantallas.
- Animaciones: `animateItem` (reordenamiento de tarjetas al filtrar), `animateColorAsState` (color según prioridad), `AnimatedVisibility` (errores y mensajes) y `animateContentSize` (tarjetas expandibles).
- Estados vacíos, indicadores de carga y mensajes de confirmación o error en las operaciones.

## Arquitectura

La app sigue el patrón **MVVM** con una capa de repositorios, y la interfaz está construida íntegramente con **Jetpack Compose**.

```
UI (Compose Screens)  →  ViewModel  →  Repository  →  DAO (Room)  →  SQLite
```

- **UI (`ui/`)**: pantallas Compose organizadas por módulo (login, dashboard, oportunidades, detalle, directorio, eventos, seguimiento, supervisor, perfil, navegación y tema).
- **ViewModel**: mantiene el estado de cada pantalla y lo expone con `StateFlow` y corrutinas.
- **Repository (`data/repository/`)**: aplica las reglas de negocio y de permisos antes de leer o escribir.
- **Persistencia (`data/local/`)**: base de datos local con **Room** y el driver **BundledSQLiteDriver**, con un DAO por entidad.
- **Modelo (`data/model/`)**: entidades, enumeraciones (estados, tipos, perfiles) y objetos de apoyo.
- **Utilidades (`util/`)**: permisos, validaciones, sesión, fechas, calendario, fotos y cálculos del dashboard.

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

Entidades: `Usuario`, `Organizacion`, `Contacto`, `Evento`, `Oportunidad`, `Seguimiento` y `Evidencia`, relacionadas con claves primarias, claves foráneas (`ForeignKey`) e índices:

- Un `Contacto` y un `Evento` pertenecen a una `Organizacion`.
- Una `Oportunidad` pertenece a un `Evento` y tiene un `Usuario` responsable.
- Los `Seguimiento` y las `Evidencia` cuelgan de la `Oportunidad` y registran quién los creó.
- Las claves foráneas usan `RESTRICT`, de modo que el historial de gestiones nunca se pierde.

## Tecnologías

- Kotlin
- Jetpack Compose, Material 3 y Navigation Compose
- Room 3 (con BundledSQLiteDriver) y KSP
- Corrutinas y Flow
- FileProvider, cámara, galería y calendario para los recursos nativos
- SharedPreferences para la sesión
- JUnit para las pruebas unitarias

## Instrucciones de ejecución

**Requisitos**
- Android Studio (versión reciente) con el SDK de Android instalado.
- Un emulador o un dispositivo físico con **Android 7.0 (API 24) o superior**.

**Pasos**
1. Clona el repositorio:
```bash
   git clone https://github.com/CristobalViveros/Oportunidades-Corporativas.git
```
2. En Android Studio usa **File → Open** y selecciona la carpeta **`AppProyecto`** (la que contiene `gradlew`), **no** la raíz del repositorio.
3. Espera a que termine la sincronización de Gradle (*Sync Project with Gradle Files*).
4. Selecciona un emulador o conecta un dispositivo con la depuración USB activada.
5. Presiona **Run ▶** (o `Shift + F10`).
6. Inicia sesión con alguno de los [usuarios de prueba](#usuarios-de-prueba).

**Si la app se cierra al abrir**
Si el dispositivo tiene instalada una versión anterior de la app, desinstálala y vuelve a ejecutar (o usa *Wipe Data* en el Device Manager). La base está configurada con `fallbackToDestructiveMigration`, que recrea las tablas cuando cambia la versión; esto borra los datos locales y se vuelven a cargar los datos demo.

> **Nota:** el borrado automático de la base es adecuado mientras solo existan datos de prueba. Antes de publicar la app a usuarios reales deben escribirse migraciones de Room.

## Pruebas unitarias

Cubren las reglas que no dependen de Android: validaciones, permisos y transiciones de estado por rol, filtros y cálculos del dashboard.

Para ejecutarlas: clic derecho sobre la carpeta `app/src/test` → **Run Tests**, o desde la terminal en la carpeta `AppProyecto`:

```bash
./gradlew test
```

## Flujo de trabajo

Control de versiones en GitHub, con una rama por funcionalidad, commits por cada avance y Pull Requests hacia `main`.

## Nota

Proyecto con fines educativos. Todos los nombres, correos, organizaciones y
