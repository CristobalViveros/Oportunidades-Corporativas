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
import com.example.appproyecto.ui.detalle.DetalleOportunidadScreen
import com.example.appproyecto.ui.directorio.ContactoFormScreen
import com.example.appproyecto.ui.directorio.OrganizacionDetalleScreen
import com.example.appproyecto.ui.directorio.OrganizacionFormScreen
import com.example.appproyecto.ui.eventos.EventoFormScreen
import com.example.appproyecto.ui.seguimiento.SeguimientoFormScreen
object Rutas {
    const val LOGIN = "login"
    const val PRINCIPAL = "principal"
    const val FORMULARIO = "formulario/{id}"
    fun formulario(id: Long) = "formulario/$id"

    const val DETALLE = "detalle/{id}"
    fun detalle(id: Long) = "detalle/$id"

    const val SEGUIMIENTO = "seguimiento/{id}"
    fun seguimiento(id: Long) = "seguimiento/$id"

    const val EVENTO_NUEVO = "evento_nuevo"

    /** id = 0 crea una organización nueva; otro valor la edita. */
    const val ORGANIZACION_FORM = "organizacion_form/{id}"
    fun organizacionForm(id: Long) = "organizacion_form/$id"

    const val ORGANIZACION = "organizacion/{id}"
    fun organizacion(id: Long) = "organizacion/$id"

    /** contactoId = 0 crea un contacto nuevo en la organización; otro valor lo edita. */
    const val CONTACTO_FORM = "contacto_form/{orgId}/{id}"
    fun contactoForm(orgId: Long, id: Long) = "contacto_form/$orgId/$id"
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
                onAbrirOportunidad = { id -> navController.navigate(Rutas.detalle(id)) },
                onNuevoEvento = { navController.navigate(Rutas.EVENTO_NUEVO) },
                onNuevaOrganizacion = { navController.navigate(Rutas.organizacionForm(0L)) },
                onAbrirOrganizacion = { id -> navController.navigate(Rutas.organizacion(id)) }
            )
        }
        composable(
            route = Rutas.DETALLE,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) {
            DetalleOportunidadScreen(
                onVolver = { navController.popBackStack() },
                onEditar = { id -> navController.navigate(Rutas.formulario(id)) },
                onRegistrarGestion = { id -> navController.navigate(Rutas.seguimiento(id)) }
            )
        }
        composable(
            route = Rutas.SEGUIMIENTO,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) {
            SeguimientoFormScreen(onVolver = { navController.popBackStack() })
        }
        composable(Rutas.EVENTO_NUEVO) {
            EventoFormScreen(onVolver = { navController.popBackStack() })
        }
        composable(
            route = Rutas.FORMULARIO,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) {
            OportunidadFormScreen(onVolver = { navController.popBackStack() })
        }
        composable(
            route = Rutas.ORGANIZACION,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) {
            OrganizacionDetalleScreen(
                onVolver = { navController.popBackStack() },
                onEditar = { id -> navController.navigate(Rutas.organizacionForm(id)) },
                onNuevoContacto = { orgId -> navController.navigate(Rutas.contactoForm(orgId, 0L)) },
                onEditarContacto = { orgId, id -> navController.navigate(Rutas.contactoForm(orgId, id)) }
            )
        }
        composable(
            route = Rutas.ORGANIZACION_FORM,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) {
            OrganizacionFormScreen(onVolver = { navController.popBackStack() })
        }
        composable(
            route = Rutas.CONTACTO_FORM,
            arguments = listOf(
                navArgument("orgId") { type = NavType.LongType },
                navArgument("id") { type = NavType.LongType }
            )
        ) {
            ContactoFormScreen(onVolver = { navController.popBackStack() })
        }
    }
}
