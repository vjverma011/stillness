package com.stillness.app.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

object VibrationHelper {
    
    // Vibration patterns (timings in milliseconds)
    // Format: [delay, vibrate, pause, vibrate, pause, ...]
    
    val PATTERN_GENTLE = longArrayOf(0, 200, 200, 200, 200, 200)
    val PATTERN_PULSE = longArrayOf(0, 100, 100, 100, 100, 100, 100, 100, 400, 300)
    val PATTERN_WAVE = longArrayOf(0, 100, 50, 150, 50, 200, 50, 250, 50, 300)
    val PATTERN_ESCALATING = longArrayOf(0, 100, 200, 150, 200, 200, 200, 300, 200, 400)
    
    enum class VibrationPattern(val displayName: String, val pattern: LongArray) {
        GENTLE("Gentle Pulse", PATTERN_GENTLE),
        PULSE("Quick Pulse", PATTERN_PULSE),
        WAVE("Wave", PATTERN_WAVE),
        ESCALATING("Escalating", PATTERN_ESCALATING)
    }
    
    @Suppress("DEPRECATION")
    fun vibrate(context: Context, pattern: VibrationPattern = VibrationPattern.GENTLE, repeat: Int = 0) {
        Log.d("VibrationHelper", "Vibrating with pattern: ${pattern.displayName}, repeat: $repeat")
        
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        if (!vibrator.hasVibrator()) {
            Log.d("VibrationHelper", "No vibrator available on this device/emulator")
            return
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern.pattern, repeat))
        } else {
            vibrator.vibrate(pattern.pattern, repeat)
        }
    }
    
    @Suppress("DEPRECATION")
    fun cancel(context: Context) {
        Log.d("VibrationHelper", "Cancelling vibration")
        
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        vibrator.cancel()
    }
}
