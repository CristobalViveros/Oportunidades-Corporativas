package com.example.appproyecto.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appproyecto.ui.home.HomeScreen
import com.example.appproyecto.ui.login.LoginScreen
import com.example.appproyecto.util.SesionManager

object Rutas {
    const val LOGIN = "login"
    const val HOME = "home"
}

@Composable
fun Navegacion() {
    val navController = rememberNavController()
    val sesion = SesionManager(LocalContext.current)

    // Si ya hay sesión guardada, saltamos el login.
    val destinoInicial = remember {
        if (sesion.usuarioId() != null) Rutas.HOME else Rutas.LOGIN
    }

    NavHost(navController = navController, startDestination = destinoInicial) {
        composable(Rutas.LOGIN) {
            LoginScreen(
                onLoginExitoso = {
                    navController.navigate(Rutas.HOME) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Rutas.HOME) {
            HomeScreen(
                onCerrarSesion = {
                    sesion.cerrar()
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(Rutas.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}