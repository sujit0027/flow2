package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.blocker.AppBlockConfig
import com.example.data.blocker.DEFAULT_DISTRACTING_PACKAGES
import com.example.data.sound.SoundRingtoneConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "flowstudy_user_prefs")

data class FocusScreenCustomization(
    val showTaskTitle: Boolean = true,
    val showTimer: Boolean = true,
    val showElapsedTime: Boolean = true,
    val showMinRemaining: Boolean = true,
    val showProgressRing: Boolean = true,
    val showSessionLabel: Boolean = true,
    val showNextTaskPreview: Boolean = true,
    val showMotivationalQuote: Boolean = false,
    val showTimerControls: Boolean = true,
    val showExtraStats: Boolean = true,
    val isUltraMinimal: Boolean = false
)

data class WallpaperConfig(
    val homeWallpaperType: String = "AMOLED", // AMOLED, FOREST, MOUNTAIN, SKY, COUNTRYSIDE, GRADIENT, CUSTOM
    val focusWallpaperType: String = "AMOLED",
    val customWallpaperUri: String = "",
    val dimLevel: Float = 0.35f,
    val useBlur: Boolean = false
)

data class AppThemeConfig(
    val themeMode: String = "DARK", // DARK, LIGHT, SYSTEM, AMOLED
    val accentColor: String = "EMERALD", // EMERALD, MINT, TEAL, SKY, AMBER, ROSE
    val timerStyle: String = "CIRCULAR", // CIRCULAR, DIGITAL, STOPWATCH, TEXT_ONLY
    val dailyStudyTargetHours: Int = 4,
    val isOnboardingCompleted: Boolean = false
)

data class AutoFlowConfig(
    val autoAdvanceCompulsoryOnly: Boolean = true,
    val countdownDurationSeconds: Int = 30,
    val ringAlarmOnCountdownExpiry: Boolean = true,
    val autoSkipOptionalBreaksIfIdle: Boolean = true,
    val alertOnSkippedTask: Boolean = true,
    val notifyNextTaskMinutes: Int = 1,
    val autoStartNextTask: Boolean = false,
    val wakeAlarmDurationSeconds: Int = 30,
    val transitionAlarmDurationSeconds: Int = 20,
    val wakeAlarmMode: String = "ALARM", // ALARM, VIBRATION_ONLY, OVERTIME_AUTO, AUTO_NEXT_COMPULSORY
    val enableWakeAlarm: Boolean = true,
    val celebrationWallpaperUri: String = "",
    val celebrationMessage: String = "Sensational effort! You crushed all compulsory study tasks today.",
    val celebrationShowQuote: Boolean = true
)

class UserPreferencesStore(private val context: Context) {

    private object Keys {
        // Focus Customization
        val SHOW_TASK_TITLE = booleanPreferencesKey("focus_show_task_title")
        val SHOW_TIMER = booleanPreferencesKey("focus_show_timer")
        val SHOW_ELAPSED_TIME = booleanPreferencesKey("focus_show_elapsed_time")
        val SHOW_MIN_REMAINING = booleanPreferencesKey("focus_show_min_remaining")
        val SHOW_PROGRESS_RING = booleanPreferencesKey("focus_show_progress_ring")
        val SHOW_SESSION_LABEL = booleanPreferencesKey("focus_show_session_label")
        val SHOW_NEXT_TASK_PREVIEW = booleanPreferencesKey("focus_show_next_task_preview")
        val SHOW_MOTIVATIONAL_QUOTE = booleanPreferencesKey("focus_show_motivational_quote")
        val SHOW_TIMER_CONTROLS = booleanPreferencesKey("focus_show_timer_controls")
        val SHOW_EXTRA_STATS = booleanPreferencesKey("focus_show_extra_stats")
        val IS_ULTRA_MINIMAL = booleanPreferencesKey("focus_is_ultra_minimal")

        // Wallpaper Config
        val HOME_WALLPAPER_TYPE = stringPreferencesKey("wp_home_type")
        val FOCUS_WALLPAPER_TYPE = stringPreferencesKey("wp_focus_type")
        val CUSTOM_WALLPAPER_URI = stringPreferencesKey("wp_custom_uri")
        val WALLPAPER_DIM_LEVEL = floatPreferencesKey("wp_dim_level")
        val USE_BLUR = booleanPreferencesKey("wp_use_blur")

        // Theme Config
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val TIMER_STYLE = stringPreferencesKey("timer_style")
        val DAILY_STUDY_TARGET_HOURS = intPreferencesKey("daily_study_target_hours")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

        // App Blocker Config
        val BLOCKER_ENABLED = booleanPreferencesKey("blocker_enabled")
        val BLOCK_ALL_DISTRACTIONS = booleanPreferencesKey("blocker_all_distractions")
        val BLOCKED_PACKAGES = stringSetPreferencesKey("blocker_packages")
        val STRICT_MODE_NO_EXIT = booleanPreferencesKey("blocker_strict_mode")
        val ALLOW_YOUTUBE_FOR_LECTURES = booleanPreferencesKey("allow_youtube_for_lectures")

        // Sound & Ringtone Config
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val TARGET_SOUND_ID = stringPreferencesKey("target_sound_id")
        val TARGET_SOUND_URI = stringPreferencesKey("target_sound_uri")
        val TARGET_SOUND_NAME = stringPreferencesKey("target_sound_name")
        val NEXT_TASK_SOUND_ID = stringPreferencesKey("next_task_sound_id")
        val NEXT_TASK_SOUND_URI = stringPreferencesKey("next_task_sound_uri")
        val NEXT_TASK_SOUND_NAME = stringPreferencesKey("next_task_sound_name")
        val BREAK_SOUND_ID = stringPreferencesKey("break_sound_id")
        val BREAK_SOUND_URI = stringPreferencesKey("break_sound_uri")
        val BREAK_SOUND_NAME = stringPreferencesKey("break_sound_name")

        // Auto Flow Config
        val AUTO_START_NEXT_TASK = booleanPreferencesKey("auto_start_next_task")
        val AUTO_ADVANCE_COMPULSORY_ONLY = booleanPreferencesKey("auto_advance_compulsory_only")
        val COUNTDOWN_DURATION_SECONDS = intPreferencesKey("countdown_duration_seconds")
        val RING_ALARM_ON_EXPIRY = booleanPreferencesKey("ring_alarm_on_expiry")
        val AUTO_SKIP_OPTIONAL_IF_IDLE = booleanPreferencesKey("auto_skip_optional_if_idle")
        val ALERT_ON_SKIPPED_TASK = booleanPreferencesKey("alert_on_skipped_task")
        val NOTIFY_NEXT_TASK_MINUTES = intPreferencesKey("notify_next_task_minutes")
        val WAKE_ALARM_DURATION_SECONDS = intPreferencesKey("wake_alarm_duration_seconds")
        val TRANSITION_ALARM_DURATION_SECONDS = intPreferencesKey("transition_alarm_duration_seconds")
        val WAKE_ALARM_MODE = stringPreferencesKey("wake_alarm_mode")
        val ENABLE_WAKE_ALARM = booleanPreferencesKey("enable_wake_alarm")
        val CELEBRATION_WALLPAPER_URI = stringPreferencesKey("celebration_wallpaper_uri")
        val CELEBRATION_MESSAGE = stringPreferencesKey("celebration_message")
        val CELEBRATION_SHOW_QUOTE = booleanPreferencesKey("celebration_show_quote")
    }

    val focusCustomization: Flow<FocusScreenCustomization> = context.dataStore.data.map { prefs ->
        FocusScreenCustomization(
            showTaskTitle = prefs[Keys.SHOW_TASK_TITLE] ?: true,
            showTimer = prefs[Keys.SHOW_TIMER] ?: true,
            showElapsedTime = prefs[Keys.SHOW_ELAPSED_TIME] ?: true,
            showMinRemaining = prefs[Keys.SHOW_MIN_REMAINING] ?: true,
            showProgressRing = prefs[Keys.SHOW_PROGRESS_RING] ?: true,
            showSessionLabel = prefs[Keys.SHOW_SESSION_LABEL] ?: true,
            showNextTaskPreview = prefs[Keys.SHOW_NEXT_TASK_PREVIEW] ?: true,
            showMotivationalQuote = prefs[Keys.SHOW_MOTIVATIONAL_QUOTE] ?: false,
            showTimerControls = prefs[Keys.SHOW_TIMER_CONTROLS] ?: true,
            showExtraStats = prefs[Keys.SHOW_EXTRA_STATS] ?: true,
            isUltraMinimal = prefs[Keys.IS_ULTRA_MINIMAL] ?: false
        )
    }

    val wallpaperConfig: Flow<WallpaperConfig> = context.dataStore.data.map { prefs ->
        WallpaperConfig(
            homeWallpaperType = prefs[Keys.HOME_WALLPAPER_TYPE] ?: "AMOLED",
            focusWallpaperType = prefs[Keys.FOCUS_WALLPAPER_TYPE] ?: "AMOLED",
            customWallpaperUri = prefs[Keys.CUSTOM_WALLPAPER_URI] ?: "",
            dimLevel = prefs[Keys.WALLPAPER_DIM_LEVEL] ?: 0.35f,
            useBlur = prefs[Keys.USE_BLUR] ?: false
        )
    }

    val themeConfig: Flow<AppThemeConfig> = context.dataStore.data.map { prefs ->
        AppThemeConfig(
            themeMode = prefs[Keys.THEME_MODE] ?: "DARK",
            accentColor = prefs[Keys.ACCENT_COLOR] ?: "EMERALD",
            timerStyle = prefs[Keys.TIMER_STYLE] ?: "CIRCULAR",
            dailyStudyTargetHours = prefs[Keys.DAILY_STUDY_TARGET_HOURS] ?: 4,
            isOnboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false
        )
    }

    val appBlockConfig: Flow<AppBlockConfig> = context.dataStore.data.map { prefs ->
        AppBlockConfig(
            isBlockerEnabled = prefs[Keys.BLOCKER_ENABLED] ?: false,
            blockAllDistractions = prefs[Keys.BLOCK_ALL_DISTRACTIONS] ?: false,
            blockedPackages = prefs[Keys.BLOCKED_PACKAGES] ?: emptySet(),
            strictModeNoExit = prefs[Keys.STRICT_MODE_NO_EXIT] ?: false,
            allowYouTubeForLectures = prefs[Keys.ALLOW_YOUTUBE_FOR_LECTURES] ?: true
        )
    }

    val soundConfig: Flow<SoundRingtoneConfig> = context.dataStore.data.map { prefs ->
        SoundRingtoneConfig(
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            targetSoundId = prefs[Keys.TARGET_SOUND_ID] ?: "zen_gong",
            targetSoundUri = prefs[Keys.TARGET_SOUND_URI] ?: "",
            targetSoundName = prefs[Keys.TARGET_SOUND_NAME] ?: "Zen Gong (Serene)",
            nextTaskSoundId = prefs[Keys.NEXT_TASK_SOUND_ID] ?: "digital_alarm",
            nextTaskSoundUri = prefs[Keys.NEXT_TASK_SOUND_URI] ?: "",
            nextTaskSoundName = prefs[Keys.NEXT_TASK_SOUND_NAME] ?: "Digital Beep (Alert)",
            breakSoundId = prefs[Keys.BREAK_SOUND_ID] ?: "temple_bell",
            breakSoundUri = prefs[Keys.BREAK_SOUND_URI] ?: "",
            breakSoundName = prefs[Keys.BREAK_SOUND_NAME] ?: "Temple Bell (Mindful)"
        )
    }

    val autoFlowConfig: Flow<AutoFlowConfig> = context.dataStore.data.map { prefs ->
        AutoFlowConfig(
            autoAdvanceCompulsoryOnly = prefs[Keys.AUTO_ADVANCE_COMPULSORY_ONLY] ?: true,
            countdownDurationSeconds = prefs[Keys.COUNTDOWN_DURATION_SECONDS] ?: 30,
            ringAlarmOnCountdownExpiry = prefs[Keys.RING_ALARM_ON_EXPIRY] ?: true,
            autoSkipOptionalBreaksIfIdle = prefs[Keys.AUTO_SKIP_OPTIONAL_IF_IDLE] ?: true,
            alertOnSkippedTask = prefs[Keys.ALERT_ON_SKIPPED_TASK] ?: true,
            notifyNextTaskMinutes = prefs[Keys.NOTIFY_NEXT_TASK_MINUTES] ?: 1,
            autoStartNextTask = prefs[Keys.AUTO_START_NEXT_TASK] ?: false,
            wakeAlarmDurationSeconds = prefs[Keys.WAKE_ALARM_DURATION_SECONDS] ?: 30,
            transitionAlarmDurationSeconds = prefs[Keys.TRANSITION_ALARM_DURATION_SECONDS] ?: 20,
            wakeAlarmMode = prefs[Keys.WAKE_ALARM_MODE] ?: "ALARM",
            enableWakeAlarm = prefs[Keys.ENABLE_WAKE_ALARM] ?: true,
            celebrationWallpaperUri = prefs[Keys.CELEBRATION_WALLPAPER_URI] ?: "",
            celebrationMessage = prefs[Keys.CELEBRATION_MESSAGE] ?: "Sensational effort! You crushed all compulsory study tasks today.",
            celebrationShowQuote = prefs[Keys.CELEBRATION_SHOW_QUOTE] ?: true
        )
    }

    suspend fun updateFocusCustomization(config: FocusScreenCustomization) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SHOW_TASK_TITLE] = config.showTaskTitle
            prefs[Keys.SHOW_TIMER] = config.showTimer
            prefs[Keys.SHOW_ELAPSED_TIME] = config.showElapsedTime
            prefs[Keys.SHOW_MIN_REMAINING] = config.showMinRemaining
            prefs[Keys.SHOW_PROGRESS_RING] = config.showProgressRing
            prefs[Keys.SHOW_SESSION_LABEL] = config.showSessionLabel
            prefs[Keys.SHOW_NEXT_TASK_PREVIEW] = config.showNextTaskPreview
            prefs[Keys.SHOW_MOTIVATIONAL_QUOTE] = config.showMotivationalQuote
            prefs[Keys.SHOW_TIMER_CONTROLS] = config.showTimerControls
            prefs[Keys.SHOW_EXTRA_STATS] = config.showExtraStats
            prefs[Keys.IS_ULTRA_MINIMAL] = config.isUltraMinimal
        }
    }

    suspend fun updateWallpaperConfig(config: WallpaperConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HOME_WALLPAPER_TYPE] = config.homeWallpaperType
            prefs[Keys.FOCUS_WALLPAPER_TYPE] = config.focusWallpaperType
            prefs[Keys.CUSTOM_WALLPAPER_URI] = config.customWallpaperUri
            prefs[Keys.WALLPAPER_DIM_LEVEL] = config.dimLevel
            prefs[Keys.USE_BLUR] = config.useBlur
        }
    }

    suspend fun updateThemeConfig(config: AppThemeConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = config.themeMode
            prefs[Keys.ACCENT_COLOR] = config.accentColor
            prefs[Keys.TIMER_STYLE] = config.timerStyle
            prefs[Keys.DAILY_STUDY_TARGET_HOURS] = config.dailyStudyTargetHours
            prefs[Keys.ONBOARDING_COMPLETED] = config.isOnboardingCompleted
        }
    }

    suspend fun updateAppBlockConfig(config: AppBlockConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BLOCKER_ENABLED] = config.isBlockerEnabled
            prefs[Keys.BLOCK_ALL_DISTRACTIONS] = config.blockAllDistractions
            prefs[Keys.BLOCKED_PACKAGES] = config.blockedPackages
            prefs[Keys.STRICT_MODE_NO_EXIT] = config.strictModeNoExit
            prefs[Keys.ALLOW_YOUTUBE_FOR_LECTURES] = config.allowYouTubeForLectures
        }
    }

    suspend fun updateSoundConfig(config: SoundRingtoneConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SOUND_ENABLED] = config.soundEnabled
            prefs[Keys.TARGET_SOUND_ID] = config.targetSoundId
            prefs[Keys.TARGET_SOUND_URI] = config.targetSoundUri
            prefs[Keys.TARGET_SOUND_NAME] = config.targetSoundName
            prefs[Keys.NEXT_TASK_SOUND_ID] = config.nextTaskSoundId
            prefs[Keys.NEXT_TASK_SOUND_URI] = config.nextTaskSoundUri
            prefs[Keys.NEXT_TASK_SOUND_NAME] = config.nextTaskSoundName
            prefs[Keys.BREAK_SOUND_ID] = config.breakSoundId
            prefs[Keys.BREAK_SOUND_URI] = config.breakSoundUri
            prefs[Keys.BREAK_SOUND_NAME] = config.breakSoundName
        }
    }

    suspend fun updateAutoFlowConfig(config: AutoFlowConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AUTO_ADVANCE_COMPULSORY_ONLY] = config.autoAdvanceCompulsoryOnly
            prefs[Keys.COUNTDOWN_DURATION_SECONDS] = config.countdownDurationSeconds
            prefs[Keys.RING_ALARM_ON_EXPIRY] = config.ringAlarmOnCountdownExpiry
            prefs[Keys.AUTO_SKIP_OPTIONAL_IF_IDLE] = config.autoSkipOptionalBreaksIfIdle
            prefs[Keys.AUTO_START_NEXT_TASK] = config.autoStartNextTask
            prefs[Keys.ALERT_ON_SKIPPED_TASK] = config.alertOnSkippedTask
            prefs[Keys.NOTIFY_NEXT_TASK_MINUTES] = config.notifyNextTaskMinutes
            prefs[Keys.WAKE_ALARM_DURATION_SECONDS] = config.wakeAlarmDurationSeconds
            prefs[Keys.TRANSITION_ALARM_DURATION_SECONDS] = config.transitionAlarmDurationSeconds
            prefs[Keys.WAKE_ALARM_MODE] = config.wakeAlarmMode
            prefs[Keys.ENABLE_WAKE_ALARM] = config.enableWakeAlarm
            prefs[Keys.CELEBRATION_WALLPAPER_URI] = config.celebrationWallpaperUri
            prefs[Keys.CELEBRATION_MESSAGE] = config.celebrationMessage
            prefs[Keys.CELEBRATION_SHOW_QUOTE] = config.celebrationShowQuote
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun resetAllPreferences() {
        context.dataStore.edit { it.clear() }
    }
}
