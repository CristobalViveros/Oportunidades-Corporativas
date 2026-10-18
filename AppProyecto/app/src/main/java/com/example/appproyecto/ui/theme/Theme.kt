package com.example.appproyecto.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val EsquemaClaro = lightColorScheme(
    primary = Azul40,
    onPrimary = Blanco,
    primaryContainer = Azul90,
    onPrimaryContainer = Azul10,
    secondary = Gris40,
    onSecondary = Blanco,
    secondaryContainer = Gris90,
    onSecondaryContainer = Gris10,
    tertiary = Turquesa40,
    onTertiary = Blanco,
    tertiaryContainer = Turquesa90,
    onTertiaryContainer = Turquesa10,
    error = Rojo40,
    onError = Blanco,
    errorContainer = Rojo90,
    onErrorContainer = Rojo10,
    background = Neutro99,
    onBackground = Neutro10,
    surface = Neutro99,
    onSurface = Neutro10,
    surfaceVariant = NeutroVar90,
    onSurfaceVariant = NeutroVar30,
    outline = NeutroVar50,
    outlineVariant = NeutroVar80
)

private val EsquemaOscuro = darkColorScheme(
    primary = Azul80,
    onPrimary = Azul20,
    primaryContainer = Azul30,
    onPrimaryContainer = Azul90,
    secondary = Gris80,
    onSecondary = Gris30,
    secondaryContainer = Gris30,
    onSecondaryContainer = Gris90,
    tertiary = Turquesa80,
    onTertiary = Turquesa30,
    tertiaryContainer = Turquesa30,
    onTertiaryContainer = Turquesa90,
    error = Rojo80,
    onError = Rojo10,
    errorContainer = Rojo40,
    onErrorContainer = Rojo90,
    background = Neutro10,
    onBackground = Neutro90,
    surface = Neutro10,
    onSurface = Neutro90,
    surfaceVariant = NeutroVar30,
    onSurfaceVariant = NeutroVar80,
    outline = NeutroVar60,
    outlineVariant = NeutroVar30
)

/**
 * Tema de la app. No usa "color dinámico" del sistema a propósito: así los colores de marca y los
 * semánticos (estados, éxito, advertencia) se ven igual en todos los teléfonos.
 */
@Composable
fun AppProyectoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalColoresApp provides (if (darkTheme) ColoresAppOscuro else ColoresAppClaro)
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
            typography = Typography,
            content = content
        )
    }
}
