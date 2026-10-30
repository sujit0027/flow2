import React from 'react';
import { Play, Pause, EyeOff, Eye } from 'lucide-react';
import { TimerSessionSnapshot, CATEGORY_CONFIG } from '../types';
import { formatDurationHMS } from '../utils/storage';

interface MiniPlayerProps {
  snapshot: TimerSessionSnapshot;
  isMiniPlayerHidden: boolean;
  onExpand: () => void;
  onTogglePlayPause: () => void;
  onHide: () => void;
  onUnhide: () => void;
}

export const MiniPlayer: React.FC<MiniPlayerProps> = ({
  snapshot,
  isMiniPlayerHidden,
  onExpand,
  onTogglePlayPause,
  onHide,
  onUnhide,
}) => {
  if (snapshot.taskId === 0) return null;

  const categoryMeta = CATEGORY_CONFIG[snapshot.category] || CATEGORY_CONFIG.STUDY;

  if (isMiniPlayerHidden) {
    return (
      <div className="fixed bottom-16 right-4 z-40">
        <button
          onClick={onUnhide}
          data-testid="mini_player_unhide_pill"
          className="flex items-center space-x-2 px-3 py-1.5 rounded-full bg-[#1C2420]/95 border border-[#10B981]/50 shadow-xl backdrop-blur-md cursor-pointer hover:bg-[#26322C] transition-colors"
        >
          <span
            className={`w-2 h-2 rounded-full ${
              snapshot.isMinCompleted ? 'bg-[#10B981] animate-pulse' : 'bg-[#34D399]'
            }`}
          />
          <span className="font-mono-digits text-xs font-semibold text-[#34D399]">
            {formatDurationHMS(snapshot.currentElapsedSeconds)}
          </span>
          <span className="text-[11px] text-[#98ABA1]">• Show</span>
          <Eye className="w-3.5 h-3.5 text-[#34D399]" />
        </button>
      </div>
    );
  }

  return (
    <div className="fixed bottom-16 left-0 right-0 z-40 mx-auto max-w-lg px-3">
      <div
        data-testid="mini_focus_player"
        onClick={onExpand}
        className="flex items-center justify-between px-3.5 py-2 rounded-xl bg-[#1C2420] border border-[#10B981]/40 shadow-2xl cursor-pointer hover:border-[#10B981]/70 transition-all backdrop-blur-md"
      >
        {/* Left info */}
        <div className="flex items-center space-x-3 overflow-hidden pr-2">
          <div
            className="w-2.5 h-2.5 shrink-0 rounded-full"
            style={{
              backgroundColor: snapshot.isMinCompleted ? '#10B981' : categoryMeta.accentColor,
            }}
          />
          <div className="flex flex-col min-w-0">
            <span className="text-sm font-semibold text-[#F3F7F5] truncate">
              {snapshot.taskTitle || 'Flow Study'}
            </span>
            <span
              className={`text-[11px] truncate ${
                snapshot.isMinCompleted ? 'text-[#34D399] font-medium' : 'text-[#98ABA1]'
              }`}
            >
              {snapshot.isMinCompleted
                ? `⚡ Flow Active (+${formatDurationHMS(snapshot.flowOvertimeSeconds)})`
                : `Min: ${formatDurationHMS(snapshot.remainingCompulsorySeconds)} left`}
            </span>
          </div>
        </div>

        {/* Right controls */}
        <div
          className="flex items-center space-x-2 shrink-0"
          onClick={(e) => e.stopPropagation()}
        >
          <span className="font-mono-digits text-sm font-bold text-[#34D399] tracking-tight">
            {formatDurationHMS(snapshot.currentElapsedSeconds)}
          </span>

          <button
            onClick={onTogglePlayPause}
            data-testid="mini_player_toggle_play_pause"
            className="p-1.5 rounded-lg bg-[#132F23] text-[#34D399] hover:bg-[#10B981] hover:text-black transition-colors cursor-pointer"
            title={snapshot.isRunning ? 'Pause' : 'Resume'}
          >
            {snapshot.isRunning ? <Pause className="w-4 h-4" /> : <Play className="w-4 h-4" />}
          </button>

          <button
            onClick={onHide}
            data-testid="mini_player_hide_button"
            className="p-1.5 rounded-lg text-[#64756B] hover:text-[#98ABA1] transition-colors cursor-pointer"
            title="Hide Countdown (Lecture Mode)"
          >
            <EyeOff className="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  );
};
