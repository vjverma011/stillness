package com.stillness.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.stillness.app.MainActivity
import com.stillness.app.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Foreground service that keeps the meditation timer alive when the app is backgrounded.
 *
 * Communication pattern:
 *  - **Inbound**: Intent actions (START, PAUSE, RESUME, STOP) sent via [Context.startService].
 *  - **Outbound**: Companion [timerState] StateFlow observed by [TimerViewModel] / UI.
 *
 * The notification displays remaining time and a Pause/Resume action button.
 */
class TimerForegroundService : Service() {

    companion object {
        private const val TAG = "TimerFgService"

        const val CHANNEL_ID = "stillness_timer_channel"
        const val NOTIFICATION_ID = 1

        // Intent actions
        const val ACTION_START = "com.stillness.app.action.START"
        const val ACTION_PAUSE = "com.stillness.app.action.PAUSE"
        const val ACTION_RESUME = "com.stillness.app.action.RESUME"
        const val ACTION_STOP = "com.stillness.app.action.STOP"

        // Intent extras
        const val EXTRA_TOTAL_MILLIS = "extra_total_millis"
        const val EXTRA_REMAINING_MILLIS = "extra_remaining_millis"

        // Shared timer state — observed by ViewModel / UI
        private val _timerState = MutableStateFlow(ServiceTimerState())
        val timerState: StateFlow<ServiceTimerState> = _timerState.asStateFlow()

        /** Create the notification channel. Call once at app startup. */
        fun createNotificationChannel(context: Context) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Meditation Timer",
                NotificationManager.IMPORTANCE_LOW   // no sound/vibration for ongoing updates
            ).apply {
                description = "Shows the running meditation timer"
                setShowBadge(false)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private var countDownTimer: CountDownTimer? = null
    private var totalMillis: Long = 0L
    private var pausedRemainingMillis: Long = 0L

    // ── Lifecycle ───────────────────────────────────────────────────────

    override fun onBind(intent: Intent?): IBinder? = null  // Not a bound service

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                totalMillis = intent.getLongExtra(EXTRA_TOTAL_MILLIS, 0L)
                val remaining = intent.getLongExtra(EXTRA_REMAINING_MILLIS, totalMillis)
                Log.d(TAG, "START: total=${totalMillis}ms, remaining=${remaining}ms")
                startForegroundWithNotification(remaining)
                startCountdown(remaining)
            }
            ACTION_PAUSE -> {
                Log.d(TAG, "PAUSE")
                pauseCountdown()
            }
            ACTION_RESUME -> {
                Log.d(TAG, "RESUME")
                resumeCountdown()
            }
            ACTION_STOP -> {
                Log.d(TAG, "STOP")
                stopCountdown()
                stopSelf()
            }
            else -> {
                Log.d(TAG, "Unknown action: ${intent?.action}")
            }
        }
        return START_NOT_STICKY  // Don't auto-restart if killed
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        Log.d(TAG, "Service destroyed")
    }

    // ── Countdown logic ─────────────────────────────────────────────────

    private fun startCountdown(startMillis: Long) {
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(startMillis, 100) {
            override fun onTick(millisUntilFinished: Long) {
                _timerState.value = ServiceTimerState(
                    totalTimeMillis = totalMillis,
                    remainingTimeMillis = millisUntilFinished,
                    isRunning = true,
                    isPaused = false,
                    isCompleted = false
                )
                // Update notification every ~1 second (avoid flooding)
                // CountDownTimer ticks every 100ms; update notification when seconds change
                val prevSec = (_timerState.value.remainingTimeMillis / 1000)
                val curSec = millisUntilFinished / 1000
                if (prevSec != curSec || curSec == millisUntilFinished / 1000) {
                    updateNotification(millisUntilFinished, isPaused = false)
                }
            }

            override fun onFinish() {
                Log.d(TAG, "Timer completed!")
                _timerState.value = ServiceTimerState(
                    totalTimeMillis = totalMillis,
                    remainingTimeMillis = 0L,
                    isRunning = false,
                    isPaused = false,
                    isCompleted = true
                )
                updateNotification(0L, isPaused = false, completed = true)
                // Don't stopSelf() here — let the UI handle completion (vibration, dismiss).
                // The UI will send ACTION_STOP when the user dismisses.
            }
        }.start()

        _timerState.value = ServiceTimerState(
            totalTimeMillis = totalMillis,
            remainingTimeMillis = startMillis,
            isRunning = true,
            isPaused = false,
            isCompleted = false
        )
    }

    private fun pauseCountdown() {
        countDownTimer?.cancel()
        pausedRemainingMillis = _timerState.value.remainingTimeMillis

        _timerState.value = _timerState.value.copy(
            isRunning = true,
            isPaused = true
        )
        updateNotification(pausedRemainingMillis, isPaused = true)
    }

    private fun resumeCountdown() {
        startCountdown(pausedRemainingMillis)
    }

    private fun stopCountdown() {
        countDownTimer?.cancel()
        pausedRemainingMillis = 0L

        _timerState.value = ServiceTimerState(
            totalTimeMillis = totalMillis,
            remainingTimeMillis = totalMillis,
            isRunning = false,
            isPaused = false,
            isCompleted = false
        )
    }

    // ── Notification ────────────────────────────────────────────────────

    private fun startForegroundWithNotification(remainingMillis: Long) {
        val notification = buildNotification(remainingMillis, isPaused = false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // API 34+ requires foreground service type
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(remainingMillis: Long, isPaused: Boolean, completed: Boolean = false) {
        val notification = buildNotification(remainingMillis, isPaused, completed)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(
        remainingMillis: Long,
        isPaused: Boolean,
        completed: Boolean = false
    ): Notification {
        // Tap notification → open app
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPending = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(openPending)
            .setOngoing(!completed)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)

        if (completed) {
            builder.setContentTitle("Session Complete")
            builder.setContentText("Your meditation session has ended")
            // No action buttons on completed notification — user taps to open app
        } else {
            val timeText = formatMillisForNotification(remainingMillis)
            builder.setContentTitle("Stillness — Meditating")
            builder.setContentText(timeText)

            // Show progress bar
            val totalSec = (totalMillis / 1000).toInt()
            val remainSec = (remainingMillis / 1000).toInt()
            val progressSec = totalSec - remainSec
            builder.setProgress(totalSec, progressSec, false)

            // Pause / Resume action
            if (isPaused) {
                val resumeIntent = Intent(this, TimerForegroundService::class.java).apply {
                    action = ACTION_RESUME
                }
                val resumePending = PendingIntent.getService(
                    this, 1, resumeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(0, "Resume", resumePending)
            } else {
                val pauseIntent = Intent(this, TimerForegroundService::class.java).apply {
                    action = ACTION_PAUSE
                }
                val pausePending = PendingIntent.getService(
                    this, 1, pauseIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(0, "Pause", pausePending)
            }

            // Stop action
            val stopIntent = Intent(this, TimerForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            val stopPending = PendingIntent.getService(
                this, 2, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, "Stop", stopPending)
        }

        return builder.build()
    }

    private fun formatMillisForNotification(millis: Long): String {
        val totalSeconds = (millis + 999) / 1000  // round up so "0s" doesn't show while still ticking
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 -> String.format("%d:%02d:%02d remaining", hours, minutes, seconds)
            minutes > 0 -> String.format("%d:%02d remaining", minutes, seconds)
            else -> String.format("0:%02d remaining", seconds)
        }
    }
}

/**
 * Timer state exposed by the foreground service.
 * Mirrors [com.stillness.app.viewmodel.TimerState] but lives outside the ViewModel.
 */
data class ServiceTimerState(
    val totalTimeMillis: Long = 0L,
    val remainingTimeMillis: Long = 0L,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false
)
