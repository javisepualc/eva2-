package cl.inacap.smartwaste.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cl.inacap.smartwaste.ui.screens.actuators.ActuatorsScreen
import cl.inacap.smartwaste.ui.screens.alarm.TiltAlarmScreen
import cl.inacap.smartwaste.ui.screens.auth.ForgotPasswordScreen
import cl.inacap.smartwaste.ui.screens.auth.LoginScreen
import cl.inacap.smartwaste.ui.screens.auth.RegisterScreen
import cl.inacap.smartwaste.ui.screens.dashboard.DashboardScreen
import cl.inacap.smartwaste.ui.screens.diagnosis.DiagnosisScreen
import cl.inacap.smartwaste.ui.screens.history.HistoryScreen
import cl.inacap.smartwaste.ui.screens.settings.SettingsScreen
import cl.inacap.smartwaste.ui.screens.supervisor.SupervisorScreen
import cl.inacap.smartwaste.viewmodel.AuthViewModel
import cl.inacap.smartwaste.viewmodel.WasteBinViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    wasteBinViewModel: WasteBinViewModel,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // PANTALLAS DE AUTENTICACIÓN
        composable(Screen.Login.route) {
            LoginScreen(navController = navController, viewModel = authViewModel)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController, viewModel = authViewModel)
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController, viewModel = authViewModel)
        }

        // PANTALLAS PRINCIPALES DEL SISTEMA IOT
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                navController = navController,
                authViewModel = authViewModel,
                wasteBinViewModel = wasteBinViewModel
            )
        }
        composable(Screen.SupervisorDashboard.route) {
            SupervisorScreen(
                navController = navController,
                authViewModel = authViewModel,
                wasteBinViewModel = wasteBinViewModel
            )
        }
        composable(Screen.Actuators.route) {
            ActuatorsScreen(navController = navController, viewModel = wasteBinViewModel)
        }
        composable(Screen.Diagnosis.route) {
            DiagnosisScreen(navController = navController, viewModel = wasteBinViewModel)
        }
        composable(Screen.TiltAlarm.route) {
            TiltAlarmScreen(navController = navController, viewModel = wasteBinViewModel)
        }
        composable(Screen.History.route) {
            HistoryScreen(navController = navController, viewModel = wasteBinViewModel)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}
