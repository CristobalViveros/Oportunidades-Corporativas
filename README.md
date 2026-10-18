# Gestión móvil de oportunidades y activaciones corporativas

Aplicación Android (MVP) para una agencia de marketing y beneficios corporativos. Permite registrar y dar seguimiento a oportunidades de activación en eventos, ferias y organizaciones. Es un proyecto académico: **todos los datos son ficticios** y no existen integraciones reales.

**Asignatura:** DSY1105 – Desarrollo de Aplicaciones Móviles
**Evaluación:** Parcial 2 (Encargo) – Duoc UC

## Integrantes

| Nombre | Usuario GitHub |
|---|---|
| Cristóbal Viveros | [CristobalViveros](https://github.com/CristobalViveros) |
| Ignacio Espinoza | [IgnacioEspinoza333](https://github.com/IgnacioEspinoza333) |

## Funcionalidades

**Implementadas**
- **Autenticación ficticia:** inicio de sesión con validación por campo (correo `@...cl` y contraseña de mínimo 8 caracteres), contraseña almacenada como hash SHA-256 y sesión persistente.
- **Perfil:** datos del usuario en sesión (perfil, área y zona) y cierre de sesión.
- **Oportunidades:**
  - Lista con tarjetas de color según prioridad.
  - Búsqueda por nombre, evento, organización o comuna.
  - Filtros por prioridad y estado.
  - Crear y editar una oportunidad desde un formulario con validación visual.
- **Datos relacionados:** eventos, organizaciones y contactos ficticios, cargados automáticamente en el primer arranque.
- **Animaciones:** reordenamiento de tarjetas al filtrar, cambio gradual de color por prioridad y aparición suave de mensajes.
- **Persistencia local** con Room (SQLite): la app funciona sin conexión.

**Recursos nativos del dispositivo**
- [Completar al terminar: cámara y galería para la foto del evento.]

**Fuera del alcance de esta entrega** (quedan como trabajo futuro)
- Seguimiento con historial de acciones y cambios de estado
- Alertas de seguimientos pendientes
- Gestión de eventos, organizaciones y contactos desde formularios
- Perfiles supervisor y marketing con permisos diferenciados

## Tecnologías

- Kotlin y Jetpack Compose (Material Design 3)
- Navigation Compose (navegación principal + barra inferior)
- Arquitectura **MVVM** con `StateFlow` y `UiState`
- Room 3 con KSP y `BundledSQLiteDriver`
- Validaciones centralizadas en `util/Validaciones.kt`, desacopladas de la interfaz

## Estructura del proyecto

```
app/src/main/java/com/example/appproyecto/
├── data/
│   ├── local/        DAOs y AppDatabase (Room)
│   ├── model/        Entidades, enums y modelos de lectura
│   └── repository/   Repositorios
├── ui/
│   ├── login/        Pantalla y ViewModel de inicio de sesión
│   ├── oportunidades/  Lista, filtros y formulario
│   ├── perfil/       Perfil de usuario
│   └── navigation/   Rutas y barra de navegación inferior
└── util/             Validaciones, filtros, fechas y sesión
```

## Cómo ejecutar el proyecto

**Requisitos:** Android Studio (versión reciente), JDK incluido con Android Studio y un emulador o dispositivo con Android 8.0 (API 26) o superior.

1. Clonar el repositorio:
```
   git clone https://github.com/CristobalViveros/Oportunidades-Corporativas.git
```
2. En Android Studio: **File → Open** y seleccionar la carpeta **`AppProyecto`** (la que contiene `gradlew`, no la raíz del repositorio).
3. Esperar a que termine la sincronización de Gradle.
4. Seleccionar un emulador o dispositivo y presionar **Run ▶**.

**Usuario de prueba (ficticio):**

| Correo | Contraseña |
|---|---|
| `ana.ejecutiva@ejemplo.cl` | `Ana12345` |

Si cambia el esquema de la base de datos entre versiones, desinstalar la app o usar *Wipe Data* en el emulador antes de ejecutar.

## Flujo de trabajo

- Control de versiones en GitHub, con una rama por funcionalidad y Pull Requests hacia `main`.
- Gestión de tareas en Trello: [enlace al tablero]

## Nota

Proyecto con fines educativos. Todos los nombres, correos y organizaciones son ficticios.
