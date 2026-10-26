package cl.inacap.smartwaste.ui.screens.diagnosis

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import cl.inacap.smartwaste.ui.theme.*
import cl.inacap.smartwaste.viewmodel.WasteBinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosisScreen(
    navController: NavController,
    viewModel: WasteBinViewModel
) {
    val telemetry by viewModel.telemetry.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diagnóstico Técnico de Sensores", fontWeight = FontWeight.ExtraBold, color = SlateTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = SlateTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PastelCanvas)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PastelCanvas)
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Estado de Sensores y Pines GPIO",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SlateTextPrimary
            )

            // Sensor HC-SR04
            SensorDiagnosisCard(
                sensorName = "Ultrasónico HC-SR04",
                pins = "GPIO 13 (TRIG) • GPIO 14 (ECHO 2.5V)",
                currentValue = "${telemetry.distanciaCm} cm (${telemetry.llenadoPorcentaje}%)",
                details = "Algoritmo de 9 muestras con mediana e histéresis (<15%). Divisor con resistencias de 3.3kΩ.",
                statusOk = true,
                pastelTint = PastelMintLight,
                borderColor = PastelMintBorder,
                accentColor = PastelMintDark
            )

            // Sensor DHT11
            SensorDiagnosisCard(
                sensorName = "DHT11 (Temperatura & Humedad)",
                pins = "GPIO 27 (Bus Digital One-Wire)",
                currentValue = "${telemetry.temperatura} °C • ${telemetry.humedad}% HR",
                details = "Cruce de datos para indicios de descomposición bacteriana activa.",
                statusOk = true,
                pastelTint = PastelPeachLight,
                borderColor = PastelPeachBorder,
                accentColor = PastelPeachDark
            )

            // Sensor MQ de Gas
            SensorDiagnosisCard(
                sensorName = "Sensor de Gas MQ (Lectura Cruda)",
                pins = "GPIO 32 (Canal ADC1 - Entrada Analógica)",
                currentValue = "${telemetry.gasADC} ADC (Base: ${telemetry.lineaBaseGas})",
                details = "Calibración automática al arranque (40 muestras en aire limpio). Delta actual: +${telemetry.gasADC - telemetry.lineaBaseGas} ADC.",
                statusOk = true,
                pastelTint = PastelLavenderLight,
                borderColor = PastelLavenderBorder,
                accentColor = PastelLavenderDark
            )

            // Sensor KY-027
            SensorDiagnosisCard(
                sensorName = "KY-027 Sensor de Inclinación",
                pins = "GPIO 4 (Entrada Digital con Interrupción)",
                currentValue = if (telemetry.volcado) "Inclinado / Volcado" else "Vertical (Normal)",
                details = "Detección inmediata de volcamiento accidental con bloqueo preventivo de tapa.",
                statusOk = !telemetry.volcado,
                pastelTint = if (telemetry.volcado) PastelRoseLight else PastelButterLight,
                borderColor = if (telemetry.volcado) PastelRoseBorder else PastelButterBorder,
                accentColor = if (telemetry.volcado) PastelRoseDark else PastelButterDark
            )

            // Sensor FC-51
            SensorDiagnosisCard(
                sensorName = "FC-51 Proximidad Infrarroja",
                pins = "GPIO 35 (Entrada Digital)",
                currentValue = if (telemetry.presenciaDetectada) "Usuario detectado (<12cm)" else "En reposo",
                details = "Disparo ajustado a 12 cm con antirebote por software de 80 ms.",
                statusOk = true,
                pastelTint = PastelSkyLight,
                borderColor = PastelSkyBorder,
                accentColor = PastelSkyDark
            )

            // Parámetros de calibración
            SoftCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("⚙️ Parámetros de Calibración Registrados", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateTextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Distancia en vacío: 21.0 cm", fontSize = 12.sp, color = SlateTextSecondary)
                    Text("• Distancia en lleno: 5.0 cm (fuera de zona muerta de 3cm)", fontSize = 12.sp, color = SlateTextSecondary)
                    Text("• Ángulos de servomotores: 0° cerrado, 90° abierto", fontSize = 12.sp, color = SlateTextSecondary)
                    Text("• MAC del ESP32: 78:1C:3C:B7:72:70", fontSize = 12.sp, color = SlateTextSecondary)
                }
            }
        }
    }
}

@Composable
fun SensorDiagnosisCard(
    sensorName: String,
    pins: String,
    currentValue: String,
    details: String,
    statusOk: Boolean,
    pastelTint: Color = PastelSurface,
    borderColor: Color = PastelWellBorder,
    accentColor: Color = SlateTextPrimary
) {
    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = 5.dp,
        pastelTint = pastelTint,
        borderColor = borderColor
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(sensorName, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = SlateTextPrimary)
                Surface(
                    color = if (statusOk) PastelMintLight else PastelRoseLight,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.border(
                        BorderStroke(1.dp, if (statusOk) PastelMintBorder else PastelRoseBorder),
                        RoundedCornerShape(50)
                    )
                ) {
                    Text(
                        text = if (statusOk) "OPERATIVO" else "ALERTA",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = if (statusOk) PastelMintDark else PastelRoseDark,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Text(pins, fontSize = 11.sp, color = accentColor, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Lectura actual: $currentValue", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateTextPrimary)
            Text(details, fontSize = 11.sp, color = SlateTextSecondary)
        }
    }
}
