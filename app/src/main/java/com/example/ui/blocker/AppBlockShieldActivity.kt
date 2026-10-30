package com.example.ui.blocker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity
import com.example.data.timer.TimerEngine
import com.example.service.TimerForegroundService
import com.example.ui.components.FlowCard
import com.example.ui.components.PrimaryFlowButton
import com.example.ui.components.SecondaryFlowButton
import com.example.ui.components.formatDurationHMS
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FlowStudyTheme
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.WarningAmber

class AppBlockShieldActivity : ComponentActivity() {

    private lateinit var timerEngine: TimerEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        timerEngine = TimerEngine.getInstance(this)

        val blockedPkg = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE) ?: ""
        val blockedName = intent.getStringExtra(EXTRA_BLOCKED_NAME) ?: "Distracting App"

        // Tactile vibration alert when app is blocked
        triggerBlockVibration()

        setContent {
            FlowStudyTheme {
                AppBlockShieldScreen(
                    blockedPackage = blockedPkg,
                    blockedAppName = blockedName,
                    timerEngine = timerEngine,
                    onReturnToFocus = {
                        val returnIntent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            putExtra("OPEN_ACTIVE_TIMER", true)
                        }
                        startActivity(returnIntent)
                        finish()
                    },
                    onEmergencyStop = {
                        timerEngine.cancelSession()
                        TimerForegroundService.stop(this)
                        val returnIntent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        startActivity(returnIntent)
                        finish()
                    }
                )
            }
        }
    }

    private fun triggerBlockVibration() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 150, 100, 200), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(300L)
            }
        } catch (ignored: Exception) {}
    }

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "extra_blocked_package"
        const val EXTRA_BLOCKED_NAME = "extra_blocked_name"

        fun launch(context: Context, packageName: String, appName: String) {
            val intent = Intent(context, AppBlockShieldActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(EXTRA_BLOCKED_PACKAGE, packageName)
                putExtra(EXTRA_BLOCKED_NAME, appName)
            }
            context.startActivity(intent)
        }
    }
}

@Composable
fun AppBlockShieldScreen(
    blockedPackage: String,
    blockedAppName: String,
    timerEngine: TimerEngine,
    onReturnToFocus: () -> Unit,
    onEmergencyStop: () -> Unit
) {
    val snapshot by timerEngine.snapshot.collectAsState()
    var showEmergencyDialog by remember { mutableStateOf(false) }

    // Intercept back button to return user to FlowStudy instead of allowing the blocked app
    BackHandler {
        onReturnToFocus()
    }

    val pulseTransition = rememberInfiniteTransition(label = "shield_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF090D0B),
                        DarkBackground,
                        Color(0xFF0F1713)
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Shield Icon with pulsing aura
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(110.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(EmeraldContainer.copy(alpha = 0.4f))
                    .border(2.dp, EmeraldPrimary, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Shield Active",
                    tint = EmeraldLight,
                    modifier = Modifier.size(56.dp)
                )
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = DarkBackground,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "FlowStudy Focus Shield",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = EmeraldLight,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Access Blocked",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = DarkSurfaceVariant,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Text(
                    text = blockedAppName.ifBlank { "Distracting App" },
                    color = WarningAmber,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "You made a commitment to finish this task before getting distracted. Protect your flow state!",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Task Progress Summary Card
            FlowCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = EmeraldPrimary.copy(alpha = 0.4f)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ACTIVE COMPULSORY TASK",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = snapshot.taskTitle.ifBlank { "Study Session" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    if (snapshot.subject.isNotBlank()) {
                        Text(
                            text = snapshot.subject,
                            style = MaterialTheme.typography.bodyMedium,
                            color = EmeraldLight
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (snapshot.isMinCompleted) {
                                "Minimum Target Complete! Finishing optional."
                            } else {
                                "${formatDurationHMS(snapshot.remainingCompulsorySeconds)} minimum remaining"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            color = if (snapshot.isMinCompleted) EmeraldLight else TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Primary Return Action
            PrimaryFlowButton(
                text = "Return to Focus Study",
                onClick = onReturnToFocus,
                modifier = Modifier.fillMaxWidth().testTag("shield_return_button"),
                icon = Icons.Default.ArrowBack
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Emergency Exit Action
            TextButton(
                onClick = { showEmergencyDialog = true },
                modifier = Modifier.testTag("shield_emergency_exit_button")
            ) {
                Text(
                    text = "Emergency Stop (Record as Interrupted)",
                    color = TextTertiary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    if (showEmergencyDialog) {
        AlertDialog(
            onDismissRequest = { showEmergencyDialog = false },
            containerColor = DarkSurface,
            icon = {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = WarningAmber)
            },
            title = {
                Text("Emergency Stop Task?", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "You have not fulfilled the compulsory minimum study duration. If you exit now, this session will be logged as interrupted and incomplete.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEmergencyDialog = false
                        onEmergencyStop()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text("Exit & Record Interrupted", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmergencyDialog = false }) {
                    Text("Keep Studying", color = EmeraldPrimary)
                }
            }
        )
    }
}
