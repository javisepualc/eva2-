package cl.inacap.smartwaste.ui.screens.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import cl.inacap.smartwaste.ui.theme.RedDark
import cl.inacap.smartwaste.ui.theme.RedDanger
import cl.inacap.smartwaste.viewmodel.WasteBinViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TiltAlarmScreen(
    navController: NavController,
    viewModel: WasteBinViewModel
) {
    val telemetry by viewModel.telemetry.collectAsState()
    val currentTime = remember {
        SimpleDateFormat("HH:mm:ss - dd/MM/yyyy", Locale.getDefault()).format(Date())
    }

    // Si ya no está volcado, regresar automáticamente
    LaunchedEffect(telemetry.volcado) {
        if (!telemetry.volcado) {
            navController.popBackStack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RedDark)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icono de Alerta Roja
                Surface(
                    shape = RoundedCornerShape(50),
                    color = RedDanger.copy(alpha = 0.15f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = RedDanger,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Text(
                    text = "Alarma de Seguridad",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = RedDanger
                )

                Text(
                    text = "Contenedor Volcado",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = "El sensor de inclinación KY-027 (GPIO 4) detectó pérdida total de verticalidad.\n\n" +
                            "Mecanismo de seguridad activado:\n" +
                            "• Tapa bloqueada automáticamente para evitar derrames.\n" +
                            "• Alarma acústica activada en el zumbador (GPIO 33).",
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )

                // Detalle del Incidente
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("📍 Ubicación: Pabellón A - Piso 2", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text("🕒 Registro: $currentTime", fontSize = 11.sp, color = Color.Gray)
                        Text("📡 Sensor: KY-027 (GPIO 4: Low State)", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Silenciar zumbador
                OutlinedButton(
                    onClick = { viewModel.clearFeedback() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeOff, contentDescription = null, tint = RedDanger)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Silenciar Zumbador", color = RedDanger, fontWeight = FontWeight.Bold)
                }

                // Confirmar reposición
                Button(
                    onClick = { viewModel.confirmReposition() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RedDanger)
                ) {
                    Text("Confirmar Reposición", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
