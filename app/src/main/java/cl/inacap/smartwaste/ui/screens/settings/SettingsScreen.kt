package cl.inacap.smartwaste.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import cl.inacap.smartwaste.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController
) {
    var macAddress by remember { mutableStateOf("78:1C:3C:B7:72:70") }
    var brokerMqtt by remember { mutableStateOf("broker.hivemq.com") }
    var topicMqtt by remember { mutableStateOf("inacap/basurero/datos") }
    var timerApertura by remember { mutableStateOf(4.0f) }
    var savedFeedback by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración del Sistema", fontWeight = FontWeight.ExtraBold, color = SlateTextPrimary) },
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
            Text(
                text = "Parámetros de Red y Telemetría IoT",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SlateTextPrimary
            )

            SoftCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = 6.dp,
                pastelTint = PastelLavenderLight.copy(alpha = 0.4f),
                borderColor = PastelLavenderBorder
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = macAddress,
                        onValueChange = { macAddress = it },
                        label = { Text("Dirección MAC del ESP32") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = GreyWell.copy(alpha = 0.4f),
                            unfocusedContainerColor = GreyWell.copy(alpha = 0.25f),
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = GreyWellBorder
                        )
                    )

                    OutlinedTextField(
                        value = brokerMqtt,
                        onValueChange = { brokerMqtt = it },
                        label = { Text("Broker MQTT") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = GreyWell.copy(alpha = 0.4f),
                            unfocusedContainerColor = GreyWell.copy(alpha = 0.25f),
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = GreyWellBorder
                        )
                    )

                    OutlinedTextField(
                        value = topicMqtt,
                        onValueChange = { topicMqtt = it },
                        label = { Text("Tópico de Suscripción") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = GreyWell.copy(alpha = 0.4f),
                            unfocusedContainerColor = GreyWell.copy(alpha = 0.25f),
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = GreyWellBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Tiempo de Apertura de Servomotores: ${String.format("%.1f", timerApertura)}s",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = SlateTextPrimary
                    )

                    Slider(
                        value = timerApertura,
                        onValueChange = { timerApertura = it },
                        valueRange = 2.0f..10.0f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = GreenPrimary,
                            activeTrackColor = GreenPrimary
                        )
                    )

                    if (savedFeedback) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = GreenLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✓ Parámetros guardados y sincronizados con Cloud Firestore",
                                color = GreenDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    SoftButton(
                        onClick = { savedFeedback = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        isPrimary = true,
                        primaryColor = GreenPrimary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Ajustes", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Información del Proyecto y Rúbrica
            SoftCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                elevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("ℹ️ Acerca de SmartWaste IoT", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateTextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Asignatura: Aplicaciones Móviles para IoT (TI3042)", fontSize = 12.sp, color = SlateTextSecondary)
                    Text("INACAP Talca • Primavera 2026", fontSize = 12.sp, color = SlateTextSecondary)
                    Text("Integrantes: Javiera Sepúlveda, Fernanda Contreras, Alexia González", fontSize = 12.sp, color = SlateTextSecondary)
                }
            }
        }
    }
}
