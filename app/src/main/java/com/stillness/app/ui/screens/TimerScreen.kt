package com.stillness.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stillness.app.ui.components.*
import com.stillness.app.util.VibrationHelper
import com.stillness.app.viewmodel.SettingsViewModel
import com.stillness.app.viewmodel.TimerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    onNavigateToSettings: () -> Unit = {},
    viewModel: TimerViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val timerState by viewModel.timerState.collectAsState()
    val selectedPattern by settingsViewModel.vibrationPattern.collectAsState()
    val context = LocalContext.current

    // Adaptive spacing based on screen height
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val horizontalPadding = (screenWidth * 0.06f).coerceIn(12.dp, 32.dp)
    val sectionSpacing = (screenHeight * 0.04f).coerceIn(16.dp, 56.dp)
    
    // Handle timer completion — vibrate using the pattern's own repeat behaviour
    // Continuous patterns loop until dismissed; auto-stop patterns play once and go silent.
    LaunchedEffect(timerState.isCompleted) {
        if (timerState.isCompleted) {
            VibrationHelper.vibrate(context, selectedPattern)
        }
    }
    
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.background,
                        colorScheme.surfaceVariant,
                        colorScheme.surfaceVariant
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = horizontalPadding, vertical = horizontalPadding / 2),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stillness",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colorScheme.onBackground
                )
                
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Circular timer
            CircularTimer(
                totalTimeMillis = timerState.totalTimeMillis,
                remainingTimeMillis = timerState.remainingTimeMillis,
                isRunning = timerState.isRunning && !timerState.isPaused
            )
            
            Spacer(modifier = Modifier.height(sectionSpacing))
            
            // Quick timer buttons (hidden when running)
            AnimatedVisibility(
                visible = !timerState.isRunning && !timerState.isPaused,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Preset duration buttons
                    QuickTimerButtons(
                        selectedSeconds = timerState.selectedSeconds,
                        onOptionSelected = { viewModel.selectDuration(it) }
                    )
                    
                    Spacer(modifier = Modifier.height(sectionSpacing / 4))

                    // Quick-add chips (+15s, +30s, +1m, +5m)
                    QuickAddChips(
                        onAddSeconds = { viewModel.addSeconds(it) }
                    )

                    Spacer(modifier = Modifier.height(sectionSpacing / 4))
                    
                    // Custom duration button
                    CustomDurationButton(
                        currentSeconds = timerState.selectedSeconds,
                        onDurationSelected = { viewModel.selectDuration(it) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(sectionSpacing))
            
            // Start/Pause/Stop button
            StartButton(
                isRunning = timerState.isRunning,
                isPaused = timerState.isPaused,
                onStart = { viewModel.startTimer() },
                onPause = { viewModel.pauseTimer() },
                onResume = { viewModel.resumeTimer() },
                onStop = {
                    VibrationHelper.cancel(context)
                    viewModel.stopTimer()
                },
                enabled = timerState.selectedSeconds != null
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Completion message
            AnimatedVisibility(
                visible = timerState.isCompleted,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "\uD83E\uDDD8",
                        style = MaterialTheme.typography.displayLarge
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Session Complete",
                        style = MaterialTheme.typography.headlineMedium,
                        color = colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    val dismissBorderColor = colorScheme.outline.copy(alpha = 0.4f)
                    val dismissShape = RoundedCornerShape(20.dp)

                    TextButton(
                        onClick = {
                            VibrationHelper.cancel(context)
                            viewModel.resetAfterCompletion()
                        },
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = dismissBorderColor,
                                shape = dismissShape
                            ),
                        shape = dismissShape
                    ) {
                        Text(
                            text = "Dismiss",
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
