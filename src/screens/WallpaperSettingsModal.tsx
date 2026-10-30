import React from 'react';
import { X, Check } from 'lucide-react';
import { useFlowStudy } from '../context/FlowStudyContext';
import { WallpaperConfig } from '../types';

interface WallpaperSettingsModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const WallpaperSettingsModal: React.FC<WallpaperSettingsModalProps> = ({
  isOpen,
  onClose,
}) => {
  const { wallpaperConfig, updateWallpaperConfig } = useFlowStudy();

  if (!isOpen) return null;

  const wallpaperTypes: {
    id: WallpaperConfig['homeWallpaperType'];
    name: string;
    desc: string;
    preview: string;
  }[] = [
    {
      id: 'AMOLED',
      name: 'Pure AMOLED',
      desc: 'True black obsidian minimal surface',
      preview: 'bg-[#0C100E]',
    },
    {
      id: 'FOREST',
      name: 'Misty Forest',
      desc: 'Deep pine woods for serene tranquility',
      preview: 'bg-emerald-950',
    },
    {
      id: 'MOUNTAIN',
      name: 'Alpine Dusk',
      desc: 'High-contrast solitary mountain ridge',
      preview: 'bg-slate-900',
    },
    {
      id: 'SKY',
      name: 'Starry Twilight',
      desc: 'Peaceful celestial nocturnal horizon',
      preview: 'bg-indigo-950',
    },
    {
      id: 'COUNTRYSIDE',
      name: 'Golden Countryside',
      desc: 'Warm soothing rolling grassy hills',
      preview: 'bg-amber-950',
    },
    {
      id: 'GRADIENT',
      name: 'Aurora Glow',
      desc: 'Subtle emerald spruce deep ambient gradient',
      preview: 'bg-gradient-to-r from-emerald-900 to-teal-950',
    },
    {
      id: 'CUSTOM',
      name: 'Custom Image URL',
      desc: 'Paste any image link of your choice',
      preview: 'bg-[#1C2420]',
    },
  ];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
      <div className="w-full max-w-md max-h-[90vh] overflow-y-auto rounded-2xl bg-[#131916] border border-[#25332B] p-5 shadow-2xl space-y-4 animate-in fade-in zoom-in-95 duration-200 text-left">
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-[#25332B]">
          <div>
            <h2 className="text-base font-bold text-[#F3F7F5]">Wallpapers & Scrims</h2>
            <p className="text-xs text-[#98ABA1]">Choose study backdrops and ambient levels</p>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-[#98ABA1] hover:text-[#F3F7F5] transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Wallpaper Picker */}
        <div className="space-y-2">
          <label className="text-[11px] font-bold uppercase tracking-wider text-[#98ABA1]">
            Select Wallpaper Style
          </label>
          <div className="grid grid-cols-2 gap-2">
            {wallpaperTypes.map((item) => {
              const isSelected = wallpaperConfig.focusWallpaperType === item.id;
              return (
                <div
                  key={item.id}
                  onClick={() =>
                    updateWallpaperConfig({
                      ...wallpaperConfig,
                      homeWallpaperType: item.id,
                      focusWallpaperType: item.id,
                    })
                  }
                  className={`p-3 rounded-xl border transition-all cursor-pointer flex flex-col justify-between ${
                    isSelected
                      ? 'bg-[#132F23] border-[#10B981]'
                      : 'bg-[#1C2420] border-[#25332B] hover:border-[#64756B]'
                  }`}
                >
                  <div className="flex items-center justify-between mb-1.5">
                    <span className="text-xs font-bold text-[#F3F7F5]">{item.name}</span>
                    {isSelected && <Check className="w-3.5 h-3.5 text-[#10B981]" />}
                  </div>
                  <p className="text-[10px] text-[#98ABA1] line-clamp-1">{item.desc}</p>
                </div>
              );
            })}
          </div>
        </div>

        {/* Custom URL input */}
        {wallpaperConfig.focusWallpaperType === 'CUSTOM' && (
          <div className="space-y-1">
            <label className="text-[11px] font-bold uppercase tracking-wider text-[#98ABA1]">
              Custom Wallpaper Image URL
            </label>
            <input
              type="url"
              value={wallpaperConfig.customWallpaperUri}
              onChange={(e) =>
                updateWallpaperConfig({
                  ...wallpaperConfig,
                  customWallpaperUri: e.target.value,
                })
              }
              placeholder="https://images.unsplash.com/..."
              className="w-full px-3 py-2 rounded-xl bg-[#1C2420] border border-[#25332B] text-xs text-[#F3F7F5] placeholder-[#64756B] focus:outline-none focus:border-[#10B981]"
            />
          </div>
        )}

        {/* Dimming Level Slider */}
        <div className="p-3.5 rounded-xl bg-[#1C2420] border border-[#25332B] space-y-2">
          <div className="flex items-center justify-between text-xs">
            <span className="font-semibold text-[#F3F7F5]">Dimming Scrim Level</span>
            <span className="font-mono-digits text-[#34D399]">
              {Math.round(wallpaperConfig.dimLevel * 100)}%
            </span>
          </div>
          <input
            type="range"
            min="0.1"
            max="0.85"
            step="0.05"
            value={wallpaperConfig.dimLevel}
            onChange={(e) =>
              updateWallpaperConfig({
                ...wallpaperConfig,
                dimLevel: parseFloat(e.target.value),
              })
            }
            className="w-full accent-[#10B981] cursor-pointer"
          />
          <p className="text-[10px] text-[#64756B]">
            Higher dimming ensures highest contrast for timer digits.
          </p>
        </div>

        {/* Background Blur Toggle */}
        <div className="flex items-center justify-between p-3 rounded-xl bg-[#1C2420] border border-[#25332B]">
          <div>
            <h4 className="text-xs font-semibold text-[#F3F7F5]">Background Subtle Blur</h4>
            <p className="text-[11px] text-[#98ABA1]">Softens background image textures</p>
          </div>
          <input
            type="checkbox"
            checked={wallpaperConfig.useBlur}
            onChange={(e) =>
              updateWallpaperConfig({
                ...wallpaperConfig,
                useBlur: e.target.checked,
              })
            }
            className="w-4 h-4 accent-[#10B981] rounded cursor-pointer"
          />
        </div>

        <button
          onClick={onClose}
          className="w-full py-2.5 rounded-xl bg-[#10B981] hover:bg-[#34D399] text-black font-bold text-xs transition-colors cursor-pointer"
        >
          Save Wallpapers
        </button>
      </div>
    </div>
  );
};
