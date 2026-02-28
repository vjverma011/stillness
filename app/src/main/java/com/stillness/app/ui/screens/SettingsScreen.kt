package com.stillness.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stillness.app.ui.theme.*
import com.stillness.app.util.VibrationHelper
import com.stillness.app.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val selectedPattern by settingsViewModel.vibrationPattern.collectAsState()
    val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()
    val context = LocalContext.current

    // Adaptive padding
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val horizontalPadding = (screenWidth * 0.06f).coerceIn(12.dp, 32.dp)

    // Split patterns into groups
    val continuousPatterns = VibrationHelper.VibrationPattern.entries.filter { it.repeating }
    val autoStopPatterns = VibrationHelper.VibrationPattern.entries.filter { !it.repeating }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = horizontalPadding)
        ) {
            Spacer(modifier = Modifier.height(horizontalPadding / 2))

            // Top bar with back arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Scrollable content area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // ── Continuous Patterns Section ─────────────────────
                Text(
                    text = "CONTINUOUS PATTERNS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                )
                Text(
                    text = "Loop until you dismiss the completion screen",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    continuousPatterns.forEachIndexed { index, pattern ->
                        VibrationPatternItem(
                            pattern = pattern,
                            isSelected = pattern == selectedPattern,
                            onClick = {
                                settingsViewModel.setVibrationPattern(pattern)
                                // Preview: play once (no looping) so user gets a taste
                                VibrationHelper.vibrate(context, pattern, repeatOverride = -1)
                            }
                        )

                        if (index < continuousPatterns.size - 1) {
                            Divider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                thickness = 0.5.dp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Auto-Stop Patterns Section ──────────────────────
                Text(
                    text = "AUTO-STOP PATTERNS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                )
                Text(
                    text = "Play a set number of times, then go silent",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    autoStopPatterns.forEachIndexed { index, pattern ->
                        VibrationPatternItem(
                            pattern = pattern,
                            isSelected = pattern == selectedPattern,
                            onClick = {
                                settingsViewModel.setVibrationPattern(pattern)
                                // Auto-stop patterns play their full sequence naturally
                                VibrationHelper.vibrate(context, pattern)
                            }
                        )

                        if (index < autoStopPatterns.size - 1) {
                            Divider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                thickness = 0.5.dp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tap a pattern to preview and select it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // -- Appearance Section --
                Text(
                    text = "APPEARANCE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dark Theme",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isDarkTheme) "Currently using dark theme" else "Currently using light theme",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { settingsViewModel.setDarkTheme(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentLavender,
                            checkedTrackColor = AccentLavenderDark,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // App version footer (stays at bottom)
            Text(
                text = "Stillness v0.1",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun VibrationPatternItem(
    pattern: VibrationHelper.VibrationPattern,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected)
            AccentLavender.copy(alpha = 0.1f)
        else
            MaterialTheme.colorScheme.surface,
        animationSpec = tween(200),
        label = "patternBgColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pattern.displayName,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) AccentLavender else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = patternDescription(pattern),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isSelected) {
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(AccentLavender),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun patternDescription(pattern: VibrationHelper.VibrationPattern): String {
    return when (pattern) {
        VibrationHelper.VibrationPattern.GENTLE_PULSE -> "Soft, evenly spaced pulses"
        VibrationHelper.VibrationPattern.QUICK_PULSE -> "Quick succession of short bursts"
        VibrationHelper.VibrationPattern.WAVE -> "Gradually lengthening pulses"
        VibrationHelper.VibrationPattern.ESCALATING -> "Increasing intensity over time"
        VibrationHelper.VibrationPattern.GENTLE_BELLS -> "Three sets of soft double-taps"
        VibrationHelper.VibrationPattern.TRIPLE_CHIME -> "Five sets of three firm pulses"
        VibrationHelper.VibrationPattern.FADE_OUT -> "Four bursts that fade to silence"
    }
}
