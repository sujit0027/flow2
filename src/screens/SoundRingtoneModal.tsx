import React from 'react';
import { X, Play, Bell } from 'lucide-react';
import { useFlowStudy } from '../context/FlowStudyContext';
import { BUILT_IN_SOUNDS, playSynthesizedTone } from '../utils/audio';

interface SoundRingtoneModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const SoundRingtoneModal: React.FC<SoundRingtoneModalProps> = ({
  isOpen,
  onClose,
}) => {
  const { soundConfig, updateSoundConfig } = useFlowStudy();

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
      <div className="w-full max-w-md max-h-[90vh] overflow-y-auto rounded-2xl bg-[#131916] border border-[#25332B] p-5 shadow-2xl space-y-4 animate-in fade-in zoom-in-95 duration-200 text-left">
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-[#25332B]">
          <div className="flex items-center space-x-2">
            <div className="w-8 h-8 rounded-lg bg-[#132F23] text-[#34D399] flex items-center justify-center">
              <Bell className="w-4 h-4" />
            </div>
            <div>
              <h2 className="text-base font-bold text-[#F3F7F5]">Sound & Alert Ringtones</h2>
              <p className="text-xs text-[#98ABA1]">Harmonic bells and wake-up alerts</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-[#98ABA1] hover:text-[#F3F7F5] transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Master Sound Toggle */}
        <div className="flex items-center justify-between p-3.5 rounded-xl bg-[#1C2420] border border-[#10B981]/40">
          <div>
            <h4 className="text-xs font-bold text-[#F3F7F5]">Audio Alerts Enabled</h4>
            <p className="text-[11px] text-[#98ABA1]">
              Plays gentle chimes at milestones and waking alarms on expiry
            </p>
          </div>
          <input
            type="checkbox"
            checked={soundConfig.soundEnabled}
            onChange={(e) =>
              updateSoundConfig({
                ...soundConfig,
                soundEnabled: e.target.checked,
              })
            }
            className="w-4 h-4 accent-[#10B981] rounded cursor-pointer shrink-0"
          />
        </div>

        {/* Minimum Target Reached Sound */}
        <div className="p-3.5 rounded-xl bg-[#1C2420] border border-[#25332B] space-y-2">
          <div className="flex items-center justify-between">
            <div>
              <h4 className="text-xs font-bold text-[#F3F7F5]">Minimum Target Reached Sound</h4>
              <p className="text-[11px] text-[#98ABA1]">
                Fires when reaching compulsory target and entering Flow
              </p>
            </div>
            <button
              onClick={() => playSynthesizedTone(soundConfig.targetSoundId)}
              className="p-1.5 rounded-lg bg-[#132F23] text-[#34D399] hover:bg-[#10B981] hover:text-black transition-colors cursor-pointer"
              title="Preview Sound"
            >
              <Play className="w-3.5 h-3.5" />
            </button>
          </div>

          <div className="grid grid-cols-2 gap-1.5 pt-1">
            {BUILT_IN_SOUNDS.map((snd) => {
              const isSelected = soundConfig.targetSoundId === snd.id;
              return (
                <button
                  key={snd.id}
                  onClick={() =>
                    updateSoundConfig({
                      ...soundConfig,
                      targetSoundId: snd.id,
                      targetSoundName: snd.name,
                    })
                  }
                  className={`p-2 rounded-lg text-xs font-medium text-left border transition-colors cursor-pointer ${
                    isSelected
                      ? 'bg-[#132F23] border-[#10B981] text-[#34D399]'
                      : 'bg-[#131916] border-[#25332B] text-[#98ABA1] hover:text-[#F3F7F5]'
                  }`}
                >
                  <div className="font-semibold">{snd.name}</div>
                  <div className="text-[10px] text-[#64756B] truncate">{snd.description}</div>
                </button>
              );
            })}
          </div>
        </div>

        {/* Next Task Waking Alarm Sound */}
        <div className="p-3.5 rounded-xl bg-[#1C2420] border border-[#25332B] space-y-2">
          <div className="flex items-center justify-between">
            <div>
              <h4 className="text-xs font-bold text-[#F3F7F5]">Next Task Auto-Start Alarm</h4>
              <p className="text-[11px] text-[#98ABA1]">
                Energetic alert when 30-second break countdown ends
              </p>
            </div>
            <button
              onClick={() => playSynthesizedTone(soundConfig.nextTaskSoundId)}
              className="p-1.5 rounded-lg bg-[#132F23] text-[#34D399] hover:bg-[#10B981] hover:text-black transition-colors cursor-pointer"
              title="Preview Sound"
            >
              <Play className="w-3.5 h-3.5" />
            </button>
          </div>

          <div className="grid grid-cols-2 gap-1.5 pt-1">
            {BUILT_IN_SOUNDS.map((snd) => {
              const isSelected = soundConfig.nextTaskSoundId === snd.id;
              return (
                <button
                  key={snd.id}
                  onClick={() =>
                    updateSoundConfig({
                      ...soundConfig,
                      nextTaskSoundId: snd.id,
                      nextTaskSoundName: snd.name,
                    })
                  }
                  className={`p-2 rounded-lg text-xs font-medium text-left border transition-colors cursor-pointer ${
                    isSelected
                      ? 'bg-[#132F23] border-[#10B981] text-[#34D399]'
                      : 'bg-[#131916] border-[#25332B] text-[#98ABA1] hover:text-[#F3F7F5]'
                  }`}
                >
                  <div className="font-semibold">{snd.name}</div>
                  <div className="text-[10px] text-[#64756B] truncate">{snd.description}</div>
                </button>
              );
            })}
          </div>
        </div>

        <button
          onClick={onClose}
          className="w-full py-2.5 rounded-xl bg-[#10B981] hover:bg-[#34D399] text-black font-bold text-xs transition-colors cursor-pointer"
        >
          Save Sound Config
        </button>
      </div>
    </div>
  );
};
