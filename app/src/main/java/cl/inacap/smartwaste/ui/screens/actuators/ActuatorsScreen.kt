package cl.inacap.smartwaste.ui.screens.actuators

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
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
fun ActuatorsScreen(
    navController: NavController,
    viewModel: WasteBinViewModel
) {
    val telemetry by viewModel.telemetry.collectAsState()
    val isSending by viewModel.isSendingCommand.collectAsState()
    val alertaUrgente by viewModel.alertaUrgente.collectAsState()
    val alertaSupervisor by viewModel.alertaSupervisor.collectAsState()
    var irEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Control Remoto de Actuadores", fontWeight = FontWeight.ExtraBold, color = SlateTextPrimary) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (alertaUrgente) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PastelRoseLight,
                    border = BorderStroke(1.dp, PastelRoseBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PriorityHigh,
                            contentDescription = null,
                            tint = PastelRoseDark,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Orden de Vaciado Urgente Activa",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PastelRoseDark
                            )
                            if (alertaSupervisor.isNotBlank()) {
                                Text(
                                    text = "Despachada por: $alertaSupervisor",
                                    fontSize = 10.sp,
                                    color = SlateTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = "Control de Actuadores ESP32",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SlateTextPrimary
            )

            // Servomotores SG90
            SoftCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = 6.dp,
                pastelTint = PastelMintLight,
                borderColor = PastelMintBorder
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = PastelMintLight,
                            modifier = Modifier.size(36.dp).border(BorderStroke(1.dp, PastelMintBorder), CircleShape)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Sync, contentDescription = null, tint = PastelMintDark, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Servomotores Gemelos SG90 (GPIO 25 & 26)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = SlateTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Rota la tapa a 90° con rampa suave (1.5° cada 20ms) y la mantiene abierta durante 4 segundos antes de iniciar el descenso.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateTextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SoftButton(
                        onClick = { viewModel.triggerLidOpening(4.0) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        isPrimary = true,
                        primaryColor = if (isSending) PastelPeachPrimary else PastelMintPrimary,
                        enabled = !isSending && !telemetry.modoLimpieza,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isSending) "Enviando comando..." else "Abrir Tapa (4s)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Modo Limpieza
            SoftCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = 6.dp,
                pastelTint = PastelSkyLight,
                borderColor = PastelSkyBorder
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = PastelSkyLight,
                                modifier = Modifier.size(28.dp).border(BorderStroke(1.dp, PastelSkyBorder), CircleShape)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.CleaningServices, contentDescription = null, tint = PastelSkyDark, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Modo Limpieza / Vaciado",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = SlateTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Bloquea la tapa a 90° de forma continua para que el auxiliar reemplace la bolsa cómodamente sin cierres automáticos.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SlateTextSecondary
                        )
                    }
                    Switch(
                        checked = telemetry.modoLimpieza,
                        onCheckedChange = { viewModel.toggleCleaningMode(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = PastelSkyDark, checkedTrackColor = PastelSkyLight)
                    )
                }
            }

            // Zumbador Acústico
            SoftCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = 6.dp,
                pastelTint = PastelPeachLight,
                borderColor = PastelPeachBorder
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = PastelPeachLight,
                            modifier = Modifier.size(36.dp).border(BorderStroke(1.dp, PastelPeachBorder), CircleShape)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = PastelPeachDark, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Zumbador Acústico (Buzzer GPIO 33)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = SlateTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Acciona un pitido de confirmación en el basurero para verificar la calidad del enlace inalámbrico.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateTextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SoftButton(
                        onClick = { viewModel.testBuzzer() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        isPrimary = true,
                        primaryColor = PastelPeachPrimary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Probar Pitido", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Sensor Infrarrojo
            SoftCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = 6.dp,
                pastelTint = PastelLavenderLight,
                borderColor = PastelLavenderBorder
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = PastelLavenderLight,
                                modifier = Modifier.size(28.dp).border(BorderStroke(1.dp, PastelLavenderBorder), CircleShape)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Sensors, contentDescription = null, tint = PastelLavenderDark, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sensor Proximidad IR (GPIO 35)",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = SlateTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Habilita la detección automática de usuarios frontales a 12 cm con filtro antirebote de 80ms.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SlateTextSecondary
                        )
                    }
                    Switch(
                        checked = irEnabled,
                        onCheckedChange = {
                            irEnabled = it
                            viewModel.toggleProximitySensor(it)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = PastelLavenderDark, checkedTrackColor = PastelLavenderLight)
                    )
                }
            }
        }
    }
}
