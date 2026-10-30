package com.example.ui.routine

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import com.example.ui.components.formatMinutesDetailed
import com.example.ui.components.formatMinutesHuman
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.RoutineTask
import com.example.data.local.model.TaskCategory
import com.example.ui.components.PrimaryFlowButton
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskEditBottomSheet(
    taskToEdit: RoutineTask?,
    onDismiss: () -> Unit,
    onSaveTask: (RoutineTask) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var category by remember { mutableStateOf(taskToEdit?.category ?: TaskCategory.STUDY) }
    var customCategoryName by remember { mutableStateOf(taskToEdit?.customCategoryName ?: "") }
    var subject by remember { mutableStateOf(taskToEdit?.subject ?: "") }
    var minDuration by remember { mutableIntStateOf(taskToEdit?.minDurationMinutes ?: 35) }
    var extensionTarget by remember { mutableIntStateOf(taskToEdit?.extensionTargetMinutes ?: 15) }
    var isExtensionAllowed by remember { mutableStateOf(taskToEdit?.isExtensionAllowed ?: true) }
    var isCompulsory by remember {
        mutableStateOf(taskToEdit?.isCompulsory ?: (category == TaskCategory.STUDY))
    }
    var isOptional by remember {
        mutableStateOf(taskToEdit?.isOptional ?: (category != TaskCategory.STUDY))
    }
    var canBeSkipped by remember {
        mutableStateOf(taskToEdit?.canBeSkipped ?: (category != TaskCategory.STUDY))
    }
    var mealType by remember { mutableStateOf(taskToEdit?.mealType ?: "") }
    var notes by remember { mutableStateOf(taskToEdit?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (taskToEdit == null) "New Routine Task" else "Edit Task",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_task_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title Field
            Text(
                text = "TASK TITLE",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (it.isNotBlank()) errorMessage = null
                },
                placeholder = { Text("e.g. Wave Optics Theory, Lunch, CET MCQs", color = TextTertiary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_title_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant
                ),
                isError = errorMessage != null,
                singleLine = true
            )
            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subject / Topic (e.g. Physics, Chemistry, Math, History)
            Text(
                text = "SUBJECT OR TOPIC (OPTIONAL)",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                placeholder = { Text("e.g. Physics, Chemistry, Test Prep, Revision", color = TextTertiary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_subject_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Category Segmented Chips
            Text(
                text = "CATEGORY",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TaskCategory.entries.forEach { cat ->
                    val isSelected = category == cat
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                category = cat
                                if (taskToEdit == null) {
                                    isCompulsory = cat.defaultIsCompulsory
                                    isOptional = cat.defaultIsOptional
                                    minDuration = cat.defaultDurationMinutes
                                }
                            }
                            .testTag("select_category_${cat.name}"),
                        color = if (isSelected) cat.containerColor else DarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) cat.accentColor else DarkBorder
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(cat.accentColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cat.displayName,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) cat.accentColor else TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Custom Category Name
            if (category == TaskCategory.CUSTOM) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = customCategoryName,
                    onValueChange = { customCategoryName = it },
                    placeholder = { Text("Enter custom category name (e.g. Coding, Reading)", color = TextTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant
                    ),
                    singleLine = true
                )
            }

            // Meal Type Selector (if category == MEAL)
            if (category == TaskCategory.MEAL) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "MEAL RECURRENCE TYPE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("breakfast" to "Breakfast", "lunch" to "Lunch", "dinner" to "Dinner", "snack" to "Snack").forEach { (typeKey, label) ->
                        val isSelected = mealType.lowercase() == typeKey
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { mealType = typeKey },
                            color = if (isSelected) DarkSurfaceHighlight else DarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) EmeraldLight else DarkBorder
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Compulsory Minimum Duration Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "COMPULSORY MINIMUM DURATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = formatMinutesDetailed(minDuration),
                    style = MaterialTheme.typography.titleMedium,
                    color = EmeraldLight,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Standard & Extended Quick Presets (15m up to 3 hrs)
            Text(
                text = "STANDARD & EXTENDED PRESETS",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    15 to "15m",
                    25 to "25m",
                    30 to "30m",
                    45 to "45m",
                    60 to "1 hr",
                    90 to "1h 30m",
                    120 to "2 hrs",
                    150 to "2h 30m",
                    180 to "3 hrs"
                ).forEach { (mins, label) ->
                    val isSelected = minDuration == mins
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { minDuration = mins }
                            .testTag("min_duration_${mins}_chip"),
                        color = if (isSelected) EmeraldPrimary.copy(alpha = 0.2f) else DarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) EmeraldPrimary else DarkBorder
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) EmeraldLight else TextSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Custom Hours & Minutes Adjuster Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
                color = DarkSurfaceVariant,
                shape = RoundedCornerShape(12.dp)
            ) {
                val currentHours = minDuration / 60
                val currentMins = minDuration % 60

                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "CUSTOM TIME ADJUSTER",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                        fontSize = 10.sp,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Hours Stepper
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(DarkBackground, RoundedCornerShape(8.dp))
                                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "HOURS",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        if (currentHours > 0) {
                                            val newTotal = (currentHours - 1) * 60 + currentMins
                                            minDuration = newTotal.coerceAtLeast(5)
                                        }
                                    },
                                    modifier = Modifier.size(28.dp).testTag("decrement_hours_button"),
                                    enabled = currentHours > 0
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Minus 1 Hour",
                                        tint = if (currentHours > 0) TextPrimary else TextTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Text(
                                    text = "$currentHours hr${if (currentHours != 1) "s" else ""}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldLight
                                )

                                IconButton(
                                    onClick = {
                                        if (currentHours < 12) {
                                            val newTotal = (currentHours + 1) * 60 + currentMins
                                            minDuration = newTotal
                                        }
                                    },
                                    modifier = Modifier.size(28.dp).testTag("increment_hours_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Plus 1 Hour",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Minutes Stepper
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(DarkBackground, RoundedCornerShape(8.dp))
                                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "MINUTES",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        val newMins = (currentMins - 5).coerceAtLeast(0)
                                        val newTotal = currentHours * 60 + newMins
                                        minDuration = newTotal.coerceAtLeast(5)
                                    },
                                    modifier = Modifier.size(28.dp).testTag("decrement_minutes_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Minus 5 Minutes",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Text(
                                    text = "$currentMins min",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldLight
                                )

                                IconButton(
                                    onClick = {
                                        val newMins = if (currentMins + 5 >= 60) 0 else currentMins + 5
                                        val addHours = if (currentMins + 5 >= 60) 1 else 0
                                        val newTotal = (currentHours + addHours) * 60 + newMins
                                        minDuration = newTotal.coerceAtLeast(5)
                                    },
                                    modifier = Modifier.size(28.dp).testTag("increment_minutes_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Plus 5 Minutes",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick additive pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            15 to "+15m",
                            30 to "+30m",
                            60 to "+1 hr",
                            120 to "+2 hrs"
                        ).forEach { (addMins, label) ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { minDuration = (minDuration + addMins).coerceAtMost(720) },
                                color = DarkSurfaceHighlight,
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(vertical = 5.dp),
                                    textAlign = TextAlign.Center,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Optional Extension Target (Flow State)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OPTIONAL EXTENSION TARGET (FLOW)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (extensionTarget > 0) "+${formatMinutesHuman(extensionTarget)}" else "Open-ended",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    0 to "None",
                    15 to "+15m",
                    30 to "+30m",
                    45 to "+45m",
                    60 to "+1 hr",
                    90 to "+1h 30m",
                    120 to "+2 hrs"
                ).forEach { (ext, label) ->
                    val isSelected = extensionTarget == ext
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { extensionTarget = ext }
                            .testTag("extension_target_${ext}_chip"),
                        color = if (isSelected) DarkSurfaceHighlight else DarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) TextSecondary else DarkBorder
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Configuration Switches Group
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
                color = DarkSurfaceVariant,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Compulsory Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Compulsory Minimum",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Minimum duration is mandatory for normal task completion.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = isCompulsory,
                            onCheckedChange = { isCompulsory = it },
                            modifier = Modifier.testTag("compulsory_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldPrimary,
                                checkedTrackColor = EmeraldPrimary.copy(alpha = 0.3f),
                                uncheckedThumbColor = TextTertiary,
                                uncheckedTrackColor = DarkBackground
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Allow Extension Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Allow Flow Extension",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Allows continuing study after minimum target is completed.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = isExtensionAllowed,
                            onCheckedChange = { isExtensionAllowed = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldPrimary,
                                checkedTrackColor = EmeraldPrimary.copy(alpha = 0.3f),
                                uncheckedThumbColor = TextTertiary,
                                uncheckedTrackColor = DarkBackground
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Can Be Skipped Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Can Be Skipped",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Ideal for flexible breaks and optional meals.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = canBeSkipped,
                            onCheckedChange = { canBeSkipped = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldPrimary,
                                checkedTrackColor = EmeraldPrimary.copy(alpha = 0.3f),
                                uncheckedThumbColor = TextTertiary,
                                uncheckedTrackColor = DarkBackground
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Notes / Intent
            Text(
                text = "NOTES & STUDY INTENT (OPTIONAL)",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("e.g. Complete chapter exercises 1 to 15", color = TextTertiary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_notes_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            PrimaryFlowButton(
                text = if (taskToEdit == null) "Add to Routine" else "Save Changes",
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Please enter a task title"
                        return@PrimaryFlowButton
                    }
                    val updatedTask = (taskToEdit ?: RoutineTask(
                        title = title.trim(),
                        category = category,
                        minDurationMinutes = minDuration
                    )).copy(
                        title = title.trim(),
                        category = category,
                        customCategoryName = customCategoryName.trim(),
                        subject = subject.trim(),
                        minDurationMinutes = minDuration,
                        extensionTargetMinutes = extensionTarget,
                        isExtensionAllowed = isExtensionAllowed,
                        isCompulsory = isCompulsory,
                        isOptional = isOptional,
                        canBeSkipped = canBeSkipped,
                        mealType = mealType.trim().lowercase(),
                        notes = notes.trim()
                    )
                    onSaveTask(updatedTask)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                testTag = "save_task_submit_button"
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
