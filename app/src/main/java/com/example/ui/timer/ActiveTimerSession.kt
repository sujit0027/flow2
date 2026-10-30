package com.example.ui.timer

import android.app.Activity
import android.app.PictureInPictureParams
import android.os.Build
import android.util.Rational
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.LockClock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.RoutineTask
import com.example.data.preferences.FocusScreenCustomization
import com.example.data.preferences.WallpaperConfig
import com.example.data.timer.TimerSessionSnapshot
import com.example.ui.components.CategoryBadge
import com.example.ui.components.FlowConfirmationDialog
import com.example.ui.components.PrimaryFlowButton
import com.example.ui.components.SecondaryFlowButton
import com.example.ui.components.formatDurationHMS
import com.example.ui.components.formatMinutesHuman
import com.example.ui.theme.CategoryBreak
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
import com.example.ui.theme.WarningAmber
import com.example.ui.wallpaper.WallpaperBackground

@Composable
fun ActiveTimerSessionScreen(
    snapshot: TimerSessionSnapshot,
    nextTask: RoutineTask?,
    customization: FocusScreenCustomization,
    wallpaperConfig: WallpaperConfig,
    isInPipMode: Boolean = false,
    onTogglePlayPause: () -> Unit,
    onAddSeconds: (Long) -> Unit,
    onFinishTask: (nextChoice: PostTaskChoice) -> Unit,
    onEmergencyStop: () -> Unit,
    onSkipTask: () -> Unit,
    onMinimize: () -> Unit,
    onHideForLecture: () -> Unit = {},
    onContinueInFlow: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showEmergencyStopDialog by remember { mutableStateOf(false) }
    var distractionCount by remember { mutableIntStateOf(0) }

    val elapsedSeconds = snapshot.currentElapsedSeconds
    val minDurationSeconds = snapshot.minDurationSeconds
    val isMinCompleted = snapshot.isMinCompleted
    val remainingCompulsory = snapshot.remainingCompulsorySeconds
    val flowOvertime = snapshot.flowOvertimeSeconds

    BackHandler {
        onMinimize()
    }

    // Gentle pulse for Flow state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // PiP Ultra-Compact Mode: Minimalist floating sliver that does not block YouTube lectures
    if (isInPipMode) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
                .clickable { onTogglePlayPause() }
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(if (isMinCompleted) EmeraldPrimary else WarningAmber, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formatDurationHMS(elapsedSeconds),
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMinCompleted) EmeraldLight else TextPrimary
                )
                if (!isMinCompleted) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${formatDurationHMS(remainingCompulsory)})",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarningAmber,
                        fontSize = 10.sp
                    )
                }
            }
        }
        return
    }

    // Full Focus Screen
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("active_timer_session_screen")
    ) {
        // Wallpaper Layer
        WallpaperBackground(
            wallpaperType = wallpaperConfig.focusWallpaperType,
            dimLevel = wallpaperConfig.dimLevel,
            customUri = wallpaperConfig.customWallpaperUri
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Minimize, Subject/Category, Floating PiP Trigger
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onMinimize,
                        modifier = Modifier.testTag("timer_minimize_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Minimize Timer",
                            tint = TextSecondary
                        )
                    }

                    IconButton(
                        onClick = onHideForLecture,
                        modifier = Modifier.testTag("timer_hide_lecture_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = "Hide Timer (Lecture Mode)",
                            tint = EmeraldLight
                        )
                    }
                }

                if (customization.showSessionLabel && !customization.isUltraMinimal) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        CategoryBadge(category = snapshot.category)
                        if (snapshot.subject.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = DarkSurfaceVariant
                            ) {
                                Text(
                                    text = snapshot.subject.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // PiP Button (only on supported Android 8.0+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    IconButton(
                        onClick = {
                            val activity = context as? Activity
                            try {
                                val params = PictureInPictureParams.Builder()
                                    .setAspectRatio(Rational(16, 9))
                                    .build()
                                activity?.enterPictureInPictureMode(params)
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier.testTag("timer_pip_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureInPicture,
                            contentDescription = "Floating Window (PiP)",
                            tint = TextSecondary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(40.dp))
                }
            }

            // Task Title & Motivational Quote
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                if (customization.showTaskTitle) {
                    Text(
                        text = snapshot.taskTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                }

                if (customization.showMotivationalQuote && !customization.isUltraMinimal) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "\"Small focused disciplines build unstoppable momentum.\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dynamic Status Badge
                val statusText: String
                val statusColor: Color
                val statusBg: Color

                if (isMinCompleted) {
                    statusText = "⚡ IN FLOW STATE — MINIMUM TARGET REACHED"
                    statusColor = EmeraldLight
                    statusBg = EmeraldContainer.copy(alpha = 0.85f)
                } else if (snapshot.isCompulsory) {
                    statusText = "COMPULSORY MINIMUM: ${formatMinutesHuman((snapshot.minDurationSeconds / 60).toInt())}"
                    statusColor = WarningAmber
                    statusBg = DarkSurfaceVariant
                } else {
                    statusText = "FLEXIBLE TASK: ${formatMinutesHuman((snapshot.minDurationSeconds / 60).toInt())}"
                    statusColor = CategoryBreak
                    statusBg = DarkSurfaceVariant
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            // Circular Chronograph Progress Ring or Minimal Timer
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(270.dp)
                    .scale(if (isMinCompleted && snapshot.isRunning) pulseScale else 1f)
                    .testTag("timer_display_area")
            ) {
                val progress = if (minDurationSeconds > 0) {
                    (elapsedSeconds.toFloat() / minDurationSeconds.toFloat()).coerceIn(0f, 1f)
                } else 1f

                val dialColor = if (isMinCompleted) EmeraldPrimary else snapshot.category.accentColor

                if (customization.showProgressRing && !customization.isUltraMinimal) {
                    Canvas(modifier = Modifier.size(250.dp)) {
                        drawCircle(
                            color = DarkSurfaceVariant.copy(alpha = 0.7f),
                            style = Stroke(width = 10.dp.toPx())
                        )
                        drawArc(
                            color = dialColor,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (customization.showTimer) {
                        Text(
                            text = if (!isMinCompleted) {
                                formatDurationHMS(remainingCompulsory)
                            } else {
                                formatDurationHMS(elapsedSeconds)
                            },
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            letterSpacing = (-0.5).sp,
                            modifier = Modifier.testTag("timer_elapsed_text")
                        )
                    }

                    if (customization.showMinRemaining && !customization.isUltraMinimal) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (!isMinCompleted) {
                                "${formatDurationHMS(elapsedSeconds)} active elapsed"
                            } else {
                                "+${formatDurationHMS(flowOvertime)} overtime in flow"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isMinCompleted) EmeraldLight else TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (!snapshot.isRunning) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "PAUSED",
                            style = MaterialTheme.typography.labelSmall,
                            color = WarningAmber,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Next Task Preview (if enabled)
            if (customization.showNextTaskPreview && nextTask != null && !customization.isUltraMinimal) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceVariant.copy(alpha = 0.7f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Next: ${nextTask.title}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = formatMinutesHuman(nextTask.minDurationMinutes),
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldLight,
                            fontFamily = JetBrainsMonoFamily
                        )
                    }
                }
            }

            // Controls & Actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Extension Chips (+5m, +15m) & Distraction Logger
                if (!customization.isUltraMinimal) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Extend:", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                            listOf(5 to "+5m", 15 to "+15m").forEach { (mins, label) ->
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                        .clickable { onAddSeconds(mins * 60L) },
                                    color = DarkSurfaceVariant
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Distraction Counter Pill
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                .clickable { distractionCount += 1 },
                            color = DarkSurfaceVariant
                        ) {
                            Text(
                                text = "Distractions: $distractionCount (+)",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Play / Pause Circle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(if (snapshot.isRunning) DarkSurfaceVariant else EmeraldPrimary)
                            .border(1.dp, if (snapshot.isRunning) DarkBorder else EmeraldLight, CircleShape)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = onTogglePlayPause,
                            modifier = Modifier.size(62.dp).testTag("timer_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (snapshot.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (snapshot.isRunning) "Pause" else "Resume",
                                tint = if (snapshot.isRunning) TextPrimary else DarkBackground,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }

                // When minimum is reached and awaiting user action (with 30s countdown before wake alarm)
                if (snapshot.isAwaitingContinue) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterHorizontally
                    ) {
                        PrimaryFlowButton(
                            text = "Continue Working in Flow (No Extension Needed)",
                            onClick = onContinueInFlow,
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Default.FastForward,
                            testTag = "timer_continue_flow_button"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SecondaryFlowButton(
                                text = "Stop / Exit",
                                onClick = { showEmergencyStopDialog = true },
                                modifier = Modifier.weight(0.4f),
                                testTag = "timer_emergency_stop_button"
                            )

                            PrimaryFlowButton(
                                text = "Finish Task →",
                                onClick = { onFinishTask(PostTaskChoice.JUST_FINISH) },
                                modifier = Modifier.weight(0.6f),
                                icon = Icons.Default.Check,
                                testTag = "timer_finish_button"
                            )
                        }

                        if (snapshot.awaitingContinueSecondsLeft > 0 && !snapshot.isWakeAlarmRinging) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Wake alarm in ${snapshot.awaitingContinueSecondsLeft}s if no action taken",
                                style = MaterialTheme.typography.labelSmall,
                                color = WarningAmber,
                                fontSize = 11.sp
                            )
                        } else if (snapshot.isWakeAlarmRinging) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "⚠️ Wake alarm ringing! Tap Continue or Finish",
                                style = MaterialTheme.typography.labelSmall,
                                color = WarningAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    // Action Buttons: Finish Task vs Emergency Stop
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Emergency Stop / Incomplete Exit
                        SecondaryFlowButton(
                            text = "Stop / Exit",
                            onClick = { showEmergencyStopDialog = true },
                            modifier = Modifier.weight(0.4f),
                            testTag = "timer_emergency_stop_button"
                        )

                        // Finish Task (Enabled only after minimum is reached for compulsory tasks)
                        PrimaryFlowButton(
                            text = if (isMinCompleted || snapshot.hasContinuedIntoFlow) "Finish Task" else "Finish (Min ${formatMinutesHuman((snapshot.minDurationSeconds / 60).toInt())})",
                            onClick = {
                                if (isMinCompleted || snapshot.hasContinuedIntoFlow || !snapshot.isCompulsory) {
                                    onFinishTask(PostTaskChoice.JUST_FINISH)
                                } else {
                                    showEmergencyStopDialog = true
                                }
                            },
                            enabled = isMinCompleted || snapshot.hasContinuedIntoFlow || !snapshot.isCompulsory,
                            modifier = Modifier.weight(0.6f),
                            icon = Icons.Default.Check,
                            testTag = "timer_finish_button"
                        )
                    }
                }
            }
        }
    }

    // Emergency Stop / Incomplete Exit Dialog
    if (showEmergencyStopDialog) {
        val remainingMins = (remainingCompulsory / 60) + 1
        AlertDialog(
            onDismissRequest = { showEmergencyStopDialog = false },
            title = {
                Text(
                    text = if (!isMinCompleted && snapshot.isCompulsory) "Compulsory Minimum Not Reached" else "End Session Early?",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = if (!isMinCompleted && snapshot.isCompulsory) {
                        "This study task has a compulsory minimum duration. There are ~$remainingMins minutes remaining. Stopping now will log this session as interrupted / incomplete rather than completed."
                    } else {
                        "Do you want to save active progress and end this session?"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEmergencyStopDialog = false
                        onEmergencyStop()
                    }
                ) {
                    Text("Record as Incomplete", color = WarningAmber, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmergencyStopDialog = false }) {
                    Text("Keep Flowing", color = EmeraldPrimary)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

enum class PostTaskChoice {
    ADVANCE_NEXT_TASK,
    TAKE_BREAK,
    HAVE_MEAL,
    CONTINUE_STUDYING,
    JUST_FINISH
}

@Composable
private fun PostTaskChoiceModal(
    nextTask: RoutineTask?,
    flowOvertimeSeconds: Long,
    onChoiceSelected: (PostTaskChoice) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "🎉 Minimum Completed!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (flowOvertimeSeconds > 0) {
                    Text(
                        text = "+${formatDurationHMS(flowOvertimeSeconds)} bonus flow time logged",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldLight
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "What would you like to do next?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (nextTask != null) {
                    ChoiceButton(
                        title = "Advance to Next Task",
                        subtitle = "${nextTask.title} (${formatMinutesHuman(nextTask.minDurationMinutes)})",
                        onClick = { onChoiceSelected(PostTaskChoice.ADVANCE_NEXT_TASK) },
                        highlight = true
                    )
                }

                ChoiceButton(
                    title = "Take a Flexible Break",
                    subtitle = "Relax eyes & hydrate for 5–10 mins",
                    onClick = { onChoiceSelected(PostTaskChoice.TAKE_BREAK) }
                )

                ChoiceButton(
                    title = "Have a Meal or Snack",
                    subtitle = "Lunch, dinner, or nutritious snack",
                    onClick = { onChoiceSelected(PostTaskChoice.HAVE_MEAL) }
                )

                ChoiceButton(
                    title = "Keep Studying in Flow",
                    subtitle = "Resume timer and extend focus",
                    onClick = { onChoiceSelected(PostTaskChoice.CONTINUE_STUDYING) }
                )

                ChoiceButton(
                    title = "Just Finish & Return Home",
                    subtitle = "Mark task completed and close session",
                    onClick = { onChoiceSelected(PostTaskChoice.JUST_FINISH) }
                )
            }
        },
        confirmButton = {},
        dismissButton = {},
        containerColor = DarkSurface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun ChoiceButton(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    highlight: Boolean = false
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        color = if (highlight) EmeraldContainer else DarkSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (highlight) EmeraldPrimary else DarkBorder),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (highlight) EmeraldLight else TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}
