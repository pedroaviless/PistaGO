package me.nacimiento.pistago

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import me.nacimiento.pistago.presentation.navigation.Routes
import me.nacimiento.pistago.presentation.screens.auth.LoginScreen
import me.nacimiento.pistago.presentation.screens.auth.RegisterScreen
import me.nacimiento.pistago.presentation.screens.home.HomeScreen
import me.nacimiento.pistago.presentation.screens.perfil.PerfilScreen
import me.nacimiento.pistago.presentation.screens.pistas.PistasScreen
import me.nacimiento.pistago.presentation.screens.reservas.MisReservasScreen
import me.nacimiento.pistago.presentation.screens.reservas.ReservarScreen
import me.nacimiento.pistago.ui.theme.PistaGOTheme
import com.google.firebase.messaging.FirebaseMessaging
import me.nacimiento.pistago.presentation.screens.espera.ListaEsperaScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                android.util.Log.d("FCM_TOKEN", "Token: $token")
            }
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PistaGOTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Routes.LOGIN
                ) {
                    composable(Routes.LOGIN) {
                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigate(Routes.HOME) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            },
                            onNavigateToRegister = {
                                navController.navigate(Routes.REGISTER)
                            }
                        )
                    }

                    composable(Routes.REGISTER) {
                        RegisterScreen(
                            onRegisterSuccess = {
                                navController.navigate(Routes.HOME) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            },
                            onNavigateToLogin = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(Routes.HOME) {
                        HomeScreen(
                            onNavigateToPistas = { navController.navigate(Routes.PISTAS) },
                            onNavigateToMisReservas = { navController.navigate(Routes.MIS_RESERVAS) },
                            onNavigateToPerfil = { navController.navigate(Routes.PERFIL) },
                            onNavigateToListaEspera = { navController.navigate(Routes.LISTA_ESPERA) }
                        )
                    }

                    composable(Routes.PISTAS) {
                        PistasScreen(
                            onPistaClick = { pistaId ->
                                navController.navigate(Routes.reservar(pistaId))
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Routes.RESERVAR) { backStackEntry ->
                        val pistaId = backStackEntry.arguments?.getString("pistaId")?.toLong() ?: 0L
                        ReservarScreen(
                            pistaId = pistaId,
                            onReservaCreada = {
                                navController.navigate(Routes.MIS_RESERVAS) {
                                    popUpTo(Routes.HOME)
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Routes.MIS_RESERVAS) {
                        MisReservasScreen(onBack = { navController.popBackStack() })
                    }

                    composable(Routes.LISTA_ESPERA) {
                        ListaEsperaScreen()
                    }

                    composable(Routes.PERFIL) {
                        PerfilScreen(
                            onLogout = {
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(Routes.HOME) { inclusive = true }
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}