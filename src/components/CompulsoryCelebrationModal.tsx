import React from 'react';
import { Trophy, Sparkles, CheckCircle2, Flame, ArrowRight, Settings2, X } from 'lucide-react';
import { useFlowStudy } from '../context/FlowStudyContext';
import { formatDurationHMS } from '../utils/storage';

interface CompulsoryCelebrationModalProps {
  isOpen: boolean;
  onClose: () => void;
  onOpenSettings: () => void;
}

export const CompulsoryCelebrationModal: React.FC<CompulsoryCelebrationModalProps> = ({
  isOpen,
  onClose,
  onOpenSettings,
}) => {
  const { tasks, todayLogs, autoFlowConfig } = useFlowStudy();

  if (!isOpen) return null;

  const compulsoryTasks = tasks.filter((t) => t.isCompulsory);
  const totalLoggedSecondsToday = todayLogs.reduce((acc, log) => acc + log.durationSeconds, 0);
  const bonusOvertimeSeconds = todayLogs.reduce((acc, log) => acc + (log.extensionSeconds || 0), 0);

  const customWallpaper = autoFlowConfig.celebrationWallpaperUri?.trim();
  const customMessage = autoFlowConfig.celebrationMessage?.trim() ||
    'Sensational dedication! You conquered all compulsory study milestones today.';

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/90 backdrop-blur-md animate-in fade-in duration-300">
      {/* Background Wallpaper if customized by user */}
      {customWallpaper && (
        <div
          className="absolute inset-0 bg-cover bg-center transition-all duration-700 opacity-35"
          style={{ backgroundImage: `url(${customWallpaper})` }}
        />
      )}

      {/* Radiant ambient glow */}
      <div className="absolute inset-0 bg-radial from-[#10B981]/20 via-transparent to-black pointer-events-none" />

      <div
        data-testid="compulsory_celebration_dialog"
        className="relative w-full max-w-md max-h-[92vh] overflow-y-auto rounded-3xl bg-[#111714]/95 border border-[#10B981]/40 p-6 sm:p-7 shadow-2xl flex flex-col items-center text-center space-y-4 backdrop-blur-xl"
      >
        {/* Close icon */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 p-2 rounded-full bg-[#1C2420]/80 text-[#98ABA1] hover:text-[#F3F7F5] transition-colors cursor-pointer"
          title="Close celebration"
        >
          <X className="w-4 h-4" />
        </button>

        {/* Celebration Trophy with Glow */}
        <div className="relative my-2">
          <div className="w-20 h-20 rounded-full bg-gradient-to-tr from-[#132F23] to-[#10B981]/30 border-2 border-[#10B981] flex items-center justify-center shadow-[0_0_35px_rgba(16,185,129,0.35)] animate-bounce duration-1000">
            <Trophy className="w-10 h-10 text-[#34D399]" />
          </div>
          <div className="absolute -top-1 -right-1 p-1 rounded-full bg-[#10B981] text-black">
            <Sparkles className="w-3.5 h-3.5 fill-current" />
          </div>
        </div>

        {/* Title */}
        <div className="space-y-1">
          <span className="px-3 py-1 rounded-full bg-[#132F23] text-[#34D399] border border-[#10B981]/30 text-[10px] font-bold tracking-widest uppercase">
            COMPULSORY TARGET COMPLETE
          </span>
          <h2 className="text-2xl font-black tracking-tight text-[#F3F7F5] pt-1">
            All Core Tasks Done!
          </h2>
        </div>

        {/* User-Customized Motivational Message */}
        <div className="p-4 rounded-2xl bg-[#18221D] border border-[#25332B] w-full text-left">
          <div className="flex items-center space-x-1.5 text-[10px] font-bold text-[#10B981] uppercase tracking-wider mb-1">
            <Flame className="w-3.5 h-3.5" />
            <span>Daily Discipline Affirmation</span>
          </div>
          <p className="text-xs sm:text-sm text-[#E2ECE7] font-medium leading-relaxed italic">
            "{customMessage}"
          </p>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-3 gap-2.5 w-full">
          <div className="p-3 rounded-xl bg-[#151D19] border border-[#25332B] flex flex-col items-center justify-center">
            <span className="text-[10px] text-[#98ABA1] uppercase font-bold">Focus Time</span>
            <span className="font-mono-digits text-sm sm:text-base font-bold text-[#34D399] mt-0.5">
              {formatDurationHMS(totalLoggedSecondsToday)}
            </span>
          </div>

          <div className="p-3 rounded-xl bg-[#151D19] border border-[#25332B] flex flex-col items-center justify-center">
            <span className="text-[10px] text-[#98ABA1] uppercase font-bold">Tasks</span>
            <span className="font-mono-digits text-sm sm:text-base font-bold text-[#F3F7F5] mt-0.5">
              {compulsoryTasks.length}/{compulsoryTasks.length}
            </span>
          </div>

          <div className="p-3 rounded-xl bg-[#151D19] border border-[#25332B] flex flex-col items-center justify-center">
            <span className="text-[10px] text-[#98ABA1] uppercase font-bold">Overtime Flow</span>
            <span className="font-mono-digits text-sm sm:text-base font-bold text-[#A7F3D0] mt-0.5">
              +{formatDurationHMS(bonusOvertimeSeconds)}
            </span>
          </div>
        </div>

        {/* Completed Task Checklist Preview */}
        <div className="w-full text-left space-y-1.5 max-h-36 overflow-y-auto pr-1">
          <span className="text-[10px] text-[#64756B] uppercase font-bold tracking-wider">
            Compulsory Milestones Conquered:
          </span>
          {compulsoryTasks.map((task) => (
            <div
              key={task.id}
              className="flex items-center space-x-2 text-xs py-1 px-2.5 rounded-lg bg-[#151D19]/60 text-[#98ABA1]"
            >
              <CheckCircle2 className="w-3.5 h-3.5 text-[#10B981] shrink-0" />
              <span className="truncate flex-1 font-medium text-[#E2ECE7]">{task.title}</span>
              <span className="text-[10px] text-[#64756B] shrink-0 font-mono-digits">
                {task.accumulatedDurationSeconds > 0
                  ? formatDurationHMS(task.accumulatedDurationSeconds)
                  : `${task.minDurationMinutes}m`}
              </span>
            </div>
          ))}
        </div>

        {/* Action Buttons */}
        <div className="w-full space-y-2 pt-2">
          <button
            onClick={onClose}
            className="w-full py-3 rounded-xl bg-[#10B981] hover:bg-[#34D399] text-black font-bold text-xs sm:text-sm flex items-center justify-center space-x-2 shadow-lg transition-all cursor-pointer"
          >
            <span>Celebrate & Return to Dashboard</span>
            <ArrowRight className="w-4 h-4" />
          </button>

          <button
            onClick={() => {
              onClose();
              onOpenSettings();
            }}
            className="w-full py-2.5 rounded-xl bg-[#1C2420] hover:bg-[#25332B] text-[#98ABA1] hover:text-[#F3F7F5] border border-[#25332B] text-xs font-semibold flex items-center justify-center space-x-1.5 transition-colors cursor-pointer"
          >
            <Settings2 className="w-3.5 h-3.5" />
            <span>Customize Wallpaper & Motivation Quote</span>
          </button>
        </div>
      </div>
    </div>
  );
};
