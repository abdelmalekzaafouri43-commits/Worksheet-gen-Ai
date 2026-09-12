package com.example

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Applies a luxurious animated rotating gradient border around any Composable component.
 */
@Composable
fun Modifier.animatedBorder(
    borderWidth: Dp = 1.5.dp,
    cornerRadius: Dp = 16.dp,
    borderColors: List<Color> = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
        MaterialTheme.colorScheme.primary
    ),
    durationMillis: Int = 3600,
    isActive: Boolean = true
): Modifier {
    if (!isActive) {
        return this.border(
            width = borderWidth,
            color = borderColors.first().copy(alpha = 0.25f),
            shape = RoundedCornerShape(cornerRadius)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "BorderAnimation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "BorderAngle"
    )

    val density = LocalDensity.current
    val strokeWidthPx = with(density) { borderWidth.toPx() }
    val cornerRadiusPx = with(density) { cornerRadius.toPx() }

    return this
        .clip(RoundedCornerShape(cornerRadius))
        .drawWithContent {
            drawContent()

            val halfStroke = strokeWidthPx / 2f
            rotate(degrees = angle, pivot = center) {
                val brush = Brush.sweepGradient(
                    colors = borderColors,
                    center = center
                )
                drawRoundRect(
                    brush = brush,
                    topLeft = Offset(halfStroke, halfStroke),
                    size = Size(size.width - strokeWidthPx, size.height - strokeWidthPx),
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                    style = Stroke(width = strokeWidthPx)
                )
            }
        }
}

/**
 * Shimmering pulsing border that expands and fades periodically.
 */
@Composable
fun Modifier.pulsingBorder(
    borderWidth: Dp = 1.5.dp,
    cornerRadius: Dp = 16.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    durationMillis: Int = 1800
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "PulseBorder")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    return this.border(
        width = borderWidth,
        color = color.copy(alpha = alpha),
        shape = RoundedCornerShape(cornerRadius)
    )
}
