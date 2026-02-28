package com.stillness.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stillness.app.ui.components.*
import com.stillness.app.ui.theme.*
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
    
    // Handle timer completion - use user's selected vibration pattern
    LaunchedEffect(timerState.isCompleted) {
        if (timerState.isCompleted) {
            VibrationHelper.vibrate(context, selectedPattern, repeat = 2)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientMiddle, GradientEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
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
                    color = TextPrimary
                )
                
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextSecondary
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
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Quick timer buttons (hidden when running)
            AnimatedVisibility(
                visible = !timerState.isRunning && !timerState.isPaused,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    QuickTimerButtons(
                        selectedMinutes = timerState.selectedMinutes,
                        onOptionSelected = { viewModel.selectDuration(it) }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    CustomDurationButton(
                        currentMinutes = timerState.selectedMinutes,
                        onDurationSelected = { viewModel.selectDuration(it) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
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
                enabled = timerState.selectedMinutes != null
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
                        text = "🧘",
                        style = MaterialTheme.typography.displayLarge
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Session Complete",
                        style = MaterialTheme.typography.headlineMedium,
                        color = AccentLavender
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextButton(
                        onClick = {
                            VibrationHelper.cancel(context)
                            viewModel.resetAfterCompletion()
                        }
                    ) {
                        Text(
                            text = "Dismiss",
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
