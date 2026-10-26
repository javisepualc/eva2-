package cl.inacap.smartwaste.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import cl.inacap.smartwaste.data.model.UserRole
import cl.inacap.smartwaste.ui.navigation.Screen
import cl.inacap.smartwaste.ui.theme.*
import cl.inacap.smartwaste.viewmodel.AuthUiState
import cl.inacap.smartwaste.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("operador@inacapmail.cl") }
    var password by remember { mutableStateOf("123456") }
    var passwordVisible by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            val user = (uiState as AuthUiState.Success).user
            val targetRoute = if (user.userRole == UserRole.SUPERVISOR || user.userRole == UserRole.ADMIN) {
                Screen.SupervisorDashboard.route
            } else {
                Screen.Dashboard.route
            }
            navController.navigate(targetRoute) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PastelCanvas)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icono principal
            Surface(
                shape = CircleShape,
                color = PastelMintLight,
                modifier = Modifier
                    .size(80.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        ambientColor = PastelShadowAmbient.copy(alpha = 0.5f),
                        spotColor = PastelMintPrimary.copy(alpha = 0.4f)
                    )
                    .border(
                        BorderStroke(
                            1.5.dp,
                            Brush.verticalGradient(
                                listOf(PastelSurfaceLight, PastelMintBorder)
                            )
                        ),
                        shape = CircleShape
                    )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🗑️", fontSize = 38.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SmartWaste IoT",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = SlateTextPrimary
            )
            Text(
                text = "Monitoreo y Control de Residuos",
                style = MaterialTheme.typography.bodyMedium,
                color = SlateTextSecondary
            )

            Spacer(modifier = Modifier.height(26.dp))

            // Formulario de login
            SoftCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = 8.dp,
                pastelTint = PastelMintLight.copy(alpha = 0.5f),
                borderColor = PastelMintBorder
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Iniciar Sesión",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = SlateTextPrimary
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo Institucional") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SlateTextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = PastelSurface,
                            unfocusedContainerColor = PastelSurface.copy(alpha = 0.7f),
                            focusedBorderColor = PastelMintPrimary,
                            unfocusedBorderColor = PastelWellBorder
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SlateTextSecondary) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = SlateTextSecondary
                                )
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = PastelSurface,
                            unfocusedContainerColor = PastelSurface.copy(alpha = 0.7f),
                            focusedBorderColor = PastelMintPrimary,
                            unfocusedBorderColor = PastelWellBorder
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        TextButton(
                            onClick = {
                                viewModel.clearState()
                                navController.navigate(Screen.ForgotPassword.route)
                            }
                        ) {
                            Text("¿Olvidaste tu contraseña?", fontSize = 12.sp, color = PastelMintDark, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (uiState is AuthUiState.Error) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PastelRoseLight,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
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

                    Spacer(modifier = Modifier.height(8.dp))

                    SoftButton(
                        onClick = { viewModel.login(email, password) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState !is AuthUiState.Loading,
                        isPrimary = true,
                        primaryColor = PastelMintPrimary,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (uiState is AuthUiState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                        } else {
                            Text("Iniciar Sesión", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Acceso rápido para pruebas en campus
                    Text(
                        text = "Credenciales Institucionales",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = {
                                email = "operador@inacapmail.cl"
                                password = "123456"
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (email == "operador@inacapmail.cl") PastelMintLight else PastelWell,
                            border = BorderStroke(1.dp, if (email == "operador@inacapmail.cl") PastelMintBorder else PastelWellBorder.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f).height(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🧹 Operador Aseo", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (email == "operador@inacapmail.cl") PastelMintDark else SlateTextSecondary)
                            }
                        }
                        Surface(
                            onClick = {
                                email = "supervisor@inacapmail.cl"
                                password = "123456"
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (email == "supervisor@inacapmail.cl") PastelSkyLight else PastelWell,
                            border = BorderStroke(1.dp, if (email == "supervisor@inacapmail.cl") PastelSkyBorder else PastelWellBorder.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f).height(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("📋 Supervisor Sede", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (email == "supervisor@inacapmail.cl") PastelSkyDark else SlateTextSecondary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿No tienes una cuenta?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateTextSecondary
                )
                TextButton(
                    onClick = {
                        viewModel.clearState()
                        navController.navigate(Screen.Register.route)
                    }
                ) {
                    Text(
                        text = "Regístrate aquí",
                        fontWeight = FontWeight.Bold,
                        color = PastelMintDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                color = PastelMintLight.copy(alpha = 0.4f),
                shape = RoundedCornerShape(50),
                modifier = Modifier.border(BorderStroke(1.dp, PastelMintBorder.copy(alpha = 0.5f)), RoundedCornerShape(50))
            ) {
                Text(
                    text = "INACAP Talca • TI3042",
                    style = MaterialTheme.typography.labelSmall,
                    color = PastelMintDark,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}
