package com.example.appproyecto.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appproyecto.ui.login.LoginScreen
import com.example.appproyecto.util.SesionManager

object Rutas {
    const val LOGIN = "login"
    const val PRINCIPAL = "principal"
}

@Composable
fun Navegacion() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Si ya hay sesión guardada, saltamos el login.
    val destinoInicial = remember {
        if (SesionManager(context).usuarioId() != null) Rutas.PRINCIPAL else Rutas.LOGIN
    }

    NavHost(navController = navController, startDestination = destinoInicial) {
        composable(Rutas.LOGIN) {
            LoginScreen(
                onLoginExitoso = {
                    navController.navigate(Rutas.PRINCIPAL) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Rutas.PRINCIPAL) {
            PrincipalScreen(
                onCerrarSesion = {
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(Rutas.PRINCIPAL) { inclusive = true }
                    }
                },
                onNuevaOportunidad = {
                    // Se conecta con el formulario en la próxima tanda.
                }
            )
        }
    }
}