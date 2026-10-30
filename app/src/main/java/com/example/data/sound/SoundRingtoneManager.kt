package com.example.data.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

data class SoundOption(
    val id: String,
    val name: String,
    val description: String,
    val isCustom: Boolean = false,
    val uriString: String? = null
)

enum class SoundEventType {
    TARGET_REACHED,
    NEXT_TASK_ALARM,
    BREAK_OVER
}

data class SoundRingtoneConfig(
    val soundEnabled: Boolean = true,
    val targetSoundId: String = "zen_gong",
    val targetSoundUri: String = "",
    val targetSoundName: String = "Zen Gong (Serene)",
    val nextTaskSoundId: String = "digital_alarm",
    val nextTaskSoundUri: String = "",
    val nextTaskSoundName: String = "Digital Beep (Alert)",
    val breakSoundId: String = "temple_bell",
    val breakSoundUri: String = "",
    val breakSoundName: String = "Temple Bell (Mindful)"
)

object SoundRingtoneManager {

    private const val TAG = "SoundRingtoneManager"
    private val scope = CoroutineScope(Dispatchers.Default)

    private var previewJob: Job? = null
    private var activeMediaPlayer: MediaPlayer? = null
    private var activeRingtone: Ringtone? = null

    val BUILT_IN_SOUNDS = listOf(
        SoundOption("zen_gong", "Zen Gong", "Deep resonant 432Hz meditative bell"),
        SoundOption("cyber_pulse", "Cyber Pulse", "Clean two-tone modern focus pulse"),
        SoundOption("temple_bell", "Temple Bell", "Peaceful harmonic Tibetan singing chime"),
        SoundOption("digital_alarm", "Digital Beep", "Energetic waking beep sequence"),
        SoundOption("system_alarm", "System Default Alarm", "Native device alarm ringtone"),
        SoundOption("system_notification", "System Default Notification", "Native device notification chime")
    )

    fun getDisplayName(soundId: String, customName: String?): String {
        if (!customName.isNullOrBlank()) return customName
        return BUILT_IN_SOUNDS.firstOrNull { it.id == soundId }?.name ?: "Default Ringtone"
    }

    /**
     * Plays a preview of the specified sound ID or custom URI string.
     */
    fun playPreview(
        context: Context,
        soundId: String,
        uriString: String? = null,
        onFinished: (() -> Unit)? = null
    ) {
        stopPlayback()
        previewJob = scope.launch {
            try {
                when (soundId) {
                    "custom_uri" -> {
                        if (!uriString.isNullOrBlank()) {
                            playUriSound(context, Uri.parse(uriString), onFinished)
                        } else {
                            playSynthesizedTone("zen_gong")
                            onFinished?.invoke()
                        }
                    }
                    "system_alarm" -> {
                        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                        playUriSound(context, alarmUri, onFinished)
                    }
                    "system_notification" -> {
                        val notifUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                        playUriSound(context, notifUri, onFinished)
                    }
                    else -> {
                        playSynthesizedTone(soundId)
                        onFinished?.invoke()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing sound preview: ${e.message}", e)
                onFinished?.invoke()
            }
        }
    }

    /**
     * Fires a sound for a real workflow event (Target Reached, Next Task Alert, Break Over).
     */
    fun triggerEventSound(context: Context, event: SoundEventType, config: SoundRingtoneConfig) {
        if (!config.soundEnabled) return

        val (soundId, soundUri) = when (event) {
            SoundEventType.TARGET_REACHED -> config.targetSoundId to config.targetSoundUri
            SoundEventType.NEXT_TASK_ALARM -> config.nextTaskSoundId to config.nextTaskSoundUri
            SoundEventType.BREAK_OVER -> config.breakSoundId to config.breakSoundUri
        }

        playPreview(context, soundId, soundUri)
    }

    fun stopPlayback() {
        previewJob?.cancel()
        previewJob = null

        try {
            activeMediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (ignored: Exception) {}
        activeMediaPlayer = null

        try {
            activeRingtone?.let {
                if (it.isPlaying) it.stop()
            }
        } catch (ignored: Exception) {}
        activeRingtone = null
    }

    private fun playUriSound(context: Context, uri: Uri, onFinished: (() -> Unit)? = null) {
        try {
            val ringtone = RingtoneManager.getRingtone(context.applicationContext, uri)
            if (ringtone != null) {
                activeRingtone = ringtone
                ringtone.play()
                scope.launch {
                    delay(3500L)
                    try {
                        if (ringtone.isPlaying) ringtone.stop()
                    } catch (ignored: Exception) {}
                    onFinished?.invoke()
                }
                return
            }
        } catch (e: Exception) {
            Log.w(TAG, "RingtoneManager playback failed, trying MediaPlayer: ${e.message}")
        }

        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context.applicationContext, uri)
                prepare()
                setOnCompletionListener {
                    it.release()
                    onFinished?.invoke()
                }
                start()
            }
            activeMediaPlayer = player
        } catch (e: Exception) {
            Log.e(TAG, "MediaPlayer failed on URI $uri, fallback to synthesized tone", e)
            playSynthesizedTone("zen_gong")
            onFinished?.invoke()
        }
    }

    /**
     * Synthesizes pleasing sine-wave tones using AudioTrack.
     * Guaranteed zero-dependency audio generation that works without assets!
     */
    fun playSynthesizedTone(toneId: String) {
        val sampleRate = 44100
        val durationSeconds: Double
        val frequencies: List<Double>
        val envelopeType: String

        when (toneId) {
            "zen_gong" -> {
                durationSeconds = 2.2
                frequencies = listOf(432.0, 864.0 * 0.5, 648.0)
                envelopeType = "bell"
            }
            "cyber_pulse" -> {
                durationSeconds = 1.0
                frequencies = listOf(587.33, 880.0) // D5, A5
                envelopeType = "pulse"
            }
            "temple_bell" -> {
                durationSeconds = 2.5
                frequencies = listOf(528.0, 1056.0 * 0.4, 792.0)
                envelopeType = "bell"
            }
            "digital_alarm" -> {
                durationSeconds = 3.6
                frequencies = listOf(880.0, 1174.66) // A5, D6 - high urgency alarm
                envelopeType = "alarm_staccato"
            }
            else -> {
                durationSeconds = 1.5
                frequencies = listOf(440.0)
                envelopeType = "bell"
            }
        }

        val numSamples = (durationSeconds * sampleRate).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val time = i.toDouble() / sampleRate
            val progress = time / durationSeconds

            val envelope = when (envelopeType) {
                "bell" -> {
                    // Quick attack, slow exponential decay
                    val attack = (time * 30.0).coerceAtMost(1.0)
                    attack * exp(-3.0 * progress)
                }
                "pulse" -> {
                    // Soft attack and smooth release
                    sin(PI * progress)
                }
                "alarm_staccato" -> {
                    // Beep-beep staccato gating (4 beeps per second)
                    val cycle = (time * 4.0) % 1.0
                    val isBeep = if (cycle < 0.6) 1.0 else 0.0
                    isBeep * (1.0 - progress * 0.2)
                }
                else -> exp(-2.5 * progress)
            }

            var sampleValue = 0.0
            for ((idx, freq) in frequencies.withIndex()) {
                val weight = 1.0 / (idx + 1)
                sampleValue += weight * sin(2.0 * PI * freq * time)
            }

            // Normalize and scale to 16-bit PCM amplitude
            val normalized = (sampleValue / frequencies.size.coerceAtLeast(1)) * envelope * 0.75
            buffer[i] = (normalized * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()

            // Wait until tone finishes playing before releasing
            Thread.sleep((durationSeconds * 1000).toLong() + 200L)
            audioTrack.stop()
            audioTrack.release()
        } catch (e: Exception) {
            Log.e(TAG, "AudioTrack synthesis error: ${e.message}")
        }
    }
}
