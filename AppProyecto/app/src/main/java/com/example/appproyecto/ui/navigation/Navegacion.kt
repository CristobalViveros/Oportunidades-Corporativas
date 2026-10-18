package com.example.appproyecto.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appproyecto.ui.login.LoginScreen
import com.example.appproyecto.util.SesionManager
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.appproyecto.ui.oportunidades.OportunidadFormScreen
import com.example.appproyecto.ui.eventos.EventoFormScreen
object Rutas {
    const val LOGIN = "login"
    const val PRINCIPAL = "principal"
    const val FORMULARIO = "formulario/{id}"
    fun formulario(id: Long) = "formulario/$id"

    const val EVENTO_NUEVO = "evento_nuevo"
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
                onNuevaOportunidad = { navController.navigate(Rutas.formulario(0L)) },
                onEditarOportunidad = { id -> navController.navigate(Rutas.formulario(id)) },
                onNuevoEvento = { navController.navigate(Rutas.EVENTO_NUEVO) }
            )
        }
        composable(
            route = Rutas.FORMULARIO,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) {
            OportunidadFormScreen(onVolver = { navController.popBackStack() })
        }
    }
}
