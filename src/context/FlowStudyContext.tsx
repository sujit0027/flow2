import React, { createContext, useContext, useState, useEffect, useCallback, useRef } from 'react';
import confetti from 'canvas-confetti';
import {
  AppSettings,
  AppThemeConfig,
  AutoFlowConfig,
  FocusScreenCustomization,
  NavigationTab,
  PostTaskChoice,
  RoutineEntity,
  RoutineTask,
  SoundRingtoneConfig,
  StudySessionLog,
  TaskTransitionCountdownState,
  TimerSessionSnapshot,
  WallpaperConfig,
  AppBlockConfig,
} from '../types';
import {
  DEFAULT_APP_SETTINGS,
  DEFAULT_AUTO_FLOW_CONFIG,
  DEFAULT_FOCUS_CUSTOMIZATION,
  DEFAULT_SAMPLE_ROUTINE,
  DEFAULT_SAMPLE_TASKS,
  DEFAULT_SOUND_CONFIG,
  DEFAULT_THEME_CONFIG,
  DEFAULT_WALLPAPER_CONFIG,
  DEFAULT_APP_BLOCK_CONFIG,
  PRESET_ROUTINES,
  KEYS,
  loadStored,
  saveStored,
} from '../utils/storage';
import { playEventSound, playSynthesizedTone, triggerVibration } from '../utils/audio';

interface FlowStudyContextType {
  // Navigation & tabs
  currentTab: NavigationTab;
  setCurrentTab: (tab: NavigationTab) => void;

  // Data
  routines: RoutineEntity[];
  activeRoutine: RoutineEntity | null;
  tasks: RoutineTask[];
  allLogs: StudySessionLog[];
  todayLogs: StudySessionLog[];

  // Configs
  settings: AppSettings;
  focusCustomization: FocusScreenCustomization;
  wallpaperConfig: WallpaperConfig;
  themeConfig: AppThemeConfig;
  autoFlowConfig: AutoFlowConfig;
  soundConfig: SoundRingtoneConfig;
  appBlockConfig: AppBlockConfig;

  // Active Timer
  timerSnapshot: TimerSessionSnapshot;
  isTimerScreenOpen: boolean;
  setIsTimerScreenOpen: (open: boolean) => void;
  isMiniPlayerHidden: boolean;
  setIsMiniPlayerHidden: (hidden: boolean) => void;

  // Transition & choice modals
  transitionCountdownState: TaskTransitionCountdownState | null;
  showPostTaskModal: boolean;
  setShowPostTaskModal: (show: boolean) => void;

  // Task edit sheet
  taskBeingEdited: RoutineTask | null;
  isEditModalOpen: boolean;
  openCreateTask: () => void;
  openEditTask: (task: RoutineTask) => void;
  closeEditModal: () => void;
  saveTask: (task: RoutineTask) => void;
  duplicateTask: (task: RoutineTask) => void;
  deleteTask: (task: RoutineTask) => void;
  moveTaskUp: (task: RoutineTask) => void;
  moveTaskDown: (task: RoutineTask) => void;

  // Timer actions
  startFocusSession: (task: RoutineTask) => void;
  togglePlayPause: () => void;
  addTimerSeconds: (seconds: number) => void;
  finishCurrentTask: (choice?: PostTaskChoice) => void;
  emergencyStopSession: () => void;
  skipCurrentTask: () => void;
  closeActiveTimerScreen: () => void;
  minimizeTimerScreen: (hideCompletely?: boolean) => void;

  // Routine & task actions
  toggleTaskComplete: (task: RoutineTask) => void;
  resetTodayCompletions: () => void;
  carryForwardUnfinished: () => void;
  loadPreset: (presetName: string) => void;
  restoreSampleRoutine: () => void;

  // Transition options
  onTransitionTakeBreak: (task: RoutineTask) => void;
  onTransitionExtendPrevious: (taskId: number) => void;
  onTransitionStartCompulsory: (task: RoutineTask) => void;
  onTransitionCancelPause: () => void;

  // Settings & update functions
  updateSettings: (newSettings: AppSettings) => void;
  updateFocusCustomization: (newConfig: FocusScreenCustomization) => void;
  updateWallpaperConfig: (newConfig: WallpaperConfig) => void;
  updateThemeConfig: (newConfig: AppThemeConfig) => void;
  updateAutoFlowConfig: (newConfig: AutoFlowConfig) => void;
  updateSoundConfig: (newConfig: SoundRingtoneConfig) => void;
  updateAppBlockConfig: (newConfig: AppBlockConfig) => void;

  // Backups & export
  exportBackupJson: () => string;
  restoreBackupJson: (json: string) => boolean;
  exportLogsCsv: () => string;
  clearAllLogs: () => void;
  resetAllData: () => void;
  completeOnboarding: (timerStyle: AppThemeConfig['timerStyle'], dailyHours: number) => void;
}

const FlowStudyContext = createContext<FlowStudyContextType | undefined>(undefined);

const EMPTY_SNAPSHOT: TimerSessionSnapshot = {
  taskId: 0,
  taskTitle: '',
  subject: '',
  category: 'STUDY',
  minDurationSeconds: 0,
  extensionTargetSeconds: 0,
  isCompulsory: true,
  isRunning: false,
  accumulatedActiveSeconds: 0,
  currentElapsedSeconds: 0,
  isMinCompleted: false,
  remainingCompulsorySeconds: 0,
  flowOvertimeSeconds: 0,
};

export const FlowStudyProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [currentTab, setCurrentTab] = useState<NavigationTab>('HOME');

  // Stored state
  const [routines, setRoutines] = useState<RoutineEntity[]>(() =>
    loadStored<RoutineEntity[]>(KEYS.ROUTINES, [DEFAULT_SAMPLE_ROUTINE])
  );
  const [tasks, setTasks] = useState<RoutineTask[]>(() =>
    loadStored<RoutineTask[]>(KEYS.TASKS, DEFAULT_SAMPLE_TASKS)
  );
  const [allLogs, setAllLogs] = useState<StudySessionLog[]>(() =>
    loadStored<StudySessionLog[]>(KEYS.LOGS, [])
  );
  const [settings, setSettings] = useState<AppSettings>(() =>
    loadStored<AppSettings>(KEYS.SETTINGS, DEFAULT_APP_SETTINGS)
  );
  const [focusCustomization, setFocusCustomization] = useState<FocusScreenCustomization>(() =>
    loadStored<FocusScreenCustomization>(KEYS.FOCUS_CONFIG, DEFAULT_FOCUS_CUSTOMIZATION)
  );
  const [wallpaperConfig, setWallpaperConfig] = useState<WallpaperConfig>(() =>
    loadStored<WallpaperConfig>(KEYS.WALLPAPER_CONFIG, DEFAULT_WALLPAPER_CONFIG)
  );
  const [themeConfig, setThemeConfig] = useState<AppThemeConfig>(() =>
    loadStored<AppThemeConfig>(KEYS.THEME_CONFIG, DEFAULT_THEME_CONFIG)
  );
  const [autoFlowConfig, setAutoFlowConfig] = useState<AutoFlowConfig>(() =>
    loadStored<AutoFlowConfig>(KEYS.AUTO_FLOW_CONFIG, DEFAULT_AUTO_FLOW_CONFIG)
  );
  const [soundConfig, setSoundConfig] = useState<SoundRingtoneConfig>(() =>
    loadStored<SoundRingtoneConfig>(KEYS.SOUND_CONFIG, DEFAULT_SOUND_CONFIG)
  );
  const [appBlockConfig, setAppBlockConfig] = useState<AppBlockConfig>(() =>
    loadStored<AppBlockConfig>(KEYS.BLOCK_CONFIG, DEFAULT_APP_BLOCK_CONFIG)
  );

  // Active Timer state
  const [timerSnapshot, setTimerSnapshot] = useState<TimerSessionSnapshot>(() =>
    loadStored<TimerSessionSnapshot>(KEYS.TIMER_STATE, EMPTY_SNAPSHOT)
  );
  const [isTimerScreenOpen, setIsTimerScreenOpen] = useState(false);
  const [isMiniPlayerHidden, setIsMiniPlayerHidden] = useState(false);

  // Edit Sheet State
  const [taskBeingEdited, setTaskBeingEdited] = useState<RoutineTask | null>(null);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);

  // Transition modal state
  const [transitionCountdownState, setTransitionCountdownState] =
    useState<TaskTransitionCountdownState | null>(null);

  // Post task choices modal
  const [showPostTaskModal, setShowPostTaskModal] = useState(false);

  // Keep screen wake lock if supported
  const wakeLockRef = useRef<WakeLockSentinel | null>(null);

  // Active Routine
  const activeRoutine = routines.find((r) => r.isActive) || routines[0] || null;

  // Filter Today's logs
  const todayLogs = React.useMemo(() => {
    const startOfToday = new Date();
    startOfToday.setHours(0, 0, 0, 0);
    const startTimestamp = startOfToday.getTime();
    return allLogs.filter((log) => log.completedTimestamp >= startTimestamp);
  }, [allLogs]);

  // Sync to localStorage
  useEffect(() => {
    saveStored(KEYS.ROUTINES, routines);
  }, [routines]);

  useEffect(() => {
    saveStored(KEYS.TASKS, tasks);
  }, [tasks]);

  useEffect(() => {
    saveStored(KEYS.LOGS, allLogs);
  }, [allLogs]);

  useEffect(() => {
    saveStored(KEYS.SETTINGS, settings);
  }, [settings]);

  useEffect(() => {
    saveStored(KEYS.FOCUS_CONFIG, focusCustomization);
  }, [focusCustomization]);

  useEffect(() => {
    saveStored(KEYS.WALLPAPER_CONFIG, wallpaperConfig);
  }, [wallpaperConfig]);

  useEffect(() => {
    saveStored(KEYS.THEME_CONFIG, themeConfig);
  }, [themeConfig]);

  useEffect(() => {
    saveStored(KEYS.AUTO_FLOW_CONFIG, autoFlowConfig);
  }, [autoFlowConfig]);

  useEffect(() => {
    saveStored(KEYS.SOUND_CONFIG, soundConfig);
  }, [soundConfig]);

  useEffect(() => {
    saveStored(KEYS.BLOCK_CONFIG, appBlockConfig);
  }, [appBlockConfig]);

  useEffect(() => {
    saveStored(KEYS.TIMER_STATE, timerSnapshot);
  }, [timerSnapshot]);

  // Screen awake handling
  useEffect(() => {
    if (timerSnapshot.isRunning && settings.keepScreenOn && 'wakeLock' in navigator) {
      navigator.wakeLock.request('screen').then((sentinel) => {
        wakeLockRef.current = sentinel;
      }).catch(() => {});
    } else {
      if (wakeLockRef.current) {
        wakeLockRef.current.release().catch(() => {});
        wakeLockRef.current = null;
      }
    }
  }, [timerSnapshot.isRunning, settings.keepScreenOn]);

  // Timer Tick Engine
  useEffect(() => {
    if (!timerSnapshot.isRunning || timerSnapshot.taskId === 0) return;

    const interval = setInterval(() => {
      setTimerSnapshot((prev) => {
        if (!prev.isRunning || prev.taskId === 0) return prev;

        const now = Date.now();
        const stintStart = prev.stintStartTimestamp || now;
        const currentStintSeconds = Math.floor((now - stintStart) / 1000);
        const newElapsed = prev.accumulatedActiveSeconds + currentStintSeconds;

        const isMinCompleted = newElapsed >= prev.minDurationSeconds && prev.minDurationSeconds > 0;
        const remainingCompulsory = Math.max(0, prev.minDurationSeconds - newElapsed);
        const flowOvertime = Math.max(0, newElapsed - prev.minDurationSeconds);

        // Check if just reached minimum
        if (!prev.isMinCompleted && isMinCompleted) {
          playEventSound('TARGET_REACHED', soundConfig);
          if (settings.vibrateOnComplete) {
            triggerVibration([250, 100, 250]);
          }
          confetti({
            particleCount: 40,
            spread: 60,
            origin: { y: 0.6 },
            colors: ['#10B981', '#34D399', '#38BDF8'],
          });
        }

        return {
          ...prev,
          currentElapsedSeconds: newElapsed,
          isMinCompleted,
          remainingCompulsorySeconds: remainingCompulsory,
          flowOvertimeSeconds: flowOvertime,
        };
      });
    }, 1000);

    return () => clearInterval(interval);
  }, [timerSnapshot.isRunning, timerSnapshot.taskId, settings.vibrateOnComplete, soundConfig]);

  // Transition Countdown Tick Engine
  useEffect(() => {
    if (!transitionCountdownState || !transitionCountdownState.isRunning) return;

    const interval = setInterval(() => {
      setTransitionCountdownState((prev) => {
        if (!prev || !prev.isRunning) return null;
        if (prev.remainingSeconds <= 1) {
          // Alarm expiry!
          playSynthesizedTone('digital_alarm');
          triggerVibration([400, 200, 400]);

          // Auto-start next compulsory task
          startFocusSession(prev.nextCompulsoryTask);
          return null;
        }
        return {
          ...prev,
          remainingSeconds: prev.remainingSeconds - 1,
        };
      });
    }, 1000);

    return () => clearInterval(interval);
  }, [transitionCountdownState]);

  // Start focus session
  const startFocusSession = useCallback((task: RoutineTask) => {
    const minSecs = task.minDurationMinutes * 60;
    const extSecs = task.extensionTargetMinutes * 60;
    const accumulated = task.accumulatedDurationSeconds || 0;

    const snapshot: TimerSessionSnapshot = {
      taskId: task.id,
      taskTitle: task.title,
      subject: task.subject || '',
      category: task.category,
      minDurationSeconds: minSecs,
      extensionTargetSeconds: extSecs,
      isCompulsory: task.isCompulsory,
      isRunning: true,
      accumulatedActiveSeconds: accumulated,
      currentElapsedSeconds: accumulated,
      isMinCompleted: accumulated >= minSecs && minSecs > 0,
      remainingCompulsorySeconds: Math.max(0, minSecs - accumulated),
      flowOvertimeSeconds: Math.max(0, accumulated - minSecs),
      stintStartTimestamp: Date.now(),
    };

    setTimerSnapshot(snapshot);
    setIsTimerScreenOpen(true);
    setIsMiniPlayerHidden(false);
    setTransitionCountdownState(null);
  }, []);

  const togglePlayPause = useCallback(() => {
    setTimerSnapshot((prev) => {
      if (prev.taskId === 0) return prev;
      if (prev.isRunning) {
        // Pause: finalize accumulatedActiveSeconds
        return {
          ...prev,
          isRunning: false,
          accumulatedActiveSeconds: prev.currentElapsedSeconds,
          stintStartTimestamp: undefined,
        };
      } else {
        // Resume
        return {
          ...prev,
          isRunning: true,
          stintStartTimestamp: Date.now(),
        };
      }
    });
  }, []);

  const addTimerSeconds = useCallback((seconds: number) => {
    setTimerSnapshot((prev) => ({
      ...prev,
      minDurationSeconds: prev.minDurationSeconds + seconds,
      extensionTargetSeconds: prev.extensionTargetSeconds + seconds,
      remainingCompulsorySeconds: Math.max(0, prev.minDurationSeconds + seconds - prev.currentElapsedSeconds),
    }));
  }, []);

  const closeActiveTimerScreen = useCallback(() => {
    setIsTimerScreenOpen(false);
  }, []);

  const minimizeTimerScreen = useCallback((hideCompletely: boolean = false) => {
    setIsTimerScreenOpen(false);
    if (hideCompletely) {
      setIsMiniPlayerHidden(true);
    }
  }, []);

  // Complete & log a session
  const logSession = useCallback(
    (
      taskId: number,
      actualDurationSeconds: number,
      wasInterrupted: boolean,
      notes: string = '',
      distractionCount: number = 0
    ) => {
      const task = tasks.find((t) => t.id === taskId);
      if (!task) return;

      const minSeconds = task.minDurationMinutes * 60;
      const extSeconds = Math.max(0, actualDurationSeconds - minSeconds);
      const isMinCompleted = actualDurationSeconds >= minSeconds;
      const shouldMarkCompleted = !wasInterrupted && (isMinCompleted || !task.isCompulsory);

      // Update task
      setTasks((prev) =>
        prev.map((t) =>
          t.id === taskId
            ? {
                ...t,
                accumulatedDurationSeconds: actualDurationSeconds,
                isCompleted: shouldMarkCompleted ? true : t.isCompleted,
                completedAt: shouldMarkCompleted ? Date.now() : t.completedAt,
              }
            : t
        )
      );

      // Add log
      if (actualDurationSeconds > 0) {
        const newLog: StudySessionLog = {
          sessionId: Date.now(),
          taskId: task.id,
          taskTitle: task.title,
          category: task.category,
          subject: task.subject || '',
          durationSeconds: actualDurationSeconds,
          minRequiredSeconds: minSeconds,
          extensionSeconds: extSeconds,
          wasCompulsory: task.isCompulsory,
          wasInterrupted,
          notes: notes || task.notes || '',
          distractionCount,
          completedTimestamp: Date.now(),
        };

        setAllLogs((prev) => [newLog, ...prev]);
      }
    },
    [tasks]
  );

  // Finish current task with user choice
  const finishCurrentTask = useCallback(
    (choice: PostTaskChoice = 'JUST_FINISH') => {
      const currentTaskId = timerSnapshot.taskId;
      const actualElapsed = timerSnapshot.currentElapsedSeconds;
      const task = tasks.find((t) => t.id === currentTaskId);

      if (!task) {
        setTimerSnapshot(EMPTY_SNAPSHOT);
        setIsTimerScreenOpen(false);
        return;
      }

      logSession(currentTaskId, actualElapsed, false);
      setTimerSnapshot(EMPTY_SNAPSHOT);
      setIsTimerScreenOpen(false);

      if (choice === 'CONTINUE_STUDYING') {
        startFocusSession(task);
        return;
      }

      if (choice === 'JUST_FINISH') {
        return;
      }

      // Check upcoming tasks
      const remainingTasks = tasks.filter((t) => t.id !== currentTaskId && !t.isCompleted);
      const nextCompulsory = remainingTasks.find((t) => t.isCompulsory);
      const intermediate = nextCompulsory
        ? remainingTasks.slice(0, remainingTasks.indexOf(nextCompulsory))
        : remainingTasks;

      if (choice === 'ADVANCE_NEXT_TASK') {
        const immediateNext = remainingTasks[0];
        if (immediateNext) {
          startFocusSession(immediateNext);
        }
      } else if (choice === 'TAKE_BREAK') {
        const breakTask = remainingTasks.find((t) => t.category === 'BREAK');
        if (breakTask) {
          startFocusSession(breakTask);
        } else {
          // Create instant 10m break task
          const instantBreak: RoutineTask = {
            id: Date.now(),
            routineId: 1,
            title: 'Flexible Study Break',
            category: 'BREAK',
            minDurationMinutes: 10,
            extensionTargetMinutes: 5,
            isExtensionAllowed: true,
            isCompulsory: false,
            isOptional: true,
            canBeSkipped: true,
            taskOrder: tasks.length + 1,
            isCompleted: false,
            accumulatedDurationSeconds: 0,
            createdAt: Date.now(),
          };
          setTasks((prev) => [...prev, instantBreak]);
          startFocusSession(instantBreak);
        }
      } else if (choice === 'HAVE_MEAL') {
        const mealTask = remainingTasks.find((t) => t.category === 'MEAL');
        if (mealTask) {
          startFocusSession(mealTask);
        } else {
          const instantMeal: RoutineTask = {
            id: Date.now(),
            routineId: 1,
            title: 'Nutritious Meal',
            category: 'MEAL',
            minDurationMinutes: 30,
            extensionTargetMinutes: 15,
            isExtensionAllowed: true,
            isCompulsory: false,
            isOptional: true,
            canBeSkipped: true,
            mealType: 'meal',
            taskOrder: tasks.length + 1,
            isCompleted: false,
            accumulatedDurationSeconds: 0,
            createdAt: Date.now(),
          };
          setTasks((prev) => [...prev, instantMeal]);
          startFocusSession(instantMeal);
        }
      }

      // Trigger 30s auto-flow transition countdown if next compulsory exists and autoAdvance is set
      if (nextCompulsory && autoFlowConfig.autoAdvanceCompulsoryOnly) {
        setTransitionCountdownState({
          finishedTaskId: currentTaskId,
          finishedTaskTitle: task.title,
          intermediateTasks: intermediate,
          optionalNextTask: intermediate[0],
          nextCompulsoryTask: nextCompulsory,
          totalCountdownSeconds: autoFlowConfig.countdownDurationSeconds || 30,
          remainingSeconds: autoFlowConfig.countdownDurationSeconds || 30,
          isRunning: true,
        });
      }
    },
    [timerSnapshot, tasks, logSession, startFocusSession, autoFlowConfig]
  );

  const emergencyStopSession = useCallback(() => {
    const currentTaskId = timerSnapshot.taskId;
    const actualElapsed = timerSnapshot.currentElapsedSeconds;

    logSession(currentTaskId, actualElapsed, true);
    setTimerSnapshot(EMPTY_SNAPSHOT);
    setIsTimerScreenOpen(false);
  }, [timerSnapshot, logSession]);

  const skipCurrentTask = useCallback(() => {
    const currentTaskId = timerSnapshot.taskId;
    const task = tasks.find((t) => t.id === currentTaskId);
    if (task) {
      setTasks((prev) =>
        prev.map((t) => (t.id === currentTaskId ? { ...t, isCompleted: true } : t))
      );
    }
    setTimerSnapshot(EMPTY_SNAPSHOT);
    setIsTimerScreenOpen(false);
  }, [timerSnapshot, tasks]);

  const toggleTaskComplete = useCallback(
    (task: RoutineTask) => {
      const newStatus = !task.isCompleted;
      setTasks((prev) =>
        prev.map((t) =>
          t.id === task.id
            ? { ...t, isCompleted: newStatus, completedAt: newStatus ? Date.now() : null }
            : t
        )
      );

      if (newStatus && task.accumulatedDurationSeconds > 0) {
        logSession(task.id, task.accumulatedDurationSeconds, false);
      }
    },
    [logSession]
  );

  // Transition handlers
  const onTransitionTakeBreak = useCallback(
    (task: RoutineTask) => {
      setTransitionCountdownState(null);
      startFocusSession(task);
    },
    [startFocusSession]
  );

  const onTransitionExtendPrevious = useCallback(
    (taskId: number) => {
      setTransitionCountdownState(null);
      const task = tasks.find((t) => t.id === taskId);
      if (task) {
        startFocusSession({
          ...task,
          minDurationMinutes: task.minDurationMinutes + 15,
        });
      }
    },
    [tasks, startFocusSession]
  );

  const onTransitionStartCompulsory = useCallback(
    (task: RoutineTask) => {
      setTransitionCountdownState(null);
      startFocusSession(task);
    },
    [startFocusSession]
  );

  const onTransitionCancelPause = useCallback(() => {
    setTransitionCountdownState(null);
  }, []);

  // Task edit operations
  const openCreateTask = useCallback(() => {
    setTaskBeingEdited(null);
    setIsEditModalOpen(true);
  }, []);

  const openEditTask = useCallback((task: RoutineTask) => {
    setTaskBeingEdited(task);
    setIsEditModalOpen(true);
  }, []);

  const closeEditModal = useCallback(() => {
    setTaskBeingEdited(null);
    setIsEditModalOpen(false);
  }, []);

  const saveTask = useCallback((task: RoutineTask) => {
    setTasks((prev) => {
      const exists = prev.some((t) => t.id === task.id);
      if (exists) {
        return prev.map((t) => (t.id === task.id ? task : t));
      } else {
        const nextOrder = prev.length + 1;
        return [...prev, { ...task, id: Date.now(), taskOrder: nextOrder }];
      }
    });
  }, []);

  const duplicateTask = useCallback((task: RoutineTask) => {
    setTasks((prev) => {
      const nextOrder = prev.length + 1;
      const copy: RoutineTask = {
        ...task,
        id: Date.now(),
        title: `${task.title} (Copy)`,
        taskOrder: nextOrder,
        isCompleted: false,
        accumulatedDurationSeconds: 0,
        completedAt: null,
        createdAt: Date.now(),
      };
      return [...prev, copy];
    });
  }, []);

  const deleteTask = useCallback((task: RoutineTask) => {
    setTasks((prev) => {
      const filtered = prev.filter((t) => t.id !== task.id);
      return filtered.map((t, idx) => ({ ...t, taskOrder: idx + 1 }));
    });
  }, []);

  const moveTaskUp = useCallback((task: RoutineTask) => {
    setTasks((prev) => {
      const index = prev.findIndex((t) => t.id === task.id);
      if (index <= 0) return prev;
      const copy = [...prev];
      const temp = copy[index - 1];
      copy[index - 1] = copy[index];
      copy[index] = temp;
      return copy.map((t, idx) => ({ ...t, taskOrder: idx + 1 }));
    });
  }, []);

  const moveTaskDown = useCallback((task: RoutineTask) => {
    setTasks((prev) => {
      const index = prev.findIndex((t) => t.id === task.id);
      if (index === -1 || index >= prev.length - 1) return prev;
      const copy = [...prev];
      const temp = copy[index + 1];
      copy[index + 1] = copy[index];
      copy[index] = temp;
      return copy.map((t, idx) => ({ ...t, taskOrder: idx + 1 }));
    });
  }, []);

  const resetTodayCompletions = useCallback(() => {
    setTasks((prev) =>
      prev.map((t) => ({
        ...t,
        isCompleted: false,
        completedAt: null,
        accumulatedDurationSeconds: 0,
      }))
    );
  }, []);

  const carryForwardUnfinished = useCallback(() => {
    setTasks((prev) => {
      const incomplete = prev.filter((t) => !t.isCompleted);
      const completed = prev.filter((t) => t.isCompleted);
      const combined = [...incomplete, ...completed];
      return combined.map((t, idx) => ({ ...t, taskOrder: idx + 1 }));
    });
  }, []);

  const loadPreset = useCallback((presetName: string) => {
    const presetTasks = PRESET_ROUTINES[presetName];
    if (presetTasks) {
      setTasks(presetTasks.map((t, idx) => ({ ...t, id: Date.now() + idx, taskOrder: idx + 1 })));
    }
  }, []);

  const restoreSampleRoutine = useCallback(() => {
    setTasks(DEFAULT_SAMPLE_TASKS);
    setRoutines([DEFAULT_SAMPLE_ROUTINE]);
  }, []);

  // Backups and export
  const exportBackupJson = useCallback(() => {
    const backup = {
      version: 1,
      appName: 'FlowStudy',
      timestamp: Date.now(),
      routines,
      tasks,
      logs: allLogs,
      settings,
      focusCustomization,
      wallpaperConfig,
      themeConfig,
      autoFlowConfig,
      soundConfig,
      appBlockConfig,
    };
    return JSON.stringify(backup, null, 2);
  }, [
    routines,
    tasks,
    allLogs,
    settings,
    focusCustomization,
    wallpaperConfig,
    themeConfig,
    autoFlowConfig,
    soundConfig,
    appBlockConfig,
  ]);

  const restoreBackupJson = useCallback((jsonString: string): boolean => {
    try {
      const data = JSON.parse(jsonString);
      if (data && Array.isArray(data.tasks)) {
        if (data.routines) setRoutines(data.routines);
        setTasks(data.tasks);
        if (data.logs) setAllLogs(data.logs);
        if (data.settings) setSettings(data.settings);
        if (data.focusCustomization) setFocusCustomization(data.focusCustomization);
        if (data.wallpaperConfig) setWallpaperConfig(data.wallpaperConfig);
        if (data.themeConfig) setThemeConfig(data.themeConfig);
        if (data.autoFlowConfig) setAutoFlowConfig(data.autoFlowConfig);
        if (data.soundConfig) setSoundConfig(data.soundConfig);
        if (data.appBlockConfig) setAppBlockConfig(data.appBlockConfig);
        return true;
      }
      return false;
    } catch {
      return false;
    }
  }, []);

  const exportLogsCsv = useCallback((): string => {
    const headers = [
      'SessionID',
      'Date',
      'TaskTitle',
      'Category',
      'Subject',
      'DurationMinutes',
      'DurationSeconds',
      'MinRequiredMinutes',
      'ExtensionMinutes',
      'WasCompulsory',
      'WasInterrupted',
      'Distractions',
      'Notes',
    ];

    const rows = allLogs.map((log) => [
      log.sessionId,
      new Date(log.completedTimestamp).toISOString(),
      `"${log.taskTitle.replace(/"/g, '""')}"`,
      log.category,
      `"${(log.subject || '').replace(/"/g, '""')}"`,
      (log.durationSeconds / 60).toFixed(1),
      log.durationSeconds,
      (log.minRequiredSeconds / 60).toFixed(1),
      (log.extensionSeconds / 60).toFixed(1),
      log.wasCompulsory ? 'Yes' : 'No',
      log.wasInterrupted ? 'Yes' : 'No',
      log.distractionCount,
      `"${(log.notes || '').replace(/"/g, '""')}"`,
    ]);

    return [headers.join(','), ...rows.map((r) => r.join(','))].join('\n');
  }, [allLogs]);

  const clearAllLogs = useCallback(() => {
    setAllLogs([]);
  }, []);

  const resetAllData = useCallback(() => {
    localStorage.clear();
    setRoutines([DEFAULT_SAMPLE_ROUTINE]);
    setTasks(DEFAULT_SAMPLE_TASKS);
    setAllLogs([]);
    setSettings(DEFAULT_APP_SETTINGS);
    setFocusCustomization(DEFAULT_FOCUS_CUSTOMIZATION);
    setWallpaperConfig(DEFAULT_WALLPAPER_CONFIG);
    setThemeConfig(DEFAULT_THEME_CONFIG);
    setAutoFlowConfig(DEFAULT_AUTO_FLOW_CONFIG);
    setSoundConfig(DEFAULT_SOUND_CONFIG);
    setAppBlockConfig(DEFAULT_APP_BLOCK_CONFIG);
    setTimerSnapshot(EMPTY_SNAPSHOT);
  }, []);

  const completeOnboarding = useCallback(
    (timerStyle: AppThemeConfig['timerStyle'], dailyHours: number) => {
      setThemeConfig((prev) => ({
        ...prev,
        timerStyle,
        dailyStudyTargetHours: dailyHours,
        isOnboardingCompleted: true,
      }));
    },
    []
  );

  return (
    <FlowStudyContext.Provider
      value={{
        currentTab,
        setCurrentTab,
        routines,
        activeRoutine,
        tasks,
        allLogs,
        todayLogs,
        settings,
        focusCustomization,
        wallpaperConfig,
        themeConfig,
        autoFlowConfig,
        soundConfig,
        appBlockConfig,
        timerSnapshot,
        isTimerScreenOpen,
        setIsTimerScreenOpen,
        isMiniPlayerHidden,
        setIsMiniPlayerHidden,
        transitionCountdownState,
        showPostTaskModal,
        setShowPostTaskModal,
        taskBeingEdited,
        isEditModalOpen,
        openCreateTask,
        openEditTask,
        closeEditModal,
        saveTask,
        duplicateTask,
        deleteTask,
        moveTaskUp,
        moveTaskDown,
        startFocusSession,
        togglePlayPause,
        addTimerSeconds,
        finishCurrentTask,
        emergencyStopSession,
        skipCurrentTask,
        closeActiveTimerScreen,
        minimizeTimerScreen,
        toggleTaskComplete,
        resetTodayCompletions,
        carryForwardUnfinished,
        loadPreset,
        restoreSampleRoutine,
        onTransitionTakeBreak,
        onTransitionExtendPrevious,
        onTransitionStartCompulsory,
        onTransitionCancelPause,
        updateSettings: setSettings,
        updateFocusCustomization: setFocusCustomization,
        updateWallpaperConfig: setWallpaperConfig,
        updateThemeConfig: setThemeConfig,
        updateAutoFlowConfig: setAutoFlowConfig,
        updateSoundConfig: setSoundConfig,
        updateAppBlockConfig: setAppBlockConfig,
        exportBackupJson,
        restoreBackupJson,
        exportLogsCsv,
        clearAllLogs,
        resetAllData,
        completeOnboarding,
      }}
    >
      {children}
    </FlowStudyContext.Provider>
  );
};

export function useFlowStudy(): FlowStudyContextType {
  const context = useContext(FlowStudyContext);
  if (!context) {
    throw new Error('useFlowStudy must be used within a FlowStudyProvider');
  }
  return context;
}
