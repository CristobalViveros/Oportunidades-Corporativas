package com.example.appproyecto.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.appproyecto.ui.oportunidades.OportunidadesScreen
import com.example.appproyecto.ui.perfil.PerfilScreen
import com.example.appproyecto.ui.eventos.EventosScreen
import androidx.compose.material.icons.filled.Event

enum class Pestana(val ruta: String, val titulo: String, val icono: ImageVector) {
    OPORTUNIDADES("oportunidades", "Oportunidades", Icons.Filled.Work),
    EVENTOS("eventos", "Eventos", Icons.Filled.Event),
    PERFIL("perfil", "Perfil", Icons.Filled.Person)
}

@Composable
fun PrincipalScreen(
    onCerrarSesion: () -> Unit,
    onNuevaOportunidad: () -> Unit,
    onEditarOportunidad: (Long) -> Unit,
    onNuevoEvento: () -> Unit
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
            }
        },
        bottomBar = {
            NavigationBar {
                Pestana.entries.forEach { pestana ->
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
            startDestination = Pestana.OPORTUNIDADES.ruta,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Pestana.OPORTUNIDADES.ruta) { OportunidadesScreen(onEditar = onEditarOportunidad) }
            composable(Pestana.PERFIL.ruta) { PerfilScreen(onCerrarSesion = onCerrarSesion) }
            composable(Pestana.EVENTOS.ruta) { EventosScreen() }
        }
    }
}