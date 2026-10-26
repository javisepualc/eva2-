package cl.inacap.smartwaste.ui.screens.supervisor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import cl.inacap.smartwaste.ui.navigation.Screen
import cl.inacap.smartwaste.ui.theme.*
import cl.inacap.smartwaste.viewmodel.AuthViewModel
import cl.inacap.smartwaste.viewmodel.WasteBinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    wasteBinViewModel: WasteBinViewModel
) {
    val telemetry by wasteBinViewModel.telemetry.collectAsState()
    val historyList by wasteBinViewModel.history.collectAsState()
    val feedbackMessage by wasteBinViewModel.feedbackMessage.collectAsState()
    val alertaUrgente by wasteBinViewModel.alertaUrgente.collectAsState()
    val alertaSupervisor by wasteBinViewModel.alertaSupervisor.collectAsState()
    val umbralAlerta by wasteBinViewModel.umbralAlerta.collectAsState()
    val buzzerSilenciado by wasteBinViewModel.buzzerSilenciado.collectAsState()

    val currentUser = authViewModel.currentUser
    val supervisorName = currentUser?.nombre ?: "Supervisor de Turno"

    // Calcular métricas de turno
    val vaciadosHoy = historyList.count { item ->
        val calItem = Calendar.getInstance().apply { timeInMillis = item.fechaHora }
        val calNow = Calendar.getInstance()
        calItem.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) &&
                calItem.get(Calendar.DAY_OF_YEAR) == calNow.get(Calendar.DAY_OF_YEAR)
    }

    val ultimoVaciado = historyList.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Supervisión de Campus",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = SlateTextPrimary
                        )
                        Text(
                            text = "Pabellón A • Piso 2",
                            fontSize = 11.sp,
                            color = SlateTextSecondary
                        )
                    }
                },
                actions = {
                    // Botón para alternar a vista de operario
                    SoftIconButton(
                        onClick = { navController.navigate(Screen.Dashboard.route) },
                        tint = PastelMintLight.copy(alpha = 0.6f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Vista Operario",
                            tint = PastelMintDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Ajustes generales
                    SoftIconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuración",
                            tint = SlateTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Cerrar sesión
                    SoftIconButton(
                        onClick = {
                            authViewModel.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        tint = PastelRoseLight.copy(alpha = 0.5f)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar Sesión",
                            tint = PastelRoseDark,
                            modifier = Modifier.size(20.dp)
                        )
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
            // TARJETA DE BIENVENIDA Y PERFIL
            SoftCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = 6.dp,
                pastelTint = PastelSkyLight.copy(alpha = 0.35f),
                borderColor = PastelSkyBorder
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PastelSkyLight,
                        modifier = Modifier
                            .size(46.dp)
                            .border(BorderStroke(1.dp, PastelSkyBorder), CircleShape)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = PastelSkyDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = supervisorName,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = SlateTextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = PastelSkyLight,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = "Supervisor / Encargado",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PastelSkyDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (telemetry.isHardwareConnected) "• Sede en línea" else "• En espera",
                                fontSize = 11.sp,
                                color = if (telemetry.isHardwareConnected) PastelMintDark else SlateTextSecondary
                            )
                        }
                    }
                }
            }

            // MENSAJE DE FEEDBACK REACTIVO
            feedbackMessage?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PastelSkyLight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.dp, PastelSkyBorder), RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = PastelSkyDark, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(msg, fontSize = 12.sp, color = PastelSkyDark, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // BANNER DE ALERTA URGENTE ACTIVA (SI CORRESPONDE)
            if (alertaUrgente) {
                SoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    elevation = 6.dp,
                    pastelTint = PastelRoseLight,
                    borderColor = PastelRoseBorder
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PriorityHigh,
                                contentDescription = null,
                                tint = PastelRoseDark,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Alerta de Vaciado Urgente Activa",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = PastelRoseDark
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Se ha notificado al personal de aseo que el contenedor requiere atención inmediata.",
                            fontSize = 12.sp,
                            color = SlateTextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        SoftButton(
                            onClick = { wasteBinViewModel.cancelarAlertaUrgente() },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            isPrimary = true,
                            primaryColor = PastelRosePrimary,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancelar Alerta Urgente", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // MÓDULO 1: KPIS Y RENDIMIENTO DEL TURNO
            Text(
                text = "Indicadores Clave del Turno",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SlateTextPrimary
            )

            // Cuadrícula 2x2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // KPI 1: Vaciados Hoy (Pastel Menta)
                SoftCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    elevation = 5.dp,
                    pastelTint = PastelMintLight,
                    borderColor = PastelMintBorder
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = PastelMintLight,
                                modifier = Modifier.size(28.dp).border(BorderStroke(1.dp, PastelMintBorder), CircleShape)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PastelMintDark, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Vaciados Hoy", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PastelMintDark)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("$vaciadosHoy", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = PastelMintDark)
                        Text("Registros en Firestore", fontSize = 10.sp, color = SlateTextSecondary)
                    }
                }

                // KPI 2: Nivel Actual (Pastel Cielo)
                SoftCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    elevation = 5.dp,
                    pastelTint = PastelSkyLight,
                    borderColor = PastelSkyBorder
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = PastelSkyLight,
                                modifier = Modifier.size(28.dp).border(BorderStroke(1.dp, PastelSkyBorder), CircleShape)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = PastelSkyDark, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Capacidad", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PastelSkyDark)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val fillPct = if (telemetry.isHardwareConnected) "${telemetry.llenadoPorcentaje}%" else "--%"
                        Text(fillPct, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = PastelSkyDark)
                        Text(
                            text = if (telemetry.isHardwareConnected) "Dist: ${telemetry.distanciaCm} cm" else "Sin enlace",
                            fontSize = 10.sp,
                            color = SlateTextSecondary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // KPI 3: Último Vaciado (Pastel Lavanda)
                SoftCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    elevation = 5.dp,
                    pastelTint = PastelLavenderLight,
                    borderColor = PastelLavenderBorder
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = PastelLavenderLight,
                                modifier = Modifier.size(28.dp).border(BorderStroke(1.dp, PastelLavenderBorder), CircleShape)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = PastelLavenderDark, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Último Vaciado", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PastelLavenderDark)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (ultimoVaciado != null) {
                            val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ultimoVaciado.fechaHora))
                            Text(timeStr, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = PastelLavenderDark)
                            Text(ultimoVaciado.usuarioNombre.take(15), fontSize = 10.sp, color = SlateTextSecondary, fontWeight = FontWeight.Medium)
                        } else {
                            Text("--:--", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = SlateTextPrimary)
                            Text("Sin registros", fontSize = 10.sp, color = SlateTextSecondary)
                        }
                    }
                }

                // KPI 4: Estado Ambiental (Pastel Durazno)
                SoftCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    elevation = 5.dp,
                    pastelTint = PastelPeachLight,
                    borderColor = PastelPeachBorder
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = PastelPeachLight,
                                modifier = Modifier.size(28.dp).border(BorderStroke(1.dp, PastelPeachBorder), CircleShape)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Thermostat, contentDescription = null, tint = PastelPeachDark, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ambiente", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PastelPeachDark)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val tempStr = if (telemetry.isHardwareConnected) "${telemetry.temperatura}°C" else "--°C"
                        Text(tempStr, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = PastelPeachDark)
                        Text(
                            text = if (telemetry.isHardwareConnected) "Gas: ${telemetry.gasADC} ADC" else "Sin lectura",
                            fontSize = 10.sp,
                            color = SlateTextSecondary
                        )
                    }
                }
            }

            // MÓDULO 2: GESTIÓN DE DESPACHO
            SoftCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = 6.dp,
                pastelTint = if (alertaUrgente) PastelRoseLight.copy(alpha = 0.35f) else PastelPeachLight.copy(alpha = 0.35f),
                borderColor = if (alertaUrgente) PastelRoseBorder else PastelPeachBorder
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Gestión de Despacho y Alertas",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = SlateTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Emite una orden de vaciado prioritario para coordinar con el personal de aseo asignado a este pabellón.",
                        fontSize = 12.sp,
                        color = SlateTextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    SoftButton(
                        onClick = {
                            if (alertaUrgente) {
                                wasteBinViewModel.cancelarAlertaUrgente()
                            } else {
                                wasteBinViewModel.solicitarVaciadoUrgente(supervisorName)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        isPrimary = true,
                        primaryColor = if (alertaUrgente) PastelRosePrimary else PastelPeachPrimary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (alertaUrgente) Icons.Default.Close else Icons.Default.NotificationImportant,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (alertaUrgente) "Cancelar Orden de Vaciado" else "Solicitar Vaciado Urgente",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Botón secundario para ver diagnóstico de sensores
                    OutlinedButton(
                        onClick = { navController.navigate(Screen.Diagnosis.route) },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.2.dp, PastelSkyBorder)
                    ) {
                        Icon(Icons.Default.Sensors, contentDescription = null, tint = PastelSkyDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ver Estado de Sensores y Pines GPIO", color = PastelSkyDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // MÓDULO 3: AUDITORÍA Y TRAZABILIDAD
            SoftCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = 5.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bitácora de Intervenciones",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = SlateTextPrimary
                        )
                        TextButton(onClick = { navController.navigate(Screen.History.route) }) {
                            Text("Ver todo", fontSize = 12.sp, color = PastelSkyDark, fontWeight = FontWeight.Bold)
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = PastelSkyDark, modifier = Modifier.size(14.dp))
                        }
                    }

                    if (historyList.isEmpty()) {
                        Text(
                            text = "Aún no se registran vaciados en Firestore.",
                            fontSize = 12.sp,
                            color = SlateTextSecondary,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            historyList.take(3).forEach { item ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = PastelWell.copy(alpha = 0.4f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = item.usuarioNombre,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = SlateTextPrimary
                                            )
                                            val dateStr = SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault()).format(Date(item.fechaHora))
                                            Text(dateStr, fontSize = 11.sp, color = SlateTextSecondary)
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = PastelMintLight
                                        ) {
                                            Text(
                                                text = "Vaciado al ${item.porcentajePrevio}%",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PastelMintDark,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // MÓDULO 4: CONTROL DE UMBRALES Y PARÁMETROS OPERATIVOS
            var sliderValue by remember(umbralAlerta) { mutableFloatStateOf(umbralAlerta.toFloat()) }

            SoftCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = 6.dp,
                pastelTint = PastelLavenderLight.copy(alpha = 0.35f),
                borderColor = PastelLavenderBorder
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Parámetros Operativos de Campus",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = SlateTextPrimary
                    )

                    Text(
                        text = "Umbral de Alerta de Llenado: ${sliderValue.toInt()}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SlateTextPrimary
                    )

                    Slider(
                        value = sliderValue,
                        onValueChange = {
                            sliderValue = it
                            wasteBinViewModel.setUmbralAlerta(it.toInt())
                        },
                        valueRange = 60f..95f,
                        steps = 6,
                        colors = SliderDefaults.colors(
                            thumbColor = PastelLavenderDark,
                            activeTrackColor = PastelLavenderDark,
                            inactiveTrackColor = PastelWell
                        )
                    )

                    // Switch Silenciar Buzzer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Silenciar Zumbador en Campus",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = SlateTextPrimary
                            )
                            Text(
                                text = "Evita pitidos audibles en horarios de clases o evaluaciones.",
                                fontSize = 11.sp,
                                color = SlateTextSecondary
                            )
                        }
                        Switch(
                            checked = buzzerSilenciado,
                            onCheckedChange = { wasteBinViewModel.toggleBuzzerSede(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PastelLavenderDark,
                                checkedTrackColor = PastelLavenderLight
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    SoftButton(
                        onClick = { wasteBinViewModel.guardarParametrosSupervisor() },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        isPrimary = true,
                        primaryColor = PastelLavenderPrimary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Parámetros de Sede", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
