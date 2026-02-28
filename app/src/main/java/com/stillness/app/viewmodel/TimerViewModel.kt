package com.stillness.app.viewmodel

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TimerState(
    val selectedMinutes: Int? = null,
    val totalTimeMillis: Long = 0L,
    val remainingTimeMillis: Long = 0L,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false
)

class TimerViewModel : ViewModel() {
    
    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()
    
    private var countDownTimer: CountDownTimer? = null
    private var pausedTimeRemaining: Long = 0L
    
    fun selectDuration(minutes: Int) {
        val millis = minutes * 60 * 1000L
        _timerState.value = TimerState(
            selectedMinutes = minutes,
            totalTimeMillis = millis,
            remainingTimeMillis = millis,
            isRunning = false,
            isPaused = false,
            isCompleted = false
        )
    }
    
    fun startTimer() {
        val state = _timerState.value
        if (state.totalTimeMillis <= 0) return
        
        val startTime = if (state.isPaused) pausedTimeRemaining else state.totalTimeMillis
        
        countDownTimer = object : CountDownTimer(startTime, 100) {
            override fun onTick(millisUntilFinished: Long) {
                _timerState.value = _timerState.value.copy(
                    remainingTimeMillis = millisUntilFinished,
                    isRunning = true,
                    isPaused = false
                )
            }
            
            override fun onFinish() {
                _timerState.value = _timerState.value.copy(
                    remainingTimeMillis = 0,
                    isRunning = false,
                    isPaused = false,
                    isCompleted = true
                )
            }
        }.start()
        
        _timerState.value = _timerState.value.copy(
            isRunning = true,
            isPaused = false
        )
    }
    
    fun pauseTimer() {
        countDownTimer?.cancel()
        pausedTimeRemaining = _timerState.value.remainingTimeMillis
        _timerState.value = _timerState.value.copy(
            isRunning = true,
            isPaused = true
        )
    }
    
    fun resumeTimer() {
        startTimer()
    }
    
    fun stopTimer() {
        countDownTimer?.cancel()
        pausedTimeRemaining = 0L
        _timerState.value = _timerState.value.copy(
            remainingTimeMillis = _timerState.value.totalTimeMillis,
            isRunning = false,
            isPaused = false,
            isCompleted = false
        )
    }
    
    fun resetAfterCompletion() {
        _timerState.value = _timerState.value.copy(
            remainingTimeMillis = _timerState.value.totalTimeMillis,
            isCompleted = false
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }
}
