package com.example.ui

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.timer.TimerSessionSnapshot
import com.example.ui.components.formatDurationHMS
import com.example.ui.home.HomeScreen
import com.example.ui.home.CompulsoryCelebrationDialog
import com.example.ui.timer.TaskTransitionDialog
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.progress.ProgressScreen
import com.example.ui.routine.RoutineScreen
import com.example.ui.routine.TaskEditBottomSheet
import com.example.ui.settings.AppBlockerBottomSheet
import com.example.ui.settings.AutoFlowBottomSheet
import com.example.ui.settings.FocusCustomizationBottomSheet
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SoundRingtoneBottomSheet
import com.example.ui.settings.WallpaperSettingsBottomSheet
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.timer.ActiveTimerSessionScreen

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    isInPipMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val activeRoutine by viewModel.activeRoutine.collectAsStateWithLifecycle()
    val todayLogs by viewModel.todayLogs.collectAsStateWithLifecycle()
    val allLogs by viewModel.allLogs.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()

    val timerSnapshot by viewModel.timerSnapshot.collectAsStateWithLifecycle()
    val isTimerScreenOpen by viewModel.isTimerScreenOpen.collectAsStateWithLifecycle()

    val focusCustomization by viewModel.focusCustomization.collectAsStateWithLifecycle()
    val wallpaperConfig by viewModel.wallpaperConfig.collectAsStateWithLifecycle()
    val themeConfig by viewModel.themeConfig.collectAsStateWithLifecycle()
    val appBlockConfig by viewModel.appBlockConfig.collectAsStateWithLifecycle()
    val soundConfig by viewModel.soundConfig.collectAsStateWithLifecycle()
    val autoFlowConfig by viewModel.autoFlowConfig.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()

    val taskBeingEdited by viewModel.taskBeingEdited.collectAsStateWithLifecycle()
    val isEditSheetOpen by viewModel.isEditSheetOpen.collectAsStateWithLifecycle()
    val isFocusCustomizationOpen by viewModel.isFocusCustomizationOpen.collectAsStateWithLifecycle()
    val isWallpaperSettingsOpen by viewModel.isWallpaperSettingsOpen.collectAsStateWithLifecycle()
    val isAppBlockerOpen by viewModel.isAppBlockerOpen.collectAsStateWithLifecycle()
    val isSoundSettingsOpen by viewModel.isSoundSettingsOpen.collectAsStateWithLifecycle()
    val isAutoFlowSettingsOpen by viewModel.isAutoFlowSettingsOpen.collectAsStateWithLifecycle()
    val isCelebrationDialogOpen by viewModel.isCelebrationDialogOpen.collectAsStateWithLifecycle()
    val snackMessage by viewModel.snackMessage.collectAsStateWithLifecycle()
    val transitionCountdownState by viewModel.transitionCountdownState.collectAsStateWithLifecycle()
    val isMiniPlayerHidden by viewModel.isMiniPlayerHidden.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(snackMessage) {
        snackMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackMessage()
        }
    }

    // Keep screen awake handling
    DisposableEffect(timerSnapshot.isRunning, settings.keepScreenOn) {
        val window = (context as? Activity)?.window
        if (timerSnapshot.isRunning && settings.keepScreenOn) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // If first-time user, render Onboarding
    if (!themeConfig.isOnboardingCompleted) {
        OnboardingScreen(
            onFinishOnboarding = { timerStyle, dailyHours ->
                viewModel.completeOnboarding(timerStyle, dailyHours)
            }
        )
        return
    }

    // If in Picture-in-Picture mode, render compact PiP display immediately
    if (isInPipMode && timerSnapshot.taskId != 0L) {
        ActiveTimerSessionScreen(
            snapshot = timerSnapshot,
            nextTask = tasks.firstOrNull { it.id != timerSnapshot.taskId && !it.isCompleted },
            customization = focusCustomization,
            wallpaperConfig = wallpaperConfig,
            isInPipMode = true,
            onTogglePlayPause = { viewModel.togglePlayPause() },
            onAddSeconds = { viewModel.addTimerSeconds(it) },
            onFinishTask = { viewModel.finishCurrentTask(it) },
            onEmergencyStop = { viewModel.emergencyStopSession() },
            onSkipTask = { viewModel.skipCurrentTask() },
            onMinimize = {},
            onHideForLecture = {}
        )
        return
    }

    BackHandler(enabled = isTimerScreenOpen) {
        viewModel.closeActiveTimerScreen()
    }

    Scaffold(
        modifier = modifier.fillMaxSize().background(DarkBackground),
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                // Mini Floating Player when a focus session is active and user is on another screen
                if (timerSnapshot.taskId != 0L && !isTimerScreenOpen) {
                    if (!isMiniPlayerHidden) {
                        MiniFocusPlayer(
                            snapshot = timerSnapshot,
                            onExpand = { viewModel.openActiveTimerScreen() },
                            onTogglePlayPause = { viewModel.togglePlayPause() },
                            onHide = { viewModel.hideMiniPlayer() }
                        )
                    } else {
                        MiniHiddenFloatingPill(
                            snapshot = timerSnapshot,
                            onUnhide = { viewModel.showMiniPlayer() }
                        )
                    }
                }

                NavigationBar(
                    containerColor = DarkSurface,
                    tonalElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .testTag("flow_bottom_navigation")
                ) {
                    NavigationTab.entries.forEach { tab ->
                        val selected = currentTab == tab
                        val (selectedIcon, unselectedIcon) = getTabIcons(tab)

                        NavigationBarItem(
                            selected = selected,
                            onClick = { viewModel.selectTab(tab) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) selectedIcon else unselectedIcon,
                                    contentDescription = tab.label,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = EmeraldLight,
                                selectedTextColor = EmeraldLight,
                                unselectedIconColor = TextTertiary,
                                unselectedTextColor = TextTertiary,
                                indicatorColor = EmeraldContainer
                            ),
                            modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            when (currentTab) {
                NavigationTab.HOME -> {
                    HomeScreen(
                        tasks = tasks,
                        todayLogs = todayLogs,
                        activeRoutine = activeRoutine,
                        timerSnapshot = timerSnapshot,
                        wallpaperConfig = wallpaperConfig,
                        dailyTargetHours = themeConfig.dailyStudyTargetHours,
                        onStartTask = { viewModel.startFocusSession(it) },
                        onToggleTaskComplete = { viewModel.toggleTaskComplete(it) },
                        onOpenCreateTask = { viewModel.openCreateTask() },
                        onResetTodayCompletions = { viewModel.resetTodayCompletions() },
                        onCarryForwardUnfinished = { viewModel.carryForwardUnfinished() },
                        onOpenWallpaperSettings = { viewModel.openWallpaperSettings() },
                        onOpenFocusCustomization = { viewModel.openFocusCustomization() },
                        onOpenCelebrationDialog = { viewModel.openCelebrationDialog() }
                    )
                }

                NavigationTab.ROUTINE -> {
                    RoutineScreen(
                        tasks = tasks,
                        onOpenCreateTask = { viewModel.openCreateTask() },
                        onEditTask = { viewModel.openEditTask(it) },
                        onDuplicateTask = { viewModel.duplicateTask(it) },
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onMoveUp = { viewModel.moveTaskUp(it) },
                        onMoveDown = { viewModel.moveTaskDown(it) },
                        onLoadPreset = {
                            if (it.contains("Balanced")) {
                                viewModel.restoreSampleRoutine()
                            } else {
                                viewModel.restoreSampleRoutine()
                            }
                        }
                    )
                }

                NavigationTab.PROGRESS -> {
                    ProgressScreen(
                        tasks = tasks,
                        todayLogs = todayLogs,
                        allLogs = allLogs,
                        onClearLogs = { viewModel.clearAllLogs() },
                        onExportCsv = {
                            val csv = viewModel.exportLogsCsv()
                            val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
                            clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("FlowStudy CSV", csv))
                        }
                    )
                }

                NavigationTab.SETTINGS -> {
                    SettingsScreen(
                        settings = settings,
                        onUpdateSettings = { viewModel.updateSettings(it) },
                        onOpenFocusCustomization = { viewModel.openFocusCustomization() },
                        onOpenWallpaperSettings = { viewModel.openWallpaperSettings() },
                        onOpenAppBlocker = { viewModel.openAppBlocker() },
                        onOpenSoundSettings = { viewModel.openSoundSettings() },
                        onOpenAutoFlowSettings = { viewModel.openAutoFlowSettings() },
                        onExportBackupJson = { viewModel.exportBackupJson() },
                        onExportCsv = { viewModel.exportLogsCsv() },
                        onRestoreBackupJson = { viewModel.restoreBackupJson(it) },
                        onResetToDefaultRoutine = { viewModel.restoreSampleRoutine() },
                        onClearAllData = { viewModel.resetAllData() }
                    )
                }
            }

            // Fullscreen Active Focus Timer Overlay
            AnimatedVisibility(
                visible = isTimerScreenOpen && timerSnapshot.taskId != 0L,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                ActiveTimerSessionScreen(
                    snapshot = timerSnapshot,
                    nextTask = tasks.firstOrNull { it.id != timerSnapshot.taskId && !it.isCompleted },
                    customization = focusCustomization,
                    wallpaperConfig = wallpaperConfig,
                    isInPipMode = false,
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onAddSeconds = { viewModel.addTimerSeconds(it) },
                    onFinishTask = { viewModel.finishCurrentTask(it) },
                    onEmergencyStop = { viewModel.emergencyStopSession() },
                    onSkipTask = { viewModel.skipCurrentTask() },
                    onMinimize = { viewModel.closeActiveTimerScreen() },
                    onHideForLecture = { viewModel.minimizeTimerScreen(hideCompletely = true) },
                    onContinueInFlow = { viewModel.continueInFlow() }
                )
            }

            // Task Edit / Create Bottom Sheet
            if (isEditSheetOpen) {
                TaskEditBottomSheet(
                    taskToEdit = taskBeingEdited,
                    onDismiss = { viewModel.closeEditSheet() },
                    onSaveTask = { viewModel.saveTask(it) }
                )
            }

            // Focus Screen Customization Bottom Sheet
            if (isFocusCustomizationOpen) {
                FocusCustomizationBottomSheet(
                    currentConfig = focusCustomization,
                    onSaveConfig = { viewModel.saveFocusCustomization(it) },
                    onDismiss = { viewModel.closeFocusCustomization() }
                )
            }

            // Wallpaper Settings Bottom Sheet
            if (isWallpaperSettingsOpen) {
                WallpaperSettingsBottomSheet(
                    currentConfig = wallpaperConfig,
                    onSaveConfig = { viewModel.saveWallpaperConfig(it) },
                    onDismiss = { viewModel.closeWallpaperSettings() }
                )
            }

            // App Blocker Bottom Sheet
            if (isAppBlockerOpen) {
                AppBlockerBottomSheet(
                    currentConfig = appBlockConfig,
                    installedApps = installedApps,
                    onSaveConfig = { viewModel.saveAppBlockConfig(it) },
                    onToggleApp = { pkg, shouldBlock -> viewModel.toggleAppBlock(pkg, shouldBlock) },
                    onDismiss = { viewModel.closeAppBlocker() }
                )
            }

            // Sound & Ringtone Settings Bottom Sheet
            if (isSoundSettingsOpen) {
                SoundRingtoneBottomSheet(
                    currentConfig = soundConfig,
                    onSaveConfig = { viewModel.saveSoundConfig(it) },
                    onPlayPreview = { soundId, uri -> viewModel.playTonePreview(soundId, uri) },
                    onStopPreview = { viewModel.stopTonePreview() },
                    onDismiss = { viewModel.closeSoundSettings() }
                )
            }

            // Auto-Flow & Next Task Alarms Bottom Sheet
            if (isAutoFlowSettingsOpen) {
                AutoFlowBottomSheet(
                    currentConfig = autoFlowConfig,
                    onSaveConfig = { viewModel.saveAutoFlowConfig(it) },
                    onTestAlarm = { viewModel.playTonePreview("digital_alarm") },
                    onDismiss = { viewModel.closeAutoFlowSettings() }
                )
            }

            // 30-Second Auto-Start Transition Dialog
            transitionCountdownState?.let { countdownState ->
                TaskTransitionDialog(
                    state = countdownState,
                    onTakeBreak = { viewModel.onTransitionTakeBreak(it) },
                    onExtendPrevious = { viewModel.onTransitionExtendPrevious(it) },
                    onStartCompulsory = { viewModel.onTransitionStartCompulsory(it) },
                    onCancelPause = { viewModel.onTransitionCancelPause() }
                )
            }

            // All Compulsory Tasks Completed Motivation & Celebration Dialog
            if (isCelebrationDialogOpen) {
                val totalLoggedSecondsToday = todayLogs.sumOf { it.durationSeconds }
                CompulsoryCelebrationDialog(
                    tasks = tasks,
                    todayTotalSeconds = totalLoggedSecondsToday,
                    autoFlowConfig = autoFlowConfig,
                    onCustomizeWallpaperAndQuote = {
                        viewModel.openAutoFlowSettings()
                    },
                    onDismiss = { viewModel.closeCelebrationDialog() }
                )
            }
        }
    }
}

@Composable
private fun MiniFocusPlayer(
    snapshot: TimerSessionSnapshot,
    onExpand: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onHide: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, EmeraldPrimary.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
            .clickable { onExpand() }
            .testTag("mini_focus_player"),
        color = DarkSurfaceVariant.copy(alpha = 0.95f),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(if (snapshot.isMinCompleted) EmeraldPrimary else snapshot.category.accentColor, CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = snapshot.taskTitle.ifBlank { "Flow Study" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                    Text(
                        text = if (snapshot.isMinCompleted) "⚡ Flow Active (+${formatDurationHMS(snapshot.flowOvertimeSeconds)})"
                               else "-${formatDurationHMS(snapshot.remainingCompulsorySeconds)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (snapshot.isMinCompleted) EmeraldLight else TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatDurationHMS(snapshot.currentElapsedSeconds),
                    fontFamily = JetBrainsMonoFamily,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldLight,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier.size(28.dp).testTag("mini_player_toggle_play_pause")
                ) {
                    Icon(
                        imageVector = if (snapshot.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (snapshot.isRunning) "Pause" else "Play",
                        tint = EmeraldLight,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onHide,
                    modifier = Modifier.size(28.dp).testTag("mini_player_hide_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = "Hide Countdown (Lecture Mode)",
                        tint = TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniHiddenFloatingPill(
    snapshot: TimerSessionSnapshot,
    onUnhide: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, EmeraldPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .clickable { onUnhide() }
                .testTag("mini_player_unhide_pill"),
            color = DarkSurfaceVariant.copy(alpha = 0.95f),
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(if (snapshot.isMinCompleted) EmeraldPrimary else EmeraldLight, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${formatDurationHMS(snapshot.currentElapsedSeconds)} • Tap to show",
                    fontFamily = JetBrainsMonoFamily,
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "Show Timer",
                    tint = EmeraldLight,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

private fun getTabIcons(tab: NavigationTab): Pair<ImageVector, ImageVector> {
    return when (tab) {
        NavigationTab.HOME -> Pair(Icons.Default.Today, Icons.Outlined.Today)
        NavigationTab.ROUTINE -> Pair(Icons.Default.FormatListBulleted, Icons.Outlined.FormatListBulleted)
        NavigationTab.PROGRESS -> Pair(Icons.Default.BarChart, Icons.Outlined.BarChart)
        NavigationTab.SETTINGS -> Pair(Icons.Default.Settings, Icons.Outlined.Settings)
    }
}
