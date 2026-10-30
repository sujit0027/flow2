package com.example.ui.settings

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.sound.SoundOption
import com.example.data.sound.SoundRingtoneConfig
import com.example.data.sound.SoundRingtoneManager
import com.example.ui.components.FlowCard
import com.example.ui.components.PrimaryFlowButton
import com.example.ui.components.SecondaryFlowButton
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundRingtoneBottomSheet(
    currentConfig: SoundRingtoneConfig,
    onSaveConfig: (SoundRingtoneConfig) -> Unit,
    onPlayPreview: (soundId: String, uriString: String?) -> Unit,
    onStopPreview: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var config by remember { mutableStateOf(currentConfig) }
    var activePreviewingSoundId by remember { mutableStateOf<String?>(null) }

    // Picker target: "TARGET", "NEXT_TASK", "BREAK"
    var ringtonePickerTarget by remember { mutableStateOf<String?>(null) }

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }

            if (uri != null) {
                val ringtoneTitle = try {
                    RingtoneManager.getRingtone(context, uri).getTitle(context) ?: "Custom Tone"
                } catch (e: Exception) {
                    "Custom Device Ringtone"
                }

                when (ringtonePickerTarget) {
                    "TARGET" -> {
                        config = config.copy(
                            targetSoundId = "custom_uri",
                            targetSoundUri = uri.toString(),
                            targetSoundName = ringtoneTitle
                        )
                    }
                    "NEXT_TASK" -> {
                        config = config.copy(
                            nextTaskSoundId = "custom_uri",
                            nextTaskSoundUri = uri.toString(),
                            nextTaskSoundName = ringtoneTitle
                        )
                    }
                    "BREAK" -> {
                        config = config.copy(
                            breakSoundId = "custom_uri",
                            breakSoundUri = uri.toString(),
                            breakSoundName = ringtoneTitle
                        )
                    }
                }
            }
        }
    }

    fun launchSystemRingtonePicker(target: String, currentUriStr: String) {
        ringtonePickerTarget = target
        val existingUri = if (currentUriStr.isNotBlank()) Uri.parse(currentUriStr) else null
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Focus Sound / Alarm")
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            if (existingUri != null) {
                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, existingUri)
            }
        }
        ringtonePickerLauncher.launch(intent)
    }

    ModalBottomSheet(
        onDismissRequest = {
            onStopPreview()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = DarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(EmeraldContainer)
                            .border(1.dp, EmeraldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Audiotrack,
                            contentDescription = null,
                            tint = EmeraldLight,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Ringtones & Focus Chimes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Custom sounds for milestones, alarms & breaks",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = {
                        onStopPreview()
                        onDismiss()
                    },
                    modifier = Modifier.testTag("sound_settings_close_button")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Master Toggle Row
            FlowCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Focus Audio Alerts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Play chimes on reaching minimum target & next task alarms",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = config.soundEnabled,
                        onCheckedChange = { config = config.copy(soundEnabled = it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EmeraldLight,
                            checkedTrackColor = EmeraldPrimary,
                            uncheckedThumbColor = TextTertiary,
                            uncheckedTrackColor = DarkSurfaceVariant
                        ),
                        modifier = Modifier.testTag("sound_master_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (config.soundEnabled) {
                // Section 1: Minimum Target Milestone / Flow State Tone
                SoundEventCard(
                    title = "Minimum Milestone / Flow State",
                    subtitle = "Plays when compulsory minimum study duration is completed",
                    currentSoundName = config.targetSoundName,
                    currentSoundId = config.targetSoundId,
                    currentUri = config.targetSoundUri,
                    isPlaying = activePreviewingSoundId == "TARGET",
                    onSelectSound = { sound ->
                        config = config.copy(
                            targetSoundId = sound.id,
                            targetSoundUri = "",
                            targetSoundName = sound.name
                        )
                    },
                    onPickCustom = {
                        launchSystemRingtonePicker("TARGET", config.targetSoundUri)
                    },
                    onTogglePreview = {
                        if (activePreviewingSoundId == "TARGET") {
                            onStopPreview()
                            activePreviewingSoundId = null
                        } else {
                            activePreviewingSoundId = "TARGET"
                            onPlayPreview(config.targetSoundId, config.targetSoundUri)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Section 2: Next Task Alarm & Reminder Tone
                SoundEventCard(
                    title = "Next Task Alarm & Alert",
                    subtitle = "Alerts when you skip an optional task or need to start next study task",
                    currentSoundName = config.nextTaskSoundName,
                    currentSoundId = config.nextTaskSoundId,
                    currentUri = config.nextTaskSoundUri,
                    isPlaying = activePreviewingSoundId == "NEXT_TASK",
                    onSelectSound = { sound ->
                        config = config.copy(
                            nextTaskSoundId = sound.id,
                            nextTaskSoundUri = "",
                            nextTaskSoundName = sound.name
                        )
                    },
                    onPickCustom = {
                        launchSystemRingtonePicker("NEXT_TASK", config.nextTaskSoundUri)
                    },
                    onTogglePreview = {
                        if (activePreviewingSoundId == "NEXT_TASK") {
                            onStopPreview()
                            activePreviewingSoundId = null
                        } else {
                            activePreviewingSoundId = "NEXT_TASK"
                            onPlayPreview(config.nextTaskSoundId, config.nextTaskSoundUri)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Section 3: Break Over Tone
                SoundEventCard(
                    title = "Break / Meal Finished",
                    subtitle = "Plays when scheduled rest duration concludes",
                    currentSoundName = config.breakSoundName,
                    currentSoundId = config.breakSoundId,
                    currentUri = config.breakSoundUri,
                    isPlaying = activePreviewingSoundId == "BREAK",
                    onSelectSound = { sound ->
                        config = config.copy(
                            breakSoundId = sound.id,
                            breakSoundUri = "",
                            breakSoundName = sound.name
                        )
                    },
                    onPickCustom = {
                        launchSystemRingtonePicker("BREAK", config.breakSoundUri)
                    },
                    onTogglePreview = {
                        if (activePreviewingSoundId == "BREAK") {
                            onStopPreview()
                            activePreviewingSoundId = null
                        } else {
                            activePreviewingSoundId = "BREAK"
                            onPlayPreview(config.breakSoundId, config.breakSoundUri)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SecondaryFlowButton(
                    text = "Cancel",
                    onClick = {
                        onStopPreview()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                )

                PrimaryFlowButton(
                    text = "Save Ringtone Settings",
                    onClick = {
                        onStopPreview()
                        onSaveConfig(config)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f).testTag("save_sound_settings_button"),
                    icon = Icons.Default.Check
                )
            }
        }
    }
}

@Composable
fun SoundEventCard(
    title: String,
    subtitle: String,
    currentSoundName: String,
    currentSoundId: String,
    currentUri: String,
    isPlaying: Boolean,
    onSelectSound: (SoundOption) -> Unit,
    onPickCustom: () -> Unit,
    onTogglePreview: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    FlowCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                // Audio Preview Button
                IconButton(
                    onClick = onTogglePreview,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) EmeraldPrimary else DarkSurfaceVariant)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = "Preview Tone",
                        tint = if (isPlaying) DarkSurface else EmeraldLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Current Tone selector button
            Box {
                Surface(
                    color = DarkSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { menuExpanded = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (currentSoundId == "custom_uri") Icons.Default.Audiotrack else Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = currentSoundName,
                                color = TextPrimary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = "Change",
                            color = EmeraldLight,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(DarkSurface)
                ) {
                    SoundRingtoneManager.BUILT_IN_SOUNDS.forEach { sound ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(text = sound.name, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                    Text(text = sound.description, color = TextTertiary, fontSize = 11.sp)
                                }
                            },
                            onClick = {
                                menuExpanded = false
                                onSelectSound(sound)
                            }
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder))

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Audiotrack, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Choose Custom Phone Ringtone / Audio...",
                                    color = EmeraldLight,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        onClick = {
                            menuExpanded = false
                            onPickCustom()
                        }
                    )
                }
            }
        }
    }
}
