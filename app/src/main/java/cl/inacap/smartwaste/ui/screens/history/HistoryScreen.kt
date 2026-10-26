package cl.inacap.smartwaste.ui.screens.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import cl.inacap.smartwaste.ui.theme.*
import cl.inacap.smartwaste.viewmodel.WasteBinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: WasteBinViewModel
) {
    val historyList by viewModel.history.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Operaciones", fontWeight = FontWeight.ExtraBold, color = SlateTextPrimary) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Métricas de vaciado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Total Vaciados (Pastel Menta)
                SoftCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    elevation = 6.dp,
                    pastelTint = PastelMintLight,
                    borderColor = PastelMintBorder
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Vaciados", fontSize = 12.sp, color = PastelMintDark, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${historyList.size}", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = PastelMintDark)
                        Text("Registros en Firestore", fontSize = 11.sp, color = SlateTextSecondary)
                    }
                }

                // Último Vaciado (Pastel Cielo)
                SoftCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    elevation = 6.dp,
                    pastelTint = PastelSkyLight,
                    borderColor = PastelSkyBorder
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Último Vaciado", fontSize = 12.sp, color = PastelSkyDark, fontWeight = FontWeight.Bold)
                        val lastItem = historyList.firstOrNull()
                        Spacer(modifier = Modifier.height(4.dp))
                        if (lastItem != null) {
                            val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(lastItem.fechaHora))
                            Text(timeStr, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = PastelSkyDark)
                            Text("Por: ${lastItem.usuarioNombre}", fontSize = 11.sp, color = SlateTextSecondary)
                        } else {
                            Text("--:--", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = SlateTextPrimary)
                            Text("Sin registros", fontSize = 11.sp, color = SlateTextSecondary)
                        }
                    }
                }
            }

            Text(
                text = "Bitácora de Vaciados",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SlateTextPrimary
            )

            // Lista de eventos
            if (historyList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aún no hay registros de vaciado en Firestore.\nUsa el botón 'Registrar Vaciado' en el Panel Principal para guardar el primero.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = SlateTextSecondary,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(historyList) { item ->
                        SoftCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = PastelMintLight,
                                    modifier = Modifier.size(42.dp).border(BorderStroke(1.dp, PastelMintBorder), CircleShape)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = PastelMintDark)
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.usuarioNombre, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = SlateTextPrimary)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(12.dp), tint = SlateTextSecondary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(item.fechaHora))
                                        Text(formattedDate, fontSize = 11.sp, color = SlateTextSecondary)
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${item.porcentajePrevio}%", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = PastelMintDark)
                                    Text("Vaciado", fontSize = 11.sp, color = SlateTextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
