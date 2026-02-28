package com.stillness.app.ui.components

import androidx.compose.foundation.background
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
    currentMinutes: Int?,
    onDurationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(ButtonBackground.copy(alpha = 0.5f))
            .clickable { showPicker = true }
            .padding(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (currentMinutes != null && !listOf(5, 15, 30, 60).contains(currentMinutes)) {
                    formatMinutes(currentMinutes)
                } else {
                    "Custom"
                },
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )
            Text(
                text = "▼",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
    
    if (showPicker) {
        DurationPickerDialog(
            initialMinutes = currentMinutes ?: 10,
            onDismiss = { showPicker = false },
            onConfirm = { minutes ->
                onDurationSelected(minutes)
                showPicker = false
            }
        )
    }
}

@Composable
fun DurationPickerDialog(
    initialMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var hours by remember { mutableIntStateOf(initialMinutes / 60) }
    var minutes by remember { mutableIntStateOf(initialMinutes % 60) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DarkSurface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Set Duration",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
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
                        color = TextMuted
                    )
                    
                    // Minutes picker
                    NumberPicker(
                        value = minutes,
                        range = 0..59,
                        onValueChange = { minutes = it },
                        label = "min"
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Cancel",
                            color = TextSecondary
                        )
                    }
                    
                    Button(
                        onClick = {
                            val totalMinutes = hours * 60 + minutes
                            if (totalMinutes > 0) {
                                onConfirm(totalMinutes)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentLavender
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Set",
                            color = DarkBackground
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
                text = "▲",
                style = MaterialTheme.typography.labelLarge,
                color = AccentLavender
            )
        }
        
        Text(
            text = String.format("%02d", value),
            style = MaterialTheme.typography.displayMedium,
            color = TextPrimary
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
        
        IconButton(
            onClick = {
                if (value > range.first) onValueChange(value - 1)
            }
        ) {
            Text(
                text = "▼",
                style = MaterialTheme.typography.labelLarge,
                color = AccentLavender
            )
        }
    }
}

fun formatMinutes(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60
    return if (hours > 0) {
        if (mins > 0) "${hours}h ${mins}m" else "${hours}h"
    } else {
        "${mins}m"
    }
}
