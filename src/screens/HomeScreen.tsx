import React, { useState } from 'react';
import {
  Palette,
  Sliders,
  Play,
  Check,
  CheckCircle2,
  Calendar,
} from 'lucide-react';
import { useFlowStudy } from '../context/FlowStudyContext';
import { CATEGORY_CONFIG } from '../types';
import { formatDurationHMS, formatMinutesHuman } from '../utils/storage';

interface HomeScreenProps {
  onOpenWallpaperSettings: () => void;
  onOpenFocusCustomization: () => void;
}

export const HomeScreen: React.FC<HomeScreenProps> = ({
  onOpenWallpaperSettings,
  onOpenFocusCustomization,
}) => {
  const {
    tasks,
    todayLogs,
    activeRoutine,
    themeConfig,
    startFocusSession,
    toggleTaskComplete,
    openCreateTask,
    resetTodayCompletions,
    carryForwardUnfinished,
  } = useFlowStudy();

  const [showResetDialog, setShowResetDialog] = useState(false);
  const [showCarryForwardDialog, setShowCarryForwardDialog] = useState(false);

  const nextPendingTask = tasks.find((t) => !t.isCompleted);
  const subsequentTask = tasks.filter((t) => !t.isCompleted)[1];
  const completedCount = tasks.filter((t) => t.isCompleted).length;
  const totalCount = tasks.length;
  const progressFraction = totalCount > 0 ? completedCount / totalCount : 0;

  const totalLoggedSecondsToday = todayLogs.reduce((acc, log) => acc + log.durationSeconds, 0);
  const dailyTargetSeconds = (themeConfig.dailyStudyTargetHours || 4) * 3600;
  const dailyTargetFraction = dailyTargetSeconds > 0
    ? Math.min(1, totalLoggedSecondsToday / dailyTargetSeconds)
    : 0;

  return (
    <div className="relative z-10 mx-auto max-w-lg px-4 pt-4 pb-28 space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <div className="flex items-center space-x-2">
            <h1 className="text-2xl font-bold tracking-tight text-[#F3F7F5]">FlowStudy</h1>
            {activeRoutine?.isSampleData && (
              <span className="px-1.5 py-0.5 rounded bg-[#132F23] text-[9px] font-bold text-[#34D399] tracking-wider border border-[#10B981]/30">
                SAMPLE
              </span>
            )}
          </div>
          <p className="text-xs text-[#98ABA1] truncate max-w-[200px]">
            {activeRoutine?.name || 'Flexible Daily Study Flow'}
          </p>
        </div>

        <div className="flex items-center space-x-1.5">
          <button
            onClick={onOpenWallpaperSettings}
            data-testid="home_wallpaper_button"
            className="p-2 rounded-xl text-[#98ABA1] hover:text-[#34D399] hover:bg-[#1C2420] transition-colors cursor-pointer"
            title="Wallpapers"
          >
            <Palette className="w-5 h-5" />
          </button>
          <button
            onClick={onOpenFocusCustomization}
            data-testid="home_customization_button"
            className="p-2 rounded-xl text-[#98ABA1] hover:text-[#34D399] hover:bg-[#1C2420] transition-colors cursor-pointer"
            title="Focus Screen Customization"
          >
            <Sliders className="w-5 h-5" />
          </button>
        </div>
      </div>

      {/* Target & Routine Progress Cards */}
      <div className="grid grid-cols-2 gap-3">
        {/* Today's Focus */}
        <div className="p-3.5 rounded-2xl bg-[#131916]/90 border border-[#25332B] backdrop-blur-sm">
          <div className="flex items-center justify-between text-[10px] text-[#64756B] uppercase font-bold tracking-wider">
            <span>Today's Focus</span>
            <span>{themeConfig.dailyStudyTargetHours || 4}h target</span>
          </div>
          <div className="font-mono-digits text-lg font-bold text-[#34D399] mt-1">
            {formatDurationHMS(totalLoggedSecondsToday)}
          </div>
          <div className="w-full bg-[#1C2420] h-1.5 rounded-full overflow-hidden mt-2">
            <div
              className="bg-[#10B981] h-full rounded-full transition-all duration-500"
              style={{ width: `${Math.round(dailyTargetFraction * 100)}%` }}
            />
          </div>
        </div>

        {/* Routine Completion */}
        <div className="p-3.5 rounded-2xl bg-[#131916]/90 border border-[#25332B] backdrop-blur-sm">
          <div className="flex items-center justify-between text-[10px] text-[#64756B] uppercase font-bold tracking-wider">
            <span>Routine</span>
            <span className="text-[#98ABA1]">{completedCount}/{totalCount}</span>
          </div>
          <div className="text-lg font-bold text-[#F3F7F5] mt-1">
            {Math.round(progressFraction * 100)}% Done
          </div>
          <div className="w-full bg-[#1C2420] h-1.5 rounded-full overflow-hidden mt-2">
            <div
              className="bg-[#10B981] h-full rounded-full transition-all duration-500"
              style={{ width: `${Math.round(progressFraction * 100)}%` }}
            />
          </div>
        </div>
      </div>

      {/* Current / Next Task Card (Hero) */}
      {nextPendingTask ? (
        <div
          data-testid="hero_current_task_card"
          className="p-5 rounded-2xl bg-[#131916]/95 border border-[#10B981]/50 shadow-xl backdrop-blur-md space-y-3"
        >
          <div className="flex items-center justify-between">
            <span className="text-[10px] font-bold text-[#34D399] tracking-wider uppercase">
              Active Commitment
            </span>
            <span
              className="px-2 py-0.5 rounded text-[11px] font-semibold"
              style={{
                backgroundColor: CATEGORY_CONFIG[nextPendingTask.category]?.containerColor,
                color: CATEGORY_CONFIG[nextPendingTask.category]?.textColor,
              }}
            >
              {nextPendingTask.category === 'CUSTOM' && nextPendingTask.customCategoryName
                ? nextPendingTask.customCategoryName
                : CATEGORY_CONFIG[nextPendingTask.category]?.displayName}
            </span>
          </div>

          <div>
            <h2 className="text-xl font-bold text-[#F3F7F5]">{nextPendingTask.title}</h2>
            {nextPendingTask.subject && (
              <p className="text-xs text-[#98ABA1] mt-0.5">Subject: {nextPendingTask.subject}</p>
            )}
            {nextPendingTask.notes && (
              <p className="text-xs text-[#98ABA1] mt-1.5 line-clamp-2 leading-relaxed">
                {nextPendingTask.notes}
              </p>
            )}
          </div>

          <div className="flex items-center space-x-2 text-xs">
            <span
              className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider ${
                nextPendingTask.isCompulsory
                  ? 'bg-[#3A2111] text-[#FDBA74]'
                  : 'bg-[#1C2420] text-[#7DD3FC]'
              }`}
            >
              {nextPendingTask.isCompulsory ? 'Compulsory' : 'Flexible'}
            </span>
            <span className="text-[#98ABA1]">
              • Min: {formatMinutesHuman(nextPendingTask.minDurationMinutes)}
            </span>
            {nextPendingTask.extensionTargetMinutes > 0 && (
              <span className="text-[#64756B]">
                • Flow: +{formatMinutesHuman(nextPendingTask.extensionTargetMinutes)}
              </span>
            )}
          </div>

          {/* Up Next preview */}
          {subsequentTask && (
            <div className="px-3 py-2 rounded-lg bg-[#1C2420] text-[11px] text-[#98ABA1] flex items-center justify-between">
              <span className="truncate">Up Next: {subsequentTask.title}</span>
              <span className="shrink-0 text-[#64756B] ml-2">
                {formatMinutesHuman(subsequentTask.minDurationMinutes)}
              </span>
            </div>
          )}

          <button
            onClick={() => startFocusSession(nextPendingTask)}
            data-testid="hero_start_task_button"
            className="w-full py-3 rounded-xl bg-[#10B981] hover:bg-[#34D399] text-black font-bold text-sm flex items-center justify-center space-x-2 shadow-lg transition-all cursor-pointer"
          >
            <Play className="w-4 h-4 fill-current" />
            <span>Start Focus Session</span>
          </button>
        </div>
      ) : tasks.length > 0 ? (
        <div className="p-6 rounded-2xl bg-[#131916]/95 border border-[#10B981] text-center space-y-2 backdrop-blur-md">
          <div className="w-12 h-12 mx-auto rounded-full bg-[#132F23] flex items-center justify-center text-[#34D399]">
            <CheckCircle2 className="w-7 h-7" />
          </div>
          <h3 className="text-lg font-bold text-[#F3F7F5]">Daily Routine Completed!</h3>
          <p className="text-xs text-[#98ABA1] max-w-xs mx-auto">
            All planned flexible study tasks have been fulfilled.
          </p>
        </div>
      ) : null}

      {/* Task Sequence Header & Actions */}
      <div className="flex items-center justify-between pt-2">
        <h3 className="text-base font-bold text-[#F3F7F5]">Flexible Task Sequence</h3>
        <div className="flex items-center space-x-1.5">
          {tasks.some((t) => !t.isCompleted && t.accumulatedDurationSeconds > 0) && (
            <button
              onClick={() => setShowCarryForwardDialog(true)}
              className="px-2.5 py-1 rounded-lg bg-[#1C2420] hover:bg-[#26322C] text-[#34D399] text-[11px] font-semibold border border-[#25332B] transition-colors cursor-pointer"
            >
              Carry Forward
            </button>
          )}

          {completedCount > 0 && (
            <button
              onClick={() => setShowResetDialog(true)}
              className="px-2.5 py-1 rounded-lg bg-[#1C2420] hover:bg-[#26322C] text-[#98ABA1] hover:text-[#F3F7F5] text-[11px] font-semibold border border-[#25332B] transition-colors cursor-pointer"
            >
              Reset Cycle
            </button>
          )}
        </div>
      </div>

      {/* Tasks List */}
      {tasks.length === 0 ? (
        <div className="p-8 text-center rounded-2xl bg-[#131916]/80 border border-[#25332B] space-y-3">
          <Calendar className="w-10 h-10 text-[#64756B] mx-auto" />
          <h4 className="text-sm font-bold text-[#F3F7F5]">No Tasks in Routine</h4>
          <p className="text-xs text-[#98ABA1]">
            Add flexible study milestones or load our student competitive exam routine.
          </p>
          <button
            onClick={openCreateTask}
            className="px-4 py-2 rounded-xl bg-[#10B981] text-black font-bold text-xs cursor-pointer"
          >
            Add Task
          </button>
        </div>
      ) : (
        <div className="space-y-2.5">
          {tasks.map((task) => {
            const meta = CATEGORY_CONFIG[task.category] || CATEGORY_CONFIG.STUDY;
            return (
              <div
                key={task.id}
                data-testid={`timeline_task_item_${task.id}`}
                className={`p-3.5 rounded-2xl border transition-all flex items-center justify-between backdrop-blur-sm ${
                  task.isCompleted
                    ? 'bg-[#131916]/60 border-[#25332B]/50 opacity-70'
                    : 'bg-[#131916]/95 border-[#25332B] hover:border-[#64756B]'
                }`}
              >
                {/* Checkbox */}
                <button
                  type="button"
                  onClick={() => toggleTaskComplete(task)}
                  data-testid={`task_checkbox_${task.id}`}
                  className={`w-5 h-5 rounded-md border flex items-center justify-center shrink-0 mr-3 cursor-pointer transition-colors ${
                    task.isCompleted
                      ? 'bg-[#10B981] border-[#10B981] text-black'
                      : 'border-[#64756B] hover:border-[#98ABA1]'
                  }`}
                >
                  {task.isCompleted && <Check className="w-3.5 h-3.5 stroke-[3]" />}
                </button>

                {/* Details */}
                <div
                  className="flex-1 min-w-0 cursor-pointer pr-2"
                  onClick={() => startFocusSession(task)}
                >
                  <div className="flex items-center space-x-1.5 mb-1">
                    <span
                      className="px-1.5 py-0.2 rounded text-[10px] font-semibold"
                      style={{ backgroundColor: meta.containerColor, color: meta.textColor }}
                    >
                      {meta.displayName}
                    </span>
                    <span
                      className={`px-1.5 py-0.2 rounded text-[9px] font-bold uppercase tracking-wider ${
                        task.isCompulsory
                          ? 'bg-[#3A2111] text-[#FDBA74]'
                          : 'bg-[#1C2420] text-[#7DD3FC]'
                      }`}
                    >
                      {task.isCompulsory ? 'Compulsory' : 'Flexible'}
                    </span>
                  </div>

                  <h4
                    className={`text-sm font-semibold truncate ${
                      task.isCompleted ? 'line-through text-[#64756B]' : 'text-[#F3F7F5]'
                    }`}
                  >
                    {task.title}
                  </h4>

                  {task.subject && (
                    <p className="text-[11px] text-[#98ABA1] truncate">{task.subject}</p>
                  )}

                  <div className="flex items-center space-x-2 text-[11px] mt-1 text-[#64756B]">
                    <span className="font-mono-digits text-[#34D399]">
                      Min {formatMinutesHuman(task.minDurationMinutes)}
                    </span>
                    {task.extensionTargetMinutes > 0 && (
                      <span>• Flow +{formatMinutesHuman(task.extensionTargetMinutes)}</span>
                    )}
                    {task.accumulatedDurationSeconds > 0 && (
                      <span className="font-mono-digits text-[#98ABA1]">
                        • Logged: {formatDurationHMS(task.accumulatedDurationSeconds)}
                      </span>
                    )}
                  </div>
                </div>

                {/* Play button */}
                <button
                  type="button"
                  onClick={() => startFocusSession(task)}
                  data-testid={`task_play_action_${task.id}`}
                  className={`w-9 h-9 rounded-full flex items-center justify-center shrink-0 transition-colors cursor-pointer ${
                    task.isCompleted
                      ? 'bg-[#1C2420] text-[#64756B] hover:text-[#F3F7F5]'
                      : 'bg-[#132F23] text-[#34D399] hover:bg-[#10B981] hover:text-black'
                  }`}
                  title={`Start ${task.title}`}
                >
                  <Play className="w-4 h-4 fill-current ml-0.5" />
                </button>
              </div>
            );
          })}
        </div>
      )}

      {/* Reset Confirmation Dialog */}
      {showResetDialog && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="w-full max-w-sm rounded-2xl bg-[#131916] border border-[#25332B] p-5 shadow-2xl space-y-3">
            <h3 className="text-base font-bold text-[#F3F7F5]">Reset Today's Cycle?</h3>
            <p className="text-xs text-[#98ABA1] leading-relaxed">
              This will mark routine tasks as pending for a fresh cycle while preserving all study
              analytics and logs.
            </p>
            <div className="flex items-center space-x-2 pt-2">
              <button
                onClick={() => setShowResetDialog(false)}
                className="flex-1 py-2 rounded-xl bg-[#1C2420] text-xs font-semibold text-[#98ABA1] cursor-pointer"
              >
                Cancel
              </button>
              <button
                onClick={() => {
                  setShowResetDialog(false);
                  resetTodayCompletions();
                }}
                className="flex-1 py-2 rounded-xl bg-[#10B981] text-xs font-bold text-black cursor-pointer"
              >
                Reset Cycle
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Carry Forward Dialog */}
      {showCarryForwardDialog && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="w-full max-w-sm rounded-2xl bg-[#131916] border border-[#25332B] p-5 shadow-2xl space-y-3">
            <h3 className="text-base font-bold text-[#F3F7F5]">Carry Forward Unfinished Tasks?</h3>
            <p className="text-xs text-[#98ABA1] leading-relaxed">
              Unfinished tasks will be rescheduled to the top of your sequence, preserving all
              logged minutes.
            </p>
            <div className="flex items-center space-x-2 pt-2">
              <button
                onClick={() => setShowCarryForwardDialog(false)}
                className="flex-1 py-2 rounded-xl bg-[#1C2420] text-xs font-semibold text-[#98ABA1] cursor-pointer"
              >
                Cancel
              </button>
              <button
                onClick={() => {
                  setShowCarryForwardDialog(false);
                  carryForwardUnfinished();
                }}
                className="flex-1 py-2 rounded-xl bg-[#10B981] text-xs font-bold text-black cursor-pointer"
              >
                Carry Forward
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
