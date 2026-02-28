package com.stillness.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stillness.app.util.PreferencesManager
import com.stillness.app.util.VibrationHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)

    val vibrationPattern: StateFlow<VibrationHelper.VibrationPattern> =
        preferencesManager.vibrationPatternFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = VibrationHelper.VibrationPattern.GENTLE
        )

    val isDarkTheme: StateFlow<Boolean> =
        preferencesManager.darkThemeFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun setVibrationPattern(pattern: VibrationHelper.VibrationPattern) {
        viewModelScope.launch {
            preferencesManager.setVibrationPattern(pattern)
        }
    }

    fun setDarkTheme(isDark: Boolean) {
        viewModelScope.launch {
            preferencesManager.setDarkTheme(isDark)
        }
    }
}
