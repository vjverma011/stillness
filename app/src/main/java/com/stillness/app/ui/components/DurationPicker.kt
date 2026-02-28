package com.stillness.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.stillness.app.ui.theme.*

@Composable
fun CustomDurationButton(
    currentSeconds: Int?,
    onDurationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    // Check if current selection matches any preset (5m, 15m, 30m, 1h)
    val presetSeconds = listOf(5 * 60, 15 * 60, 30 * 60, 60 * 60)
    val isCustomSelection = currentSeconds != null && !presetSeconds.contains(currentSeconds)

    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val shape = RoundedCornerShape(24.dp)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(shape)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { showPicker = true }
            .padding(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (isCustomSelection) {
                    formatDuration(currentSeconds!!)
                } else {
                    "Custom"
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "\u25BC",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
    
    if (showPicker) {
        DurationPickerDialog(
            initialTotalSeconds = currentSeconds ?: (10 * 60),
            onDismiss = { showPicker = false },
            onConfirm = { totalSeconds ->
                onDurationSelected(totalSeconds)
                showPicker = false
            }
        )
    }
}

@Composable
fun DurationPickerDialog(
    initialTotalSeconds: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var hours by remember { mutableIntStateOf(initialTotalSeconds / 3600) }
    var minutes by remember { mutableIntStateOf((initialTotalSeconds % 3600) / 60) }
    var seconds by remember { mutableIntStateOf(initialTotalSeconds % 60) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Set Duration",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hours picker
                    NumberPicker(
                        value = hours,
                        range = 0..12,
                        onValueChange = { hours = it },
                        label = "hr"
                    )
                    
                    Text(
                        text = ":",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    
                    // Minutes picker
                    NumberPicker(
                        value = minutes,
                        range = 0..59,
                        onValueChange = { minutes = it },
                        label = "min"
                    )

                    Text(
                        text = ":",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.outline
                    )

                    // Seconds picker
                    NumberPicker(
                        value = seconds,
                        range = 0..59,
                        onValueChange = { seconds = it },
                        label = "sec"
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Cancel",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Button(
                        onClick = {
                            val totalSeconds = hours * 3600 + minutes * 60 + seconds
                            if (totalSeconds > 0) {
                                onConfirm(totalSeconds)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Set",
                            color = MaterialTheme.colorScheme.background
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NumberPicker(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = {
                if (value < range.last) onValueChange(value + 1)
            }
        ) {
            Text(
                text = "\u25B2",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Text(
            text = String.format("%02d", value),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
        
        IconButton(
            onClick = {
                if (value > range.first) onValueChange(value - 1)
            }
        ) {
            Text(
                text = "\u25BC",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Format total seconds into a human-readable duration string.
 * Examples: "5m", "1h 30m", "2m 15s", "45s", "1h 5m 30s"
 */
fun formatDuration(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val mins = (totalSeconds % 3600) / 60
    val secs = totalSeconds % 60

    return buildString {
        if (hours > 0) append("${hours}h")
        if (mins > 0) {
            if (isNotEmpty()) append(" ")
            append("${mins}m")
        }
        if (secs > 0) {
            if (isNotEmpty()) append(" ")
            append("${secs}s")
        }
        if (isEmpty()) append("0s")
    }
}
