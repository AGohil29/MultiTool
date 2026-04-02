package org.arun.multitool.ui.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedWaveBackground() {
    // 1. Create an infinite animation loop for the "wave"
    val infiniteTransition = rememberInfiniteTransition(label = "WaveTransition")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WaveProgress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 2. Adjust the 'Base' height (0.7f means 70% down the screen)
        val waveHeight = height * 0.75f
        // 3. Adjust how much it moves (Amplitude)
        val amplitude = 40.dp.toPx()

        // 2. Draw using Math-based Bezier Curves
        val path = Path().apply {
            moveTo(0f, waveHeight)
            quadraticBezierTo(
                x1 = width / 2,
                y1 = waveHeight + (amplitude * (waveOffset - 0.5f) * 2),
                x2 = width,
                y2 = waveHeight
            )
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        // 3. Apply a Gradient (Shared logic across platforms)
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFC14A25), // Your brand orange
                    Color(0xFF7A2E17)  // A darker shade for depth
                ),
                startY = waveHeight,
                endY = height
            ),
            alpha = 0.6f // Subtle transparency
        )
    }
}