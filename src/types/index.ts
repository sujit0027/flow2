export type TaskCategoryType =
  | 'STUDY'
  | 'BREAK'
  | 'MEAL'
  | 'EXERCISE'
  | 'PERSONAL'
  | 'CUSTOM';

export interface CategoryMeta {
  displayName: string;
  defaultIsCompulsory: boolean;
  defaultIsOptional: boolean;
  defaultDurationMinutes: number;
  accentColor: string;
  containerColor: string;
  textColor: string;
}

export const CATEGORY_CONFIG: Record<TaskCategoryType, CategoryMeta> = {
  STUDY: {
    displayName: 'Study',
    defaultIsCompulsory: true,
    defaultIsOptional: false,
    defaultDurationMinutes: 45,
    accentColor: '#10B981', // Emerald
    containerColor: '#113224',
    textColor: '#34D399',
  },
  BREAK: {
    displayName: 'Break',
    defaultIsCompulsory: false,
    defaultIsOptional: true,
    defaultDurationMinutes: 10,
    accentColor: '#38BDF8', // Sky Blue
    containerColor: '#0C2D3E',
    textColor: '#7DD3FC',
  },
  MEAL: {
    displayName: 'Meal',
    defaultIsCompulsory: false,
    defaultIsOptional: true,
    defaultDurationMinutes: 30,
    accentColor: '#FB923C', // Warm Amber
    containerColor: '#3A2111',
    textColor: '#FDBA74',
  },
  EXERCISE: {
    displayName: 'Exercise',
    defaultIsCompulsory: false,
    defaultIsOptional: true,
    defaultDurationMinutes: 20,
    accentColor: '#A855F7', // Violet
    containerColor: '#2B143E',
    textColor: '#D8B4FE',
  },
  PERSONAL: {
    displayName: 'Personal',
    defaultIsCompulsory: false,
    defaultIsOptional: true,
    defaultDurationMinutes: 15,
    accentColor: '#F43F5E', // Rose
    containerColor: '#38101C',
    textColor: '#FDA4AF',
  },
  CUSTOM: {
    displayName: 'Custom',
    defaultIsCompulsory: false,
    defaultIsOptional: true,
    defaultDurationMinutes: 30,
    accentColor: '#34D399', // Mint
    containerColor: '#132F23',
    textColor: '#A7F3D0',
  },
};

export interface RoutineTask {
  id: number;
  routineId: number;
  title: string;
  category: TaskCategoryType;
  customCategoryName?: string;
  subject?: string;
  minDurationMinutes: number;
  extensionTargetMinutes: number;
  isExtensionAllowed: boolean;
  isCompulsory: boolean;
  isOptional: boolean;
  canBeSkipped: boolean;
  taskOrder: number;
  isCompleted: boolean;
  accumulatedDurationSeconds: number;
  notes?: string;
  completionSound?: string;
  mealType?: string; // breakfast, lunch, dinner, snack
  createdAt: number;
  completedAt?: number | null;
}

export interface RoutineEntity {
  id: number;
  name: string;
  description?: string;
  isTemplate?: boolean;
  isSampleData?: boolean;
  isActive: boolean;
  createdAt: number;
}

export interface StudySessionLog {
  sessionId: number;
  taskId: number;
  taskTitle: string;
  category: TaskCategoryType;
  subject: string;
  durationSeconds: number;
  minRequiredSeconds: number;
  extensionSeconds: number;
  wasCompulsory: boolean;
  wasInterrupted: boolean;
  notes?: string;
  distractionCount: number;
  completedTimestamp: number;
}

export interface AppSettings {
  autoAdvanceToNextTask: boolean;
  vibrateOnComplete: boolean;
  keepScreenOn: boolean;
  breaksOptionalDefault: boolean;
  defaultStudyMinDuration: number;
  enforceCompulsoryWarning: boolean;
}

export interface FocusScreenCustomization {
  showTaskTitle: boolean;
  showTimer: boolean;
  showElapsedTime: boolean;
  showMinRemaining: boolean;
  showProgressRing: boolean;
  showSessionLabel: boolean;
  showNextTaskPreview: boolean;
  showMotivationalQuote: boolean;
  showTimerControls: boolean;
  showExtraStats: boolean;
  isUltraMinimal: boolean;
}

export interface WallpaperConfig {
  homeWallpaperType: 'AMOLED' | 'FOREST' | 'MOUNTAIN' | 'SKY' | 'COUNTRYSIDE' | 'GRADIENT' | 'CUSTOM';
  focusWallpaperType: 'AMOLED' | 'FOREST' | 'MOUNTAIN' | 'SKY' | 'COUNTRYSIDE' | 'GRADIENT' | 'CUSTOM';
  customWallpaperUri: string;
  dimLevel: number; // 0 to 1
  useBlur: boolean;
}

export interface AppThemeConfig {
  themeMode: 'DARK' | 'LIGHT' | 'SYSTEM' | 'AMOLED';
  accentColor: 'EMERALD' | 'MINT' | 'TEAL' | 'SKY' | 'AMBER' | 'ROSE';
  timerStyle: 'CIRCULAR' | 'DIGITAL' | 'STOPWATCH' | 'TEXT_ONLY';
  dailyStudyTargetHours: number;
  isOnboardingCompleted: boolean;
}

export interface AutoFlowConfig {
  autoAdvanceCompulsoryOnly: boolean;
  countdownDurationSeconds: number;
  ringAlarmOnCountdownExpiry: boolean;
  autoSkipOptionalBreaksIfIdle: boolean;
  alertOnSkippedTask: boolean;
  notifyNextTaskMinutes: number;
  autoStartNextTask: boolean;
  wakeAlarmDurationSeconds: number; // Duration wake alarm sounds before stopping or auto-action
  transitionAlarmDurationSeconds: number; // Transition alarm ring duration
  wakeAlarmMode: 'ALARM' | 'VIBRATION_ONLY' | 'OVERTIME_AUTO' | 'AUTO_NEXT_COMPULSORY'; // Alarm vs vibration vs silent auto flow vs jump next
  enableWakeAlarm: boolean;
  celebrationWallpaperUri: string;
  celebrationMessage: string;
  celebrationShowQuote: boolean;
}

export interface SoundRingtoneConfig {
  soundEnabled: boolean;
  targetSoundId: string;
  targetSoundUri: string;
  targetSoundName: string;
  nextTaskSoundId: string;
  nextTaskSoundUri: string;
  nextTaskSoundName: string;
  breakSoundId: string;
  breakSoundUri: string;
  breakSoundName: string;
}

export interface AppBlockConfig {
  isBlockerEnabled: boolean;
  blockAllDistractions: boolean;
  blockedPackages: string[];
  strictModeNoExit: boolean;
  allowYouTubeForLectures: boolean;
}

export interface TimerSessionSnapshot {
  taskId: number;
  taskTitle: string;
  subject: string;
  category: TaskCategoryType;
  minDurationSeconds: number;
  extensionTargetSeconds: number;
  isCompulsory: boolean;
  isRunning: boolean;
  accumulatedActiveSeconds: number;
  currentElapsedSeconds: number;
  isMinCompleted: boolean;
  remainingCompulsorySeconds: number;
  flowOvertimeSeconds: number;
  stintStartTimestamp?: number;
  isAwaitingContinue?: boolean;
  awaitingContinueSecondsLeft?: number;
  isWakeAlarmRinging?: boolean;
  hasContinuedIntoFlow?: boolean;
}


export interface TaskTransitionCountdownState {
  finishedTaskId: number;
  finishedTaskTitle: string;
  intermediateTasks: RoutineTask[];
  optionalNextTask?: RoutineTask;
  nextCompulsoryTask: RoutineTask;
  totalCountdownSeconds: number;
  remainingSeconds: number;
  isRunning: boolean;
}

export type PostTaskChoice =
  | 'ADVANCE_NEXT_TASK'
  | 'TAKE_BREAK'
  | 'HAVE_MEAL'
  | 'CONTINUE_STUDYING'
  | 'JUST_FINISH';

export type NavigationTab = 'HOME' | 'ROUTINE' | 'PROGRESS' | 'SETTINGS';
