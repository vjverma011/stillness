package com.stillness.app.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

object VibrationHelper {

    // ── Continuous patterns (loop until dismissed) ──────────────────────
    // Format: [delay, vibrate, pause, vibrate, pause, ...]
    // These repeat from index 0 when used with repeat = 0.

    private val PATTERN_GENTLE_PULSE = longArrayOf(0, 200, 300, 200, 300, 200, 600)
    // Soft evenly-spaced pulses with a longer trailing gap to keep the loop breathing.

    private val PATTERN_QUICK_PULSE = longArrayOf(0, 80, 80, 80, 80, 80, 80, 80, 500)
    // Rapid-fire short bursts followed by a half-second rest.

    private val PATTERN_WAVE = longArrayOf(0, 100, 50, 150, 50, 200, 50, 250, 50, 300, 400)
    // Gradually lengthening pulses that crest then pause before the next cycle.

    private val PATTERN_ESCALATING = longArrayOf(0, 100, 200, 150, 200, 200, 200, 300, 200, 400, 400)
    // Each burst stronger/longer than the last, with a pause at the end.

    // ── Auto-stop patterns (play once and finish) ──────────────────────
    // All repetitions are baked into the array; played with repeat = -1.

    // Gentle Bells ×3: three sets of soft double-taps separated by silences
    private val PATTERN_GENTLE_BELLS = longArrayOf(
        0,   120, 100, 120, 600,   // set 1: tap-tap … pause
        120, 100, 120, 600,        // set 2
        120, 100, 120              // set 3 (no trailing pause needed)
    )

    // Triple Chime ×5: five sets of boom-boom-boom with rests between
    private val PATTERN_TRIPLE_CHIME = longArrayOf(
        0,   150, 120, 150, 120, 150, 700,   // set 1: boom-boom-boom … rest
        150, 120, 150, 120, 150, 700,         // set 2
        150, 120, 150, 120, 150, 700,         // set 3
        150, 120, 150, 120, 150, 700,         // set 4
        150, 120, 150, 120, 150               // set 5
    )

    // Fade Out: four bursts that get progressively shorter, like a sound fading away
    private val PATTERN_FADE_OUT = longArrayOf(
        0, 400, 300,   // long burst
        300, 300,      // medium burst
        200, 300,      // shorter burst
        100            // quick tap — done
    )

    /**
     * All available vibration patterns.
     *
     * @param displayName  Human-readable label shown in the UI.
     * @param pattern      Waveform timing array for [VibrationEffect.createWaveform].
     * @param repeating    `true` → loops continuously until cancelled (repeat index 0).
     *                     `false` → plays the baked-in array once and stops (repeat = -1).
     */
    enum class VibrationPattern(
        val displayName: String,
        val pattern: LongArray,
        val repeating: Boolean
    ) {
        // Continuous (loop until dismissed)
        GENTLE_PULSE("Gentle Pulse",  PATTERN_GENTLE_PULSE,  repeating = true),
        QUICK_PULSE("Quick Pulse",    PATTERN_QUICK_PULSE,   repeating = true),
        WAVE("Wave",                  PATTERN_WAVE,          repeating = true),
        ESCALATING("Escalating",      PATTERN_ESCALATING,    repeating = true),

        // Auto-stop (play finite times, then silence)
        GENTLE_BELLS("Gentle Bells",  PATTERN_GENTLE_BELLS,  repeating = false),
        TRIPLE_CHIME("Triple Chime",  PATTERN_TRIPLE_CHIME,  repeating = false),
        FADE_OUT("Fade Out",          PATTERN_FADE_OUT,      repeating = false);
    }

    /**
     * Trigger vibration using the pattern's own repeat behaviour.
     *
     * - Continuous patterns loop from index 0 (`repeat = 0`).
     * - Auto-stop patterns play once (`repeat = -1`).
     *
     * The caller no longer needs to supply a `repeat` argument for normal
     * usage — it is derived from [VibrationPattern.repeating].
     *
     * @param repeatOverride  Optional. If non-null, overrides the pattern's
     *                        default repeat value (useful for one-shot previews
     *                        of continuous patterns).
     */
    fun vibrate(
        context: Context,
        pattern: VibrationPattern = VibrationPattern.GENTLE_PULSE,
        repeatOverride: Int? = null
    ) {
        val repeat = repeatOverride ?: if (pattern.repeating) 0 else -1

        Log.d(
            "VibrationHelper",
            "Vibrating: ${pattern.displayName} | repeating=${pattern.repeating} | repeat=$repeat | timings=${pattern.pattern.toList()}"
        )

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

        vibrator.vibrate(VibrationEffect.createWaveform(pattern.pattern, repeat))
    }

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
