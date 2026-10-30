import React from 'react';
import { X } from 'lucide-react';
import { useFlowStudy } from '../context/FlowStudyContext';

interface FocusCustomizationModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const FocusCustomizationModal: React.FC<FocusCustomizationModalProps> = ({
  isOpen,
  onClose,
}) => {
  const { focusCustomization, updateFocusCustomization } = useFlowStudy();

  if (!isOpen) return null;

  const toggles: {
    key: keyof typeof focusCustomization;
    title: string;
    desc: string;
  }[] = [
    {
      key: 'isUltraMinimal',
      title: 'Ultra Minimal Focus Mode',
      desc: 'Hides all secondary indicators, rings, and badges for pure distraction-free focus',
    },
    {
      key: 'showTaskTitle',
      title: 'Show Task Title',
      desc: 'Displays the current study task title at the top',
    },
    {
      key: 'showTimer',
      title: 'Show Digital Chronograph',
      desc: 'Renders the large countdown / stopwatch numbers',
    },
    {
      key: 'showProgressRing',
      title: 'Show Progress Ring Dial',
      desc: 'Draws the smooth circular sweep progress indicator',
    },
    {
      key: 'showMinRemaining',
      title: 'Show Active / Remaining Subtitle',
      desc: 'Shows active elapsed or flow overtime under main timer',
    },
    {
      key: 'showSessionLabel',
      title: 'Show Category & Subject Badges',
      desc: 'Displays Study, Break, Meal badges with subject tags',
    },
    {
      key: 'showNextTaskPreview',
      title: 'Show Next Task Preview Pill',
      desc: 'Renders upcoming task name and duration during session',
    },
    {
      key: 'showMotivationalQuote',
      title: 'Show Motivational Discipline Quotes',
      desc: 'Displays subtle stoic & momentum quotes',
    },
  ];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
      <div className="w-full max-w-md max-h-[90vh] overflow-y-auto rounded-2xl bg-[#131916] border border-[#25332B] p-5 shadow-2xl space-y-4 animate-in fade-in zoom-in-95 duration-200 text-left">
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-[#25332B]">
          <div>
            <h2 className="text-base font-bold text-[#F3F7F5]">Focus Screen Customization</h2>
            <p className="text-xs text-[#98ABA1]">Choose elements visible during study</p>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-[#98ABA1] hover:text-[#F3F7F5] transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Toggles list */}
        <div className="space-y-3">
          {toggles.map((item) => {
            const isChecked = focusCustomization[item.key];
            return (
              <div
                key={item.key}
                className="flex items-center justify-between p-3 rounded-xl bg-[#1C2420] border border-[#25332B]"
              >
                <div className="pr-3">
                  <h4 className="text-xs font-semibold text-[#F3F7F5]">{item.title}</h4>
                  <p className="text-[11px] text-[#98ABA1] mt-0.5">{item.desc}</p>
                </div>
                <input
                  type="checkbox"
                  checked={Boolean(isChecked)}
                  onChange={(e) =>
                    updateFocusCustomization({
                      ...focusCustomization,
                      [item.key]: e.target.checked,
                    })
                  }
                  className="w-4 h-4 accent-[#10B981] rounded cursor-pointer shrink-0"
                />
              </div>
            );
          })}
        </div>

        <button
          onClick={onClose}
          className="w-full py-2.5 rounded-xl bg-[#10B981] hover:bg-[#34D399] text-black font-bold text-xs transition-colors cursor-pointer"
        >
          Done
        </button>
      </div>
    </div>
  );
};
