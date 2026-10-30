import React from 'react';
import { X, Shield, Video } from 'lucide-react';
import { useFlowStudy } from '../context/FlowStudyContext';
import { POPULAR_DISTRACTIONS } from '../utils/storage';

interface AppBlockerModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const AppBlockerModal: React.FC<AppBlockerModalProps> = ({
  isOpen,
  onClose,
}) => {
  const { appBlockConfig, updateAppBlockConfig } = useFlowStudy();

  if (!isOpen) return null;

  const togglePackage = (pkgId: string) => {
    const current = new Set(appBlockConfig.blockedPackages);
    if (current.has(pkgId)) {
      current.delete(pkgId);
    } else {
      current.add(pkgId);
    }
    updateAppBlockConfig({
      ...appBlockConfig,
      blockedPackages: Array.from(current),
    });
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
      <div className="w-full max-w-md max-h-[90vh] overflow-y-auto rounded-2xl bg-[#131916] border border-[#25332B] p-5 shadow-2xl space-y-4 animate-in fade-in zoom-in-95 duration-200 text-left">
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-[#25332B]">
          <div className="flex items-center space-x-2">
            <div className="w-8 h-8 rounded-lg bg-[#132F23] text-[#34D399] flex items-center justify-center">
              <Shield className="w-4 h-4" />
            </div>
            <div>
              <h2 className="text-base font-bold text-[#F3F7F5]">Focus Shield & Blocker</h2>
              <p className="text-xs text-[#98ABA1]">Lock down addictive digital distractions</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-[#98ABA1] hover:text-[#F3F7F5] transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Master Toggle */}
        <div className="flex items-center justify-between p-3.5 rounded-xl bg-[#1C2420] border border-[#10B981]/40">
          <div>
            <h4 className="text-xs font-bold text-[#F3F7F5]">Enable Focus Shield</h4>
            <p className="text-[11px] text-[#98ABA1]">
              Enforces distraction protection during active study stints
            </p>
          </div>
          <input
            type="checkbox"
            checked={appBlockConfig.isBlockerEnabled}
            onChange={(e) =>
              updateAppBlockConfig({
                ...appBlockConfig,
                isBlockerEnabled: e.target.checked,
              })
            }
            className="w-4 h-4 accent-[#10B981] rounded cursor-pointer shrink-0"
          />
        </div>

        {/* Allow YouTube for Lectures Toggle */}
        <div className="flex items-center justify-between p-3 rounded-xl bg-[#1C2420] border border-[#25332B]">
          <div className="flex items-start space-x-2.5 pr-2">
            <Video className="w-4 h-4 text-[#EF4444] mt-0.5 shrink-0" />
            <div>
              <h4 className="text-xs font-semibold text-[#F3F7F5]">
                Allow YouTube for Lectures
              </h4>
              <p className="text-[11px] text-[#98ABA1]">
                Exempts educational video lectures from strict blocking
              </p>
            </div>
          </div>
          <input
            type="checkbox"
            checked={appBlockConfig.allowYouTubeForLectures}
            onChange={(e) =>
              updateAppBlockConfig({
                ...appBlockConfig,
                allowYouTubeForLectures: e.target.checked,
              })
            }
            className="w-4 h-4 accent-[#10B981] rounded cursor-pointer shrink-0"
          />
        </div>

        {/* Distraction Checklist */}
        <div className="space-y-2">
          <label className="text-[11px] font-bold uppercase tracking-wider text-[#98ABA1]">
            Target Blocklist
          </label>
          <div className="space-y-1.5 max-h-56 overflow-y-auto pr-1">
            {POPULAR_DISTRACTIONS.map((app) => {
              const isBlocked = appBlockConfig.blockedPackages.includes(app.id);
              return (
                <div
                  key={app.id}
                  onClick={() => togglePackage(app.id)}
                  className={`flex items-center justify-between p-2.5 rounded-xl border transition-colors cursor-pointer ${
                    isBlocked
                      ? 'bg-[#132F23] border-[#10B981]/50'
                      : 'bg-[#1C2420] border-[#25332B] hover:border-[#64756B]'
                  }`}
                >
                  <div>
                    <h5 className="text-xs font-semibold text-[#F3F7F5]">{app.name}</h5>
                    <span className="text-[10px] text-[#98ABA1]">{app.category}</span>
                  </div>
                  <input
                    type="checkbox"
                    checked={isBlocked}
                    onChange={() => {}} // Controlled by container onClick
                    className="w-4 h-4 accent-[#10B981] rounded cursor-pointer"
                  />
                </div>
              );
            })}
          </div>
        </div>

        <button
          onClick={onClose}
          className="w-full py-2.5 rounded-xl bg-[#10B981] hover:bg-[#34D399] text-black font-bold text-xs transition-colors cursor-pointer"
        >
          Save Shield Settings
        </button>
      </div>
    </div>
  );
};
