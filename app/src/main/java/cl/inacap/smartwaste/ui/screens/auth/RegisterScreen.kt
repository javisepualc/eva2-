package cl.inacap.smartwaste.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.ASEO) }
    var passwordVisible by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Nueva Cuenta", fontWeight = FontWeight.Bold, color = SlateTextPrimary) },
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
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Únete a SmartWaste IoT",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = SlateTextPrimary
                )
                Text(
                    text = "Gestiona contenedores de forma segura y en tiempo real",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateTextSecondary
                )

                Spacer(modifier = Modifier.height(18.dp))

                SoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = 8.dp,
                    pastelTint = PastelMintLight.copy(alpha = 0.5f),
                    borderColor = PastelMintBorder
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Nombre y Apellido
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre y Apellido") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SlateTextSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = PastelSurface,
                                unfocusedContainerColor = PastelSurface.copy(alpha = 0.7f),
                                focusedBorderColor = PastelMintPrimary,
                                unfocusedBorderColor = PastelWellBorder
                            )
                        )

                        // Correo Institucional
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Correo Institucional (@inacapmail.cl)") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SlateTextSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = PastelSurface,
                                unfocusedContainerColor = PastelSurface.copy(alpha = 0.7f),
                                focusedBorderColor = PastelMintPrimary,
                                unfocusedBorderColor = PastelWellBorder
                            )
                        )

                        // Selector de Rol inicial con variantes pasteles
                        Column {
                            Text(
                                text = "Rol Funcional Solicitado",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = SlateTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(UserRole.ASEO, UserRole.SUPERVISOR).forEach { role ->
                                    val isSelected = selectedRole == role
                                    val (bg, textColor, border) = if (role == UserRole.ASEO)
                                        Triple(PastelMintLight, PastelMintDark, PastelMintBorder)
                                    else
                                        Triple(PastelSkyLight, PastelSkyDark, PastelSkyBorder)

                                    Surface(
                                        onClick = { selectedRole = role },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) bg else PastelWell,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                            .border(
                                                BorderStroke(
                                                    1.dp,
                                                    if (isSelected) border else PastelWellBorder.copy(alpha = 0.5f)
                                                ),
                                                RoundedCornerShape(12.dp)
                                            )
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = role.displayName,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) textColor else SlateTextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Contraseña
                        val hasMinLength = password.length >= 8
                        val hasUpperCase = password.any { it.isUpperCase() }
                        val hasDigit = password.any { it.isDigit() }
                        val hasSpecial = password.any { !it.isLetterOrDigit() }

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña (mínimo 8 caracteres)") },
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = PastelSurface,
                                unfocusedContainerColor = PastelSurface.copy(alpha = 0.7f),
                                focusedBorderColor = PastelMintPrimary,
                                unfocusedBorderColor = PastelWellBorder
                            )
                        )

                        // Validación de requisitos de contraseña
                        if (password.isNotEmpty()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .softWellStyle(RoundedCornerShape(14.dp), tint = PastelWell),
                                color = Color.Transparent
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Requisitos de la contraseña:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SlateTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    RequirementRow(label = "Mínimo 8 caracteres", met = hasMinLength)
                                    RequirementRow(label = "Al menos una mayúscula (A-Z)", met = hasUpperCase)
                                    RequirementRow(label = "Al menos un número (0-9)", met = hasDigit)
                                    RequirementRow(label = "Al menos un símbolo especial (!@#$...)", met = hasSpecial)
                                }
                            }
                        }

                        // Confirmar Contraseña
                        OutlinedTextField(
                            value = passwordConfirm,
                            onValueChange = { passwordConfirm = it },
                            label = { Text("Confirmar Contraseña") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SlateTextSecondary) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = PastelSurface,
                                unfocusedContainerColor = PastelSurface.copy(alpha = 0.7f),
                                focusedBorderColor = PastelMintPrimary,
                                unfocusedBorderColor = PastelWellBorder
                            )
                        )

                        // Mensajes de Error
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

                        Spacer(modifier = Modifier.height(4.dp))

                        // Botón de registro
                        SoftButton(
                            onClick = {
                                viewModel.register(
                                    nombre = nombre,
                                    email = email,
                                    pass = password,
                                    passConfirm = passwordConfirm,
                                    rol = selectedRole
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState !is AuthUiState.Loading,
                            isPrimary = true,
                            primaryColor = PastelMintPrimary,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (uiState is AuthUiState.Loading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                            } else {
                                Text("Registrar Cuenta", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Volver a Login
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "¿Ya tienes una cuenta registrada?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateTextSecondary
                    )
                    TextButton(onClick = {
                        viewModel.clearState()
                        navController.popBackStack()
                    }) {
                        Text("Iniciar Sesión", fontWeight = FontWeight.Bold, color = PastelMintDark)
                    }
                }
            }
        }
    }
}

@Composable
private fun RequirementRow(label: String, met: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = if (met) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (met) PastelMintDark else SlateTextMuted,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (met) PastelMintDark else SlateTextSecondary,
            fontWeight = if (met) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
