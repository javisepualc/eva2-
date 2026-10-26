package cl.inacap.smartwaste.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Dashboard : Screen("dashboard")
    object SupervisorDashboard : Screen("supervisor_dashboard")
    object Actuators : Screen("actuators")
    object Diagnosis : Screen("diagnosis")
    object TiltAlarm : Screen("tilt_alarm")
    object History : Screen("history")
    object Settings : Screen("settings")
}
