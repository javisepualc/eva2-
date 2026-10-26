package cl.inacap.smartwaste.ui.screens.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import cl.inacap.smartwaste.ui.navigation.Screen
import cl.inacap.smartwaste.ui.theme.*
import cl.inacap.smartwaste.viewmodel.AuthViewModel
import cl.inacap.smartwaste.viewmodel.ConnectionType
import cl.inacap.smartwaste.viewmodel.WasteBinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    wasteBinViewModel: WasteBinViewModel
) {
    val telemetry by wasteBinViewModel.telemetry.collectAsState()
    val isSending by wasteBinViewModel.isSendingCommand.collectAsState()
    val feedbackMessage by wasteBinViewModel.feedbackMessage.collectAsState()
    val connectionType by wasteBinViewModel.connectionType.collectAsState()
    val esp32Ip by wasteBinViewModel.esp32Ip.collectAsState()
    val alertaUrgente by wasteBinViewModel.alertaUrgente.collectAsState()
    val alertaSupervisor by wasteBinViewModel.alertaSupervisor.collectAsState()

    var showConnectDialog by remember { mutableStateOf(false) }
    var inputIp by remember(esp32Ip) { mutableStateOf(esp32Ip) }

    val isConnected = telemetry.isHardwareConnected || connectionType == ConnectionType.SIMULATION

    // Redirección inmediata a pantalla de Alarma si el sensor KY-027 del hardware detecta volcamiento
    LaunchedEffect(telemetry.volcado) {
        if (telemetry.volcado) {
            navController.navigate(Screen.TiltAlarm.route)
        }
    }

    val animatedFill by animateFloatAsState(
        targetValue = telemetry.llenadoPorcentaje / 100f,
        label = "fill_animation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (isConnected) PastelMintLight else PastelRoseLight,
                            modifier = Modifier.size(16.dp).border(BorderStroke(1.dp, if (isConnected) PastelMintBorder else PastelRoseBorder), CircleShape)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isConnected) PastelMintPrimary else PastelRosePrimary)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SmartWaste IoT",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = SlateTextPrimary
                            )
                            Text(
                                text = "Contenedor Pabellón A • Piso 2",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {
                    SoftIconButton(
                        onClick = { showConnectDialog = true },
                        tint = if (isConnected) PastelSkyLight else PastelSurface
                    ) {
                        Icon(
                            Icons.Default.Sensors,
                            contentDescription = "Conectar ESP32",
                            tint = if (isConnected) PastelSkyDark else SlateTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    SoftIconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración", tint = SlateTextSecondary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    SoftIconButton(
                        onClick = {
                            authViewModel.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        tint = PastelRoseLight.copy(alpha = 0.5f)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar Sesión", tint = PastelRoseDark, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PastelCanvas
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = PastelSurface,
                modifier = Modifier
                    .shadow(10.dp, ambientColor = PastelShadowAmbient, spotColor = PastelShadowSpot)
                    .border(BorderStroke(1.dp, Brush.verticalGradient(listOf(PastelSurfaceLight, PastelWellBorder.copy(alpha = 0.5f)))))
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("Panel", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PastelMintDark,
                        selectedTextColor = PastelMintDark,
                        indicatorColor = PastelMintLight
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Actuators.route) },
                    icon = { Icon(Icons.Default.TouchApp, contentDescription = null) },
                    label = { Text("Control") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = SlateTextSecondary,
                        unselectedTextColor = SlateTextSecondary
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Diagnosis.route) },
                    icon = { Icon(Icons.Default.Build, contentDescription = null) },
                    label = { Text("Sensores") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = SlateTextSecondary,
                        unselectedTextColor = SlateTextSecondary
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.History.route) },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text("Historial") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = SlateTextSecondary,
                        unselectedTextColor = SlateTextSecondary
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PastelCanvas)
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ALERTA DE VACIADO URGENTE DESPACHADA POR SUPERVISIÓN
            if (alertaUrgente) {
                SoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = 8.dp,
                    pastelTint = PastelRoseLight,
                    borderColor = PastelRoseBorder
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = PastelRosePrimary.copy(alpha = 0.2f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PriorityHigh,
                                        contentDescription = "Alerta Prioritaria",
                                        tint = PastelRoseDark,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ORDEN DE VACIADO URGENTE",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PastelRoseDark
                                )
                                Text(
                                    text = if (alertaSupervisor.isNotBlank()) "Emitida por: $alertaSupervisor" else "Emitida por Supervisión de Sede",
                                    fontSize = 11.sp,
                                    color = SlateTextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Supervisión ha marcado este contenedor con prioridad de retiro. Acude al punto de reciclaje para vaciar el depósito y confirma a continuación.",
                            fontSize = 12.sp,
                            color = SlateTextPrimary,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SoftButton(
                                onClick = {
                                    val user = authViewModel.currentUser
                                    wasteBinViewModel.registerDisposal(
                                        userName = user?.nombre ?: "Personal de Aseo",
                                        userUid = user?.uid ?: "sin_uid"
                                    )
                                },
                                modifier = Modifier.weight(1f).height(44.dp),
                                isPrimary = true,
                                primaryColor = PastelRosePrimary,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Vaciado Realizado", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            SoftButton(
                                onClick = { wasteBinViewModel.cancelarAlertaUrgente() },
                                modifier = Modifier.height(44.dp),
                                isPrimary = false,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Marcar Atendida", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SlateTextPrimary)
                            }
                        }
                    }
                }
            }

            // Estado de conexión del dispositivo
            val isSimulated = connectionType == ConnectionType.SIMULATION
            SoftCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = 6.dp,
                pastelTint = if (isConnected) (if (isSimulated) PastelLavenderLight.copy(alpha = 0.5f) else PastelSkyLight) else PastelRoseLight,
                borderColor = if (isConnected) (if (isSimulated) PastelLavenderBorder else PastelSkyBorder) else PastelRoseBorder
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isConnected) (if (isSimulated) PastelLavenderLight else PastelSkyLight) else PastelRoseLight,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                                    contentDescription = null,
                                    tint = if (isConnected) (if (isSimulated) PastelLavenderDark else PastelSkyDark) else PastelRoseDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when {
                                    isSimulated -> "Modo Demostración Activo"
                                    isConnected -> "Sistema en línea"
                                    else -> "Dispositivo fuera de línea"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = when {
                                    isSimulated -> PastelLavenderDark
                                    isConnected -> PastelSkyDark
                                    else -> PastelRoseDark
                                }
                            )
                            Text(
                                text = when {
                                    isSimulated -> "Telemetría simulada en tiempo real • Sensores activos"
                                    isConnected -> "Enlace activo • Telemetría en tiempo real"
                                    else -> "En espera de sincronización con el contenedor"
                                },
                                fontSize = 11.sp,
                                color = SlateTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Conexión con el ESP32
                    SoftButton(
                        onClick = { showConnectDialog = true },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        isPrimary = true,
                        primaryColor = when {
                            isSimulated -> PastelLavenderPrimary
                            isConnected -> PastelSkyPrimary
                            else -> PastelMintPrimary
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSimulated) "Gestionar conexión o cambiar a ESP32" else if (isConnected) "Gestionar conexión ESP32" else "Conectar al ESP32",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Estado del comando
            feedbackMessage?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PastelSkyLight,
                    modifier = Modifier.fillMaxWidth().border(BorderStroke(1.dp, PastelSkyBorder), RoundedCornerShape(14.dp))
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

            // Nivel de llenado del contenedor
            SoftCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = 8.dp,
                pastelTint = PastelMintLight,
                borderColor = PastelMintBorder
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = PastelMintLight,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = PastelMintDark, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Capacidad del Contenedor (HC-SR04)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Contenedor Gráfico Vectorial en Inset Well
                    Box(
                        modifier = Modifier
                            .width(152.dp)
                            .height(192.dp)
                            .softWellStyle(
                                RoundedCornerShape(
                                    topStart = 10.dp,
                                    topEnd = 10.dp,
                                    bottomStart = 24.dp,
                                    bottomEnd = 24.dp
                                ),
                                tint = PastelWell
                            ),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        val fillColor = when {
                            !isConnected -> Color(0xFF94A3B8)
                            telemetry.llenadoPorcentaje >= 80 -> PastelRosePrimary
                            telemetry.llenadoPorcentaje >= 60 -> PastelPeachPrimary
                            else -> PastelMintPrimary
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(if (isConnected) animatedFill else 0f)
                                .clip(
                                    RoundedCornerShape(
                                        bottomStart = 24.dp,
                                        bottomEnd = 24.dp
                                    )
                                )
                                .background(fillColor)
                        )

                        // Texto central en el gráfico
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (isConnected) {
                                Text(
                                    text = "${telemetry.llenadoPorcentaje}%",
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (animatedFill > 0.45f) Color.White else SlateTextPrimary
                                )
                                Text(
                                    text = "${telemetry.distanciaCm} cm",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (animatedFill > 0.45f) Color.White.copy(alpha = 0.9f) else SlateTextSecondary
                                )
                            } else {
                                Text(
                                    text = "-- %",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateTextMuted
                                )
                                Text(
                                    text = "Sin lectura",
                                    fontSize = 12.sp,
                                    color = SlateTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Chip de Recomendación Semáforo Pastel
                    val (recText, urgency) = telemetry.recomendacion
                    val (chipBg, chipText, chipDot) = when (urgency) {
                        "DANGER" -> Triple(PastelRoseLight, PastelRoseDark, PastelRosePrimary)
                        "WARN" -> Triple(PastelPeachLight, PastelPeachDark, PastelPeachPrimary)
                        "OFFLINE" -> Triple(PastelWell, SlateTextMuted, SlateTextMuted)
                        else -> Triple(PastelMintLight, PastelMintDark, PastelMintPrimary)
                    }
                    Surface(
                        color = chipBg,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.border(BorderStroke(1.dp, chipDot.copy(alpha = 0.35f)), RoundedCornerShape(50))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(chipDot)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = recText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = chipText
                            )
                        }
                    }
                }
            }

            // Sensores ambientales (DHT11 y MQ)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Tarjeta Temperatura y Humedad
                SoftCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    elevation = 6.dp,
                    pastelTint = PastelPeachLight,
                    borderColor = PastelPeachBorder
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = PastelPeachLight,
                                modifier = Modifier.size(34.dp).border(BorderStroke(1.dp, PastelPeachBorder), CircleShape)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Thermostat, contentDescription = null, tint = PastelPeachDark, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ambiente", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PastelPeachDark)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isConnected) "${telemetry.temperatura} °C" else "-- °C",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SlateTextPrimary
                        )
                        Text(
                            text = if (isConnected) "Humedad: ${telemetry.humedad}%" else "Humedad: --",
                            fontSize = 12.sp,
                            color = SlateTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (telemetry.estadoContenido == "En descomposición") PastelRoseLight else PastelPeachLight,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = telemetry.estadoContenido,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (telemetry.estadoContenido == "En descomposición") PastelRoseDark else PastelPeachDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Tarjeta Gases y Descomposición MQ
                SoftCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    elevation = 6.dp,
                    pastelTint = PastelLavenderLight,
                    borderColor = PastelLavenderBorder
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = PastelLavenderLight,
                                modifier = Modifier.size(34.dp).border(BorderStroke(1.dp, PastelLavenderBorder), CircleShape)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Cloud, contentDescription = null, tint = PastelLavenderDark, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gas / Olor", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PastelLavenderDark)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isConnected) "${telemetry.gasADC} ADC" else "-- ADC",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SlateTextPrimary
                        )
                        Text(
                            text = if (isConnected) "Línea Base: ${telemetry.lineaBaseGas}" else "Línea Base: --",
                            fontSize = 12.sp,
                            color = SlateTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (telemetry.estadoAire.contains("peligroso")) PastelRoseLight else PastelLavenderLight,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = telemetry.estadoAire,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (telemetry.estadoAire.contains("peligroso")) PastelRoseDark else PastelLavenderDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Accionamiento manual de tapa
            SoftButton(
                onClick = { wasteBinViewModel.triggerLidOpening(4.0) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                isPrimary = true,
                primaryColor = if (isSending) PastelPeachPrimary else PastelMintPrimary,
                enabled = isConnected && !isSending && !telemetry.modoLimpieza
            ) {
                if (isSending) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Enviando orden...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Sensors, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Abrir Tapa (4s)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            // Registro de vaciado
            SoftButton(
                onClick = {
                    val user = authViewModel.currentUser
                    wasteBinViewModel.registerDisposal(
                        userName = user?.nombre ?: "Auxiliar de Aseo",
                        userUid = user?.uid ?: "sin_uid"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                isPrimary = false,
                shape = RoundedCornerShape(14.dp),
                enabled = isConnected
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = PastelSkyDark)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Registrar Vaciado", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PastelSkyDark)
            }
        }
    }

    // Diálogo de conexión
    if (showConnectDialog) {
        AlertDialog(
            onDismissRequest = { showConnectDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = PastelSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = PastelSkyLight,
                        modifier = Modifier.size(38.dp).border(BorderStroke(1.dp, PastelSkyBorder), CircleShape)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Sensors, contentDescription = null, tint = PastelSkyDark, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Conexión con Nodo ESP32", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = SlateTextPrimary)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "Selecciona el método de comunicación con el hardware:",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateTextSecondary
                    )

                    // 1. CONEXIÓN WI-FI LOCAL / IP (Nodo 2 Gateway - Pastel Menta)
                    Surface(
                        color = if (connectionType == ConnectionType.DIRECT_WIFI) PastelMintLight.copy(alpha = 0.5f) else PastelWell.copy(alpha = 0.4f),
                        border = BorderStroke(1.2.dp, if (connectionType == ConnectionType.DIRECT_WIFI) PastelMintPrimary else PastelWellBorder),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                "1. Conexión Wi-Fi / IP (Nodo 2 Gateway)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = SlateTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Conéctate directo al Punto de Acceso o IP local del ESP32.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateTextSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = inputIp,
                                onValueChange = { inputIp = it },
                                label = { Text("IP del ESP32") },
                                placeholder = { Text("192.168.4.1") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = PastelSurface,
                                    unfocusedContainerColor = PastelSurface,
                                    focusedBorderColor = PastelMintPrimary,
                                    unfocusedBorderColor = PastelWellBorder
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            SoftButton(
                                onClick = {
                                    wasteBinViewModel.connectDirectWifi(inputIp)
                                    showConnectDialog = false
                                },
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                isPrimary = true,
                                primaryColor = PastelMintPrimary,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Conectar por Wi-Fi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 2. CONEXIÓN EN LA NUBE (Cloud Firestore - Pastel Cielo)
                    Surface(
                        color = if (connectionType == ConnectionType.CLOUD) PastelSkyLight.copy(alpha = 0.5f) else PastelWell.copy(alpha = 0.4f),
                        border = BorderStroke(1.2.dp, if (connectionType == ConnectionType.CLOUD) PastelSkyPrimary else PastelWellBorder),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                "2. Sincronización Cloud (Firestore)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = SlateTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Escucha continua del documento en la nube de Firebase.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateTextSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            SoftButton(
                                onClick = {
                                    wasteBinViewModel.switchToCloud()
                                    showConnectDialog = false
                                },
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                isPrimary = true,
                                primaryColor = PastelSkyPrimary,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Conectar por Nube (Firestore)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 3. MODO DEMOSTRACIÓN (Datos Simulados en Vivo - Pastel Lavanda)
                    Surface(
                        color = if (connectionType == ConnectionType.SIMULATION) PastelLavenderLight.copy(alpha = 0.5f) else PastelWell.copy(alpha = 0.4f),
                        border = BorderStroke(1.2.dp, if (connectionType == ConnectionType.SIMULATION) PastelLavenderPrimary else PastelWellBorder),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                "3. Modo Demostración (Datos de Muestra)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = SlateTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Genera telemetría dinámica creíble y registros sin depender del hardware encendido.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateTextSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            SoftButton(
                                onClick = {
                                    wasteBinViewModel.startSimulation()
                                    showConnectDialog = false
                                },
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                isPrimary = true,
                                primaryColor = PastelLavenderPrimary,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Activar Modo Demostración", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showConnectDialog = false }) {
                    Text("Cerrar", color = SlateTextSecondary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
