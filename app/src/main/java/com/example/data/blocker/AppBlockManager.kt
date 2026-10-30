package com.example.data.blocker

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.util.Log

data class AppBlockConfig(
    val isBlockerEnabled: Boolean = false,
    val blockAllDistractions: Boolean = false,
    val blockedPackages: Set<String> = emptySet(),
    val strictModeNoExit: Boolean = false,
    val allowYouTubeForLectures: Boolean = true
)

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val isBlocked: Boolean,
    val category: String,
    val isDistractionDefault: Boolean
)

val DEFAULT_DISTRACTING_PACKAGES = setOf(
    "com.google.android.youtube",
    "com.instagram.android",
    "com.whatsapp",
    "com.zhiliaoapp.musically",      // TikTok
    "com.facebook.katana",
    "com.facebook.orca",            // Messenger
    "com.twitter.android",
    "com.snapchat.android",
    "com.reddit.frontpage",
    "tv.twitch.android.app",
    "com.netflix.mediaclient",
    "com.discord",
    "org.telegram.messenger",
    "com.spotify.music",
    "com.android.chrome",
    "com.pinterest",
    "com.valvesoftware.android.steam.community",
    "com.supercell.clashofclans",
    "com.supercell.brawlstars",
    "com.roblox.client",
    "com.dts.freefireth",
    "com.tencent.ig",               // PUBG
    "com.pubg.imobile"              // BGMI
)

val POPULAR_DISTRACTION_PRESETS = listOf(
    "com.google.android.youtube" to "YouTube",
    "com.instagram.android" to "Instagram",
    "com.whatsapp" to "WhatsApp",
    "com.snapchat.android" to "Snapchat",
    "com.facebook.katana" to "Facebook",
    "com.twitter.android" to "X (Twitter)",
    "com.reddit.frontpage" to "Reddit",
    "com.netflix.mediaclient" to "Netflix",
    "org.telegram.messenger" to "Telegram",
    "com.discord" to "Discord",
    "com.spotify.music" to "Spotify",
    "tv.twitch.android.app" to "Twitch",
    "com.zhiliaoapp.musically" to "TikTok",
    "com.android.chrome" to "Google Chrome",
    "com.pinterest" to "Pinterest",
    "com.supercell.clashofclans" to "Clash of Clans",
    "com.roblox.client" to "Roblox",
    "com.pubg.imobile" to "BGMI / PUBG"
)

object AppBlockManager {

    private const val TAG = "AppBlockManager"

    /**
     * Checks if the user has granted PACKAGE_USAGE_STATS permission via Android Settings.
     */
    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Launches the system Usage Access settings page so the user can toggle permission.
     */
    fun openUsageAccessSettings(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch ACTION_USAGE_ACCESS_SETTINGS: ${e.message}")
        }
    }

    /**
     * Retrieves all user-launchable apps installed on device, categorized and sorted.
     * Includes third-party installed apps, launcher activities, and popular distraction presets.
     */
    fun getInstalledLauncherApps(context: Context, blockedPackages: Set<String>): List<InstalledAppItem> {
        val pm = context.packageManager
        val result = mutableListOf<InstalledAppItem>()
        val seen = mutableSetOf<String>()

        // 1. Query launcher intent activities
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = try {
            pm.queryIntentActivities(mainIntent, 0)
        } catch (e: Exception) {
            emptyList()
        }

        for (resolve in resolveInfos) {
            val pkg = resolve.activityInfo.packageName ?: continue
            if (pkg == context.packageName || seen.contains(pkg)) continue
            seen.add(pkg)

            val label = resolve.loadLabel(pm)?.toString() ?: pkg
            val isDefaultDistraction = DEFAULT_DISTRACTING_PACKAGES.contains(pkg) ||
                    label.contains("Instagram", ignoreCase = true) ||
                    label.contains("YouTube", ignoreCase = true) ||
                    label.contains("WhatsApp", ignoreCase = true) ||
                    label.contains("TikTok", ignoreCase = true) ||
                    label.contains("Twitter", ignoreCase = true) ||
                    label.contains("Facebook", ignoreCase = true) ||
                    label.contains("Reddit", ignoreCase = true) ||
                    label.contains("Snapchat", ignoreCase = true)

            val isSystem = (resolve.activityInfo.applicationInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM) != 0) &&
                    (resolve.activityInfo.applicationInfo?.flags?.and(ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0)

            val category = when {
                isDefaultDistraction -> "Social & Video"
                !isSystem -> "Installed Apps"
                else -> "System Utility"
            }

            result.add(
                InstalledAppItem(
                    packageName = pkg,
                    appName = label,
                    isBlocked = blockedPackages.contains(pkg) || (blockedPackages.isEmpty() && isDefaultDistraction),
                    category = category,
                    isDistractionDefault = isDefaultDistraction
                )
            )
        }

        // 2. Query all installed applications to catch user-installed apps without launcher filter
        val installedAppsList = try {
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
        } catch (e: Exception) {
            emptyList()
        }

        for (appInfo in installedAppsList) {
            val pkg = appInfo.packageName ?: continue
            if (pkg == context.packageName || seen.contains(pkg)) continue

            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 &&
                    (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0
            val launchIntent = pm.getLaunchIntentForPackage(pkg)

            // Only include if it has a launch intent or is user-installed non-system app
            if (launchIntent != null || !isSystem) {
                seen.add(pkg)
                val label = appInfo.loadLabel(pm)?.toString() ?: pkg
                val isDefaultDistraction = DEFAULT_DISTRACTING_PACKAGES.contains(pkg) ||
                        label.contains("Instagram", ignoreCase = true) ||
                        label.contains("YouTube", ignoreCase = true) ||
                        label.contains("WhatsApp", ignoreCase = true) ||
                        label.contains("TikTok", ignoreCase = true)

                val category = when {
                    isDefaultDistraction -> "Social & Video"
                    !isSystem -> "Installed Apps"
                    else -> "System Utility"
                }

                result.add(
                    InstalledAppItem(
                        packageName = pkg,
                        appName = label,
                        isBlocked = blockedPackages.contains(pkg) || (blockedPackages.isEmpty() && isDefaultDistraction),
                        category = category,
                        isDistractionDefault = isDefaultDistraction
                    )
                )
            }
        }

        // Only real, actually installed apps on the device are returned.
        // No uninstalled presets are added.

        return result.sortedWith(
            compareByDescending<InstalledAppItem> { it.isBlocked }
                .thenByDescending { it.isDistractionDefault }
                .thenBy { it.appName.lowercase() }
        )
    }

    /**
     * Resolves the package name of the app currently in foreground using UsageStatsManager.
     */
    fun getForegroundPackageName(context: Context): String? {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return null

        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 10_000L // Look back 10 seconds

        val events = usageStatsManager.queryEvents(beginTime, endTime) ?: return null
        val event = UsageEvents.Event()
        var lastForegroundPkg: String? = null

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                event.eventType == 1 /* UsageEvents.Event.MOVE_TO_FOREGROUND */
            ) {
                lastForegroundPkg = event.packageName
            }
        }

        return lastForegroundPkg
    }

    /**
     * Determines whether the given package should be blocked right now.
     */
    fun shouldBlockPackage(
        targetPackage: String,
        ownPackage: String,
        config: AppBlockConfig
    ): Boolean {
        if (!config.isBlockerEnabled) return false
        if (targetPackage == ownPackage) return false

        // Allow YouTube for educational lectures if enabled
        if (config.allowYouTubeForLectures && targetPackage == "com.google.android.youtube") {
            return false
        }

        // Always allow system UI, keyboards, dialers, and settings
        if (targetPackage.startsWith("com.android.systemui") ||
            targetPackage.startsWith("com.google.android.inputmethod") ||
            targetPackage.contains("dialer", ignoreCase = true) ||
            targetPackage.contains("telecom", ignoreCase = true)
        ) {
            return false
        }

        if (config.blockAllDistractions) {
            return true
        }

        return config.blockedPackages.contains(targetPackage)
    }
}
