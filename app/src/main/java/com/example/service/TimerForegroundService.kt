package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.blocker.AppBlockConfig
import com.example.data.blocker.AppBlockManager
import com.example.data.preferences.UserPreferencesStore
import com.example.data.sound.SoundEventType
import com.example.data.sound.SoundRingtoneConfig
import com.example.data.sound.SoundRingtoneManager
import com.example.data.timer.TimerEngine
import com.example.ui.blocker.AppBlockShieldActivity
import com.example.ui.components.formatDurationHMS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var updateJob: Job? = null
    private lateinit var timerEngine: TimerEngine
    private lateinit var prefsStore: UserPreferencesStore

    private var currentBlockConfig = AppBlockConfig()
    private var currentSoundConfig = SoundRingtoneConfig()
    private var hasTriggeredMilestoneTone = false
    private var lastShieldLaunchTimestamp = 0L

    override fun onCreate() {
        super.onCreate()
        timerEngine = TimerEngine.getInstance(this)
        prefsStore = UserPreferencesStore(this)
        createNotificationChannels()

        serviceScope.launch {
            prefsStore.appBlockConfig.collect { config ->
                currentBlockConfig = config
            }
        }

        serviceScope.launch {
            prefsStore.soundConfig.collect { config ->
                currentSoundConfig = config
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE -> {
                timerEngine.pauseSession()
                updateNotification()
            }
            ACTION_RESUME -> {
                timerEngine.resumeSession()
                updateNotification()
            }
            ACTION_FINISH, ACTION_STOP -> {
                stopForegroundService()
                return START_NOT_STICKY
            }
            ACTION_START -> {
                hasTriggeredMilestoneTone = timerEngine.snapshot.value.isMinCompleted
                startForegroundWithNotification()
                startTicker()
            }
            else -> {
                startForegroundWithNotification()
                startTicker()
            }
        }
        return START_STICKY
    }

    private fun startForegroundWithNotification() {
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startTicker() {
        updateJob?.cancel()
        updateJob = serviceScope.launch {
            while (isActive) {
                val snapshot = timerEngine.snapshot.value
                if (snapshot.taskId == 0L) {
                    stopForegroundService()
                    return@launch
                }

                // Check Milestone Target Sound
                if (snapshot.isMinCompleted && !hasTriggeredMilestoneTone) {
                    hasTriggeredMilestoneTone = true
                    SoundRingtoneManager.triggerEventSound(
                        this@TimerForegroundService,
                        SoundEventType.TARGET_REACHED,
                        currentSoundConfig
                    )
                }

                // Check App Blocker Enforcement
                checkAndEnforceAppBlocker(snapshot)

                updateNotification()
                delay(1000L)
            }
        }
    }

    private fun checkAndEnforceAppBlocker(snapshot: com.example.data.timer.TimerSessionSnapshot) {
        if (!currentBlockConfig.isBlockerEnabled || !snapshot.isRunning) return
        // Only enforce while compulsory duration has not been completed or strict mode
        if (snapshot.isMinCompleted && !currentBlockConfig.strictModeNoExit) return

        if (!AppBlockManager.hasUsageStatsPermission(this)) return

        val foregroundPkg = AppBlockManager.getForegroundPackageName(this) ?: return
        if (foregroundPkg == packageName) return

        if (AppBlockManager.shouldBlockPackage(foregroundPkg, packageName, currentBlockConfig)) {
            val now = System.currentTimeMillis()
            // Throttle launches to avoid spamming activity intents
            if (now - lastShieldLaunchTimestamp > 2500L) {
                lastShieldLaunchTimestamp = now
                val appLabel = try {
                    val appInfo = packageManager.getApplicationInfo(foregroundPkg, 0)
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    foregroundPkg
                }
                AppBlockShieldActivity.launch(this, foregroundPkg, appLabel)
            }
        }
    }

    private fun updateNotification() {
        val snapshot = timerEngine.snapshot.value
        if (snapshot.taskId == 0L) {
            stopForegroundService()
            return
        }
        val notification = buildNotification()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(): Notification {
        val snapshot = timerEngine.snapshot.value
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_ACTIVE_TIMER", true)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val subtext = if (snapshot.subject.isNotBlank()) snapshot.subject else snapshot.category.displayName

        val contentText = if (snapshot.isMinCompleted) {
            "⚡ Flow State: ${formatDurationHMS(snapshot.currentElapsedSeconds)} (+${formatDurationHMS(snapshot.flowOvertimeSeconds)})"
        } else {
            "${formatDurationHMS(snapshot.currentElapsedSeconds)} elapsed • ${formatDurationHMS(snapshot.remainingCompulsorySeconds)} min left"
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(snapshot.taskTitle.ifBlank { "FlowStudy Session" })
            .setContentText(contentText)
            .setSubText(subtext)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(snapshot.isRunning)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // Notification Action: Play / Pause
        if (snapshot.isRunning) {
            val pauseIntent = Intent(this, TimerForegroundService::class.java).apply {
                action = ACTION_PAUSE
            }
            val pausePending = PendingIntent.getService(
                this,
                1,
                pauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_media_pause, "Pause", pausePending)
        } else {
            val resumeIntent = Intent(this, TimerForegroundService::class.java).apply {
                action = ACTION_RESUME
            }
            val resumePending = PendingIntent.getService(
                this,
                2,
                resumeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_media_play, "Resume", resumePending)
        }

        // Notification Action: Finish (only when min duration reached or optional)
        if (snapshot.isMinCompleted || !snapshot.isCompulsory) {
            val finishIntent = Intent(this, MainActivity::class.java).apply {
                action = "ACTION_OPEN_FINISH_PROMPT"
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val finishPending = PendingIntent.getActivity(
                this,
                3,
                finishIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.checkbox_on_background, "Finish", finishPending)
        }

        return builder.build()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timerChannel = NotificationChannel(
                CHANNEL_ID,
                "Active Focus Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time focus study timer in notification drawer and lockscreen."
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val alarmChannel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "Next Task Alarms & Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority alarms reminding you to start the next task when idle or skipping breaks."
                setShowBadge(true)
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(timerChannel)
            manager?.createNotificationChannel(alarmChannel)
        }
    }

    private fun stopForegroundService() {
        updateJob?.cancel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        updateJob?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "flow_study_timer_channel"
        const val ALARM_CHANNEL_ID = "flow_study_alarm_channel"
        const val NOTIFICATION_ID = 1001
        const val ALARM_NOTIFICATION_ID = 1002

        const val ACTION_START = "com.example.service.ACTION_START"
        const val ACTION_PAUSE = "com.example.service.ACTION_PAUSE"
        const val ACTION_RESUME = "com.example.service.ACTION_RESUME"
        const val ACTION_FINISH = "com.example.service.ACTION_FINISH"
        const val ACTION_STOP = "com.example.service.ACTION_STOP"

        fun start(context: Context) {
            val intent = Intent(context, TimerForegroundService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, TimerForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun showNextTaskAlarmNotification(
            context: Context,
            taskTitle: String,
            reason: String
        ) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager != null) {
                if (manager.getNotificationChannel(ALARM_CHANNEL_ID) == null) {
                    val alarmChannel = NotificationChannel(
                        ALARM_CHANNEL_ID,
                        "Next Task Alarms & Reminders",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "High priority alarms reminding you to start the next task when idle or skipping breaks."
                        setShowBadge(true)
                        enableVibration(true)
                        lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    }
                    manager.createNotificationChannel(alarmChannel)
                }
            }

            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("START_NEXT_PENDING_TASK", true)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                101,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("🔔 Time for Next Task: $taskTitle")
                .setContentText(reason)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            manager?.notify(ALARM_NOTIFICATION_ID, builder.build())
        }
    }
}
