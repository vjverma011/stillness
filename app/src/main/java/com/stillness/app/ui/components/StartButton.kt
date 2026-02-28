package com.stillness.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.stillness.app.ui.theme.*

@Composable
fun StartButton(
    isRunning: Boolean,
    isPaused: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val buttonText = when {
        isRunning && !isPaused -> "PAUSE"
        isPaused -> "RESUME"
        else -> "START"
    }
    
    // Subtle scale animation on press
    val scale by animateFloatAsState(
        targetValue = if (isRunning && !isPaused) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "buttonScale"
    )
    
    // Pulsing glow when ready to start
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    val showGlow = !isRunning && enabled
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .scale(scale)
                .size(100.dp)
        ) {
            // Glow effect
            if (showGlow) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    AccentLavender.copy(alpha = glowAlpha * 0.5f),
                                    AccentLavender.copy(alpha = 0f)
                                )
                            )
                        )
                )
            }
            
            // Main button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        if (enabled) {
                            Brush.linearGradient(
                                colors = listOf(AccentLavender, AccentLavenderDark)
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(ButtonBackground, ButtonBackground)
                            )
                        }
                    )
                    .clickable(enabled = enabled) {
                        when {
                            isRunning && !isPaused -> onPause()
                            isPaused -> onResume()
                            else -> onStart()
                        }
                    }
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (enabled) DarkBackground else TextMuted
                )
            }
        }
        
        // Stop button (shown when running or paused)
        if (isRunning || isPaused) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "STOP",
                style = MaterialTheme.typography.labelMedium,
                color = AccentCoral,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onStop)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}
