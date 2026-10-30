package com.example.ui.timer

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.RoutineTask
import com.example.data.local.model.TaskCategory
import com.example.ui.components.PrimaryFlowButton
import com.example.ui.components.formatMinutesHuman
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

data class TaskTransitionCountdownState(
    val finishedTaskId: Long,
    val finishedTaskTitle: String,
    val intermediateTasks: List<RoutineTask> = emptyList(),
    val optionalNextTask: RoutineTask? = intermediateTasks.firstOrNull(),
    val nextCompulsoryTask: RoutineTask,
    val totalCountdownSeconds: Int = 30,
    val remainingSeconds: Int = 30,
    val isRunning: Boolean = true
)

@Composable
fun TaskTransitionDialog(
    state: TaskTransitionCountdownState,
    onTakeBreak: (RoutineTask) -> Unit,
    onExtendPrevious: (taskId: Long) -> Unit,
    onStartCompulsory: (RoutineTask) -> Unit,
    onCancelPause: () -> Unit
) {
    val progress = (state.remainingSeconds.toFloat() / state.totalCountdownSeconds.coerceAtLeast(1).toFloat())
        .coerceIn(0f, 1f)

    val isUrgent = state.remainingSeconds <= 10
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_alarm")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isUrgent) 1.08f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isUrgent) 400 else 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    AlertDialog(
        onDismissRequest = { /* Modal: Require explicit choice or cancel */ },
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 540.dp)
                    .verticalScroll(rememberScrollState())
                    .testTag("task_transition_dialog"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated Countdown Circular Ring
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(pulseScale),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(100.dp)) {
                        // Track background
                        drawCircle(
                            color = DarkBorder,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                        // Progress sweep
                        drawArc(
                            color = if (isUrgent) WarningAmber else EmeraldPrimary,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${state.remainingSeconds}s",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUrgent) WarningAmber else EmeraldLight
                        )
                        Text(
                            text = if (isUrgent) "ALARM SOON" else "AUTO-START",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isUrgent) WarningAmber else TextTertiary,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Completed: ${state.finishedTaskTitle}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (state.intermediateTasks.isNotEmpty()) {
                        "Below are all flexible tasks before '${state.nextCompulsoryTask.title}'. Choose any task to start, or wait ${state.remainingSeconds}s for the wake-up alarm to auto-start '${state.nextCompulsoryTask.title}'!"
                    } else {
                        "Next compulsory study task will auto-start with a waking alert in ${state.remainingSeconds}s if no action is chosen."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Intermediate optional / skipped tasks before next compulsory
                if (state.intermediateTasks.isNotEmpty()) {
                    Text(
                        text = "Upcoming Flexible Tasks (Tap to switch):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = WarningAmber,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    state.intermediateTasks.forEach { task ->
                        val icon = when (task.category) {
                            TaskCategory.BREAK -> Icons.Default.Coffee
                            TaskCategory.MEAL -> Icons.Default.Coffee
                            TaskCategory.EXERCISE -> Icons.Default.DirectionsRun
                            else -> Icons.Default.FastForward
                        }
                        TransitionOptionCard(
                            icon = icon,
                            title = "Take ${task.title}",
                            subtitle = "${task.category.displayName} • ${formatMinutesHuman(task.minDurationMinutes)} • Tap to start",
                            highlightColor = task.category.accentColor,
                            onClick = { onTakeBreak(task) },
                            testTag = "transition_task_${task.id}"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                } else if (state.optionalNextTask != null) {
                    TransitionOptionCard(
                        icon = Icons.Default.Coffee,
                        title = "Take ${state.optionalNextTask.title}",
                        subtitle = "Flexible rest • ${formatMinutesHuman(state.optionalNextTask.minDurationMinutes)}",
                        highlightColor = WarningAmber,
                        onClick = { onTakeBreak(state.optionalNextTask) },
                        testTag = "transition_start_break_btn"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Option 2: Extend Current Study Time (+15 mins)
                TransitionOptionCard(
                    icon = Icons.Default.MoreTime,
                    title = "Extend Previous Study (+15m)",
                    subtitle = "Continue studying ${state.finishedTaskTitle} in flow",
                    highlightColor = EmeraldLight,
                    onClick = { onExtendPrevious(state.finishedTaskId) },
                    testTag = "transition_extend_btn"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Option 3: Start Next Compulsory Task Right Now
                TransitionOptionCard(
                    icon = Icons.Default.PlayArrow,
                    title = "Start Compulsory Task Now",
                    subtitle = "${state.nextCompulsoryTask.title} • ${formatMinutesHuman(state.nextCompulsoryTask.minDurationMinutes)}",
                    highlightColor = EmeraldPrimary,
                    isProminent = true,
                    onClick = { onStartCompulsory(state.nextCompulsoryTask) },
                    testTag = "transition_start_compulsory_btn"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Option 4: Cancel / Pause Routine
                TextButton(
                    onClick = onCancelPause,
                    modifier = Modifier.testTag("transition_pause_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pause Routine & Return Home",
                        color = TextTertiary,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {},
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun TransitionOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    highlightColor: Color,
    isProminent: Boolean = false,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        color = if (isProminent) EmeraldContainer else DarkSurfaceVariant,
        border = BorderStroke(1.dp, if (isProminent) EmeraldPrimary else DarkBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(highlightColor.copy(alpha = 0.18f))
                    .border(1.dp, highlightColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = highlightColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isProminent) EmeraldLight else TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}
