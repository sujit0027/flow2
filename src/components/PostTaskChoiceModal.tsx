import React from 'react';
import { ArrowRight, Coffee, Utensils, Flame, Check } from 'lucide-react';
import { useFlowStudy } from '../context/FlowStudyContext';
import { PostTaskChoice } from '../types';
import { formatDurationHMS, formatMinutesHuman } from '../utils/storage';

interface PostTaskChoiceModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const PostTaskChoiceModal: React.FC<PostTaskChoiceModalProps> = ({
  isOpen,
  onClose,
}) => {
  const { timerSnapshot, tasks, finishCurrentTask } = useFlowStudy();

  if (!isOpen || timerSnapshot.taskId === 0) return null;

  const currentTaskId = timerSnapshot.taskId;
  const flowOvertime = timerSnapshot.flowOvertimeSeconds;
  const nextTask = tasks.find((t) => t.id !== currentTaskId && !t.isCompleted);

  const handleSelect = (choice: PostTaskChoice) => {
    onClose();
    finishCurrentTask(choice);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
      <div className="w-full max-w-sm rounded-2xl bg-[#131916] border border-[#25332B] p-5 shadow-2xl space-y-4 animate-in fade-in zoom-in-95 duration-200 text-left">
        {/* Title */}
        <div>
          <h3 className="text-lg font-bold text-[#F3F7F5]">🎉 Minimum Completed!</h3>
          {flowOvertime > 0 && (
            <p className="text-xs text-[#34D399] font-medium mt-0.5">
              +{formatDurationHMS(flowOvertime)} bonus flow time logged
            </p>
          )}
          <p className="text-xs text-[#98ABA1] mt-1">What would you like to do next?</p>
        </div>

        {/* Choices Stack */}
        <div className="space-y-2">
          {nextTask && (
            <button
              onClick={() => handleSelect('ADVANCE_NEXT_TASK')}
              className="w-full p-3 rounded-xl bg-[#132F23] border border-[#10B981] hover:bg-[#10B981]/25 transition-all text-left flex items-center justify-between cursor-pointer group"
            >
              <div>
                <div className="text-xs font-bold text-[#34D399]">Advance to Next Task</div>
                <div className="text-[11px] text-[#A7F3D0] truncate">
                  {nextTask.title} ({formatMinutesHuman(nextTask.minDurationMinutes)})
                </div>
              </div>
              <ArrowRight className="w-4 h-4 text-[#34D399] shrink-0 ml-2 group-hover:translate-x-1 transition-transform" />
            </button>
          )}

          <button
            onClick={() => handleSelect('TAKE_BREAK')}
            className="w-full p-3 rounded-xl bg-[#1C2420] border border-[#25332B] hover:border-[#38BDF8] transition-all text-left flex items-center justify-between cursor-pointer group"
          >
            <div>
              <div className="text-xs font-semibold text-[#F3F7F5] group-hover:text-[#38BDF8] transition-colors">
                Take a Flexible Break
              </div>
              <div className="text-[11px] text-[#98ABA1]">
                Relax eyes & hydrate for 5–10 mins
              </div>
            </div>
            <Coffee className="w-4 h-4 text-[#38BDF8] shrink-0 ml-2" />
          </button>

          <button
            onClick={() => handleSelect('HAVE_MEAL')}
            className="w-full p-3 rounded-xl bg-[#1C2420] border border-[#25332B] hover:border-[#FB923C] transition-all text-left flex items-center justify-between cursor-pointer group"
          >
            <div>
              <div className="text-xs font-semibold text-[#F3F7F5] group-hover:text-[#FB923C] transition-colors">
                Have a Meal or Snack
              </div>
              <div className="text-[11px] text-[#98ABA1]">Lunch, dinner, or nutritious snack</div>
            </div>
            <Utensils className="w-4 h-4 text-[#FB923C] shrink-0 ml-2" />
          </button>

          <button
            onClick={() => handleSelect('CONTINUE_STUDYING')}
            className="w-full p-3 rounded-xl bg-[#1C2420] border border-[#25332B] hover:border-[#34D399] transition-all text-left flex items-center justify-between cursor-pointer group"
          >
            <div>
              <div className="text-xs font-semibold text-[#F3F7F5] group-hover:text-[#34D399] transition-colors">
                Keep Studying in Flow
              </div>
              <div className="text-[11px] text-[#98ABA1]">Resume timer and extend focus</div>
            </div>
            <Flame className="w-4 h-4 text-[#34D399] shrink-0 ml-2" />
          </button>

          <button
            onClick={() => handleSelect('JUST_FINISH')}
            className="w-full p-3 rounded-xl bg-[#1C2420] border border-[#25332B] hover:border-[#64756B] transition-all text-left flex items-center justify-between cursor-pointer group"
          >
            <div>
              <div className="text-xs font-semibold text-[#F3F7F5]">
                Just Finish & Return Home
              </div>
              <div className="text-[11px] text-[#98ABA1]">
                Mark task completed and close session
              </div>
            </div>
            <Check className="w-4 h-4 text-[#98ABA1] shrink-0 ml-2" />
          </button>
        </div>
      </div>
    </div>
  );
};
