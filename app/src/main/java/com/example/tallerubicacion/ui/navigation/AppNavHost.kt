package com.example.tallerubicacion.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.tallerubicacion.ui.screens.LoginScreen
import com.example.tallerubicacion.ui.screens.MapaScreen
import com.example.tallerubicacion.ui.screens.PerfilScreen
import com.example.tallerubicacion.ui.screens.RegistroScreen
import com.example.tallerubicacion.viewmodels.AuthViewModel
import com.example.tallerubicacion.viewmodels.MapaViewModel
import com.example.tallerubicacion.viewmodels.PerfilViewModel

@Composable
fun AppNavHost() {
    // Se crean aqui (ligados a la Activity) para que el rastreo siga activo al ir al perfil
    val authViewModel: AuthViewModel = viewModel()
    val mapaViewModel: MapaViewModel = viewModel()
    val perfilViewModel: PerfilViewModel = viewModel()

    val inicio = if (authViewModel.estaAutenticado()) MapaRoute else LoginRoute
    val backStack = rememberNavBackStack(inicio)

    val irAlMapa: () -> Unit = {
        backStack.clear()
        backStack.add(MapaRoute)
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = { key ->
            when (key) {
                is LoginRoute -> NavEntry(key) {
                    LoginScreen(
                        viewModel = authViewModel,
                        onLoginExitoso = irAlMapa,
                        onCrearCuenta = { backStack.add(RegistroRoute) }
                    )
                }

                is RegistroRoute -> NavEntry(key) {
                    RegistroScreen(
                        viewModel = authViewModel,
                        onRegistroExitoso = irAlMapa,
                        onVolver = { backStack.removeLastOrNull() }
                    )
                }

                is MapaRoute -> NavEntry(key) {
                    MapaScreen(
                        viewModel = mapaViewModel,
                        onPerfil = { backStack.add(PerfilRoute(authViewModel.uidActual())) },
                        onCerrarSesion = {
                            backStack.clear()
                            backStack.add(LoginRoute)
                        }
                    )
                }

                is PerfilRoute -> NavEntry(key) {
                    PerfilScreen(
                        uid = key.uid,
                        viewModel = perfilViewModel,
                        onVolver = { backStack.removeLastOrNull() }
                    )
                }

                else -> NavEntry(key) { }
            }
        }
    )
}
