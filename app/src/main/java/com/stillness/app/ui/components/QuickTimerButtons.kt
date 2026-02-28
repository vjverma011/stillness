package com.stillness.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.stillness.app.ui.theme.*

data class QuickTimerOption(
    val label: String,
    val durationMinutes: Int
)

val defaultQuickTimerOptions = listOf(
    QuickTimerOption("5m", 5),
    QuickTimerOption("15m", 15),
    QuickTimerOption("30m", 30),
    QuickTimerOption("1h", 60)
)

@Composable
fun QuickTimerButtons(
    selectedMinutes: Int?,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    options: List<QuickTimerOption> = defaultQuickTimerOptions
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
    ) {
        options.forEach { option ->
            QuickTimerButton(
                label = option.label,
                isSelected = selectedMinutes == option.durationMinutes,
                onClick = { onOptionSelected(option.durationMinutes) }
            )
        }
    }
}

@Composable
fun QuickTimerButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) AccentLavender else ButtonBackground,
        animationSpec = tween(200),
        label = "buttonBackground"
    )
    
    val textColor by animateColorAsState(
        targetValue = if (isSelected) DarkBackground else TextPrimary,
        animationSpec = tween(200),
        label = "buttonText"
    )
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = textColor
        )
    }
}
