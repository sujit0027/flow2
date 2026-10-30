import React, { useState, useEffect, useMemo, useRef } from 'react';
import {
  ArrowRight,
  Home,
  CheckCircle2,
  Clock,
  Bell,
  Star,
  Flame,
  VolumeX,
} from 'lucide-react';
import { useFlowStudy } from '../context/FlowStudyContext';
import { CATEGORY_CONFIG, RoutineTask } from '../types';
import { formatDurationHMS, formatMinutesHuman } from '../utils/storage';
import { startWakeAlarm, stopWakeAlarm } from '../utils/audio';


interface PostTaskChoiceModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const PostTaskChoiceModal: React.FC<PostTaskChoiceModalProps> = ({
  isOpen,
  onClose,
}) => {
  const {
    timerSnapshot,
    tasks,
    startUpcomingTaskFromModal,
    finishTaskAndReturnHome,
    silenceWakeAlarm,
  } = useFlowStudy();

  const [secondsLeft, setSecondsLeft] = useState(30);
  const [alarmSounding, setAlarmSounding] = useState(false);
  const autoStartedRef = useRef(false);

  // Preserve finished task info even across state transitions
  const [cachedTaskId, setCachedTaskId] = useState<number>(0);
  const [cachedOvertime, setCachedOvertime] = useState<number>(0);

  useEffect(() => {
    if (isOpen && timerSnapshot.taskId !== 0) {
      setCachedTaskId(timerSnapshot.taskId);
      setCachedOvertime(timerSnapshot.flowOvertimeSeconds);
    }
  }, [isOpen, timerSnapshot.taskId, timerSnapshot.flowOvertimeSeconds]);

  const activeTaskId = timerSnapshot.taskId !== 0 ? timerSnapshot.taskId : cachedTaskId;
  const flowOvertime = timerSnapshot.taskId !== 0 ? timerSnapshot.flowOvertimeSeconds : cachedOvertime;
  const finishedTask = tasks.find((t) => t.id === activeTaskId);

  // Compute eligible upcoming tasks from the user's active routine:
  // Shows all non-compulsory tasks up to and INCLUDING the next compulsory task
  const { eligibleTasks, nextCompulsoryTask } = useMemo(() => {
    if (!activeTaskId || tasks.length === 0) {
      return { eligibleTasks: [] as RoutineTask[], nextCompulsoryTask: null as RoutineTask | null };
    }

    const currentIndex = tasks.findIndex((t) => t.id === activeTaskId);
    let upcoming = tasks.slice(currentIndex + 1).filter((t) => !t.isCompleted);

    // If reached end of list, check if any uncompleted tasks exist earlier in routine
    if (upcoming.length === 0) {
      upcoming = tasks.filter((t) => t.id !== activeTaskId && !t.isCompleted);
    }

    const nextCompulsoryIdx = upcoming.findIndex((t) => t.isCompulsory);
    let eligible: RoutineTask[] = [];
    let compTask: RoutineTask | null = null;

    if (nextCompulsoryIdx !== -1) {
      eligible = upcoming.slice(0, nextCompulsoryIdx + 1);
      compTask = upcoming[nextCompulsoryIdx];
    } else {
      eligible = upcoming;
      compTask = upcoming[0] || null;
    }

    return { eligibleTasks: eligible, nextCompulsoryTask: compTask };
  }, [tasks, activeTaskId]);

  // Keep references to latest tasks and callbacks so timer interval NEVER re-creates
  const nextCompulsoryRef = useRef(nextCompulsoryTask);
  nextCompulsoryRef.current = nextCompulsoryTask;

  const eligibleTasksRef = useRef(eligibleTasks);
  eligibleTasksRef.current = eligibleTasks;

  const startUpcomingTaskRef = useRef(startUpcomingTaskFromModal);
  startUpcomingTaskRef.current = startUpcomingTaskFromModal;

  const finishHomeRef = useRef(finishTaskAndReturnHome);
  finishHomeRef.current = finishTaskAndReturnHome;

  // Single reliable countdown effect driven by target timestamp
  useEffect(() => {
    if (!isOpen) {
      stopWakeAlarm();
      setAlarmSounding(false);
      autoStartedRef.current = false;
      return;
    }

    setSecondsLeft(30);
    setAlarmSounding(false);
    autoStartedRef.current = false;

    // Fixed target: exactly 30 seconds from now
    const targetEndTime = Date.now() + 30000;

    const intervalId = window.setInterval(() => {
      const now = Date.now();
      const remainingMs = targetEndTime - now;
      const remainingSecs = Math.max(0, Math.ceil(remainingMs / 1000));

      setSecondsLeft(remainingSecs);

      if (remainingSecs <= 0) {
        window.clearInterval(intervalId);

        if (!autoStartedRef.current) {
          autoStartedRef.current = true;
          startWakeAlarm();
          setAlarmSounding(true);

          const target = nextCompulsoryRef.current || eligibleTasksRef.current[0];
          if (target) {
            // Keep alarm ringing when auto-started so sleeping user is alerted
            startUpcomingTaskRef.current(target, true);
          } else {
            finishHomeRef.current();
          }
        }
      }
    }, 250);

    return () => {
      window.clearInterval(intervalId);
    };
  }, [isOpen]);

  if (!isOpen || activeTaskId === 0) return null;

  const handleSelectTask = (task: RoutineTask) => {
    stopWakeAlarm();
    silenceWakeAlarm();
    setAlarmSounding(false);
    onClose();
    startUpcomingTaskFromModal(task, false);
  };

  const handleReturnHome = () => {
    stopWakeAlarm();
    silenceWakeAlarm();
    setAlarmSounding(false);
    onClose();
    finishTaskAndReturnHome();
  };

  const handleSilenceAlarm = () => {
    stopWakeAlarm();
    silenceWakeAlarm();
    setAlarmSounding(false);
  };

  const progressPercentage = Math.max(0, Math.min(100, (secondsLeft / 30) * 100));

  return (
    <div
      data-testid="post_task_modal_backdrop"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-md animate-in fade-in duration-200"
    >
      <div
        data-testid="post_task_selection_dialog"
        className="w-full max-w-md rounded-3xl bg-[#131916] border border-[#25332B] p-5 sm:p-6 shadow-2xl space-y-4 text-left max-h-[90vh] overflow-y-auto"
      >

        {/* Wake Alarm Sounding Banner */}
        {alarmSounding && (
          <div className="p-3 rounded-2xl bg-[#EF4444]/25 border-2 border-[#EF4444] animate-pulse flex items-center justify-between shadow-xl">
            <div className="flex items-center space-x-2">
              <Bell className="w-5 h-5 text-[#EF4444] animate-bounce shrink-0" />
              <div>
                <div className="text-xs font-black text-white uppercase tracking-wider">
                  ⏰ WAKE UP ALARM SOUNDED!
                </div>
                <div className="text-[11px] text-[#FCA5A5]">
                  Auto-starting next compulsory task...
                </div>
              </div>
            </div>
            <button
              onClick={handleSilenceAlarm}
              className="p-1.5 rounded-lg bg-[#EF4444] text-white hover:bg-[#DC2626] transition-colors cursor-pointer"
              title="Silence Alarm"
            >
              <VolumeX className="w-4 h-4" />
            </button>
          </div>
        )}

        {/* Header: Task Completion Celebration */}
        <div className="space-y-1">
          <div className="flex items-center justify-between">
            <span className="inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-full bg-[#132F23] text-[#34D399] border border-[#10B981]/40 text-[11px] font-bold uppercase tracking-wider">
              <CheckCircle2 className="w-3.5 h-3.5" />
              <span>Task Finished</span>
            </span>

            {flowOvertime > 0 && (
              <span className="inline-flex items-center space-x-1 text-xs font-bold text-[#34D399] font-mono-digits">
                <Flame className="w-3.5 h-3.5 text-[#10B981]" />
                <span>+{formatDurationHMS(flowOvertime)} flow logged</span>
              </span>
            )}
          </div>

          <h3 className="text-xl font-extrabold text-[#F3F7F5] tracking-tight">
            {finishedTask?.title || 'Current Task'}
          </h3>
          <p className="text-xs text-[#98ABA1]">
            Select your next task below, or return to home.
          </p>
        </div>

        {/* 30s Countdown Alert Bar */}
        <div className="p-3 rounded-2xl bg-[#1C2420] border border-[#25332B] space-y-2">
          <div className="flex items-center justify-between text-xs">
            <div className="flex items-center space-x-1.5 text-[#98ABA1]">
              <Clock className="w-3.5 h-3.5 text-[#FBBF24]" />
              <span>Auto-start next compulsory in:</span>
            </div>
            <span className="font-mono-digits font-extrabold text-sm text-[#FBBF24]">
              {secondsLeft}s
            </span>
          </div>

          <div className="w-full bg-[#131916] h-1.5 rounded-full overflow-hidden">
            <div
              className={`h-full transition-all duration-300 ease-linear rounded-full ${
                secondsLeft <= 10 ? 'bg-[#EF4444]' : 'bg-[#10B981]'
              }`}
              style={{ width: `${progressPercentage}%` }}
            />
          </div>

          <p className="text-[10px] text-[#64756B] text-center">
            If inactive for 30s, wake alarm will sound and next compulsory task will auto-start.
          </p>
        </div>

        {/* Next Tasks List (Up to and including next compulsory) */}
        <div className="space-y-2">
          <div className="flex items-center justify-between text-[11px] font-bold text-[#98ABA1] uppercase tracking-wider px-1">
            <span>Upcoming Tasks In Routine</span>
            {nextCompulsoryTask && (
              <span className="text-[#34D399] font-semibold text-[10px]">
                Target: {nextCompulsoryTask.title}
              </span>
            )}
          </div>

          {eligibleTasks.length > 0 ? (
            <div className="space-y-2">
              {eligibleTasks.map((task) => {
                const categoryMeta =
                  CATEGORY_CONFIG[task.category] || CATEGORY_CONFIG.STUDY;
                const isTargetCompulsory = task.id === nextCompulsoryTask?.id;

                return (
                  <button
                    key={task.id}
                    onClick={() => handleSelectTask(task)}
                    data-testid={`select_task_${task.id}`}
                    className={`w-full p-3.5 rounded-2xl border text-left transition-all flex items-center justify-between cursor-pointer group ${
                      isTargetCompulsory
                        ? 'bg-[#132F23]/80 border-[#10B981] hover:bg-[#10B981]/25 hover:border-[#34D399] shadow-md shadow-[#10B981]/10'
                        : 'bg-[#1C2420] border-[#25332B] hover:border-[#38BDF8] hover:bg-[#1C2420]/80'
                    }`}
                  >
                    <div className="space-y-1 min-w-0 pr-3">
                      <div className="flex items-center space-x-1.5 flex-wrap gap-y-1">
                        <span
                          className="px-2 py-0.5 rounded-md text-[10px] font-bold uppercase tracking-wider"
                          style={{
                            backgroundColor: categoryMeta.containerColor,
                            color: categoryMeta.textColor,
                          }}
                        >
                          {categoryMeta.displayName}
                        </span>

                        {task.isCompulsory ? (
                          <span className="inline-flex items-center space-x-0.5 px-2 py-0.5 rounded-md bg-[#132F23] text-[#34D399] border border-[#10B981]/40 text-[10px] font-bold">
                            <Star className="w-2.5 h-2.5 fill-current" />
                            <span>COMPULSORY TARGET</span>
                          </span>
                        ) : (
                          <span className="px-2 py-0.5 rounded-md bg-[#0C2D3E] text-[#7DD3FC] border border-[#38BDF8]/30 text-[10px] font-medium">
                            FLEXIBLE
                          </span>
                        )}

                        {task.subject && (
                          <span className="text-[10px] text-[#98ABA1] font-semibold uppercase">
                            • {task.subject}
                          </span>
                        )}
                      </div>

                      <div className="text-sm font-bold text-[#F3F7F5] truncate group-hover:text-white">
                        {task.title}
                      </div>

                      <div className="text-xs text-[#98ABA1] font-mono-digits">
                        Duration: {formatMinutesHuman(task.minDurationMinutes)}
                      </div>
                    </div>

                    <div className="shrink-0 flex items-center space-x-1">
                      <div
                        className={`w-8 h-8 rounded-xl flex items-center justify-center transition-all ${
                          isTargetCompulsory
                            ? 'bg-[#10B981] text-black group-hover:scale-110'
                            : 'bg-[#25332B] text-[#98ABA1] group-hover:text-white group-hover:bg-[#34D399] group-hover:text-black'
                        }`}
                      >
                        <ArrowRight className="w-4 h-4 stroke-[2.5]" />
                      </div>
                    </div>
                  </button>
                );
              })}
            </div>
          ) : (
            <div className="p-4 rounded-2xl bg-[#1C2420] border border-[#25332B] text-center space-y-1">
              <div className="text-xs font-bold text-[#34D399]">
                🎉 All Routine Tasks Completed!
              </div>
              <p className="text-[11px] text-[#98ABA1]">
                You have completed all planned tasks for today's routine.
              </p>
            </div>
          )}
        </div>

        {/* Return to Home Button */}
        <div className="pt-1">
          <button
            onClick={handleReturnHome}
            data-testid="return_home_button"
            className="w-full py-3 px-4 rounded-2xl bg-[#1C2420] hover:bg-[#25332B] text-[#F3F7F5] hover:text-white border border-[#25332B] hover:border-[#64756B] font-bold text-xs flex items-center justify-center space-x-2 transition-all cursor-pointer"
          >
            <Home className="w-4 h-4 text-[#98ABA1]" />
            <span>Return to Home (Mark Task Complete & Close)</span>
          </button>
        </div>
      </div>
    </div>
  );
};
