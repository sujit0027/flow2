package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.backup.DataBackupManager
import com.example.data.blocker.AppBlockConfig
import com.example.data.blocker.AppBlockManager
import com.example.data.blocker.InstalledAppItem
import com.example.data.local.FlowStudyDatabase
import com.example.data.local.model.AppSettings
import com.example.data.local.model.RoutineEntity
import com.example.data.local.model.RoutineTask
import com.example.data.local.model.StudySessionLog
import com.example.data.preferences.AppThemeConfig
import com.example.data.preferences.AutoFlowConfig
import com.example.data.preferences.FocusScreenCustomization
import com.example.data.preferences.UserPreferencesStore
import com.example.data.preferences.WallpaperConfig
import com.example.data.repository.FlowStudyRepository
import com.example.data.sound.SoundEventType
import com.example.data.sound.SoundRingtoneConfig
import com.example.data.sound.SoundRingtoneManager
import com.example.data.timer.TimerEngine
import com.example.data.timer.TimerSessionSnapshot
import com.example.service.TimerForegroundService
import com.example.ui.timer.PostTaskChoice
import com.example.ui.timer.TaskTransitionCountdownState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = FlowStudyDatabase.getDatabase(application)
    private val repository = FlowStudyRepository(
        routineDao = database.routineDao(),
        taskDao = database.routineTaskDao(),
        logDao = database.studyLogDao(),
        settingsDao = database.settingsDao()
    )

    private val prefsStore = UserPreferencesStore(application)
    private val timerEngine = TimerEngine.getInstance(application)

    val tasks: StateFlow<List<RoutineTask>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val routines: StateFlow<List<RoutineEntity>> = repository.allRoutines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeRoutine: StateFlow<RoutineEntity?> = repository.activeRoutine
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayLogs: StateFlow<List<StudySessionLog>> = repository.getTodayLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<StudySessionLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<AppSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    val focusCustomization: StateFlow<FocusScreenCustomization> = prefsStore.focusCustomization
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FocusScreenCustomization())

    val wallpaperConfig: StateFlow<WallpaperConfig> = prefsStore.wallpaperConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WallpaperConfig())

    val themeConfig: StateFlow<AppThemeConfig> = prefsStore.themeConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppThemeConfig())

    val appBlockConfig: StateFlow<AppBlockConfig> = prefsStore.appBlockConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppBlockConfig())

    val soundConfig: StateFlow<SoundRingtoneConfig> = prefsStore.soundConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SoundRingtoneConfig())

    val autoFlowConfig: StateFlow<AutoFlowConfig> = prefsStore.autoFlowConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AutoFlowConfig())

    private val _installedApps = MutableStateFlow<List<InstalledAppItem>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppItem>> = _installedApps.asStateFlow()

    // Monotonic Clock Timer Snapshot from TimerEngine
    val timerSnapshot: StateFlow<TimerSessionSnapshot> = timerEngine.snapshot

    private val _currentTab = MutableStateFlow(NavigationTab.HOME)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _isTimerScreenOpen = MutableStateFlow(false)
    val isTimerScreenOpen: StateFlow<Boolean> = _isTimerScreenOpen.asStateFlow()

    private val _taskBeingEdited = MutableStateFlow<RoutineTask?>(null)
    val taskBeingEdited: StateFlow<RoutineTask?> = _taskBeingEdited.asStateFlow()

    private val _isEditSheetOpen = MutableStateFlow(false)
    val isEditSheetOpen: StateFlow<Boolean> = _isEditSheetOpen.asStateFlow()

    private val _isFocusCustomizationOpen = MutableStateFlow(false)
    val isFocusCustomizationOpen: StateFlow<Boolean> = _isFocusCustomizationOpen.asStateFlow()

    private val _isWallpaperSettingsOpen = MutableStateFlow(false)
    val isWallpaperSettingsOpen: StateFlow<Boolean> = _isWallpaperSettingsOpen.asStateFlow()

    private val _isAppBlockerOpen = MutableStateFlow(false)
    val isAppBlockerOpen: StateFlow<Boolean> = _isAppBlockerOpen.asStateFlow()

    private val _isSoundSettingsOpen = MutableStateFlow(false)
    val isSoundSettingsOpen: StateFlow<Boolean> = _isSoundSettingsOpen.asStateFlow()

    private val _isAutoFlowSettingsOpen = MutableStateFlow(false)
    val isAutoFlowSettingsOpen: StateFlow<Boolean> = _isAutoFlowSettingsOpen.asStateFlow()

    private val _snackMessage = MutableStateFlow<String?>(null)
    val snackMessage: StateFlow<String?> = _snackMessage.asStateFlow()

    // 30-Second Auto-Start Transition State
    private val _transitionCountdownState = MutableStateFlow<TaskTransitionCountdownState?>(null)
    val transitionCountdownState: StateFlow<TaskTransitionCountdownState?> = _transitionCountdownState.asStateFlow()

    // Lecture Mode: Allows completely hiding the floating/minimized timer screen so students can watch YouTube lectures
    private val _isMiniPlayerHidden = MutableStateFlow(false)
    val isMiniPlayerHidden: StateFlow<Boolean> = _isMiniPlayerHidden.asStateFlow()

    private var countdownJob: Job? = null
    private var hasTriggeredMinVibration = false

    init {
        viewModelScope.launch {
            repository.seedDefaultDataIfEmpty()
        }

        // Reconnect foreground service if timer was already running (e.g. process restart)
        if (timerEngine.snapshot.value.isRunning) {
            TimerForegroundService.start(application)
        }

        // Play target reached tone when student reaches compulsory minimum duration
        viewModelScope.launch {
            timerEngine.snapshot.collect { snapshot ->
                if (snapshot.taskId != 0L && snapshot.isMinCompleted && !hasTriggeredMinVibration) {
                    hasTriggeredMinVibration = true
                    SoundRingtoneManager.triggerEventSound(
                        getApplication(),
                        SoundEventType.TARGET_REACHED,
                        soundConfig.value
                    )
                }
            }
        }
    }

    fun selectTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun clearSnackMessage() {
        _snackMessage.value = null
    }

    // Timer Operations using Monotonic Clock Engine
    fun startFocusSession(task: RoutineTask) {
        timerEngine.startSession(task)
        hasTriggeredMinVibration = task.isMinDurationReached
        _isTimerScreenOpen.value = true
        _isMiniPlayerHidden.value = false
        TimerForegroundService.start(getApplication())
    }

    fun openActiveTimerScreen() {
        if (timerEngine.snapshot.value.taskId != 0L) {
            _isTimerScreenOpen.value = true
            _isMiniPlayerHidden.value = false
        }
    }

    fun closeActiveTimerScreen() {
        _isTimerScreenOpen.value = false
    }

    fun hideMiniPlayer() {
        _isMiniPlayerHidden.value = true
        _snackMessage.value = "Timer hidden for video lectures. Studying silently in background!"
    }

    fun showMiniPlayer() {
        _isMiniPlayerHidden.value = false
    }

    fun toggleMiniPlayer() {
        _isMiniPlayerHidden.value = !_isMiniPlayerHidden.value
    }

    fun minimizeTimerScreen(hideCompletely: Boolean = false) {
        _isTimerScreenOpen.value = false
        if (hideCompletely) {
            _isMiniPlayerHidden.value = true
            _snackMessage.value = "Timer hidden for video lectures. Studying silently in background!"
        }
    }

    fun togglePlayPause() {
        val current = timerEngine.snapshot.value
        if (current.isRunning) {
            timerEngine.pauseSession()
            syncAccumulatedToDatabase(markCompleted = false)
        } else {
            timerEngine.resumeSession()
            TimerForegroundService.start(getApplication())
        }
    }

    fun addTimerSeconds(seconds: Long) {
        timerEngine.addExtensionSeconds(seconds)
        val taskId = timerEngine.snapshot.value.taskId
        if (taskId != 0L) {
            viewModelScope.launch {
                val task = repository.getTaskById(taskId) ?: return@launch
                val addedMinutes = (seconds / 60).toInt()
                val updated = task.copy(
                    minDurationMinutes = task.minDurationMinutes + addedMinutes,
                    extensionTargetMinutes = task.extensionTargetMinutes + addedMinutes
                )
                repository.updateTask(updated)
                _snackMessage.value = "Target extended by +${addedMinutes}m (${updated.minDurationMinutes}m total)"
            }
        }
    }

    fun finishCurrentTask(choice: PostTaskChoice = PostTaskChoice.JUST_FINISH) {
        val snapshot = timerEngine.finishSession()
        TimerForegroundService.stop(getApplication())

        val finishedTaskId = snapshot.taskId
        val finishedTaskTitle = snapshot.taskTitle

        _isTimerScreenOpen.value = false

        viewModelScope.launch {
            if (finishedTaskId != 0L) {
                val task = repository.getTaskById(finishedTaskId)
                if (task != null) {
                    repository.logCompletedSession(
                        task = task,
                        actualDurationSeconds = snapshot.currentElapsedSeconds,
                        wasInterrupted = false
                    )
                }
            }

            val autoFlow = autoFlowConfig.value
            // Handle user choice after completing task
            when (choice) {
                PostTaskChoice.ADVANCE_NEXT_TASK -> {
                    val currentTasks = tasks.value
                    val nextPending = currentTasks.firstOrNull { !it.isCompleted && it.id != finishedTaskId }
                    if (nextPending != null) {
                        startFocusSession(nextPending)
                    }
                }
                PostTaskChoice.TAKE_BREAK -> {
                    val breakTask = tasks.value.firstOrNull {
                        it.category == com.example.data.local.model.TaskCategory.BREAK && !it.isCompleted && it.id != finishedTaskId
                    }
                    if (breakTask != null) {
                        startFocusSession(breakTask)
                    } else {
                        _snackMessage.value = "Take a peaceful 5-10 minute rest!"
                    }
                }
                PostTaskChoice.HAVE_MEAL -> {
                    val mealTask = tasks.value.firstOrNull {
                        it.category == com.example.data.local.model.TaskCategory.MEAL && !it.isCompleted && it.id != finishedTaskId
                    }
                    if (mealTask != null) {
                        startFocusSession(mealTask)
                    } else {
                        _snackMessage.value = "Enjoy a nourishing meal or snack!"
                    }
                }
                PostTaskChoice.CONTINUE_STUDYING -> {
                    val task = repository.getTaskById(snapshot.taskId)
                    if (task != null) {
                        startFocusSession(task)
                    }
                }
                PostTaskChoice.JUST_FINISH -> {
                    val currentTasks = tasks.value
                    val remainingTasks = currentTasks.filter { !it.isCompleted && it.id != finishedTaskId }

                    if (remainingTasks.isEmpty()) {
                        _snackMessage.value = "🎉 Fantastic! All scheduled routine tasks completed for today!"
                        return@launch
                    }

                    // Look for next compulsory task in remaining sequence
                    val nextCompulsory = remainingTasks.firstOrNull { it.isCompulsory }
                    val targetCompulsory: RoutineTask
                    val intermediateTasks: List<RoutineTask>

                    if (nextCompulsory != null) {
                        targetCompulsory = nextCompulsory
                        val index = remainingTasks.indexOfFirst { it.id == nextCompulsory.id }
                        // All tasks before the next compulsory task (flexible / skipped tasks)
                        intermediateTasks = if (index > 0) remainingTasks.subList(0, index) else emptyList()
                    } else {
                        // If no compulsory task remains, first one is auto-start target and rest are intermediate
                        targetCompulsory = remainingTasks.first()
                        intermediateTasks = if (remainingTasks.size > 1) remainingTasks.subList(1, remainingTasks.size) else emptyList()
                    }

                    startTransitionCountdown(
                        finishedTaskId = finishedTaskId,
                        finishedTaskTitle = finishedTaskTitle.ifBlank { "Study Session" },
                        intermediateTasks = intermediateTasks,
                        nextCompulsoryTask = targetCompulsory
                    )
                }
            }
        }
    }

    fun emergencyStopSession() {
        val snapshot = timerEngine.cancelSession()
        TimerForegroundService.stop(getApplication())

        if (snapshot.taskId != 0L) {
            viewModelScope.launch {
                val task = repository.getTaskById(snapshot.taskId)
                if (task != null) {
                    repository.logCompletedSession(
                        task = task,
                        actualDurationSeconds = snapshot.currentElapsedSeconds,
                        wasInterrupted = true
                    )
                }
            }
        }
        _isTimerScreenOpen.value = false
    }

    fun skipCurrentTask() {
        val currentTaskId = timerEngine.snapshot.value.taskId
        timerEngine.clearSession()
        TimerForegroundService.stop(getApplication())
        _isTimerScreenOpen.value = false

        viewModelScope.launch {
            val currentTasks = tasks.value
            val remainingTasks = currentTasks.filter { !it.isCompleted && it.id != currentTaskId }
            if (remainingTasks.isEmpty()) {
                _snackMessage.value = "All scheduled routine tasks completed!"
                return@launch
            }

            val nextCompulsory = remainingTasks.firstOrNull { it.isCompulsory }
            val targetCompulsory: RoutineTask
            val intermediateTasks: List<RoutineTask>

            if (nextCompulsory != null) {
                targetCompulsory = nextCompulsory
                val index = remainingTasks.indexOfFirst { it.id == nextCompulsory.id }
                intermediateTasks = if (index > 0) remainingTasks.subList(0, index) else emptyList()
            } else {
                targetCompulsory = remainingTasks.first()
                intermediateTasks = if (remainingTasks.size > 1) remainingTasks.subList(1, remainingTasks.size) else emptyList()
            }

            startTransitionCountdown(
                finishedTaskId = currentTaskId,
                finishedTaskTitle = "Skipped Task",
                intermediateTasks = intermediateTasks,
                nextCompulsoryTask = targetCompulsory
            )
        }
    }

    // 30-Second Auto-Start Transition Operations
    fun startTransitionCountdown(
        finishedTaskId: Long,
        finishedTaskTitle: String,
        intermediateTasks: List<RoutineTask> = emptyList(),
        nextCompulsoryTask: RoutineTask
    ) {
        countdownJob?.cancel()
        val totalSecs = autoFlowConfig.value.countdownDurationSeconds.coerceIn(5, 120)

        countdownJob = viewModelScope.launch {
            for (remaining in totalSecs downTo 1) {
                _transitionCountdownState.value = TaskTransitionCountdownState(
                    finishedTaskId = finishedTaskId,
                    finishedTaskTitle = finishedTaskTitle,
                    intermediateTasks = intermediateTasks,
                    optionalNextTask = intermediateTasks.firstOrNull(),
                    nextCompulsoryTask = nextCompulsoryTask,
                    totalCountdownSeconds = totalSecs,
                    remainingSeconds = remaining,
                    isRunning = true
                )
                delay(1000L)
            }

            // Expiry reached! User was idle or fell asleep during the 30 seconds
            _transitionCountdownState.value = null

            // 1. Ring loud waking alarm
            if (autoFlowConfig.value.ringAlarmOnCountdownExpiry) {
                SoundRingtoneManager.triggerEventSound(
                    getApplication(),
                    SoundEventType.NEXT_TASK_ALARM,
                    soundConfig.value
                )
                triggerWakeVibration()
            }

            // 2. High priority notification
            TimerForegroundService.showNextTaskAlarmNotification(
                getApplication(),
                nextCompulsoryTask.title,
                "⏰ Wake-Up Alert: Break skipped. Starting ${nextCompulsoryTask.title} now!"
            )

            // 3. Automatically launch the next COMPULSORY task!
            startFocusSession(nextCompulsoryTask)
            _snackMessage.value = "⏰ Wake Alarm: Auto-started compulsory task '${nextCompulsoryTask.title}'"
        }
    }

    fun onTransitionTakeBreak(breakTask: RoutineTask) {
        countdownJob?.cancel()
        countdownJob = null
        _transitionCountdownState.value = null
        startFocusSession(breakTask)
    }

    fun onTransitionExtendPrevious(taskId: Long, extendMinutes: Long = 15) {
        countdownJob?.cancel()
        countdownJob = null
        _transitionCountdownState.value = null
        viewModelScope.launch {
            val task = repository.getTaskById(taskId)
            if (task != null) {
                val updated = task.copy(
                    minDurationMinutes = task.minDurationMinutes + extendMinutes.toInt(),
                    extensionTargetMinutes = task.extensionTargetMinutes + extendMinutes.toInt(),
                    isCompleted = false
                )
                repository.updateTask(updated)
                startFocusSession(updated)
                _snackMessage.value = "Resumed '${task.title}' with +${extendMinutes}m added to target"
            }
        }
    }

    fun onTransitionStartCompulsory(compulsoryTask: RoutineTask) {
        countdownJob?.cancel()
        countdownJob = null
        _transitionCountdownState.value = null
        startFocusSession(compulsoryTask)
    }

    fun onTransitionCancelPause() {
        countdownJob?.cancel()
        countdownJob = null
        _transitionCountdownState.value = null
        _snackMessage.value = "Routine paused. You can resume anytime from Home."
    }

    private fun triggerWakeVibration() {
        try {
            val context = getApplication<Application>()
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator != null && vibrator.hasVibrator()) {
                val pattern = longArrayOf(0, 500, 200, 500, 200, 800)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, -1)
                }
            }
        } catch (ignored: Exception) {}
    }

    private fun syncAccumulatedToDatabase(markCompleted: Boolean) {
        val snapshot = timerEngine.snapshot.value
        if (snapshot.taskId != 0L) {
            viewModelScope.launch {
                val task = repository.getTaskById(snapshot.taskId) ?: return@launch
                val updated = task.copy(
                    accumulatedDurationSeconds = snapshot.currentElapsedSeconds,
                    isCompleted = if (markCompleted) true else task.isCompleted
                )
                repository.updateTask(updated)
            }
        }
    }

    // Customization & Wallpapers Dialogs
    fun openFocusCustomization() { _isFocusCustomizationOpen.value = true }
    fun closeFocusCustomization() { _isFocusCustomizationOpen.value = false }
    fun saveFocusCustomization(config: FocusScreenCustomization) {
        viewModelScope.launch { prefsStore.updateFocusCustomization(config) }
    }

    fun openWallpaperSettings() { _isWallpaperSettingsOpen.value = true }
    fun closeWallpaperSettings() { _isWallpaperSettingsOpen.value = false }
    fun saveWallpaperConfig(config: WallpaperConfig) {
        viewModelScope.launch { prefsStore.updateWallpaperConfig(config) }
    }

    fun saveThemeConfig(config: AppThemeConfig) {
        viewModelScope.launch { prefsStore.updateThemeConfig(config) }
    }

    fun completeOnboarding(timerStyle: String, dailyHours: Int) {
        viewModelScope.launch {
            prefsStore.updateThemeConfig(
                themeConfig.value.copy(
                    timerStyle = timerStyle,
                    dailyStudyTargetHours = dailyHours,
                    isOnboardingCompleted = true
                )
            )
        }
    }

    // App Blocker Management
    fun openAppBlocker() {
        loadInstalledApps()
        _isAppBlockerOpen.value = true
    }

    fun closeAppBlocker() {
        _isAppBlockerOpen.value = false
    }

    fun saveAppBlockConfig(config: AppBlockConfig) {
        viewModelScope.launch { prefsStore.updateAppBlockConfig(config) }
    }

    fun toggleAppBlock(packageName: String, shouldBlock: Boolean) {
        viewModelScope.launch {
            val current = appBlockConfig.value.blockedPackages.toMutableSet()
            if (shouldBlock) current.add(packageName) else current.remove(packageName)
            prefsStore.updateAppBlockConfig(appBlockConfig.value.copy(blockedPackages = current))
            loadInstalledApps()
        }
    }

    fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = AppBlockManager.getInstalledLauncherApps(
                getApplication(),
                appBlockConfig.value.blockedPackages
            )
            _installedApps.value = apps
        }
    }

    // Sound & Ringtone Settings
    fun openSoundSettings() { _isSoundSettingsOpen.value = true }

    fun closeSoundSettings() {
        SoundRingtoneManager.stopPlayback()
        _isSoundSettingsOpen.value = false
    }

    fun saveSoundConfig(config: SoundRingtoneConfig) {
        viewModelScope.launch { prefsStore.updateSoundConfig(config) }
    }

    fun playTonePreview(soundId: String, uriString: String? = null) {
        SoundRingtoneManager.playPreview(getApplication(), soundId, uriString)
    }

    fun stopTonePreview() {
        SoundRingtoneManager.stopPlayback()
    }

    // Auto-Flow & Next Task Sequencing
    fun openAutoFlowSettings() { _isAutoFlowSettingsOpen.value = true }
    fun closeAutoFlowSettings() { _isAutoFlowSettingsOpen.value = false }
    fun saveAutoFlowConfig(config: AutoFlowConfig) {
        viewModelScope.launch { prefsStore.updateAutoFlowConfig(config) }
    }

    // Task & Routine Management
    fun openCreateTask() {
        _taskBeingEdited.value = null
        _isEditSheetOpen.value = true
    }

    fun openEditTask(task: RoutineTask) {
        _taskBeingEdited.value = task
        _isEditSheetOpen.value = true
    }

    fun closeEditSheet() {
        _isEditSheetOpen.value = false
        _taskBeingEdited.value = null
    }

    fun saveTask(task: RoutineTask) {
        viewModelScope.launch {
            if (task.id == 0L) {
                repository.insertTask(task)
            } else {
                repository.updateTask(task)
            }
        }
    }

    fun duplicateTask(task: RoutineTask) {
        viewModelScope.launch { repository.duplicateTask(task) }
    }

    fun deleteTask(task: RoutineTask) {
        viewModelScope.launch {
            if (timerEngine.snapshot.value.taskId == task.id) {
                timerEngine.clearSession()
                TimerForegroundService.stop(getApplication())
                _isTimerScreenOpen.value = false
            }
            repository.deleteTask(task)
        }
    }

    fun moveTaskUp(task: RoutineTask) { viewModelScope.launch { repository.moveTaskUp(task) } }
    fun moveTaskDown(task: RoutineTask) { viewModelScope.launch { repository.moveTaskDown(task) } }
    fun toggleTaskComplete(task: RoutineTask) { viewModelScope.launch { repository.toggleTaskCompleted(task) } }
    fun resetTodayCompletions() { viewModelScope.launch { repository.resetAllCompletions() } }
    fun carryForwardUnfinished() { viewModelScope.launch { repository.carryForwardUnfinishedTasks() } }

    fun switchRoutine(routineId: Long) {
        viewModelScope.launch { repository.switchActiveRoutine(routineId) }
    }

    fun restoreSampleRoutine() {
        viewModelScope.launch {
            timerEngine.clearSession()
            TimerForegroundService.stop(getApplication())
            repository.loadPrepopulatedSampleRoutine()
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch { repository.updateSettings(newSettings) }
    }

    fun clearAllLogs() {
        viewModelScope.launch { repository.clearHistoryLogs() }
    }

    // Backup & Export
    fun exportBackupJson(): String {
        return DataBackupManager.exportToJson(
            routines = routines.value,
            tasks = tasks.value,
            logs = allLogs.value
        )
    }

    fun exportLogsCsv(): String {
        return DataBackupManager.exportToCsv(allLogs.value)
    }

    fun restoreBackupJson(jsonString: String): Boolean {
        val parsed = DataBackupManager.parseFromJson(jsonString) ?: return false
        viewModelScope.launch {
            timerEngine.clearSession()
            TimerForegroundService.stop(getApplication())
            repository.restoreBackup(parsed)
        }
        return true
    }

    fun resetAllData() {
        viewModelScope.launch {
            timerEngine.clearSession()
            TimerForegroundService.stop(getApplication())
            _isTimerScreenOpen.value = false
            repository.clearHistoryLogs()
            repository.loadPrepopulatedSampleRoutine()
            repository.updateSettings(AppSettings())
            prefsStore.resetAllPreferences()
        }
    }
}
