package cl.inacap.smartwaste.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Modificadores de estilo para contenedores
fun Modifier.softCardStyle(
    shape: Shape = RoundedCornerShape(22.dp),
    elevation: Dp = 6.dp,
    pastelTint: Color? = null
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        ambientColor = PastelShadowAmbient.copy(alpha = 0.45f),
        spotColor = PastelShadowSpot.copy(alpha = 0.55f),
        clip = false
    )
    .clip(shape)
    .background(
        brush = Brush.verticalGradient(
            colors = if (pastelTint != null) {
                listOf(pastelTint.copy(alpha = 0.25f), PastelSurface)
            } else {
                listOf(PastelSurfaceLight, PastelSurface)
            }
        )
    )
    .border(
        BorderStroke(
            1.2.dp,
            Brush.verticalGradient(
                colors = listOf(
                    PastelSurfaceLight.copy(alpha = 0.95f),
                    PastelWellBorder.copy(alpha = 0.45f)
                )
            )
        ),
        shape = shape
    )

fun Modifier.softWellStyle(
    shape: Shape = RoundedCornerShape(16.dp),
    tint: Color = PastelWell
): Modifier = this
    .clip(shape)
    .background(tint)
    .border(
        BorderStroke(
            1.dp,
            Brush.verticalGradient(
                colors = listOf(
                    PastelWellBorder.copy(alpha = 0.75f),
                    PastelSurfaceLight.copy(alpha = 0.6f)
                )
            )
        ),
        shape = shape
    )

// Tarjeta personalizada con bordes suaves y sombra
@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    elevation: Dp = 6.dp,
    pastelTint: Color? = null,
    borderColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val gradientColors = if (pastelTint != null) {
        listOf(
            pastelTint.copy(alpha = 0.35f),
            PastelSurface
        )
    } else {
        listOf(
            PastelSurfaceLight.copy(alpha = 0.85f),
            PastelSurface
        )
    }

    val cardBorder = if (borderColor != null) {
        BorderStroke(
            1.2.dp,
            Brush.verticalGradient(
                listOf(PastelSurfaceLight.copy(alpha = 0.95f), borderColor.copy(alpha = 0.55f))
            )
        )
    } else {
        BorderStroke(
            1.2.dp,
            Brush.verticalGradient(
                listOf(
                    PastelSurfaceLight.copy(alpha = 0.95f),
                    PastelWellBorder.copy(alpha = 0.45f)
                )
            )
        )
    }

    Card(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = PastelShadowAmbient.copy(alpha = 0.4f),
                spotColor = PastelShadowSpot.copy(alpha = 0.5f),
                clip = false
            )
            .border(cardBorder, shape = shape),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = PastelSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .background(Brush.verticalGradient(gradientColors))
                .fillMaxWidth()
        ) {
            content()
        }
    }
}

// Botón con bordes redondeados y sombra
@Composable
fun SoftButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = true,
    primaryColor: Color = PastelMintPrimary,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable RowScope.() -> Unit
) {
    val btnColor = if (isPrimary) primaryColor else PastelSurface
    val contentColor = if (isPrimary) Color.White else SlateTextPrimary

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        modifier = modifier
            .height(52.dp)
            .shadow(
                elevation = if (enabled) 6.dp else 1.dp,
                shape = shape,
                ambientColor = PastelShadowAmbient.copy(alpha = 0.4f),
                spotColor = if (isPrimary) primaryColor.copy(alpha = 0.35f) else PastelShadowSpot.copy(alpha = 0.45f)
            )
            .border(
                BorderStroke(
                    1.dp,
                    if (isPrimary) {
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.45f), Color.Transparent)
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(PastelSurfaceLight.copy(alpha = 0.95f), PastelWellBorder.copy(alpha = 0.5f))
                        )
                    }
                ),
                shape = shape
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = btnColor,
            contentColor = contentColor,
            disabledContainerColor = PastelWell,
            disabledContentColor = SlateTextMuted
        ),
        content = content
    )
}

// Botón circular para acciones de barra superior
@Composable
fun SoftIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    tint: Color = PastelSurface,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = shape,
        color = tint,
        modifier = modifier
            .size(42.dp)
            .shadow(
                elevation = 4.dp,
                shape = shape,
                ambientColor = PastelShadowAmbient.copy(alpha = 0.35f),
                spotColor = PastelShadowSpot.copy(alpha = 0.4f)
            )
            .border(
                BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(PastelSurfaceLight.copy(alpha = 0.95f), PastelWellBorder.copy(alpha = 0.4f))
                    )
                ),
                shape = shape
            )
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}
