package com.stillness.app.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "stillness_settings")

class PreferencesManager(private val context: Context) {

    companion object {
        private val VIBRATION_PATTERN_KEY = stringPreferencesKey("vibration_pattern")
        private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
    }

    val vibrationPatternFlow: Flow<VibrationHelper.VibrationPattern> = context.dataStore.data.map { preferences ->
        val patternName = preferences[VIBRATION_PATTERN_KEY] ?: VibrationHelper.VibrationPattern.GENTLE_PULSE.name
        try {
            VibrationHelper.VibrationPattern.valueOf(patternName)
        } catch (e: IllegalArgumentException) {
            // Fallback for renamed or removed patterns (e.g. old "GENTLE" → "GENTLE_PULSE")
            VibrationHelper.VibrationPattern.GENTLE_PULSE
        }
    }

    val darkThemeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_THEME_KEY] ?: true // Default to dark theme
    }

    suspend fun setVibrationPattern(pattern: VibrationHelper.VibrationPattern) {
        context.dataStore.edit { preferences ->
            preferences[VIBRATION_PATTERN_KEY] = pattern.name
        }
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_THEME_KEY] = isDark
        }
    }
}
