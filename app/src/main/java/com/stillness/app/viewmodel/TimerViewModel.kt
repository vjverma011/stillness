package com.stillness.app.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stillness.app.service.TimerForegroundService
import com.stillness.app.service.ServiceTimerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TimerState(
    val selectedSeconds: Int? = null,
    val totalTimeMillis: Long = 0L,
    val remainingTimeMillis: Long = 0L,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false
)

/**
 * ViewModel that acts as a thin bridge between the UI and [TimerForegroundService].
 *
 * - Duration selection and addSeconds happen locally (no service needed yet).
 * - start/pause/resume/stop send intents to the foreground service.
 * - Timer ticks are observed from [TimerForegroundService.timerState] and merged
 *   with the locally-held [selectedSeconds].
 */
class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    init {
        // Observe the service's timer state and merge with our selectedSeconds
        viewModelScope.launch {
            TimerForegroundService.timerState.collect { serviceState ->
                val current = _timerState.value
                _timerState.value = current.copy(
                    totalTimeMillis = if (serviceState.isRunning || serviceState.isCompleted)
                        serviceState.totalTimeMillis else current.totalTimeMillis,
                    remainingTimeMillis = if (serviceState.isRunning || serviceState.isPaused || serviceState.isCompleted)
                        serviceState.remainingTimeMillis else current.remainingTimeMillis,
                    isRunning = serviceState.isRunning,
                    isPaused = serviceState.isPaused,
                    isCompleted = serviceState.isCompleted
                )
            }
        }
    }

    /**
     * Set exact duration in total seconds.
     */
    fun selectDuration(totalSeconds: Int) {
        val millis = totalSeconds * 1000L
        _timerState.value = TimerState(
            selectedSeconds = totalSeconds,
            totalTimeMillis = millis,
            remainingTimeMillis = millis,
            isRunning = false,
            isPaused = false,
            isCompleted = false
        )
    }

    /**
     * Add seconds to the current selection (or start from 0).
     * Only works when the timer is not running.
     */
    fun addSeconds(seconds: Int) {
        val state = _timerState.value
        if (state.isRunning) return
        val current = state.selectedSeconds ?: 0
        val newTotal = (current + seconds).coerceIn(1, 43200) // max 12 hours
        selectDuration(newTotal)
    }

    fun startTimer() {
        val state = _timerState.value
        if (state.totalTimeMillis <= 0) return

        val remaining = if (state.isPaused) state.remainingTimeMillis else state.totalTimeMillis

        val intent = Intent(getApplication(), TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_START
            putExtra(TimerForegroundService.EXTRA_TOTAL_MILLIS, state.totalTimeMillis)
            putExtra(TimerForegroundService.EXTRA_REMAINING_MILLIS, remaining)
        }
        startServiceCompat(intent)

        // Optimistic local update so UI responds immediately
        _timerState.value = state.copy(isRunning = true, isPaused = false)
    }

    fun pauseTimer() {
        val intent = Intent(getApplication(), TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_PAUSE
        }
        startServiceCompat(intent)

        _timerState.value = _timerState.value.copy(isRunning = true, isPaused = true)
    }

    fun resumeTimer() {
        val intent = Intent(getApplication(), TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_RESUME
        }
        startServiceCompat(intent)

        _timerState.value = _timerState.value.copy(isRunning = true, isPaused = false)
    }

    fun stopTimer() {
        val intent = Intent(getApplication(), TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_STOP
        }
        startServiceCompat(intent)

        _timerState.value = _timerState.value.copy(
            remainingTimeMillis = _timerState.value.totalTimeMillis,
            isRunning = false,
            isPaused = false,
            isCompleted = false
        )
    }

    fun resetAfterCompletion() {
        // Stop the foreground service (clears notification)
        val intent = Intent(getApplication(), TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_STOP
        }
        try {
            startServiceCompat(intent)
        } catch (_: Exception) {
            // Service may already be stopped
        }

        _timerState.value = _timerState.value.copy(
            remainingTimeMillis = _timerState.value.totalTimeMillis,
            isCompleted = false
        )
    }

    private fun startServiceCompat(intent: Intent) {
        val context = getApplication<Application>()
        // On API 26+ we must use startForegroundService for foreground services
        context.startForegroundService(intent)
    }
}
