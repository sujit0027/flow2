package com.example.data.timer

import android.content.Context
import android.content.SharedPreferences
import android.os.SystemClock
import com.example.data.local.model.RoutineTask
import com.example.data.local.model.TaskCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

data class TimerSessionSnapshot(
    val taskId: Long = 0L,
    val taskTitle: String = "",
    val subject: String = "",
    val category: TaskCategory = TaskCategory.STUDY,
    val minDurationSeconds: Long = 0L,
    val extensionTargetSeconds: Long = 0L,
    val isCompulsory: Boolean = true,
    val isRunning: Boolean = false,
    val accumulatedActiveSeconds: Long = 0L,
    val currentElapsedSeconds: Long = 0L,
    val isMinCompleted: Boolean = false,
    val remainingCompulsorySeconds: Long = 0L,
    val flowOvertimeSeconds: Long = 0L,
    val isAwaitingContinue: Boolean = false,
    val awaitingContinueSecondsLeft: Int = 30,
    val isWakeAlarmRinging: Boolean = false,
    val hasContinuedIntoFlow: Boolean = false
)

class TimerEngine private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("flowstudy_timer_state", Context.MODE_PRIVATE)

    private val scope = CoroutineScope(Dispatchers.Default)
    private var tickerJob: Job? = null

    private val _snapshot = MutableStateFlow(restoreSnapshot())
    val snapshot: StateFlow<TimerSessionSnapshot> = _snapshot.asStateFlow()

    init {
        // Start reconciliation ticker if it was running before process recreation
        if (_snapshot.value.isRunning) {
            startTicker()
        }
    }

    fun startSession(task: RoutineTask) {
        val nowElapsed = SystemClock.elapsedRealtime()
        val nowWall = System.currentTimeMillis()
        val bootTime = nowWall - nowElapsed

        val minSecs = task.minDurationSeconds
        val extSecs = task.extensionTargetMinutes * 60L

        prefs.edit()
            .putLong(KEY_TASK_ID, task.id)
            .putString(KEY_TITLE, task.title)
            .putString(KEY_SUBJECT, task.subject)
            .putString(KEY_CATEGORY, task.category.name)
            .putLong(KEY_MIN_DURATION_SECONDS, minSecs)
            .putLong(KEY_EXTENSION_TARGET_SECONDS, extSecs)
            .putBoolean(KEY_IS_COMPULSORY, task.isCompulsory)
            .putBoolean(KEY_IS_RUNNING, true)
            .putLong(KEY_ACCUMULATED_ACTIVE_SECONDS, task.accumulatedDurationSeconds)
            .putLong(KEY_STINT_START_ELAPSED, nowElapsed)
            .putLong(KEY_STINT_START_WALL, nowWall)
            .putLong(KEY_BOOT_TIME, bootTime)
            .apply()

        updateSnapshot()
        startTicker()
    }

    fun resumeSession() {
        if (!_snapshot.value.isRunning && _snapshot.value.taskId != 0L) {
            val nowElapsed = SystemClock.elapsedRealtime()
            val nowWall = System.currentTimeMillis()
            val bootTime = nowWall - nowElapsed

            prefs.edit()
                .putBoolean(KEY_IS_RUNNING, true)
                .putLong(KEY_STINT_START_ELAPSED, nowElapsed)
                .putLong(KEY_STINT_START_WALL, nowWall)
                .putLong(KEY_BOOT_TIME, bootTime)
                .apply()

            updateSnapshot()
            startTicker()
        }
    }

    fun pauseSession() {
        if (_snapshot.value.isRunning) {
            val currentElapsed = calculateElapsedFromPrefs()
            prefs.edit()
                .putBoolean(KEY_IS_RUNNING, false)
                .putLong(KEY_ACCUMULATED_ACTIVE_SECONDS, currentElapsed)
                .putLong(KEY_STINT_START_ELAPSED, 0L)
                .apply()

            tickerJob?.cancel()
            updateSnapshot()
        }
    }

    fun addExtensionSeconds(seconds: Long) {
        val currentMinDuration = prefs.getLong(KEY_MIN_DURATION_SECONDS, 0L)
        val currentExtTarget = prefs.getLong(KEY_EXTENSION_TARGET_SECONDS, 0L)
        prefs.edit()
            .putLong(KEY_MIN_DURATION_SECONDS, currentMinDuration + seconds)
            .putLong(KEY_EXTENSION_TARGET_SECONDS, currentExtTarget + seconds)
            .apply()
        updateSnapshot()
    }

    fun finishSession(): TimerSessionSnapshot {
        val finalSnapshot = _snapshot.value
        clearSession()
        return finalSnapshot
    }

    fun cancelSession(): TimerSessionSnapshot {
        val finalSnapshot = _snapshot.value
        clearSession()
        return finalSnapshot
    }

    fun clearSession() {
        tickerJob?.cancel()
        prefs.edit().clear().apply()
        _snapshot.value = TimerSessionSnapshot()
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive && _snapshot.value.isRunning) {
                updateSnapshot()
                delay(1000L)
            }
        }
    }

    private fun updateSnapshot() {
        _snapshot.value = restoreSnapshot()
    }

    fun continueInFlow() {
        val current = _snapshot.value
        if (current.taskId != 0L) {
            val nowElapsed = SystemClock.elapsedRealtime()
            val nowWall = System.currentTimeMillis()
            val bootTime = nowWall - nowElapsed
            val baseSeconds = if (current.currentElapsedSeconds >= current.minDurationSeconds) {
                current.currentElapsedSeconds
            } else {
                current.minDurationSeconds
            }

            prefs.edit()
                .putBoolean(KEY_IS_RUNNING, true)
                .putBoolean(KEY_IS_AWAITING_CONTINUE, false)
                .putBoolean(KEY_HAS_CONTINUED_INTO_FLOW, true)
                .putBoolean(KEY_IS_WAKE_ALARM_RINGING, false)
                .putInt(KEY_AWAITING_SECONDS_LEFT, 30)
                .putLong(KEY_ACCUMULATED_ACTIVE_SECONDS, baseSeconds)
                .putLong(KEY_STINT_START_ELAPSED, nowElapsed)
                .putLong(KEY_STINT_START_WALL, nowWall)
                .putLong(KEY_BOOT_TIME, bootTime)
                .apply()

            updateSnapshot()
            startTicker()
        }
    }

    fun silenceWakeAlarm() {
        prefs.edit().putBoolean(KEY_IS_WAKE_ALARM_RINGING, false).apply()
        updateSnapshot()
    }

    fun configureWakeAlarmSettings(mode: String, durationSeconds: Int) {
        prefs.edit()
            .putString(KEY_WAKE_ALARM_MODE, mode)
            .putInt(KEY_WAKE_ALARM_DURATION_SECONDS, durationSeconds)
            .apply()
        updateSnapshot()
    }

    private fun restoreSnapshot(): TimerSessionSnapshot {
        val taskId = prefs.getLong(KEY_TASK_ID, 0L)
        if (taskId == 0L) {
            return TimerSessionSnapshot()
        }

        val title = prefs.getString(KEY_TITLE, "") ?: ""
        val subject = prefs.getString(KEY_SUBJECT, "") ?: ""
        val catName = prefs.getString(KEY_CATEGORY, TaskCategory.STUDY.name) ?: TaskCategory.STUDY.name
        val category = try { TaskCategory.valueOf(catName) } catch (e: Exception) { TaskCategory.STUDY }
        val minSecs = prefs.getLong(KEY_MIN_DURATION_SECONDS, 0L)
        val extSecs = prefs.getLong(KEY_EXTENSION_TARGET_SECONDS, 0L)
        val isCompulsory = prefs.getBoolean(KEY_IS_COMPULSORY, true)
        val isRunning = prefs.getBoolean(KEY_IS_RUNNING, false)
        val hasContinued = prefs.getBoolean(KEY_HAS_CONTINUED_INTO_FLOW, false)
        var isAwaiting = prefs.getBoolean(KEY_IS_AWAITING_CONTINUE, false)
        var awaitingLeft = prefs.getInt(KEY_AWAITING_SECONDS_LEFT, 30)
        var isAlarmRinging = prefs.getBoolean(KEY_IS_WAKE_ALARM_RINGING, false)

        val rawElapsed = calculateElapsedFromPrefs()
        val isMinCompleted = rawElapsed >= minSecs && minSecs > 0

        // If min reached and hasn't continued into flow yet, trigger awaiting continue state
        if (isMinCompleted && !hasContinued && !isAwaiting && isCompulsory) {
            isAwaiting = true
            awaitingLeft = 30
            val reachedTimestamp = System.currentTimeMillis()
            prefs.edit()
                .putBoolean(KEY_IS_AWAITING_CONTINUE, true)
                .putInt(KEY_AWAITING_SECONDS_LEFT, 30)
                .putLong(KEY_TARGET_REACHED_TIMESTAMP, reachedTimestamp)
                .apply()
        }

        val wakeMode = prefs.getString(KEY_WAKE_ALARM_MODE, "ALARM") ?: "ALARM"

        if (isAwaiting && !hasContinued) {
            val reachedTime = prefs.getLong(KEY_TARGET_REACHED_TIMESTAMP, System.currentTimeMillis())
            val secondsSinceReached = ((System.currentTimeMillis() - reachedTime) / 1000L).toInt()
            val remainingAwait = (30 - secondsSinceReached).coerceAtLeast(0)
            awaitingLeft = remainingAwait
            if (remainingAwait == 0) {
                if (wakeMode == "OVERTIME_AUTO") {
                    // Silently transition into flow overtime without sounding alarm
                    continueInFlow()
                    return restoreSnapshot()
                } else if (!isAlarmRinging) {
                    isAlarmRinging = true
                    prefs.edit().putBoolean(KEY_IS_WAKE_ALARM_RINGING, true).apply()
                }
            }
        }

        val displayElapsed = if (isAwaiting && !hasContinued) minSecs else rawElapsed
        val remaining = (minSecs - displayElapsed).coerceAtLeast(0L)
        val overtime = (displayElapsed - minSecs).coerceAtLeast(0L)

        return TimerSessionSnapshot(
            taskId = taskId,
            taskTitle = title,
            subject = subject,
            category = category,
            minDurationSeconds = minSecs,
            extensionTargetSeconds = extSecs,
            isCompulsory = isCompulsory,
            isRunning = isRunning,
            accumulatedActiveSeconds = prefs.getLong(KEY_ACCUMULATED_ACTIVE_SECONDS, 0L),
            currentElapsedSeconds = displayElapsed,
            isMinCompleted = isMinCompleted,
            remainingCompulsorySeconds = remaining,
            flowOvertimeSeconds = overtime,
            isAwaitingContinue = isAwaiting,
            awaitingContinueSecondsLeft = awaitingLeft,
            isWakeAlarmRinging = isAlarmRinging,
            hasContinuedIntoFlow = hasContinued
        )
    }

    private fun calculateElapsedFromPrefs(): Long {
        val isRunning = prefs.getBoolean(KEY_IS_RUNNING, false)
        val accumulated = prefs.getLong(KEY_ACCUMULATED_ACTIVE_SECONDS, 0L)
        if (!isRunning) return accumulated

        val stintElapsed = prefs.getLong(KEY_STINT_START_ELAPSED, 0L)
        val stintWall = prefs.getLong(KEY_STINT_START_WALL, 0L)
        val savedBootTime = prefs.getLong(KEY_BOOT_TIME, 0L)

        return calculateElapsed(
            isRunning = isRunning,
            accumulatedSeconds = accumulated,
            stintStartElapsedRealtime = stintElapsed,
            stintStartWallClock = stintWall,
            bootTimestamp = savedBootTime
        )
    }

    companion object {
        private const val KEY_TASK_ID = "task_id"
        private const val KEY_TITLE = "task_title"
        private const val KEY_SUBJECT = "subject"
        private const val KEY_CATEGORY = "category"
        private const val KEY_MIN_DURATION_SECONDS = "min_duration_seconds"
        private const val KEY_EXTENSION_TARGET_SECONDS = "extension_target_seconds"
        private const val KEY_IS_COMPULSORY = "is_compulsory"
        private const val KEY_IS_RUNNING = "is_running"
        private const val KEY_ACCUMULATED_ACTIVE_SECONDS = "accumulated_active_seconds"
        private const val KEY_STINT_START_ELAPSED = "stint_start_elapsed"
        private const val KEY_STINT_START_WALL = "stint_start_wall"
        private const val KEY_BOOT_TIME = "boot_time"
        private const val KEY_IS_AWAITING_CONTINUE = "is_awaiting_continue"
        private const val KEY_AWAITING_SECONDS_LEFT = "awaiting_seconds_left"
        private const val KEY_TARGET_REACHED_TIMESTAMP = "target_reached_timestamp"
        private const val KEY_IS_WAKE_ALARM_RINGING = "is_wake_alarm_ringing"
        private const val KEY_HAS_CONTINUED_INTO_FLOW = "has_continued_into_flow"
        private const val KEY_WAKE_ALARM_MODE = "wake_alarm_mode"
        private const val KEY_WAKE_ALARM_DURATION_SECONDS = "wake_alarm_duration_seconds"

        @Volatile
        private var INSTANCE: TimerEngine? = null

        fun getInstance(context: Context): TimerEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TimerEngine(context.applicationContext).also { INSTANCE = it }
            }
        }

        // Pure calculation function for testing reboot, monotonic clocks, and reconciliation
        fun calculateElapsed(
            isRunning: Boolean,
            accumulatedSeconds: Long,
            stintStartElapsedRealtime: Long,
            stintStartWallClock: Long,
            bootTimestamp: Long,
            nowWallClock: Long = System.currentTimeMillis(),
            nowElapsedRealtime: Long = SystemClock.elapsedRealtime()
        ): Long {
            if (!isRunning) return accumulatedSeconds

            val currentBootTimestamp = nowWallClock - nowElapsedRealtime
            val rebootOccurred = abs(currentBootTimestamp - bootTimestamp) > 15_000L || nowElapsedRealtime < stintStartElapsedRealtime

            if (rebootOccurred) {
                val wallDiffSeconds = (nowWallClock - stintStartWallClock) / 1000L
                return if (wallDiffSeconds in 0..14400) {
                    accumulatedSeconds + wallDiffSeconds
                } else {
                    accumulatedSeconds
                }
            }

            val elapsedInStint = (nowElapsedRealtime - stintStartElapsedRealtime) / 1000L
            return accumulatedSeconds + elapsedInStint.coerceAtLeast(0L)
        }
    }
}
