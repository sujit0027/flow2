import React, { useState } from 'react';
import {
  X,
  EyeOff,
  Pipette,
  Play,
  Pause,
  Check,
  Flame,
} from 'lucide-react';
import { useFlowStudy } from '../context/FlowStudyContext';
import { CATEGORY_CONFIG } from '../types';
import { formatDurationHMS, formatMinutesHuman } from '../utils/storage';
import { WallpaperBackground } from '../components/WallpaperBackground';

export const ActiveTimerScreen: React.FC = () => {
  const {
    timerSnapshot,
    tasks,
    focusCustomization,
    wallpaperConfig,
    togglePlayPause,
    addTimerSeconds,
    emergencyStopSession,
    closeActiveTimerScreen,
    minimizeTimerScreen,
    setShowPostTaskModal,
  } = useFlowStudy();

  const [distractionCount, setDistractionCount] = useState(0);
  const [showEmergencyStopDialog, setShowEmergencyStopDialog] = useState(false);
  const [isCompactPiP, setIsCompactPiP] = useState(false);

  if (timerSnapshot.taskId === 0) return null;

  const nextTask = tasks.find((t) => t.id !== timerSnapshot.taskId && !t.isCompleted);
  const categoryMeta = CATEGORY_CONFIG[timerSnapshot.category] || CATEGORY_CONFIG.STUDY;

  const elapsedSeconds = timerSnapshot.currentElapsedSeconds;
  const minDurationSeconds = timerSnapshot.minDurationSeconds;
  const isMinCompleted = timerSnapshot.isMinCompleted;
  const remainingCompulsory = timerSnapshot.remainingCompulsorySeconds;
  const flowOvertime = timerSnapshot.flowOvertimeSeconds;

  // Progress computation
  const progress = minDurationSeconds > 0
    ? Math.min(1, elapsedSeconds / minDurationSeconds)
    : 1;
  const radius = 95;
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = circumference - progress * circumference;

  // Ultra-compact PiP simulation widget
  if (isCompactPiP) {
    return (
      <div className="fixed top-4 right-4 z-50 p-2.5 rounded-2xl bg-black/90 border border-[#10B981] shadow-2xl backdrop-blur-md flex items-center space-x-3 select-none animate-in fade-in zoom-in-95 duration-200">
        <div
          className={`w-2.5 h-2.5 rounded-full ${
            isMinCompleted ? 'bg-[#10B981] animate-pulse' : 'bg-[#FBBF24]'
          }`}
        />
        <div className="flex flex-col">
          <span className="font-mono-digits text-sm font-bold text-[#34D399]">
            {formatDurationHMS(elapsedSeconds)}
          </span>
          <span className="text-[10px] text-[#98ABA1]">
            {isMinCompleted ? '⚡ In Flow' : `-${formatDurationHMS(remainingCompulsory)}`}
          </span>
        </div>
        <button
          onClick={togglePlayPause}
          className="p-1 rounded-md bg-[#132F23] text-[#34D399] hover:bg-[#10B981] hover:text-black transition-colors cursor-pointer"
        >
          {timerSnapshot.isRunning ? <Pause className="w-3.5 h-3.5" /> : <Play className="w-3.5 h-3.5" />}
        </button>
        <button
          onClick={() => setIsCompactPiP(false)}
          className="p-1 text-[#64756B] hover:text-[#F3F7F5] cursor-pointer"
          title="Expand"
        >
          <X className="w-3.5 h-3.5" />
        </button>
      </div>
    );
  }

  return (
    <div
      data-testid="active_timer_session_screen"
      className="fixed inset-0 z-50 flex flex-col justify-between bg-[#0C100E] p-5 sm:p-6 text-center select-none overflow-y-auto"
    >
      {/* Background Wallpaper */}
      <WallpaperBackground
        wallpaperType={wallpaperConfig.focusWallpaperType}
        dimLevel={wallpaperConfig.dimLevel}
        customUri={wallpaperConfig.customWallpaperUri}
        useBlur={wallpaperConfig.useBlur}
      />

      {/* Top Header Bar */}
      <div className="relative z-10 flex items-center justify-between">
        <div className="flex items-center space-x-1">
          <button
            onClick={closeActiveTimerScreen}
            data-testid="timer_minimize_button"
            className="p-2 rounded-xl text-[#98ABA1] hover:text-[#F3F7F5] hover:bg-[#1C2420] transition-colors cursor-pointer"
            title="Minimize Timer"
          >
            <X className="w-5 h-5" />
          </button>

          <button
            onClick={() => minimizeTimerScreen(true)}
            data-testid="timer_hide_lecture_button"
            className="p-2 rounded-xl text-[#34D399] hover:text-[#10B981] hover:bg-[#132F23] transition-colors cursor-pointer"
            title="Hide Timer (Lecture Mode)"
          >
            <EyeOff className="w-5 h-5" />
          </button>
        </div>

        {/* Category & Subject Badges */}
        {focusCustomization.showSessionLabel && !focusCustomization.isUltraMinimal && (
          <div className="flex items-center space-x-1.5">
            <span
              className="px-2.5 py-1 rounded-lg text-xs font-semibold"
              style={{
                backgroundColor: categoryMeta.containerColor,
                color: categoryMeta.textColor,
              }}
            >
              {categoryMeta.displayName}
            </span>
            {timerSnapshot.subject && (
              <span className="px-2 py-0.5 rounded-lg bg-[#1C2420] text-[10px] font-semibold text-[#98ABA1] uppercase">
                {timerSnapshot.subject}
              </span>
            )}
          </div>
        )}

        {/* PiP button */}
        <button
          onClick={() => setIsCompactPiP(true)}
          data-testid="timer_pip_button"
          className="p-2 rounded-xl text-[#98ABA1] hover:text-[#34D399] hover:bg-[#1C2420] transition-colors cursor-pointer"
          title="Floating Window (PiP)"
        >
          <Pipette className="w-5 h-5" />
        </button>
      </div>

      {/* Center: Title & Motivational Quote & Dynamic Status */}
      <div className="relative z-10 my-auto py-4 flex flex-col items-center max-w-md mx-auto space-y-3">
        {focusCustomization.showTaskTitle && (
          <h2 className="text-2xl sm:text-3xl font-extrabold text-[#F3F7F5] tracking-tight">
            {timerSnapshot.taskTitle}
          </h2>
        )}

        {focusCustomization.showMotivationalQuote && !focusCustomization.isUltraMinimal && (
          <p className="text-xs text-[#98ABA1] italic max-w-xs">
            "Small focused disciplines build unstoppable momentum."
          </p>
        )}

        {/* Dynamic Status Pill */}
        <div
          className={`inline-flex items-center space-x-1.5 px-3 py-1 rounded-full text-[11px] font-bold tracking-wider border shadow-sm ${
            isMinCompleted
              ? 'bg-[#132F23] border-[#10B981] text-[#34D399]'
              : timerSnapshot.isCompulsory
              ? 'bg-[#3A2111] border-[#FB923C]/50 text-[#FDBA74]'
              : 'bg-[#0C2D3E] border-[#38BDF8]/50 text-[#7DD3FC]'
          }`}
        >
          {isMinCompleted ? (
            <>
              <Flame className="w-3.5 h-3.5 text-[#10B981]" />
              <span>⚡ IN FLOW STATE — MINIMUM TARGET REACHED</span>
            </>
          ) : timerSnapshot.isCompulsory ? (
            <span>COMPULSORY MINIMUM: {formatMinutesHuman(Math.floor(minDurationSeconds / 60))}</span>
          ) : (
            <span>FLEXIBLE TASK: {formatMinutesHuman(Math.floor(minDurationSeconds / 60))}</span>
          )}
        </div>

        {/* Circular Dial / Timer Area */}
        <div
          data-testid="timer_display_area"
          className={`relative w-64 h-64 my-4 flex items-center justify-center transition-transform duration-500 ${
            isMinCompleted && timerSnapshot.isRunning ? 'scale-105' : ''
          }`}
        >
          {focusCustomization.showProgressRing && !focusCustomization.isUltraMinimal && (
            <svg className="w-full h-full -rotate-90" viewBox="0 0 220 220">
              <circle
                cx="110"
                cy="110"
                r={radius}
                fill="transparent"
                stroke="#1C2420"
                strokeWidth="10"
              />
              <circle
                cx="110"
                cy="110"
                r={radius}
                fill="transparent"
                stroke={isMinCompleted ? '#10B981' : categoryMeta.accentColor}
                strokeWidth="10"
                strokeDasharray={circumference}
                strokeDashoffset={strokeDashoffset}
                strokeLinecap="round"
                className="transition-all duration-1000 ease-linear"
              />
            </svg>
          )}

          <div className="absolute inset-0 flex flex-col items-center justify-center">
            {focusCustomization.showTimer && (
              <span
                data-testid="timer_elapsed_text"
                className="font-mono-digits text-4xl sm:text-5xl font-extrabold text-[#F3F7F5] tracking-tight"
              >
                {!isMinCompleted
                  ? formatDurationHMS(remainingCompulsory)
                  : formatDurationHMS(elapsedSeconds)}
              </span>
            )}

            {focusCustomization.showMinRemaining && !focusCustomization.isUltraMinimal && (
              <span
                className={`text-xs mt-1 font-medium ${
                  isMinCompleted ? 'text-[#34D399]' : 'text-[#98ABA1]'
                }`}
              >
                {!isMinCompleted
                  ? `${formatDurationHMS(elapsedSeconds)} active elapsed`
                  : `+${formatDurationHMS(flowOvertime)} overtime in flow`}
              </span>
            )}

            {!timerSnapshot.isRunning && (
              <span className="text-[11px] font-bold text-[#FBBF24] tracking-widest mt-1 uppercase">
                PAUSED
              </span>
            )}
          </div>
        </div>

        {/* Next Task Preview */}
        {focusCustomization.showNextTaskPreview && nextTask && !focusCustomization.isUltraMinimal && (
          <div className="w-full p-2.5 rounded-xl bg-[#1C2420]/80 border border-[#25332B] flex items-center justify-between text-xs">
            <span className="text-[#98ABA1] truncate">Next: {nextTask.title}</span>
            <span className="font-mono-digits text-[#34D399] font-bold shrink-0 ml-2">
              {formatMinutesHuman(nextTask.minDurationMinutes)}
            </span>
          </div>
        )}
      </div>

      {/* Bottom Controls */}
      <div className="relative z-10 max-w-md mx-auto w-full space-y-4">
        {/* Extension Chips & Distraction pill */}
        {!focusCustomization.isUltraMinimal && (
          <div className="flex items-center justify-between px-1">
            <div className="flex items-center space-x-1.5 text-xs">
              <span className="text-[#64756B]">Extend:</span>
              <button
                onClick={() => addTimerSeconds(5 * 60)}
                className="px-2 py-1 rounded-lg bg-[#1C2420] border border-[#25332B] text-xs font-semibold text-[#98ABA1] hover:text-[#34D399] hover:border-[#10B981]/40 transition-colors cursor-pointer"
              >
                +5m
              </button>
              <button
                onClick={() => addTimerSeconds(15 * 60)}
                className="px-2 py-1 rounded-lg bg-[#1C2420] border border-[#25332B] text-xs font-semibold text-[#98ABA1] hover:text-[#34D399] hover:border-[#10B981]/40 transition-colors cursor-pointer"
              >
                +15m
              </button>
            </div>

            <button
              onClick={() => setDistractionCount((prev) => prev + 1)}
              className="px-2.5 py-1 rounded-lg bg-[#1C2420] border border-[#25332B] text-xs text-[#98ABA1] hover:text-[#F3F7F5] transition-colors cursor-pointer"
            >
              Distractions: {distractionCount} (+)
            </button>
          </div>
        )}

        {/* Play / Pause Big Center Button */}
        <div className="flex justify-center">
          <button
            onClick={togglePlayPause}
            data-testid="timer_play_pause_button"
            className={`w-16 h-16 rounded-full flex items-center justify-center shadow-xl transition-all hover:scale-105 active:scale-95 cursor-pointer ${
              timerSnapshot.isRunning
                ? 'bg-[#1C2420] border border-[#25332B] text-[#F3F7F5]'
                : 'bg-[#10B981] text-black'
            }`}
            title={timerSnapshot.isRunning ? 'Pause' : 'Resume'}
          >
            {timerSnapshot.isRunning ? (
              <Pause className="w-7 h-7" />
            ) : (
              <Play className="w-7 h-7 fill-current ml-1" />
            )}
          </button>
        </div>

        {/* Action Buttons: Stop / Exit vs Finish Task */}
        <div className="grid grid-cols-5 gap-2.5">
          <button
            onClick={() => setShowEmergencyStopDialog(true)}
            data-testid="timer_emergency_stop_button"
            className="col-span-2 py-3 rounded-xl bg-[#1C2420] hover:bg-[#26322C] text-[#98ABA1] hover:text-[#EF4444] font-semibold text-xs border border-[#25332B] transition-colors cursor-pointer"
          >
            Stop / Exit
          </button>

          <button
            onClick={() => {
              if (isMinCompleted || !timerSnapshot.isCompulsory) {
                setShowPostTaskModal(true);
              } else {
                setShowEmergencyStopDialog(true);
              }
            }}
            disabled={!isMinCompleted && timerSnapshot.isCompulsory}
            data-testid="timer_finish_button"
            className={`col-span-3 py-3 rounded-xl font-bold text-xs flex items-center justify-center space-x-1.5 transition-all shadow-lg ${
              isMinCompleted || !timerSnapshot.isCompulsory
                ? 'bg-[#10B981] hover:bg-[#34D399] text-black cursor-pointer'
                : 'bg-[#1C2420] text-[#64756B] border border-[#25332B] cursor-not-allowed opacity-50'
            }`}
          >
            <Check className="w-4 h-4 stroke-[3]" />
            <span>
              {isMinCompleted
                ? 'Finish Task'
                : `Finish (Min ${formatMinutesHuman(Math.floor(minDurationSeconds / 60))})`}
            </span>
          </button>
        </div>
      </div>

      {/* Emergency Stop Dialog */}
      {showEmergencyStopDialog && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="w-full max-w-sm rounded-2xl bg-[#131916] border border-[#25332B] p-5 shadow-2xl space-y-3 text-left">
            <h3 className="text-base font-bold text-[#F3F7F5]">
              {!isMinCompleted && timerSnapshot.isCompulsory
                ? 'Compulsory Minimum Not Reached'
                : 'End Session Early?'}
            </h3>
            <p className="text-xs text-[#98ABA1] leading-relaxed">
              {!isMinCompleted && timerSnapshot.isCompulsory
                ? `This study task has a compulsory minimum duration. There are ~${Math.ceil(
                    remainingCompulsory / 60
                  )} minutes remaining. Stopping now will log this session as interrupted / incomplete rather than completed.`
                : 'Do you want to save active progress and end this session?'}
            </p>
            <div className="flex items-center space-x-2 pt-2">
              <button
                onClick={() => setShowEmergencyStopDialog(false)}
                className="flex-1 py-2 rounded-xl bg-[#132F23] text-xs font-bold text-[#34D399] cursor-pointer"
              >
                Keep Flowing
              </button>
              <button
                onClick={() => {
                  setShowEmergencyStopDialog(false);
                  emergencyStopSession();
                }}
                className="flex-1 py-2 rounded-xl bg-[#EF4444] text-xs font-bold text-white cursor-pointer"
              >
                Record as Incomplete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
