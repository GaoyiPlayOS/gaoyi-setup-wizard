package com.android.setupwizard.ui.welcome

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DriftEasing = CubicBezierEasing(0.16f, 0.84f, 0.14f, 1f)
private val RiseEasing = CubicBezierEasing(0.37f, 0f, 0.18f, 1f)

@Composable
fun WelcomeScreen(
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.surfaceBright,
                        colorScheme.surface,
                        colorScheme.surfaceContainerLowest,
                    ),
                ),
            ),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SovereignBootAnimation(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
            )

            Spacer(modifier = Modifier.height(36.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Welcome to",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "GaoyiPlayOS",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.8).sp,
                    ),
                    color = colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalIconButton(
                    onClick = {},
                    modifier = Modifier.size(56.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessibilityNew,
                        contentDescription = "Accessibility",
                    )
                }

                Button(
                    onClick = onNext,
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Text(
                        text = "START",
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

@Composable
private fun SovereignBootAnimation(
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "sovereign-boot")

    val primaryOrbitalX by transition.animateFloat(
        initialValue = -0.16f,
        targetValue = 0.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = DriftEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "primary-orbital-x",
    )
    val primaryOrbitalY by transition.animateFloat(
        initialValue = -0.10f,
        targetValue = 0.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4700, easing = RiseEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "primary-orbital-y",
    )
    val tertiaryOrbitalX by transition.animateFloat(
        initialValue = 0.18f,
        targetValue = -0.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5900, easing = RiseEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "tertiary-orbital-x",
    )
    val tertiaryOrbitalY by transition.animateFloat(
        initialValue = 0.14f,
        targetValue = -0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5400, easing = DriftEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "tertiary-orbital-y",
    )
    val pulse by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4300, easing = DriftEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "orbital-pulse",
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(36.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        colorScheme.surfaceContainerHigh,
                        colorScheme.surfaceContainer,
                        colorScheme.surfaceBright,
                    ),
                ),
            ),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val minSide = size.minDimension
            val primaryCenter = Offset(
                x = size.width * (0.34f + primaryOrbitalX * 0.45f),
                y = size.height * (0.44f + primaryOrbitalY * 0.40f),
            )
            val tertiaryCenter = Offset(
                x = size.width * (0.70f + tertiaryOrbitalX * 0.40f),
                y = size.height * (0.52f + tertiaryOrbitalY * 0.38f),
            )
            val echoCenter = Offset(
                x = size.width * 0.54f,
                y = size.height * (0.60f - primaryOrbitalY * 0.18f),
            )

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.surfaceBright.copy(alpha = 0.96f),
                        colorScheme.surface.copy(alpha = 0.92f),
                        colorScheme.surfaceContainerLowest.copy(alpha = 0.98f),
                    ),
                ),
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colorScheme.primary.copy(alpha = 0.84f),
                        colorScheme.primary.copy(alpha = 0.14f),
                        Color.Transparent,
                    ),
                    center = primaryCenter,
                    radius = minSide * 0.44f * pulse,
                ),
                radius = minSide * 0.34f * pulse,
                center = primaryCenter,
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colorScheme.tertiary.copy(alpha = 0.72f),
                        colorScheme.tertiary.copy(alpha = 0.12f),
                        Color.Transparent,
                    ),
                    center = tertiaryCenter,
                    radius = minSide * 0.40f / pulse,
                ),
                radius = minSide * 0.29f / pulse,
                center = tertiaryCenter,
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colorScheme.primary.copy(alpha = 0.24f),
                        colorScheme.tertiary.copy(alpha = 0.08f),
                        Color.Transparent,
                    ),
                    center = echoCenter,
                    radius = minSide * 0.52f,
                ),
                radius = minSide * 0.36f,
                center = echoCenter,
            )
        }
    }
}
