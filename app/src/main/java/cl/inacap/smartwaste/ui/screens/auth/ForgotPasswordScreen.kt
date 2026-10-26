package cl.inacap.smartwaste.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import cl.inacap.smartwaste.ui.theme.*
import cl.inacap.smartwaste.viewmodel.AuthUiState
import cl.inacap.smartwaste.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar Contraseña", fontWeight = FontWeight.Bold, color = SlateTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearState()
                        navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = SlateTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PastelCanvas
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PastelCanvas)
                .padding(paddingValues)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (uiState is AuthUiState.PasswordResetSent) {
                // Mensaje de éxito
                SoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = 8.dp,
                    pastelTint = PastelMintLight,
                    borderColor = PastelMintBorder
                ) {
                    Column(
                        modifier = Modifier.padding(26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PastelMintLight,
                            modifier = Modifier
                                .size(76.dp)
                                .shadow(6.dp, CircleShape, ambientColor = PastelShadowAmbient, spotColor = PastelMintPrimary.copy(alpha = 0.4f))
                                .border(BorderStroke(1.2.dp, PastelMintBorder), CircleShape)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = PastelMintDark,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }

                        Text(
                            text = "¡Correo Enviado!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = PastelMintDark
                        )

                        Text(
                            text = "Hemos enviado las instrucciones para restablecer tu contraseña a:\n\n${(uiState as AuthUiState.PasswordResetSent).email}\n\nRevisa tu bandeja de entrada o carpeta de spam.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = SlateTextSecondary
                        )

                        SoftButton(
                            onClick = {
                                viewModel.clearState()
                                navController.popBackStack()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            isPrimary = true,
                            primaryColor = PastelMintPrimary,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Volver al Login", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            } else {
                // Formulario de Solicitud de Recuperación
                SoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = 8.dp,
                    pastelTint = PastelSkyLight.copy(alpha = 0.4f),
                    borderColor = PastelSkyBorder
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Restablece tu Acceso",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = SlateTextPrimary
                        )

                        Text(
                            text = "Ingresa tu correo institucional registrado. Te enviaremos un enlace seguro para generar una nueva contraseña.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = SlateTextSecondary
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Correo Institucional") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SlateTextSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = PastelSurface,
                                unfocusedContainerColor = PastelSurface.copy(alpha = 0.7f),
                                focusedBorderColor = PastelSkyPrimary,
                                unfocusedBorderColor = PastelWellBorder
                            )
                        )

                        if (uiState is AuthUiState.Error) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = PastelRoseLight,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = (uiState as AuthUiState.Error).message,
                                    color = PastelRoseDark,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        SoftButton(
                            onClick = { viewModel.resetPassword(email) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState !is AuthUiState.Loading,
                            isPrimary = true,
                            primaryColor = PastelSkyPrimary,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (uiState is AuthUiState.Loading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                            } else {
                                Text("Enviar Enlace", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }

                        TextButton(onClick = {
                            viewModel.clearState()
                            navController.popBackStack()
                        }) {
                            Text("Cancelar y volver", color = SlateTextSecondary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}
