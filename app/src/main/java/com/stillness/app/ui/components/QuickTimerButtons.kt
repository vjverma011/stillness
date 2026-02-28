package com.stillness.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

data class QuickTimerOption(
    val label: String,
    val durationSeconds: Int
)

val defaultQuickTimerOptions = listOf(
    QuickTimerOption("5m", 5 * 60),
    QuickTimerOption("15m", 15 * 60),
    QuickTimerOption("30m", 30 * 60),
    QuickTimerOption("1h", 60 * 60)
)

data class QuickAddOption(
    val label: String,
    val addSeconds: Int
)

val defaultQuickAddOptions = listOf(
    QuickAddOption("+15s", 15),
    QuickAddOption("+30s", 30),
    QuickAddOption("+1m", 60),
    QuickAddOption("+5m", 5 * 60)
)

@Composable
fun QuickTimerButtons(
    selectedSeconds: Int?,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    options: List<QuickTimerOption> = defaultQuickTimerOptions
) {
    // Adaptive spacing based on screen width
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val spacing = (screenWidth * 0.03f).coerceIn(6.dp, 16.dp)
    val horizontalPadding = (screenWidth * 0.045f).coerceIn(12.dp, 24.dp)
    val verticalPadding = (screenWidth * 0.035f).coerceIn(10.dp, 16.dp)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally)
    ) {
        options.forEach { option ->
            QuickTimerButton(
                label = option.label,
                isSelected = selectedSeconds == option.durationSeconds,
                onClick = { onOptionSelected(option.durationSeconds) },
                horizontalPadding = horizontalPadding,
                verticalPadding = verticalPadding
            )
        }
    }
}

@Composable
fun QuickTimerButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    horizontalPadding: androidx.compose.ui.unit.Dp = 20.dp,
    verticalPadding: androidx.compose.ui.unit.Dp = 14.dp
) {
    val unselectedBg = MaterialTheme.colorScheme.surfaceVariant
    val selectedBg = MaterialTheme.colorScheme.primary
    val selectedText = MaterialTheme.colorScheme.background
    val unselectedText = MaterialTheme.colorScheme.onSurface
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val selectedBorderColor = MaterialTheme.colorScheme.primary

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) selectedBg else unselectedBg,
        animationSpec = tween(200),
        label = "buttonBackground"
    )
    
    val textColor by animateColorAsState(
        targetValue = if (isSelected) selectedText else unselectedText,
        animationSpec = tween(200),
        label = "buttonText"
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) selectedBorderColor else borderColor,
        animationSpec = tween(200),
        label = "buttonBorder"
    )

    val shape = RoundedCornerShape(16.dp)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(shape)
            .border(width = 1.dp, color = animatedBorderColor, shape = shape)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = textColor
        )
    }
}

/**
 * Quick-add chips that let the user fine-tune the selected duration.
 * E.g. tap "5m" then "+30s" to get 5m30s.
 */
@Composable
fun QuickAddChips(
    onAddSeconds: (Int) -> Unit,
    modifier: Modifier = Modifier,
    options: List<QuickAddOption> = defaultQuickAddOptions
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val spacing = (screenWidth * 0.02f).coerceIn(4.dp, 12.dp)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally)
    ) {
        options.forEach { option ->
            QuickAddChip(
                label = option.label,
                onClick = { onAddSeconds(option.addSeconds) }
            )
        }
    }
}

@Composable
fun QuickAddChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    val shape = RoundedCornerShape(20.dp)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(shape)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
