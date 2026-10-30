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
      <div className="fixed bottom-14 right-3 z-40">
        <button
          onClick={onUnhide}
          data-testid="mini_player_unhide_pill"
          className="flex items-center space-x-1.5 px-2.5 py-1 rounded-full bg-[#1C2420]/95 border border-[#10B981]/50 shadow-lg backdrop-blur-md cursor-pointer hover:bg-[#26322C] transition-colors"
        >
          <span
            className={`w-1.5 h-1.5 rounded-full ${
              snapshot.isMinCompleted ? 'bg-[#10B981] animate-pulse' : 'bg-[#34D399]'
            }`}
          />
          <span className="font-mono-digits text-[11px] font-semibold text-[#34D399]">
            {formatDurationHMS(snapshot.currentElapsedSeconds)}
          </span>
          <span className="text-[10px] text-[#98ABA1]">Show</span>
          <Eye className="w-3 h-3 text-[#34D399]" />
        </button>
      </div>
    );
  }

  return (
    <div className="fixed bottom-14 left-0 right-0 z-40 mx-auto max-w-sm px-2.5">
      <div
        data-testid="mini_focus_player"
        onClick={onExpand}
        className="flex items-center justify-between px-3 py-1.5 rounded-lg bg-[#141C18]/95 border border-[#10B981]/40 shadow-xl cursor-pointer hover:border-[#10B981]/70 transition-all backdrop-blur-md"
      >
        {/* Left info */}
        <div className="flex items-center space-x-2 overflow-hidden pr-2">
          <div
            className="w-2 h-2 shrink-0 rounded-full"
            style={{
              backgroundColor: snapshot.isMinCompleted ? '#10B981' : categoryMeta.accentColor,
            }}
          />
          <div className="flex flex-col min-w-0">
            <span className="text-xs font-semibold text-[#F3F7F5] truncate">
              {snapshot.taskTitle || 'Flow Study'}
            </span>
            <span
              className={`text-[10px] truncate ${
                snapshot.isMinCompleted ? 'text-[#34D399] font-medium' : 'text-[#98ABA1]'
              }`}
            >
              {snapshot.isMinCompleted
                ? `⚡ In Flow (+${formatDurationHMS(snapshot.flowOvertimeSeconds)})`
                : `-${formatDurationHMS(snapshot.remainingCompulsorySeconds)}`}
            </span>
          </div>
        </div>

        {/* Right controls */}
        <div
          className="flex items-center space-x-1.5 shrink-0"
          onClick={(e) => e.stopPropagation()}
        >
          <span className="font-mono-digits text-xs font-bold text-[#34D399] tracking-tight">
            {formatDurationHMS(snapshot.currentElapsedSeconds)}
          </span>

          <button
            onClick={onTogglePlayPause}
            data-testid="mini_player_toggle_play_pause"
            className="p-1 rounded-md bg-[#132F23] text-[#34D399] hover:bg-[#10B981] hover:text-black transition-colors cursor-pointer"
            title={snapshot.isRunning ? 'Pause' : 'Resume'}
          >
            {snapshot.isRunning ? <Pause className="w-3.5 h-3.5" /> : <Play className="w-3.5 h-3.5" />}
          </button>

          <button
            onClick={onHide}
            data-testid="mini_player_hide_button"
            className="p-1 rounded-md text-[#64756B] hover:text-[#98ABA1] transition-colors cursor-pointer"
            title="Hide Countdown (Lecture Mode)"
          >
            <EyeOff className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    </div>
  );
};
