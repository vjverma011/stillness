package com.stillness.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stillness.app.ui.theme.*

@Composable
fun CircularTimer(
    totalTimeMillis: Long,
    remainingTimeMillis: Long,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    val progress = if (totalTimeMillis > 0) {
        remainingTimeMillis.toFloat() / totalTimeMillis.toFloat()
    } else {
        1f
    }

    // Calculate responsive size based on screen dimensions
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val smallestDimension = if (screenWidth < screenHeight) screenWidth else screenHeight
    // Timer ring takes up ~65% of the smallest dimension, clamped between 180dp and 360dp
    val timerSize = (smallestDimension * 0.65f).coerceIn(180.dp, 360.dp)
    // Stroke scales proportionally
    val strokeDp = (timerSize * 0.043f).coerceIn(8.dp, 16.dp)
    // Font size scales with timer size (approx 25% of timer diameter)
    val timerFontSize = with(LocalDensity.current) {
        (timerSize * 0.25f).toSp()
    }
    val subtitleFontSize = with(LocalDensity.current) {
        (timerSize * 0.05f).coerceAtLeast(10.dp).toSp()
    }

    // Subtle pulsing animation when running
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    val displayAlpha = if (isRunning) pulseAlpha else 1f

    // Capture theme colors for use inside Canvas (which has no access to MaterialTheme)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val timerTextColor = MaterialTheme.colorScheme.onBackground
    val subtitleTextColor = MaterialTheme.colorScheme.outline
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(timerSize)
    ) {
        // Background ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = strokeDp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val topLeft = Offset(
                (size.width - radius * 2) / 2,
                (size.height - radius * 2) / 2
            )
            
            // Background track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
            // Progress arc with gradient
            if (progress > 0) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            TimerRingStart.copy(alpha = displayAlpha),
                            TimerRingEnd.copy(alpha = displayAlpha),
                            TimerRingStart.copy(alpha = displayAlpha)
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
        
        // Time display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formatTime(remainingTimeMillis),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = timerFontSize,
                    lineHeight = timerFontSize * 1.1f
                ),
                color = timerTextColor.copy(alpha = displayAlpha),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            if (isRunning) {
                Text(
                    text = "remaining",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = subtitleFontSize
                    ),
                    color = subtitleTextColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
