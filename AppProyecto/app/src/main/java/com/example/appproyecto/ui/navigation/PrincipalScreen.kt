package com.example.appproyecto.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.appproyecto.data.model.Perfil
import com.example.appproyecto.ui.dashboard.DashboardScreen
import com.example.appproyecto.ui.directorio.DirectorioScreen
import com.example.appproyecto.ui.eventos.EventosScreen
import com.example.appproyecto.ui.oportunidades.OportunidadesScreen
import com.example.appproyecto.ui.perfil.PerfilScreen
import com.example.appproyecto.ui.supervisor.PanelSupervisorScreen
import com.example.appproyecto.util.rol

enum class Pestana(val ruta: String, val titulo: String, val icono: ImageVector) {
    INICIO("inicio", "Inicio", Icons.Filled.Home),
    OPORTUNIDADES("oportunidades", "Oportunidades", Icons.Filled.Work),
    EVENTOS("eventos", "Eventos", Icons.Filled.Event),
    DIRECTORIO("directorio", "Directorio", Icons.Filled.Business),
    PANEL("panel", "Equipo", Icons.Filled.SupervisorAccount),
    PERFIL("perfil", "Perfil", Icons.Filled.Person)
}

/**
 * Cada rol ve sus propias pestañas: solo el supervisor tiene el panel de equipo.
 * El perfil no es una pestaña (para no pasar de 5 en la barra): se abre desde el ícono de Inicio.
 */
fun pestanasPara(rol: Perfil): List<Pestana> = when (rol) {
    Perfil.EJECUTIVO -> listOf(Pestana.INICIO, Pestana.OPORTUNIDADES, Pestana.EVENTOS, Pestana.DIRECTORIO)
    Perfil.SUPERVISOR -> listOf(Pestana.INICIO, Pestana.OPORTUNIDADES, Pestana.EVENTOS, Pestana.DIRECTORIO, Pestana.PANEL)
}

@Composable
fun PrincipalScreen(
    onCerrarSesion: () -> Unit,
    onNuevaOportunidad: () -> Unit,
    onAbrirOportunidad: (Long) -> Unit,
    onNuevoEvento: () -> Unit,
    onNuevaOrganizacion: () -> Unit,
    onAbrirOrganizacion: (Long) -> Unit,
    viewModel: PrincipalViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    // Si la sesión guardada ya no es válida, volvemos al login.
    LaunchedEffect(estado.sesionInvalida) {
        if (estado.sesionInvalida) onCerrarSesion()
    }

    val usuario = estado.usuario
    if (usuario == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (estado.cargando) CircularProgressIndicator()
        }
    } else {
        ContenidoPrincipal(
            rol = usuario.rol(),
            onCerrarSesion = onCerrarSesion,
            onNuevaOportunidad = onNuevaOportunidad,
            onAbrirOportunidad = onAbrirOportunidad,
            onNuevoEvento = onNuevoEvento,
            onNuevaOrganizacion = onNuevaOrganizacion,
            onAbrirOrganizacion = onAbrirOrganizacion
        )
    }
}

@Composable
private fun ContenidoPrincipal(
    rol: Perfil,
    onCerrarSesion: () -> Unit,
    onNuevaOportunidad: () -> Unit,
    onAbrirOportunidad: (Long) -> Unit,
    onNuevoEvento: () -> Unit,
    onNuevaOrganizacion: () -> Unit,
    onAbrirOrganizacion: (Long) -> Unit
) {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    Scaffold(
        // Cada pantalla interna maneja su propio TopAppBar, así evitamos espacios dobles.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            when (rutaActual) {
                Pestana.OPORTUNIDADES.ruta -> FloatingActionButton(onClick = onNuevaOportunidad) {
                    Icon(Icons.Filled.Add, contentDescription = "Nueva oportunidad")
                }
                Pestana.EVENTOS.ruta -> FloatingActionButton(onClick = onNuevoEvento) {
                    Icon(Icons.Filled.Add, contentDescription = "Nuevo evento")
                }
                Pestana.DIRECTORIO.ruta -> FloatingActionButton(onClick = onNuevaOrganizacion) {
                    Icon(Icons.Filled.Add, contentDescription = "Nueva organización")
                }
            }
        },
        bottomBar = {
            NavigationBar {
                pestanasPara(rol).forEach { pestana ->
                    NavigationBarItem(
                        selected = rutaActual == pestana.ruta,
                        onClick = {
                            navController.navigate(pestana.ruta) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(pestana.icono, contentDescription = pestana.titulo) },
                        label = { Text(pestana.titulo) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Pestana.INICIO.ruta,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Pestana.INICIO.ruta) {
                DashboardScreen(
                    onAbrirOportunidad = onAbrirOportunidad,
                    onAbrirPerfil = {
                        navController.navigate(Pestana.PERFIL.ruta) { launchSingleTop = true }
                    }
                )
            }
            composable(Pestana.OPORTUNIDADES.ruta) {
                OportunidadesScreen(onAbrirOportunidad = onAbrirOportunidad)
            }
            composable(Pestana.EVENTOS.ruta) { EventosScreen() }
            composable(Pestana.DIRECTORIO.ruta) {
                DirectorioScreen(onAbrirOrganizacion = onAbrirOrganizacion)
            }
            // La ruta del panel ni siquiera existe para un ejecutivo.
            if (rol == Perfil.SUPERVISOR) {
                composable(Pestana.PANEL.ruta) { PanelSupervisorScreen() }
            }
            composable(Pestana.PERFIL.ruta) { PerfilScreen(onCerrarSesion = onCerrarSesion) }
        }
    }
}
