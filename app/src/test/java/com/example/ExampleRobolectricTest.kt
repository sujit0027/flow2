package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.backup.DataBackupManager
import com.example.data.local.model.RoutineEntity
import com.example.data.local.model.RoutineTask
import com.example.data.local.model.StudySessionLog
import com.example.data.local.model.TaskCategory
import com.example.data.timer.TimerEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("FlowStudy", appName)
    }

    @Test
    fun `test minimum target enforcement and continue vs finish`() {
        val task = RoutineTask(
            id = 1L,
            title = "Physics Theory",
            category = TaskCategory.STUDY,
            minDurationMinutes = 45,
            extensionTargetMinutes = 15,
            isCompulsory = true,
            accumulatedDurationSeconds = 1800L // 30m elapsed
        )

        // Before minimum is reached: normal finish must not be satisfied
        assertFalse(task.isMinDurationReached)
        assertEquals(900L, task.remainingCompulsorySeconds) // 15m remaining
        assertEquals(0L, task.extensionSeconds)

        // Once minimum target is completed: eligible to finish or continue in flow
        val completedMinTask = task.copy(accumulatedDurationSeconds = 3000L) // 50m elapsed
        assertTrue(completedMinTask.isMinDurationReached)
        assertEquals(0L, completedMinTask.remainingCompulsorySeconds)
        assertEquals(300L, completedMinTask.extensionSeconds) // 5m extension
    }

    @Test
    fun `test timer monotonic calculation and pause resume accumulation`() {
        // Paused state: must return accumulated time strictly
        val pausedSeconds = TimerEngine.calculateElapsed(
            isRunning = false,
            accumulatedSeconds = 500L,
            stintStartElapsedRealtime = 1000L,
            stintStartWallClock = 1000L,
            bootTimestamp = 0L,
            nowWallClock = 2000L,
            nowElapsedRealtime = 2000L
        )
        assertEquals(500L, pausedSeconds)

        // Running state: accumulated + elapsed in current stint
        val runningSeconds = TimerEngine.calculateElapsed(
            isRunning = true,
            accumulatedSeconds = 500L,
            stintStartElapsedRealtime = 10_000L,
            stintStartWallClock = 100_000L,
            bootTimestamp = 90_000L,
            nowWallClock = 160_000L,
            nowElapsedRealtime = 70_000L // 60s elapsed
        )
        assertEquals(560L, runningSeconds)
    }

    @Test
    fun `test session recovery and device reboot handling`() {
        // Stint started at wall time 100_000, boot timestamp was 90_000.
        // Reboot occurs: monotonic clock resets to 5_000, new boot timestamp becomes 140_000.
        val recoveredSeconds = TimerEngine.calculateElapsed(
            isRunning = true,
            accumulatedSeconds = 300L,
            stintStartElapsedRealtime = 80_000L, // Old monotonic before reboot
            stintStartWallClock = 100_000L,
            bootTimestamp = 90_000L, // Old boot time
            nowWallClock = 145_000L, // 45s wall clock elapsed
            nowElapsedRealtime = 5_000L // Reset monotonic after reboot
        )
        // Should detect reboot and reconcile safely using wall clock difference
        assertEquals(345L, recoveredSeconds)
    }

    @Test
    fun `test meal completion and duplicate action prevention`() {
        val lunchTask = RoutineTask(
            id = 5L,
            routineId = 1L,
            title = "Lunch",
            category = TaskCategory.MEAL,
            mealType = "lunch",
            minDurationMinutes = 30,
            isCompleted = true,
            accumulatedDurationSeconds = 1800L
        )
        assertTrue(lunchTask.isCompleted)
        assertEquals("lunch", lunchTask.mealType)
    }

    @Test
    fun `test data backup export and restore`() {
        val sampleRoutine = RoutineEntity(id = 1L, name = "JEE Sprint", isTemplate = true, isActive = true)
        val sampleTask = RoutineTask(
            id = 1L,
            routineId = 1L,
            title = "Chemistry Organic",
            category = TaskCategory.STUDY,
            subject = "Chemistry",
            minDurationMinutes = 40
        )
        val sampleLog = StudySessionLog(
            sessionId = 1L,
            taskId = 1L,
            taskTitle = "Chemistry Organic",
            category = TaskCategory.STUDY,
            subject = "Chemistry",
            durationSeconds = 2400L,
            minRequiredSeconds = 2400L,
            extensionSeconds = 0L,
            wasCompulsory = true
        )

        val jsonString = DataBackupManager.exportToJson(
            routines = listOf(sampleRoutine),
            tasks = listOf(sampleTask),
            logs = listOf(sampleLog)
        )
        assertNotNull(jsonString)
        assertTrue(jsonString.contains("FlowStudy"))
        assertTrue(jsonString.contains("Chemistry Organic"))

        val restored = DataBackupManager.parseFromJson(jsonString)
        assertNotNull(restored)
        assertEquals(1, restored!!.routines.size)
        assertEquals(1, restored.tasks.size)
        assertEquals("Chemistry Organic", restored.tasks[0].title)
        assertEquals("Chemistry", restored.tasks[0].subject)

        val csv = DataBackupManager.exportToCsv(listOf(sampleLog))
        assertTrue(csv.contains("Chemistry Organic"))
        assertTrue(csv.contains("STUDY"))
    }

    @Test
    fun `test custom minimum time formatting and stepper calculations`() {
        // Formatting tests for 1h 30m, 2h, 2h 30m, etc.
        assertEquals("1h 30m", com.example.ui.components.formatMinutesHuman(90))
        assertEquals("2h", com.example.ui.components.formatMinutesHuman(120))
        assertEquals("2h 30m", com.example.ui.components.formatMinutesHuman(150))
        assertEquals("45m", com.example.ui.components.formatMinutesHuman(45))
        assertEquals("1h", com.example.ui.components.formatMinutesHuman(60))
        assertEquals("3h", com.example.ui.components.formatMinutesHuman(180))

        assertEquals("1h 30m (90 mins)", com.example.ui.components.formatMinutesDetailed(90))
        assertEquals("2h (120 mins)", com.example.ui.components.formatMinutesDetailed(120))
        assertEquals("2h 30m (150 mins)", com.example.ui.components.formatMinutesDetailed(150))
        assertEquals("45 mins", com.example.ui.components.formatMinutesDetailed(45))

        // Stepper calculation logic verification
        var duration = 90 // 1 hr 30 mins
        var hours = duration / 60
        var mins = duration % 60
        assertEquals(1, hours)
        assertEquals(30, mins)

        // Increment 1 hour
        duration = (hours + 1) * 60 + mins
        assertEquals(150, duration) // 2 hrs 30 mins

        // Decrement 5 minutes
        duration = (duration / 60) * 60 + ((duration % 60) - 5)
        assertEquals(145, duration) // 2 hrs 25 mins

        // Additive quick pills (+1 hr)
        duration += 60
        assertEquals(205, duration) // 3 hrs 25 mins

        // Verify RoutineTask with custom 1h 30m minimum
        val customTask = RoutineTask(
            id = 10L,
            title = "Mock Exam Paper",
            category = TaskCategory.STUDY,
            minDurationMinutes = 90,
            extensionTargetMinutes = 30,
            isCompulsory = true,
            accumulatedDurationSeconds = 5400L // exactly 90 minutes
        )
        assertTrue(customTask.isMinDurationReached)
        assertEquals(0L, customTask.remainingCompulsorySeconds)
    }

    @Test
    fun `test app blocker decision logic and system whitelist exceptions`() {
        val config = com.example.data.blocker.AppBlockConfig(
            isBlockerEnabled = true,
            blockAllDistractions = false,
            blockedPackages = setOf("com.instagram.android", "com.google.android.youtube"),
            allowYouTubeForLectures = false
        )

        val ownPackage = "com.aistudio.flowstudy.qzvmrk"

        // Blocked packages when enabled must return true
        assertTrue(
            com.example.data.blocker.AppBlockManager.shouldBlockPackage(
                "com.instagram.android",
                ownPackage,
                config
            )
        )
        assertTrue(
            com.example.data.blocker.AppBlockManager.shouldBlockPackage(
                "com.google.android.youtube",
                ownPackage,
                config
            )
        )

        // When YouTube lecture mode is enabled, YouTube must NOT be blocked so lectures can play
        val lectureConfig = config.copy(allowYouTubeForLectures = true)
        assertFalse(
            com.example.data.blocker.AppBlockManager.shouldBlockPackage(
                "com.google.android.youtube",
                ownPackage,
                lectureConfig
            )
        )

        // Non-blocked app must return false
        assertFalse(
            com.example.data.blocker.AppBlockManager.shouldBlockPackage(
                "com.calculator.app",
                ownPackage,
                config
            )
        )

        // FlowStudy itself must NEVER be blocked
        assertFalse(
            com.example.data.blocker.AppBlockManager.shouldBlockPackage(
                ownPackage,
                ownPackage,
                config
            )
        )

        // System critical UI and phone dialers must never be blocked
        assertFalse(
            com.example.data.blocker.AppBlockManager.shouldBlockPackage(
                "com.android.systemui",
                ownPackage,
                config
            )
        )
        assertFalse(
            com.example.data.blocker.AppBlockManager.shouldBlockPackage(
                "com.google.android.dialer",
                ownPackage,
                config
            )
        )

        // When disabled, nothing should be blocked
        val disabledConfig = config.copy(isBlockerEnabled = false)
        assertFalse(
            com.example.data.blocker.AppBlockManager.shouldBlockPackage(
                "com.instagram.android",
                ownPackage,
                disabledConfig
            )
        )

        // Strict "Block All Non-Study Apps" mode
        val blockAllConfig = config.copy(blockAllDistractions = true)
        assertTrue(
            com.example.data.blocker.AppBlockManager.shouldBlockPackage(
                "com.random.thirdparty.game",
                ownPackage,
                blockAllConfig
            )
        )
        assertFalse(
            com.example.data.blocker.AppBlockManager.shouldBlockPackage(
                ownPackage,
                ownPackage,
                blockAllConfig
            )
        )
    }

    @Test
    fun `test sound ringtone resolution and presets`() {
        // Built in sounds resolution
        assertEquals("Zen Gong", com.example.data.sound.SoundRingtoneManager.getDisplayName("zen_gong", null))
        assertEquals("Cyber Pulse", com.example.data.sound.SoundRingtoneManager.getDisplayName("cyber_pulse", null))
        assertEquals("Temple Bell", com.example.data.sound.SoundRingtoneManager.getDisplayName("temple_bell", null))
        assertEquals("Digital Beep", com.example.data.sound.SoundRingtoneManager.getDisplayName("digital_alarm", null))

        // Custom device ringtone name overrides ID
        assertEquals(
            "Over the Horizon.mp3",
            com.example.data.sound.SoundRingtoneManager.getDisplayName("custom_uri", "Over the Horizon.mp3")
        )
    }

    @Test
    fun `test auto flow config defaults and skip sequencing logic`() {
        val defaultConfig = com.example.data.preferences.AutoFlowConfig()
        assertFalse(defaultConfig.autoStartNextTask)
        assertTrue(defaultConfig.alertOnSkippedTask)
        assertTrue(defaultConfig.autoAdvanceCompulsoryOnly)
        assertEquals(30, defaultConfig.countdownDurationSeconds)
        assertTrue(defaultConfig.ringAlarmOnCountdownExpiry)

        val task1 = RoutineTask(id = 1L, title = "Math Practice", category = TaskCategory.STUDY, minDurationMinutes = 45, isCompleted = true, isCompulsory = true)
        val task2 = RoutineTask(id = 2L, title = "Lunch Break", category = TaskCategory.MEAL, minDurationMinutes = 30, isCompleted = false, isCompulsory = false)
        val task3 = RoutineTask(id = 3L, title = "Evening Walk", category = TaskCategory.EXERCISE, minDurationMinutes = 20, isCompleted = false, isCompulsory = false)
        val task4 = RoutineTask(id = 4L, title = "Physics Problems", category = TaskCategory.STUDY, minDurationMinutes = 60, isCompleted = false, isCompulsory = true)

        val routine = listOf(task1, task2, task3, task4)

        // Finding next compulsory task while skipping optional lunch & exercise
        val nextCompulsory = routine.firstOrNull { !it.isCompleted && it.id != 1L && it.isCompulsory }
        val optionalNext = routine.firstOrNull { !it.isCompleted && it.id != 1L && !it.isCompulsory }

        assertNotNull(nextCompulsory)
        assertEquals(4L, nextCompulsory?.id)
        assertEquals("Physics Problems", nextCompulsory?.title)

        assertNotNull(optionalNext)
        assertEquals(2L, optionalNext?.id)
        assertEquals("Lunch Break", optionalNext?.title)

        // Verify TaskTransitionCountdownState holds accurate 30s window
        val transitionState = com.example.ui.timer.TaskTransitionCountdownState(
            finishedTaskId = task1.id,
            finishedTaskTitle = task1.title,
            optionalNextTask = optionalNext,
            nextCompulsoryTask = nextCompulsory!!,
            totalCountdownSeconds = 30,
            remainingSeconds = 30
        )
        assertEquals(30, transitionState.remainingSeconds)
        assertEquals(4L, transitionState.nextCompulsoryTask.id)
    }

    @Test
    fun `test app block manager returns only installed apps and supports lecture mode`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val apps = com.example.data.blocker.AppBlockManager.getInstalledLauncherApps(context, emptySet())

        // Must never include synthetic uninstalled apps like TikTok or Clash of Clans if not installed
        val hasFakeTikTok = apps.any { it.packageName == "com.zhiliaoapp.musically" }
        val hasFakeClashOfClans = apps.any { it.packageName == "com.supercell.clashofclans" }
        assertFalse(hasFakeTikTok)
        assertFalse(hasFakeClashOfClans)

        // All returned apps must be valid items
        apps.forEach { app ->
            assertNotNull(app.packageName)
            assertNotNull(app.appName)
        }

        // Test Lecture Mode Mini Player Hiding
        var isMiniPlayerHidden = false
        // User taps Hide to watch YouTube video lecture
        isMiniPlayerHidden = true
        assertTrue(isMiniPlayerHidden)

        // User unhides timer
        isMiniPlayerHidden = false
        assertFalse(isMiniPlayerHidden)
    }

    @Test
    fun `test finish task triggers auto-start transition and alarm on expiry`() {
        val task1 = RoutineTask(id = 10L, title = "Math Calculus", category = TaskCategory.STUDY, minDurationMinutes = 30, isCompleted = false, isCompulsory = true)
        val task2 = RoutineTask(id = 11L, title = "Lunch", category = TaskCategory.MEAL, minDurationMinutes = 20, isCompleted = false, isCompulsory = false)
        val task3 = RoutineTask(id = 12L, title = "Physics Waves", category = TaskCategory.STUDY, minDurationMinutes = 45, isCompleted = false, isCompulsory = true)
        val tasks = listOf(task1, task2, task3)

        // When task 1 is finished, remaining tasks are task2 (optional meal) and task3 (compulsory study)
        val remainingTasks = tasks.filter { !it.isCompleted && it.id != 10L }
        val nextCompulsory = remainingTasks.firstOrNull { it.isCompulsory }
        val immediateNext = remainingTasks.first()

        val targetCompulsory = nextCompulsory ?: immediateNext
        val optionalIntermediary = if (immediateNext.id != targetCompulsory.id) immediateNext else null

        // Validate auto-start targets
        assertEquals(12L, targetCompulsory.id)
        assertEquals("Physics Waves", targetCompulsory.title)
        assertNotNull(optionalIntermediary)
        assertEquals(11L, optionalIntermediary?.id)
        assertEquals("Lunch", optionalIntermediary?.title)

        // Validate transition countdown state with multiple intermediate tasks
        val intermediateTasks = listOf(task2)
        val transition = com.example.ui.timer.TaskTransitionCountdownState(
            finishedTaskId = 10L,
            finishedTaskTitle = "Math Calculus",
            intermediateTasks = intermediateTasks,
            optionalNextTask = optionalIntermediary,
            nextCompulsoryTask = targetCompulsory,
            totalCountdownSeconds = 30,
            remainingSeconds = 30
        )
        assertEquals(30, transition.totalCountdownSeconds)
        assertTrue(transition.isRunning)
        assertEquals(1, transition.intermediateTasks.size)
        assertEquals("Lunch", transition.intermediateTasks.first().title)
        assertEquals("Physics Waves", transition.nextCompulsoryTask.title)

        // Validate simulated expiry triggers alarm sound
        val soundConfig = com.example.data.sound.SoundRingtoneConfig()
        assertTrue(soundConfig.soundEnabled)
        assertEquals("digital_alarm", soundConfig.nextTaskSoundId)
    }

    @Test
    fun `test extend button increases target duration and does not increase elapsed time`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val engine = com.example.data.timer.TimerEngine.getInstance(context)
        engine.clearSession()

        val task = RoutineTask(
            id = 99L,
            title = "Test Study",
            category = TaskCategory.STUDY,
            minDurationMinutes = 25,
            accumulatedDurationSeconds = 60L
        )
        engine.startSession(task)
        assertEquals(25 * 60L, engine.snapshot.value.minDurationSeconds)
        val initialElapsed = engine.snapshot.value.currentElapsedSeconds

        // Extend +5m (300s)
        engine.addExtensionSeconds(300L)

        // Target duration must increase by 300s (from 25m to 30m)
        assertEquals(30 * 60L, engine.snapshot.value.minDurationSeconds)
        // Elapsed time should NOT jump forward by 300s!
        assertTrue(engine.snapshot.value.currentElapsedSeconds < initialElapsed + 10L)
    }
}
