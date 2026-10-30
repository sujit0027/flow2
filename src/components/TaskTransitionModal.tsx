import { Coffee, PlusCircle, Play, Pause } from 'lucide-react';
import { TaskTransitionCountdownState, RoutineTask, CATEGORY_CONFIG } from '../types';
import { formatMinutesHuman } from '../utils/storage';

interface TaskTransitionModalProps {
  state: TaskTransitionCountdownState;
  onTakeBreak: (task: RoutineTask) => void;
  onExtendPrevious: (taskId: number) => void;
  onStartCompulsory: (task: RoutineTask) => void;
  onCancelPause: () => void;
}

export const TaskTransitionModal: React.FC<TaskTransitionModalProps> = ({
  state,
  onTakeBreak,
  onExtendPrevious,
  onStartCompulsory,
  onCancelPause,
}) => {
  const isUrgent = state.remainingSeconds <= 10;
  const progress = Math.max(0, Math.min(1, state.remainingSeconds / (state.totalCountdownSeconds || 30)));
  const circumference = 2 * Math.PI * 42;
  const strokeDashoffset = circumference - progress * circumference;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md">
      <div
        data-testid="task_transition_dialog"
        className="relative w-full max-w-md max-h-[90vh] overflow-y-auto rounded-2xl bg-[#131916] border border-[#25332B] p-6 shadow-2xl flex flex-col items-center text-center animate-in fade-in zoom-in-95 duration-200"
      >
        {/* Circular Countdown Ring */}
        <div className={`relative w-28 h-28 my-2 flex items-center justify-center ${isUrgent ? 'animate-pulse' : ''}`}>
          <svg className="w-full h-full -rotate-90" viewBox="0 0 100 100">
            <circle
              cx="50"
              cy="50"
              r="42"
              fill="transparent"
              stroke="#25332B"
              strokeWidth="7"
            />
            <circle
              cx="50"
              cy="50"
              r="42"
              fill="transparent"
              stroke={isUrgent ? '#FBBF24' : '#10B981'}
              strokeWidth="7"
              strokeDasharray={circumference}
              strokeDashoffset={strokeDashoffset}
              strokeLinecap="round"
              className="transition-all duration-1000 ease-linear"
            />
          </svg>
          <div className="absolute inset-0 flex flex-col items-center justify-center">
            <span
              className={`font-mono-digits text-2xl font-bold ${
                isUrgent ? 'text-[#FBBF24]' : 'text-[#34D399]'
              }`}
            >
              {state.remainingSeconds}s
            </span>
            <span
              className={`text-[9px] tracking-wider font-bold ${
                isUrgent ? 'text-[#FBBF24]' : 'text-[#64756B]'
              }`}
            >
              {isUrgent ? 'ALARM SOON' : 'AUTO-START'}
            </span>
          </div>
        </div>

        <h3 className="text-lg font-bold text-[#F3F7F5] mt-2">
          Completed: {state.finishedTaskTitle}
        </h3>

        <p className="text-xs text-[#98ABA1] mt-1.5 leading-relaxed max-w-xs">
          {state.intermediateTasks.length > 0
            ? `Choose any flexible rest task below, or wait ${state.remainingSeconds}s for the wake-up alarm to auto-start '${state.nextCompulsoryTask.title}'!`
            : `Next compulsory study task will auto-start with a waking alert in ${state.remainingSeconds}s if no action is chosen.`}
        </p>

        {/* Options Stack */}
        <div className="w-full mt-5 space-y-2.5">
          {/* Intermediate optional tasks (Breaks, Meals, etc.) */}
          {state.intermediateTasks.map((task) => {
            const meta = CATEGORY_CONFIG[task.category] || CATEGORY_CONFIG.BREAK;
            return (
              <button
                key={task.id}
                onClick={() => onTakeBreak(task)}
                data-testid={`transition_task_${task.id}`}
                className="w-full text-left p-3 rounded-xl bg-[#1C2420] border border-[#25332B] hover:border-[#38BDF8] transition-all flex items-center space-x-3 cursor-pointer group"
              >
                <div
                  className="w-9 h-9 rounded-full flex items-center justify-center shrink-0"
                  style={{ backgroundColor: meta.containerColor, color: meta.textColor }}
                >
                  <Coffee className="w-4 h-4" />
                </div>
                <div className="flex-1 min-w-0">
                  <div className="text-sm font-semibold text-[#F3F7F5] group-hover:text-[#38BDF8] transition-colors truncate">
                    Take {task.title}
                  </div>
                  <div className="text-[11px] text-[#98ABA1]">
                    {meta.displayName} • {formatMinutesHuman(task.minDurationMinutes)} • Tap to start
                  </div>
                </div>
              </button>
            );
          })}

          {/* Option 2: Extend Previous (+15m) */}
          <button
            onClick={() => onExtendPrevious(state.finishedTaskId)}
            data-testid="transition_extend_btn"
            className="w-full text-left p-3 rounded-xl bg-[#1C2420] border border-[#25332B] hover:border-[#34D399] transition-all flex items-center space-x-3 cursor-pointer group"
          >
            <div className="w-9 h-9 rounded-full bg-[#132F23] text-[#34D399] flex items-center justify-center shrink-0">
              <PlusCircle className="w-4 h-4" />
            </div>
            <div className="flex-1 min-w-0">
              <div className="text-sm font-semibold text-[#F3F7F5] group-hover:text-[#34D399] transition-colors">
                Extend Previous Study (+15m)
              </div>
              <div className="text-[11px] text-[#98ABA1] truncate">
                Continue studying {state.finishedTaskTitle} in flow
              </div>
            </div>
          </button>

          {/* Option 3: Start Compulsory Task Now (Prominent) */}
          <button
            onClick={() => onStartCompulsory(state.nextCompulsoryTask)}
            data-testid="transition_start_compulsory_btn"
            className="w-full text-left p-3 rounded-xl bg-[#132F23] border border-[#10B981] hover:bg-[#10B981]/20 transition-all flex items-center space-x-3 cursor-pointer shadow-lg group"
          >
            <div className="w-9 h-9 rounded-full bg-[#10B981] text-black flex items-center justify-center shrink-0 font-bold">
              <Play className="w-4 h-4 fill-current ml-0.5" />
            </div>
            <div className="flex-1 min-w-0">
              <div className="text-sm font-bold text-[#34D399]">
                Start Compulsory Task Now
              </div>
              <div className="text-[11px] text-[#A7F3D0] truncate">
                {state.nextCompulsoryTask.title} • {formatMinutesHuman(state.nextCompulsoryTask.minDurationMinutes)}
              </div>
            </div>
          </button>
        </div>

        {/* Option 4: Cancel Pause Routine */}
        <button
          onClick={onCancelPause}
          data-testid="transition_pause_btn"
          className="mt-4 flex items-center space-x-1.5 text-xs text-[#64756B] hover:text-[#98ABA1] transition-colors py-1 cursor-pointer"
        >
          <Pause className="w-3.5 h-3.5" />
          <span>Pause Routine & Return Home</span>
        </button>
      </div>
    </div>
  );
};
