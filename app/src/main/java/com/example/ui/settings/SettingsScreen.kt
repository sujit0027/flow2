package com.example.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.LockClock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.AppSettings
import com.example.ui.components.FlowCard
import com.example.ui.components.FlowConfirmationDialog
import com.example.ui.components.PrimaryFlowButton
import com.example.ui.components.SecondaryFlowButton
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.WarningAmber

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onUpdateSettings: (AppSettings) -> Unit,
    onOpenFocusCustomization: () -> Unit,
    onOpenWallpaperSettings: () -> Unit,
    onOpenAppBlocker: () -> Unit,
    onOpenSoundSettings: () -> Unit,
    onOpenAutoFlowSettings: () -> Unit,
    onExportBackupJson: () -> String,
    onExportCsv: () -> String,
    onRestoreBackupJson: (String) -> Boolean,
    onResetToDefaultRoutine: () -> Unit,
    onClearAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showResetRoutineDialog by remember { mutableStateOf(false) }
    var showClearAllDataDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showChecklistDialog by remember { mutableStateOf(false) }
    var exportedJsonText by remember { mutableStateOf("") }
    var importJsonText by remember { mutableStateOf("") }
    var importError by remember { mutableStateOf<String?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            Column {
                Text(
                    text = "Settings & Customization",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "App blocker, ringtones, task flow & themes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // Quick Customization Panels Entry Cards (Row 1: Focus Screen & Wallpapers)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FlowCard(
                    modifier = Modifier.weight(1f).clickable { onOpenFocusCustomization() },
                    borderColor = EmeraldPrimary.copy(alpha = 0.35f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = EmeraldLight)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Focus Screen", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = "Hide elements / Ultra Minimal", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, fontSize = 11.sp)
                    }
                }

                FlowCard(
                    modifier = Modifier.weight(1f).clickable { onOpenWallpaperSettings() },
                    borderColor = EmeraldPrimary.copy(alpha = 0.35f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = EmeraldLight)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Wallpapers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = "Nature, AMOLED & Scrims", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }

        // Quick Customization Panels Entry Cards (Row 2: App Blocker & Custom Ringtones)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FlowCard(
                    modifier = Modifier.weight(1f).clickable { onOpenAppBlocker() },
                    borderColor = EmeraldPrimary.copy(alpha = 0.45f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = EmeraldLight)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "App Blocker", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = "Shield & block distracting apps", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, fontSize = 11.sp)
                    }
                }

                FlowCard(
                    modifier = Modifier.weight(1f).clickable { onOpenSoundSettings() },
                    borderColor = EmeraldPrimary.copy(alpha = 0.45f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = EmeraldLight)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Ringtones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = "Custom ringtones & audio alerts", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }

        // Quick Customization Panels Entry Cards (Row 3: Auto-Flow & Next Task Alarms)
        item {
            FlowCard(
                modifier = Modifier.fillMaxWidth().clickable { onOpenAutoFlowSettings() },
                borderColor = EmeraldPrimary.copy(alpha = 0.35f)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(EmeraldContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Auto-Start & Next Task Alarms", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = "Automatic routine flow & alert notification on skipped breaks", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, fontSize = 11.sp)
                    }
                    Text(text = "Configure", color = EmeraldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Focus & Timer Rules
        item {
            Text(
                text = "TIMER & FOCUS ENGINE",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    SettingsSwitchRow(
                        title = "Compulsory Minimum Warning",
                        subtitle = "Alerts when attempting to leave a compulsory task before minimum target",
                        checked = settings.enforceCompulsoryWarning,
                        onCheckedChange = { onUpdateSettings(settings.copy(enforceCompulsoryWarning = it)) },
                        icon = Icons.Outlined.LockClock,
                        testTag = "setting_compulsory_warning_switch"
                    )

                    SettingsDivider()

                    SettingsSwitchRow(
                        title = "Vibrate on Minimum Target",
                        subtitle = "Tactile pulse when reaching compulsory milestone & entering Flow State",
                        checked = settings.vibrateOnComplete,
                        onCheckedChange = { onUpdateSettings(settings.copy(vibrateOnComplete = it)) },
                        icon = Icons.Default.Vibration,
                        testTag = "setting_vibrate_switch"
                    )

                    SettingsDivider()

                    SettingsSwitchRow(
                        title = "Keep Screen Awake",
                        subtitle = "Prevents display sleep during active focus timer session",
                        checked = settings.keepScreenOn,
                        onCheckedChange = { onUpdateSettings(settings.copy(keepScreenOn = it)) },
                        icon = Icons.Default.Visibility,
                        testTag = "setting_screen_on_switch"
                    )

                    SettingsDivider()

                    SettingsSwitchRow(
                        title = "Breaks Optional by Default",
                        subtitle = "Newly created break & meal tasks can be skipped",
                        checked = settings.breaksOptionalDefault,
                        onCheckedChange = { onUpdateSettings(settings.copy(breaksOptionalDefault = it)) },
                        icon = Icons.Default.Info,
                        testTag = "setting_breaks_optional_switch"
                    )
                }
            }
        }

        // Android System Settings Shortcuts & Samsung Battery Optimization Notice
        item {
            Text(
                text = "SYSTEM RESTRICTIONS & NOTIFICATIONS",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.BatteryAlert, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Background Execution on Samsung & Android",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = "To guarantee the active study timer stays live when your phone screen is turned off, disable aggressive battery optimization for FlowStudy in Android settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SecondaryFlowButton(
                            text = "App Notification Settings",
                            onClick = {
                                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f)
                        )

                        SecondaryFlowButton(
                            text = "Battery Optimization",
                            onClick = {
                                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Data Backup, Export & Restore
        item {
            Text(
                text = "LOCAL DATA BACKUP & EXPORT",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SecondaryFlowButton(
                            text = "Export Backup (JSON)",
                            onClick = {
                                exportedJsonText = onExportBackupJson()
                                showExportDialog = true
                            },
                            icon = Icons.Default.FileDownload,
                            modifier = Modifier.weight(1f)
                        )

                        SecondaryFlowButton(
                            text = "Restore Backup",
                            onClick = { showImportDialog = true },
                            icon = Icons.Default.CloudDownload,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    SecondaryFlowButton(
                        text = "View QA Manual Test Checklist",
                        onClick = { showChecklistDialog = true },
                        icon = Icons.Default.CheckCircleOutline,
                        modifier = Modifier.fillMaxWidth()
                    )

                    SecondaryFlowButton(
                        text = "Restore Prepopulated Student Routine",
                        onClick = { showResetRoutineDialog = true },
                        icon = Icons.Default.RestartAlt,
                        modifier = Modifier.fillMaxWidth()
                    )

                    SecondaryFlowButton(
                        text = "Delete All Data & Start Fresh",
                        onClick = { showClearAllDataDialog = true },
                        icon = Icons.Default.RestartAlt,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Backup Data (JSON)", color = TextPrimary) },
            text = {
                Column {
                    Text("Your complete routines, tasks, and session logs in portable JSON format:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exportedJsonText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("FlowStudy Backup", exportedJsonText))
                    showExportDialog = false
                }) {
                    Text("Copy to Clipboard", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) { Text("Close", color = TextSecondary) }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Restore Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Restore From JSON Backup", color = TextPrimary) },
            text = {
                Column {
                    Text("Paste previously exported FlowStudy JSON backup below:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = {
                            importJsonText = it
                            importError = null
                        },
                        placeholder = { Text("Paste JSON here...", color = TextTertiary) },
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        isError = importError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        )
                    )
                    if (importError != null) {
                        Text(text = importError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val success = onRestoreBackupJson(importJsonText)
                    if (success) {
                        showImportDialog = false
                    } else {
                        importError = "Invalid FlowStudy JSON format. Please verify content."
                    }
                }) {
                    Text("Restore Data", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) { Text("Cancel", color = TextSecondary) }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Manual QA Test Checklist Modal
    if (showChecklistDialog) {
        AlertDialog(
            onDismissRequest = { showChecklistDialog = false },
            title = { Text("Manual Test Checklist (Requirement 16)", color = TextPrimary) },
            text = {
                Column(modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                    val checklist = listOf(
                        "1. Screen Off: Start focus timer, lock screen, verify lockscreen notification keeps live elapsed time.",
                        "2. App Minimized: Minimize app, verify active Foreground Service notification with working Pause/Resume/Finish actions.",
                        "3. App Process Killed: Force stop/reopen, verify monotonic state is restored without lost active seconds.",
                        "4. Device Reboot: Monotonic clock reset detection reconciles session safely without phantom jumps.",
                        "5. Notification Permission Denied: App continues gracefully with in-app floating & PiP fallbacks.",
                        "6. Floating PiP: Enter PiP mode via button or home swipe, compact timer layout renders cleanly.",
                        "7. Samsung Battery Optimization: Warning card provided with direct shortcut to Android battery settings.",
                        "8. Custom Wallpaper: Photo picker selects local images; missing/deleted images gracefully fall back to AMOLED.",
                        "9. Offline Guarantee: 100% functional without internet, no remote telemetry or cloud dependencies."
                    )
                    checklist.forEach { item ->
                        Text(text = item, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showChecklistDialog = false }) { Text("Got It", color = EmeraldPrimary) }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Confirmation Dialogs
    if (showResetRoutineDialog) {
        FlowConfirmationDialog(
            title = "Restore Prepopulated Routine?",
            message = "This will reload the competitive student routine (Physics Theory, Questions, Chemistry, CET MCQs & Lunch). Your session history logs will not be touched.",
            confirmText = "Restore Routine",
            dismissText = "Cancel",
            onConfirm = {
                showResetRoutineDialog = false
                onResetToDefaultRoutine()
            },
            onDismiss = { showResetRoutineDialog = false }
        )
    }

    if (showClearAllDataDialog) {
        FlowConfirmationDialog(
            title = "Delete All Data?",
            message = "This will erase all routines, session logs, and reset preferences. This action cannot be undone.",
            confirmText = "Delete Everything",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                showClearAllDataDialog = false
                onClearAllData()
            },
            onDismiss = { showClearAllDataDialog = false }
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, fontSize = 12.sp)
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = EmeraldPrimary,
                checkedTrackColor = EmeraldContainer,
                uncheckedThumbColor = TextTertiary,
                uncheckedTrackColor = DarkBackground
            )
        )
    }
}

@Composable
private fun SettingsDivider() {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder))
}
