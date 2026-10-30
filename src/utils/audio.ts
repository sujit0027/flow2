// Web Audio API Synthesizer - mirrors Android SoundRingtoneManager
import { SoundRingtoneConfig } from '../types';

let audioCtx: AudioContext | null = null;

function getAudioContext(): AudioContext {
  if (!audioCtx) {
    const AudioContextClass = window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
    audioCtx = new AudioContextClass();
  }
  if (audioCtx.state === 'suspended') {
    audioCtx.resume();
  }
  return audioCtx;
}

export const BUILT_IN_SOUNDS = [
  { id: 'zen_gong', name: 'Zen Gong', description: 'Deep resonant 432Hz meditative bell' },
  { id: 'cyber_pulse', name: 'Cyber Pulse', description: 'Clean two-tone modern focus pulse' },
  { id: 'temple_bell', name: 'Temple Bell', description: 'Peaceful harmonic Tibetan singing chime' },
  { id: 'digital_alarm', name: 'Digital Beep', description: 'Energetic waking beep sequence' },
];

export function playSynthesizedTone(toneId: string): void {
  try {
    const ctx = getAudioContext();
    const now = ctx.currentTime;

    if (toneId === 'zen_gong') {
      // 432Hz resonant gong
      const freqs = [432, 648, 864];
      freqs.forEach((freq, idx) => {
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.type = 'sine';
        osc.frequency.setValueAtTime(freq, now);

        const volume = (0.5 / (idx + 1)) * 0.6;
        gain.gain.setValueAtTime(0.001, now);
        gain.gain.exponentialRampToValueAtTime(volume, now + 0.05);
        gain.gain.exponentialRampToValueAtTime(0.0001, now + 2.2);

        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.start(now);
        osc.stop(now + 2.3);
      });
    } else if (toneId === 'cyber_pulse') {
      // D5 (587.33), A5 (880) modern pulse
      const freqs = [587.33, 880.0];
      freqs.forEach((freq, idx) => {
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.type = 'triangle';
        osc.frequency.setValueAtTime(freq, now + idx * 0.12);

        gain.gain.setValueAtTime(0.001, now + idx * 0.12);
        gain.gain.linearRampToValueAtTime(0.3, now + idx * 0.12 + 0.04);
        gain.gain.exponentialRampToValueAtTime(0.0001, now + idx * 0.12 + 0.6);

        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.start(now + idx * 0.12);
        osc.stop(now + idx * 0.12 + 0.65);
      });
    } else if (toneId === 'temple_bell') {
      // 528Hz Solfeggio frequency + harmonics
      const freqs = [528.0, 792.0, 1056.0];
      freqs.forEach((freq, idx) => {
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.type = 'sine';
        osc.frequency.setValueAtTime(freq, now);

        const volume = (0.4 / (idx + 1)) * 0.6;
        gain.gain.setValueAtTime(0.001, now);
        gain.gain.exponentialRampToValueAtTime(volume, now + 0.03);
        gain.gain.exponentialRampToValueAtTime(0.0001, now + 2.5);

        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.start(now);
        osc.stop(now + 2.6);
      });
    } else if (toneId === 'digital_alarm') {
      // Staccato alarm beeps (880Hz / 1174Hz)
      const beeps = 4;
      for (let i = 0; i < beeps; i++) {
        const startTime = now + i * 0.28;
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.type = 'square';
        osc.frequency.setValueAtTime(i % 2 === 0 ? 880 : 1174.66, startTime);

        gain.gain.setValueAtTime(0.001, startTime);
        gain.gain.linearRampToValueAtTime(0.15, startTime + 0.02);
        gain.gain.setValueAtTime(0.15, startTime + 0.14);
        gain.gain.linearRampToValueAtTime(0.001, startTime + 0.18);

        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.start(startTime);
        osc.stop(startTime + 0.2);
      }
    } else {
      // Default 440Hz chime
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(440, now);
      gain.gain.setValueAtTime(0.001, now);
      gain.gain.exponentialRampToValueAtTime(0.3, now + 0.05);
      gain.gain.exponentialRampToValueAtTime(0.0001, now + 1.2);
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.start(now);
      osc.stop(now + 1.3);
    }
  } catch (e) {
    console.warn('Audio playback not allowed or failed:', e);
  }
}

export function playEventSound(
  eventType: 'TARGET_REACHED' | 'NEXT_TASK_ALARM' | 'BREAK_OVER',
  config: SoundRingtoneConfig
): void {
  if (!config.soundEnabled) return;

  let soundId = config.targetSoundId;
  if (eventType === 'NEXT_TASK_ALARM') {
    soundId = config.nextTaskSoundId;
  } else if (eventType === 'BREAK_OVER') {
    soundId = config.breakSoundId;
  }

  playSynthesizedTone(soundId || 'zen_gong');
}

export function triggerVibration(pattern: number | number[] = [200, 100, 200]): void {
  try {
    if (typeof navigator !== 'undefined' && 'vibrate' in navigator) {
      navigator.vibrate(pattern);
    }
  } catch {
    // Ignore unsupported browser environments
  }
}

let wakeAlarmIntervalId: number | null = null;
let isAlarmActive = false;

export function isWakeAlarmActive(): boolean {
  return isAlarmActive;
}

export function startWakeAlarm(toneId: string = 'digital_alarm'): void {
  stopWakeAlarm();
  isAlarmActive = true;
  // Play sound immediately
  playSynthesizedTone(toneId);
  triggerVibration([500, 200, 500, 200, 500]);

  // Repeat every 2 seconds
  wakeAlarmIntervalId = window.setInterval(() => {
    if (!isAlarmActive) {
      stopWakeAlarm();
      return;
    }
    playSynthesizedTone(toneId);
    triggerVibration([400, 150, 400]);
  }, 2000);
}

export function stopWakeAlarm(): void {
  isAlarmActive = false;
  if (wakeAlarmIntervalId !== null) {
    clearInterval(wakeAlarmIntervalId);
    wakeAlarmIntervalId = null;
  }
}

export function requestNotificationPermission(): void {
  try {
    if (typeof window !== 'undefined' && 'Notification' in window) {
      if (Notification.permission === 'default') {
        Notification.requestPermission().catch(() => {});
      }
    }
  } catch {
    // Unsupported
  }
}

export function sendBrowserNotification(title: string, body: string): void {
  try {
    if (typeof window !== 'undefined' && 'Notification' in window) {
      if (Notification.permission === 'granted') {
        new Notification(title, {
          body,
          icon: '/favicon.ico',
        });
      }
    }
  } catch {
    // Notification error or sandboxed
  }
}

