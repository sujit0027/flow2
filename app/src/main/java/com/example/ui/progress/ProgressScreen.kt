package com.example.ui.progress

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.RoutineTask
import com.example.data.local.model.StudySessionLog
import com.example.data.local.model.TaskCategory
import com.example.ui.components.CategoryBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FlowCard
import com.example.ui.components.FlowConfirmationDialog
import com.example.ui.components.FlowProgressBar
import com.example.ui.components.formatDurationHMS
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProgressScreen(
    tasks: List<RoutineTask>,
    todayLogs: List<StudySessionLog>,
    allLogs: List<StudySessionLog>,
    onClearLogs: () -> Unit,
    onExportCsv: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var selectedTimeTab by remember { mutableIntStateOf(0) } // 0: Today, 1: All-Time / Weekly

    val currentLogs = if (selectedTimeTab == 0) todayLogs else allLogs

    val totalStudySeconds = currentLogs.filter { it.category == TaskCategory.STUDY }.sumOf { it.durationSeconds }
    val totalExtensionSeconds = currentLogs.sumOf { it.extensionSeconds }
    val completedSessionsCount = currentLogs.count { !it.wasInterrupted }
    val interruptedSessionsCount = currentLogs.count { it.wasInterrupted }

    // Subject breakdown
    val subjectMap = currentLogs
        .filter { it.subject.isNotBlank() }
        .groupBy { it.subject }
        .mapValues { entry -> entry.value.sumOf { it.durationSeconds } }
        .toList()
        .sortedByDescending { it.second }

    // Streaks calculation (days with >= 1 hour of study)
    val dayFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val studyDays = remember(allLogs) {
        allLogs
            .filter { it.category == TaskCategory.STUDY }
            .groupBy { dayFormat.format(Date(it.completedTimestamp)) }
            .filter { entry -> entry.value.sumOf { it.durationSeconds } >= 3600L }
            .keys.size
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Study Analytics",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Subject-wise & flow state insights",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onExportCsv,
                        modifier = Modifier.testTag("progress_export_csv_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Export CSV",
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    if (allLogs.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearConfirmDialog = true },
                            modifier = Modifier.testTag("progress_clear_logs_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear History",
                                tint = TextTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Today vs All-Time Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTimeTab,
                containerColor = DarkSurfaceVariant,
                contentColor = EmeraldLight,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTimeTab]),
                        color = EmeraldPrimary
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .padding(vertical = 4.dp)
            ) {
                Tab(
                    selected = selectedTimeTab == 0,
                    onClick = { selectedTimeTab = 0 },
                    text = { Text("Today's Session", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTimeTab == 1,
                    onClick = { selectedTimeTab = 1 },
                    text = { Text("Weekly / All-Time", fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        // Focus & Flow Overtime Metric Strip
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Focus Time
                FlowCard(
                    modifier = Modifier.weight(1f),
                    borderColor = EmeraldPrimary.copy(alpha = 0.4f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.AutoGraph, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(16.dp))
                            Text(text = "FOCUSED STUDY", style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formatDurationHMS(totalStudySeconds),
                            fontFamily = JetBrainsMonoFamily,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                // Flow Overtime
                FlowCard(
                    modifier = Modifier.weight(1f),
                    borderColor = DarkBorder
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(16.dp))
                            Text(text = "FLOW OVERTIME", style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "+${formatDurationHMS(totalExtensionSeconds)}",
                            fontFamily = JetBrainsMonoFamily,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldLight
                        )
                    }
                }
            }
        }

        // Completion vs Interrupted Sessions & Streaks
        item {
            FlowCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "COMPLETED SESSIONS", style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 10.sp)
                        Text(text = "$completedSessionsCount completed", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "INTERRUPTED", style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 10.sp)
                        Text(
                            text = "$interruptedSessionsCount stopped early",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (interruptedSessionsCount > 0) WarningAmber else TextSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "$studyDays Days", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = WarningAmber)
                        }
                        Text(text = "Qualifying Streak", style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 10.sp)
                    }
                }
            }
        }

        // Subject-Wise Breakdown
        if (subjectMap.isNotEmpty()) {
            item {
                FlowCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SUBJECT-WISE STUDY BREAKDOWN",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val totalSubjectSecs = subjectMap.sumOf { it.second }.coerceAtLeast(1L)
                        subjectMap.forEach { (subjectName, seconds) ->
                            val fraction = seconds.toFloat() / totalSubjectSecs.toFloat()
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = subjectName, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Medium)
                                    Text(text = formatDurationHMS(seconds), fontFamily = JetBrainsMonoFamily, style = MaterialTheme.typography.labelSmall, color = EmeraldLight)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                FlowProgressBar(progress = fraction, barColor = EmeraldPrimary)
                            }
                        }
                    }
                }
            }
        }

        // Recent Sessions Log
        item {
            Text(
                text = "SESSION HISTORY LOG (${currentLogs.size})",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                letterSpacing = 1.sp
            )
        }

        if (currentLogs.isEmpty()) {
            item {
                EmptyStateView(
                    title = "No Focus Sessions Recorded",
                    description = "Start your study routine to track active time, flow overtime, and milestones.",
                    icon = Icons.Default.History
                )
            }
        } else {
            items(currentLogs.take(20), key = { it.sessionId }) { log ->
                EnhancedSessionLogItem(log = log)
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showClearConfirmDialog) {
        FlowConfirmationDialog(
            title = "Clear History Logs?",
            message = "This will erase recorded session logs and history. Your routines and task structures will remain intact.",
            confirmText = "Clear History",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                showClearConfirmDialog = false
                onClearLogs()
            },
            onDismiss = { showClearConfirmDialog = false }
        )
    }
}

@Composable
private fun EnhancedSessionLogItem(log: StudySessionLog) {
    val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
    val formattedDate = remember(log.completedTimestamp) {
        dateFormat.format(Date(log.completedTimestamp))
    }

    FlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CategoryBadge(category = log.category)
                    if (log.subject.isNotBlank()) {
                        Text(
                            text = log.subject,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = "• $formattedDate",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = log.taskTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                if (log.notes.isNotBlank()) {
                    Text(
                        text = log.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatDurationHMS(log.durationSeconds),
                    fontFamily = JetBrainsMonoFamily,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (log.wasInterrupted) WarningAmber else EmeraldLight
                )

                if (log.wasInterrupted) {
                    Text(text = "Interrupted", style = MaterialTheme.typography.labelSmall, color = WarningAmber, fontSize = 10.sp)
                } else if (log.extensionSeconds > 0) {
                    Text(text = "+${formatDurationHMS(log.extensionSeconds)} flow", style = MaterialTheme.typography.labelSmall, color = EmeraldLight, fontSize = 10.sp)
                }
            }
        }
    }
}
