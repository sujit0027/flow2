package com.example.ui.home

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.RoutineEntity
import com.example.data.local.model.RoutineTask
import com.example.data.local.model.StudySessionLog
import com.example.data.preferences.WallpaperConfig
import com.example.data.timer.TimerSessionSnapshot
import com.example.ui.components.CategoryBadge
import com.example.ui.components.CompulsoryBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FlowCard
import com.example.ui.components.FlowConfirmationDialog
import com.example.ui.components.FlowProgressBar
import com.example.ui.components.PrimaryFlowButton
import com.example.ui.components.formatDurationHMS
import com.example.ui.components.formatMinutesHuman
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
import com.example.ui.wallpaper.WallpaperBackground

@Composable
fun HomeScreen(
    tasks: List<RoutineTask>,
    todayLogs: List<StudySessionLog>,
    activeRoutine: RoutineEntity?,
    timerSnapshot: TimerSessionSnapshot,
    wallpaperConfig: WallpaperConfig,
    dailyTargetHours: Int,
    onStartTask: (RoutineTask) -> Unit,
    onToggleTaskComplete: (RoutineTask) -> Unit,
    onOpenCreateTask: () -> Unit,
    onResetTodayCompletions: () -> Unit,
    onCarryForwardUnfinished: () -> Unit,
    onOpenWallpaperSettings: () -> Unit,
    onOpenFocusCustomization: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showResetDialog by remember { mutableStateOf(false) }
    var showCarryForwardDialog by remember { mutableStateOf(false) }

    val nextPendingTask = tasks.firstOrNull { !it.isCompleted }
    val subsequentTask = tasks.filter { !it.isCompleted }.drop(1).firstOrNull()
    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    val totalLoggedSecondsToday = todayLogs.sumOf { it.durationSeconds }
    val targetSeconds = dailyTargetHours * 3600L
    val dailyTargetFraction = if (targetSeconds > 0) {
        (totalLoggedSecondsToday.toFloat() / targetSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Box(modifier = modifier.fillMaxSize()) {
        // Wallpaper Layer
        WallpaperBackground(
            wallpaperType = wallpaperConfig.homeWallpaperType,
            dimLevel = wallpaperConfig.dimLevel,
            customUri = wallpaperConfig.customWallpaperUri
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                // Header with Routine Name and Fast Customization Shortcuts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "FlowStudy",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (activeRoutine?.isSampleData == true) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = EmeraldContainer
                                ) {
                                    Text(
                                        text = "SAMPLE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldLight,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = activeRoutine?.name ?: "Flexible Daily Study Flow",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onOpenWallpaperSettings,
                            modifier = Modifier.size(36.dp).testTag("home_wallpaper_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Wallpapers",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onOpenFocusCustomization,
                            modifier = Modifier.size(36.dp).testTag("home_customization_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Focus Screen Customization",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Daily Target & Focus Time Strip
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Daily Target Progress Card
                    FlowCard(
                        modifier = Modifier.weight(1f),
                        borderColor = DarkBorder
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "TODAY'S FOCUS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "${dailyTargetHours}h target",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatDurationHMS(totalLoggedSecondsToday),
                                fontFamily = JetBrainsMonoFamily,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldLight
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowProgressBar(
                                progress = dailyTargetFraction,
                                barColor = EmeraldPrimary
                            )
                        }
                    }

                    // Routine Progress Card
                    FlowCard(
                        modifier = Modifier.weight(1f),
                        borderColor = DarkBorder
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "ROUTINE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "$completedCount/$totalCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${(progressFraction * 100).toInt()}% Done",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowProgressBar(
                                progress = progressFraction,
                                barColor = EmeraldPrimary
                            )
                        }
                    }
                }
            }

            // Current / Next Task Card
            item {
                if (nextPendingTask != null) {
                    FlowCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hero_current_task_card"),
                        borderColor = EmeraldPrimary.copy(alpha = 0.5f)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ACTIVE COMMITMENT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldLight,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                CategoryBadge(category = nextPendingTask.category)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = nextPendingTask.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            if (nextPendingTask.subject.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Subject: ${nextPendingTask.subject}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }

                            if (nextPendingTask.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = nextPendingTask.notes,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    maxLines = 2
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CompulsoryBadge(
                                    isCompulsory = nextPendingTask.isCompulsory,
                                    isOptional = nextPendingTask.isOptional
                                )
                                Text(
                                    text = "• Min: ${formatMinutesHuman(nextPendingTask.minDurationMinutes)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                                if (nextPendingTask.extensionTargetMinutes > 0) {
                                    Text(
                                        text = "• Flow: +${formatMinutesHuman(nextPendingTask.extensionTargetMinutes)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextTertiary
                                    )
                                }
                            }

                            // Next Task Preview inside Hero Card
                            if (subsequentTask != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = DarkSurfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Up Next: ${subsequentTask.title} (${formatMinutesHuman(subsequentTask.minDurationMinutes)})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextTertiary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            PrimaryFlowButton(
                                text = "Start Focus Session",
                                onClick = { onStartTask(nextPendingTask) },
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.PlayArrow,
                                testTag = "hero_start_task_button"
                            )
                        }
                    }
                } else if (tasks.isNotEmpty()) {
                    FlowCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = EmeraldPrimary
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(EmeraldContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = EmeraldLight,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Daily Routine Completed!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "All planned flexible study tasks have been fulfilled.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Quick Sequence Header with Carry Forward & Reset Actions
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Flexible Task Sequence",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (tasks.any { !it.isCompleted && it.accumulatedDurationSeconds > 0 }) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DarkSurfaceVariant,
                                modifier = Modifier.clickable { showCarryForwardDialog = true }
                            ) {
                                Text(
                                    text = "Carry Forward",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldLight,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (completedCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DarkSurfaceVariant,
                                modifier = Modifier.clickable { showResetDialog = true }
                            ) {
                                Text(
                                    text = "Reset Cycle",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Tasks List
            if (tasks.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No Tasks in Routine",
                        description = "Add flexible study milestones or load our student competitive exam routine.",
                        actionButtonText = "Add Task",
                        onActionClick = onOpenCreateTask
                    )
                }
            } else {
                itemsIndexed(tasks, key = { _, task -> task.id }) { index, task ->
                    RoutineTimelineItem(
                        task = task,
                        stepNumber = index + 1,
                        onStart = { onStartTask(task) },
                        onToggleComplete = { onToggleTaskComplete(task) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    if (showResetDialog) {
        FlowConfirmationDialog(
            title = "Reset Today's Cycle?",
            message = "This will mark routine tasks as pending for a fresh cycle while preserving all study analytics and logs.",
            confirmText = "Reset Cycle",
            dismissText = "Cancel",
            onConfirm = {
                showResetDialog = false
                onResetTodayCompletions()
            },
            onDismiss = { showResetDialog = false }
        )
    }

    if (showCarryForwardDialog) {
        FlowConfirmationDialog(
            title = "Carry Forward Unfinished Tasks?",
            message = "Unfinished tasks will be rescheduled to the top of your sequence, preserving all logged minutes.",
            confirmText = "Carry Forward",
            dismissText = "Cancel",
            onConfirm = {
                showCarryForwardDialog = false
                onCarryForwardUnfinished()
            },
            onDismiss = { showCarryForwardDialog = false }
        )
    }
}

@Composable
private fun RoutineTimelineItem(
    task: RoutineTask,
    stepNumber: Int,
    onStart: () -> Unit,
    onToggleComplete: () -> Unit
) {
    val isCompleted = task.isCompleted

    FlowCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("timeline_task_item_${task.id}"),
        borderColor = if (isCompleted) DarkBorder.copy(alpha = 0.5f) else DarkBorder
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isCompleted,
                onCheckedChange = { onToggleComplete() },
                modifier = Modifier.testTag("task_checkbox_${task.id}"),
                colors = CheckboxDefaults.colors(
                    checkedColor = EmeraldPrimary,
                    uncheckedColor = TextTertiary,
                    checkmarkColor = DarkBackground
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onStart() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CategoryBadge(category = task.category)
                    CompulsoryBadge(
                        isCompulsory = task.isCompulsory,
                        isOptional = task.isOptional
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCompleted) TextTertiary else TextPrimary,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )

                if (task.subject.isNotBlank()) {
                    Text(
                        text = task.subject,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Min ${formatMinutesHuman(task.minDurationMinutes)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCompleted) TextTertiary else EmeraldLight,
                        fontFamily = JetBrainsMonoFamily
                    )
                    if (task.extensionTargetMinutes > 0) {
                        Text(
                            text = "• Flow +${formatMinutesHuman(task.extensionTargetMinutes)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                    }
                    if (task.accumulatedDurationSeconds > 0) {
                        Text(
                            text = "• Logged: ${formatDurationHMS(task.accumulatedDurationSeconds)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontFamily = JetBrainsMonoFamily
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) DarkSurfaceVariant else EmeraldContainer)
                    .clickable { onStart() }
                    .testTag("task_play_action_${task.id}"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start ${task.title}",
                    tint = if (isCompleted) TextTertiary else EmeraldLight,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
