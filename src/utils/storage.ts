import {
  AppSettings,
  AppThemeConfig,
  AutoFlowConfig,
  FocusScreenCustomization,
  RoutineEntity,
  RoutineTask,
  SoundRingtoneConfig,
  WallpaperConfig,
  AppBlockConfig,
} from '../types';

export function formatDurationHMS(totalSeconds: number): string {
  const safeSecs = Math.max(0, Math.floor(totalSeconds));
  const hours = Math.floor(safeSecs / 3600);
  const minutes = Math.floor((safeSecs % 3600) / 60);
  const seconds = safeSecs % 60;

  const pad = (n: number) => n.toString().padStart(2, '0');

  if (hours > 0) {
    return `${pad(hours)}:${pad(minutes)}:${pad(seconds)}`;
  }
  return `${pad(minutes)}:${pad(seconds)}`;
}

export function formatMinutesHuman(minutes: number): string {
  const safeMins = Math.max(0, minutes);
  const hours = Math.floor(safeMins / 60);
  const remMins = safeMins % 60;

  if (hours > 0 && remMins > 0) {
    return `${hours}h ${remMins}m`;
  }
  if (hours > 0) {
    return `${hours} hr${hours > 1 ? 's' : ''}`;
  }
  return `${remMins}m`;
}

export function formatMinutesDetailed(minutes: number): string {
  const safeMins = Math.max(0, minutes);
  const hours = Math.floor(safeMins / 60);
  const remMins = safeMins % 60;

  if (hours > 0 && remMins > 0) {
    return `${hours} hr ${remMins} min`;
  }
  if (hours > 0) {
    return `${hours} hour${hours > 1 ? 's' : ''}`;
  }
  return `${remMins} minutes`;
}

export const DEFAULT_SAMPLE_ROUTINE: RoutineEntity = {
  id: 1,
  name: 'Student Competitive Prep',
  description: 'Physics, Chemistry, CET MCQs & Flexible Nutrition (Sample Routine)',
  isTemplate: true,
  isSampleData: true,
  isActive: true,
  createdAt: Date.now(),
};

export const DEFAULT_SAMPLE_TASKS: RoutineTask[] = [
  {
    id: 1,
    routineId: 1,
    title: 'Physics: Wave Optics Theory',
    category: 'STUDY',
    subject: 'Physics',
    minDurationMinutes: 45,
    extensionTargetMinutes: 15,
    isExtensionAllowed: true,
    isCompulsory: true,
    isOptional: false,
    canBeSkipped: false,
    taskOrder: 1,
    isCompleted: false,
    accumulatedDurationSeconds: 0,
    notes: "Interference formulas, Young's double slit derivation, and sign conventions.",
    createdAt: Date.now(),
  },
  {
    id: 2,
    routineId: 1,
    title: 'Physics: Numerical Problem Set',
    category: 'STUDY',
    subject: 'Physics',
    minDurationMinutes: 40,
    extensionTargetMinutes: 20,
    isExtensionAllowed: true,
    isCompulsory: true,
    isOptional: false,
    canBeSkipped: false,
    taskOrder: 2,
    isCompleted: false,
    accumulatedDurationSeconds: 0,
    notes: 'Solve 20 high-yield problems on optics and diffraction.',
    createdAt: Date.now(),
  },
  {
    id: 3,
    routineId: 1,
    title: 'Mindful Hydration & Eye Rest',
    category: 'BREAK',
    minDurationMinutes: 10,
    extensionTargetMinutes: 5,
    isExtensionAllowed: true,
    isCompulsory: false,
    isOptional: true,
    canBeSkipped: true,
    taskOrder: 3,
    isCompleted: false,
    accumulatedDurationSeconds: 0,
    notes: 'Look away from screens, drink water, stretch.',
    createdAt: Date.now(),
  },
  {
    id: 4,
    routineId: 1,
    title: 'Chemistry: Organic Reaction Mechanisms',
    category: 'STUDY',
    subject: 'Chemistry',
    minDurationMinutes: 45,
    extensionTargetMinutes: 15,
    isExtensionAllowed: true,
    isCompulsory: true,
    isOptional: false,
    canBeSkipped: false,
    taskOrder: 4,
    isCompleted: false,
    accumulatedDurationSeconds: 0,
    notes: 'Electrophilic aromatic substitution and nucleophilic addition pathways.',
    createdAt: Date.now(),
  },
  {
    id: 5,
    routineId: 1,
    title: 'Balanced Nutritious Lunch',
    category: 'MEAL',
    minDurationMinutes: 35,
    extensionTargetMinutes: 15,
    isExtensionAllowed: true,
    isCompulsory: false,
    isOptional: true,
    canBeSkipped: true,
    mealType: 'lunch',
    taskOrder: 5,
    isCompleted: false,
    accumulatedDurationSeconds: 0,
    notes: 'Healthy meal away from the study desk.',
    createdAt: Date.now(),
  },
  {
    id: 6,
    routineId: 1,
    title: 'Chemistry: Questions & Conversions',
    category: 'STUDY',
    subject: 'Chemistry',
    minDurationMinutes: 40,
    extensionTargetMinutes: 15,
    isExtensionAllowed: true,
    isCompulsory: true,
    isOptional: false,
    canBeSkipped: false,
    taskOrder: 6,
    isCompleted: false,
    accumulatedDurationSeconds: 0,
    notes: 'Reagent identification and step-by-step synthetic conversions.',
    createdAt: Date.now(),
  },
  {
    id: 7,
    routineId: 1,
    title: 'CET / Entrance Mock MCQs',
    category: 'STUDY',
    subject: 'Test Prep',
    minDurationMinutes: 60,
    extensionTargetMinutes: 30,
    isExtensionAllowed: true,
    isCompulsory: true,
    isOptional: false,
    canBeSkipped: false,
    taskOrder: 7,
    isCompleted: false,
    accumulatedDurationSeconds: 0,
    notes: 'Timed exam simulation for accuracy and speed.',
    createdAt: Date.now(),
  },
  {
    id: 8,
    routineId: 1,
    title: 'Weak Topics Targeted Review',
    category: 'STUDY',
    subject: 'Revision',
    minDurationMinutes: 35,
    extensionTargetMinutes: 15,
    isExtensionAllowed: true,
    isCompulsory: true,
    isOptional: false,
    canBeSkipped: false,
    taskOrder: 8,
    isCompleted: false,
    accumulatedDurationSeconds: 0,
    notes: 'Target questions answered incorrectly during morning sets.',
    createdAt: Date.now(),
  },
  {
    id: 9,
    routineId: 1,
    title: 'Evening Walk & Posture Mobility',
    category: 'EXERCISE',
    minDurationMinutes: 20,
    extensionTargetMinutes: 10,
    isExtensionAllowed: true,
    isCompulsory: false,
    isOptional: true,
    canBeSkipped: true,
    taskOrder: 9,
    isCompleted: false,
    accumulatedDurationSeconds: 0,
    notes: 'Fresh air walk and lumbar spine mobility.',
    createdAt: Date.now(),
  },
  {
    id: 10,
    routineId: 1,
    title: 'Active Recall & Flashcard Synthesis',
    category: 'STUDY',
    subject: 'Revision',
    minDurationMinutes: 30,
    extensionTargetMinutes: 15,
    isExtensionAllowed: true,
    isCompulsory: true,
    isOptional: false,
    canBeSkipped: false,
    taskOrder: 10,
    isCompleted: false,
    accumulatedDurationSeconds: 0,
    notes: "Anki / flashcard run on today's key definitions and formulas.",
    createdAt: Date.now(),
  },
];

export const PRESET_ROUTINES: Record<string, RoutineTask[]> = {
  'Balanced Flow': [
    {
      id: 101,
      routineId: 1,
      title: 'Deep Focus Morning Block',
      category: 'STUDY',
      subject: 'Core Study',
      minDurationMinutes: 45,
      extensionTargetMinutes: 15,
      isExtensionAllowed: true,
      isCompulsory: true,
      isOptional: false,
      canBeSkipped: false,
      taskOrder: 1,
      isCompleted: false,
      accumulatedDurationSeconds: 0,
      createdAt: Date.now(),
    },
    {
      id: 102,
      routineId: 1,
      title: 'Eye Rest & Hydration',
      category: 'BREAK',
      minDurationMinutes: 10,
      extensionTargetMinutes: 5,
      isExtensionAllowed: true,
      isCompulsory: false,
      isOptional: true,
      canBeSkipped: true,
      taskOrder: 2,
      isCompleted: false,
      accumulatedDurationSeconds: 0,
      createdAt: Date.now(),
    },
    {
      id: 103,
      routineId: 1,
      title: 'Applied Problem Solving',
      category: 'STUDY',
      subject: 'Practice',
      minDurationMinutes: 50,
      extensionTargetMinutes: 20,
      isExtensionAllowed: true,
      isCompulsory: true,
      isOptional: false,
      canBeSkipped: false,
      taskOrder: 3,
      isCompleted: false,
      accumulatedDurationSeconds: 0,
      createdAt: Date.now(),
    },
    {
      id: 104,
      routineId: 1,
      title: 'Nutritious Lunch & Recharge',
      category: 'MEAL',
      minDurationMinutes: 35,
      extensionTargetMinutes: 10,
      isExtensionAllowed: true,
      isCompulsory: false,
      isOptional: true,
      canBeSkipped: true,
      mealType: 'lunch',
      taskOrder: 4,
      isCompleted: false,
      accumulatedDurationSeconds: 0,
      createdAt: Date.now(),
    },
    {
      id: 105,
      routineId: 1,
      title: 'Afternoon Speed Drill',
      category: 'STUDY',
      subject: 'Exam Prep',
      minDurationMinutes: 45,
      extensionTargetMinutes: 15,
      isExtensionAllowed: true,
      isCompulsory: true,
      isOptional: false,
      canBeSkipped: false,
      taskOrder: 5,
      isCompleted: false,
      accumulatedDurationSeconds: 0,
      createdAt: Date.now(),
    },
  ],
  'Deep Work Focus': [
    {
      id: 201,
      routineId: 1,
      title: 'Heavy Cognitive Theory',
      category: 'STUDY',
      subject: 'Deep Work',
      minDurationMinutes: 90,
      extensionTargetMinutes: 30,
      isExtensionAllowed: true,
      isCompulsory: true,
      isOptional: false,
      canBeSkipped: false,
      taskOrder: 1,
      isCompleted: false,
      accumulatedDurationSeconds: 0,
      createdAt: Date.now(),
    },
    {
      id: 202,
      routineId: 1,
      title: 'Mindful Walk & Coffee',
      category: 'BREAK',
      minDurationMinutes: 20,
      extensionTargetMinutes: 10,
      isExtensionAllowed: true,
      isCompulsory: false,
      isOptional: true,
      canBeSkipped: true,
      taskOrder: 2,
      isCompleted: false,
      accumulatedDurationSeconds: 0,
      createdAt: Date.now(),
    },
    {
      id: 203,
      routineId: 1,
      title: 'Complex Analysis & Synthesis',
      category: 'STUDY',
      subject: 'Deep Work',
      minDurationMinutes: 90,
      extensionTargetMinutes: 30,
      isExtensionAllowed: true,
      isCompulsory: true,
      isOptional: false,
      canBeSkipped: false,
      taskOrder: 3,
      isCompleted: false,
      accumulatedDurationSeconds: 0,
      createdAt: Date.now(),
    },
  ],
  'Fast Exam Sprint': [
    {
      id: 301,
      routineId: 1,
      title: 'Speed Formulas & Mnemonics',
      category: 'STUDY',
      subject: 'Sprint',
      minDurationMinutes: 25,
      extensionTargetMinutes: 10,
      isExtensionAllowed: true,
      isCompulsory: true,
      isOptional: false,
      canBeSkipped: false,
      taskOrder: 1,
      isCompleted: false,
      accumulatedDurationSeconds: 0,
      createdAt: Date.now(),
    },
    {
      id: 302,
      routineId: 1,
      title: 'Quick Water Break',
      category: 'BREAK',
      minDurationMinutes: 5,
      extensionTargetMinutes: 5,
      isExtensionAllowed: true,
      isCompulsory: false,
      isOptional: true,
      canBeSkipped: true,
      taskOrder: 2,
      isCompleted: false,
      accumulatedDurationSeconds: 0,
      createdAt: Date.now(),
    },
    {
      id: 303,
      routineId: 1,
      title: 'Timed Past Paper Test',
      category: 'STUDY',
      subject: 'Mock Exam',
      minDurationMinutes: 45,
      extensionTargetMinutes: 15,
      isExtensionAllowed: true,
      isCompulsory: true,
      isOptional: false,
      canBeSkipped: false,
      taskOrder: 3,
      isCompleted: false,
      accumulatedDurationSeconds: 0,
      createdAt: Date.now(),
    },
    {
      id: 304,
      routineId: 1,
      title: 'Error Log Correction',
      category: 'STUDY',
      subject: 'Revision',
      minDurationMinutes: 30,
      extensionTargetMinutes: 10,
      isExtensionAllowed: true,
      isCompulsory: true,
      isOptional: false,
      canBeSkipped: false,
      taskOrder: 4,
      isCompleted: false,
      accumulatedDurationSeconds: 0,
      createdAt: Date.now(),
    },
  ],
};

export const DEFAULT_APP_SETTINGS: AppSettings = {
  autoAdvanceToNextTask: false,
  vibrateOnComplete: true,
  keepScreenOn: true,
  breaksOptionalDefault: true,
  defaultStudyMinDuration: 30,
  enforceCompulsoryWarning: true,
};

export const DEFAULT_FOCUS_CUSTOMIZATION: FocusScreenCustomization = {
  showTaskTitle: true,
  showTimer: true,
  showElapsedTime: true,
  showMinRemaining: true,
  showProgressRing: true,
  showSessionLabel: true,
  showNextTaskPreview: true,
  showMotivationalQuote: false,
  showTimerControls: true,
  showExtraStats: true,
  isUltraMinimal: false,
};

export const DEFAULT_WALLPAPER_CONFIG: WallpaperConfig = {
  homeWallpaperType: 'AMOLED',
  focusWallpaperType: 'AMOLED',
  customWallpaperUri: '',
  dimLevel: 0.35,
  useBlur: false,
};

export const DEFAULT_THEME_CONFIG: AppThemeConfig = {
  themeMode: 'DARK',
  accentColor: 'EMERALD',
  timerStyle: 'CIRCULAR',
  dailyStudyTargetHours: 4,
  isOnboardingCompleted: false,
};

export const DEFAULT_AUTO_FLOW_CONFIG: AutoFlowConfig = {
  autoAdvanceCompulsoryOnly: true,
  countdownDurationSeconds: 30,
  ringAlarmOnCountdownExpiry: true,
  autoSkipOptionalBreaksIfIdle: true,
  alertOnSkippedTask: true,
  notifyNextTaskMinutes: 1,
  autoStartNextTask: false,
  wakeAlarmDurationSeconds: 30, // 30s customizable wake alarm duration
  transitionAlarmDurationSeconds: 20, // 20s transition alert
  wakeAlarmMode: 'ALARM', // 'ALARM' | 'VIBRATION_ONLY' | 'OVERTIME_AUTO' | 'AUTO_NEXT_COMPULSORY'
  enableWakeAlarm: true,
  celebrationWallpaperUri: '',
  celebrationMessage: 'Sensational effort! You crushed all compulsory study tasks today.',
  celebrationShowQuote: true,
};

export const DEFAULT_SOUND_CONFIG: SoundRingtoneConfig = {
  soundEnabled: true,
  targetSoundId: 'zen_gong',
  targetSoundUri: '',
  targetSoundName: 'Zen Gong (Serene)',
  nextTaskSoundId: 'digital_alarm',
  nextTaskSoundUri: '',
  nextTaskSoundName: 'Digital Beep (Alert)',
  breakSoundId: 'temple_bell',
  breakSoundUri: '',
  breakSoundName: 'Temple Bell (Mindful)',
};

export const DEFAULT_APP_BLOCK_CONFIG: AppBlockConfig = {
  isBlockerEnabled: false,
  blockAllDistractions: false,
  blockedPackages: [
    'com.instagram.android',
    'com.zhiliaoapp.musically',
    'com.twitter.android',
    'com.reddit.frontpage',
    'com.netflix.mediaclient',
  ],
  strictModeNoExit: false,
  allowYouTubeForLectures: true,
};

export const POPULAR_DISTRACTIONS = [
  { id: 'com.instagram.android', name: 'Instagram', category: 'Social Media' },
  { id: 'com.zhiliaoapp.musically', name: 'TikTok', category: 'Short Video' },
  { id: 'com.google.android.youtube', name: 'YouTube', category: 'Video Streaming' },
  { id: 'com.twitter.android', name: 'X / Twitter', category: 'Social Media' },
  { id: 'com.reddit.frontpage', name: 'Reddit', category: 'Discussion Forums' },
  { id: 'com.whatsapp', name: 'WhatsApp', category: 'Messaging' },
  { id: 'com.snapchat.android', name: 'Snapchat', category: 'Social Media' },
  { id: 'com.facebook.katana', name: 'Facebook', category: 'Social Media' },
  { id: 'com.netflix.mediaclient', name: 'Netflix', category: 'Entertainment' },
  { id: 'com.discord', name: 'Discord', category: 'Community Chat' },
  { id: 'tv.twitch.android.app', name: 'Twitch', category: 'Live Streams' },
  { id: 'com.spotify.music', name: 'Spotify', category: 'Music & Podcasts' },
  { id: 'com.roblox.client', name: 'Roblox / Mobile Games', category: 'Gaming' },
];

// Local storage key constants
const KEYS = {
  ROUTINES: 'flowstudy_routines',
  TASKS: 'flowstudy_tasks',
  LOGS: 'flowstudy_session_logs',
  SETTINGS: 'flowstudy_settings',
  FOCUS_CONFIG: 'flowstudy_focus_config',
  WALLPAPER_CONFIG: 'flowstudy_wallpaper_config',
  THEME_CONFIG: 'flowstudy_theme_config',
  AUTO_FLOW_CONFIG: 'flowstudy_auto_flow_config',
  SOUND_CONFIG: 'flowstudy_sound_config',
  BLOCK_CONFIG: 'flowstudy_block_config',
  TIMER_STATE: 'flowstudy_timer_state',
};

export function loadStored<T>(key: string, defaultValue: T): T {
  try {
    const raw = localStorage.getItem(key);
    if (!raw) return defaultValue;
    return JSON.parse(raw);
  } catch {
    return defaultValue;
  }
}

export function saveStored<T>(key: string, value: T): void {
  try {
    localStorage.setItem(key, JSON.stringify(value));
  } catch (e) {
    console.error('Failed to save to localStorage:', e);
  }
}

export { KEYS };
