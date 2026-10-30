import React from 'react';
import { X, Play, Clock } from 'lucide-react';
import { useFlowStudy } from '../context/FlowStudyContext';
import { playSynthesizedTone } from '../utils/audio';

interface AutoFlowModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const AutoFlowModal: React.FC<AutoFlowModalProps> = ({ isOpen, onClose }) => {
  const { autoFlowConfig, updateAutoFlowConfig } = useFlowStudy();

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
      <div className="w-full max-w-md max-h-[90vh] overflow-y-auto rounded-2xl bg-[#131916] border border-[#25332B] p-5 shadow-2xl space-y-4 animate-in fade-in zoom-in-95 duration-200 text-left">
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-[#25332B]">
          <div className="flex items-center space-x-2">
            <div className="w-8 h-8 rounded-lg bg-[#132F23] text-[#34D399] flex items-center justify-center">
              <Clock className="w-4 h-4" />
            </div>
            <div>
              <h2 className="text-base font-bold text-[#F3F7F5]">Auto-Flow & Waking Alarms</h2>
              <p className="text-xs text-[#98ABA1]">Automatic routine pacing and alerts</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-[#98ABA1] hover:text-[#F3F7F5] transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Switches */}
        <div className="space-y-3">
          {/* Auto-Advance Compulsory */}
          <div className="flex items-center justify-between p-3.5 rounded-xl bg-[#1C2420] border border-[#25332B]">
            <div className="pr-2">
              <h4 className="text-xs font-bold text-[#F3F7F5]">
                Auto-Advance to Next Compulsory Task
              </h4>
              <p className="text-[11px] text-[#98ABA1] mt-0.5">
                Automatically opens the transition timer once a task is finished
              </p>
            </div>
            <input
              type="checkbox"
              checked={autoFlowConfig.autoAdvanceCompulsoryOnly}
              onChange={(e) =>
                updateAutoFlowConfig({
                  ...autoFlowConfig,
                  autoAdvanceCompulsoryOnly: e.target.checked,
                })
              }
              className="w-4 h-4 accent-[#10B981] rounded cursor-pointer shrink-0"
            />
          </div>

          {/* Countdown Duration Picker */}
          <div className="p-3.5 rounded-xl bg-[#1C2420] border border-[#25332B] space-y-2">
            <div className="flex items-center justify-between text-xs">
              <span className="font-semibold text-[#F3F7F5]">
                Transition Countdown Duration
              </span>
              <span className="font-mono-digits text-[#34D399]">
                {autoFlowConfig.countdownDurationSeconds}s
              </span>
            </div>
            <div className="grid grid-cols-4 gap-1.5">
              {[15, 30, 45, 60].map((secs) => {
                const isSelected = autoFlowConfig.countdownDurationSeconds === secs;
                return (
                  <button
                    key={secs}
                    onClick={() =>
                      updateAutoFlowConfig({
                        ...autoFlowConfig,
                        countdownDurationSeconds: secs,
                      })
                    }
                    className={`py-1.5 rounded-lg text-xs font-bold border transition-colors cursor-pointer ${
                      isSelected
                        ? 'bg-[#10B981] text-black border-[#10B981]'
                        : 'bg-[#131916] text-[#98ABA1] border-[#25332B] hover:text-[#F3F7F5]'
                    }`}
                  >
                    {secs}s
                  </button>
                );
              })}
            </div>
          </div>

          {/* Ring Alarm on Expiry */}
          <div className="flex items-center justify-between p-3.5 rounded-xl bg-[#1C2420] border border-[#25332B]">
            <div className="pr-2">
              <h4 className="text-xs font-bold text-[#F3F7F5]">
                Ring Wake-Up Alarm on Countdown Expiry
              </h4>
              <p className="text-[11px] text-[#98ABA1] mt-0.5">
                Emits energetic audio alarm to resume study if break times out
              </p>
            </div>
            <input
              type="checkbox"
              checked={autoFlowConfig.ringAlarmOnCountdownExpiry}
              onChange={(e) =>
                updateAutoFlowConfig({
                  ...autoFlowConfig,
                  ringAlarmOnCountdownExpiry: e.target.checked,
                })
              }
              className="w-4 h-4 accent-[#10B981] rounded cursor-pointer shrink-0"
            />
          </div>

          {/* Test Alarm Tone Button */}
          <button
            onClick={() => playSynthesizedTone('digital_alarm')}
            className="w-full py-2.5 rounded-xl bg-[#1C2420] border border-[#25332B] hover:border-[#10B981] text-xs font-semibold text-[#34D399] flex items-center justify-center space-x-2 transition-colors cursor-pointer"
          >
            <Play className="w-3.5 h-3.5" />
            <span>Test Alarm Tone (Digital Beep)</span>
          </button>
        </div>

        <button
          onClick={onClose}
          className="w-full py-2.5 rounded-xl bg-[#10B981] hover:bg-[#34D399] text-black font-bold text-xs transition-colors cursor-pointer"
        >
          Save Auto-Flow Settings
        </button>
      </div>
    </div>
  );
};
