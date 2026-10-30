package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.FocusScreenCustomization
import com.example.ui.components.FlowCard
import com.example.ui.components.PrimaryFlowButton
import com.example.ui.components.SecondaryFlowButton
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusCustomizationBottomSheet(
    currentConfig: FocusScreenCustomization,
    onSaveConfig: (FocusScreenCustomization) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var config by remember { mutableStateOf(currentConfig) }

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
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Focus Screen Customization",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Customize elements & eliminate distraction",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live Miniature Preview
            Text(
                text = "LIVE PREVIEW",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = EmeraldPrimary.copy(alpha = 0.4f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (config.showSessionLabel && !config.isUltraMinimal) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldContainer,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = "STUDY • COMPULSORY",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldLight,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 9.sp
                            )
                        }
                    }

                    if (config.showTaskTitle) {
                        Text(
                            text = "Physics: Wave Optics Theory",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (config.showTimer) {
                        Text(
                            text = "32:45",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldLight
                        )
                    }

                    if (config.showMinRemaining && !config.isUltraMinimal) {
                        Text(
                            text = "12:15 remaining to minimum",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    if (config.showMotivationalQuote && !config.isUltraMinimal) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "\"Focus on the process, not the outcome.\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Preset: Ultra Minimal Mode
            FlowCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (config.isUltraMinimal) EmeraldPrimary else DarkBorder
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ultra Minimal Mode",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (config.isUltraMinimal) EmeraldLight else TextPrimary
                        )
                        Text(
                            text = "Hides quotes, previews, badges & secondary clutter",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = config.isUltraMinimal,
                        onCheckedChange = { config = config.copy(isUltraMinimal = it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EmeraldPrimary,
                            checkedTrackColor = EmeraldContainer,
                            uncheckedThumbColor = TextTertiary,
                            uncheckedTrackColor = DarkBackground
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Individual Element Toggles
            Text(
                text = "ELEMENT VISIBILITY",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                    CustomizationToggleRow(
                        title = "Task Title",
                        checked = config.showTaskTitle,
                        onCheckedChange = { config = config.copy(showTaskTitle = it) }
                    )
                    CustomizationToggleRow(
                        title = "Large Timer Clock",
                        checked = config.showTimer,
                        onCheckedChange = { config = config.copy(showTimer = it) }
                    )
                    CustomizationToggleRow(
                        title = "Minimum Remaining Time",
                        checked = config.showMinRemaining,
                        onCheckedChange = { config = config.copy(showMinRemaining = it) }
                    )
                    CustomizationToggleRow(
                        title = "Circular Progress Ring",
                        checked = config.showProgressRing,
                        onCheckedChange = { config = config.copy(showProgressRing = it) }
                    )
                    CustomizationToggleRow(
                        title = "Next Task Preview",
                        checked = config.showNextTaskPreview,
                        onCheckedChange = { config = config.copy(showNextTaskPreview = it) }
                    )
                    CustomizationToggleRow(
                        title = "Motivational Mindset Quote",
                        checked = config.showMotivationalQuote,
                        onCheckedChange = { config = config.copy(showMotivationalQuote = it) }
                    )
                    CustomizationToggleRow(
                        title = "Session Label & Category Chip",
                        checked = config.showSessionLabel,
                        onCheckedChange = { config = config.copy(showSessionLabel = it) }
                    )
                    CustomizationToggleRow(
                        title = "Secondary Statistics",
                        checked = config.showExtraStats,
                        onCheckedChange = { config = config.copy(showExtraStats = it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            PrimaryFlowButton(
                text = "Apply Customization",
                onClick = {
                    onSaveConfig(config)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CustomizationToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = EmeraldPrimary,
                checkedTrackColor = EmeraldContainer,
                uncheckedThumbColor = TextTertiary,
                uncheckedTrackColor = DarkBackground
            )
        )
    }
}
