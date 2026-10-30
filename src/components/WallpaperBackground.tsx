import React from 'react';
import { WallpaperConfig } from '../types';

interface WallpaperBackgroundProps {
  wallpaperType: WallpaperConfig['homeWallpaperType'];
  dimLevel: number;
  customUri?: string;
  useBlur?: boolean;
}

export const WallpaperBackground: React.FC<WallpaperBackgroundProps> = ({
  wallpaperType,
  dimLevel,
  customUri,
  useBlur,
}) => {
  const renderBackdrop = () => {
    switch (wallpaperType) {
      case 'FOREST':
        return (
          <div
            className="absolute inset-0 bg-cover bg-center transition-all duration-700"
            style={{
              backgroundImage: `url('https://images.unsplash.com/photo-1448375240586-882707db888b?auto=format&fit=crop&w=1920&q=80')`,
              filter: useBlur ? 'blur(8px)' : undefined,
            }}
          />
        );
      case 'MOUNTAIN':
        return (
          <div
            className="absolute inset-0 bg-cover bg-center transition-all duration-700"
            style={{
              backgroundImage: `url('https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1920&q=80')`,
              filter: useBlur ? 'blur(8px)' : undefined,
            }}
          />
        );
      case 'SKY':
        return (
          <div
            className="absolute inset-0 bg-cover bg-center transition-all duration-700"
            style={{
              backgroundImage: `url('https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?auto=format&fit=crop&w=1920&q=80')`,
              filter: useBlur ? 'blur(8px)' : undefined,
            }}
          />
        );
      case 'COUNTRYSIDE':
        return (
          <div
            className="absolute inset-0 bg-cover bg-center transition-all duration-700"
            style={{
              backgroundImage: `url('https://images.unsplash.com/photo-1500382017468-9049fed747ef?auto=format&fit=crop&w=1920&q=80')`,
              filter: useBlur ? 'blur(8px)' : undefined,
            }}
          />
        );
      case 'GRADIENT':
        return (
          <div className="absolute inset-0 bg-gradient-to-br from-[#0c1913] via-[#081f18] to-[#04100c] opacity-90" />
        );
      case 'CUSTOM':
        if (customUri) {
          return (
            <div
              className="absolute inset-0 bg-cover bg-center transition-all duration-700"
              style={{
                backgroundImage: `url('${customUri}')`,
                filter: useBlur ? 'blur(8px)' : undefined,
              }}
            />
          );
        }
        return <div className="absolute inset-0 bg-[#0C100E]" />;
      case 'AMOLED':
      default:
        return (
          <div className="absolute inset-0 bg-[#0C100E] bg-radial from-[#132F23]/25 via-transparent to-transparent opacity-80" />
        );
    }
  };

  return (
    <div className="pointer-events-none fixed inset-0 z-0 overflow-hidden select-none">
      {renderBackdrop()}
      {/* Dimming Scrim Layer */}
      <div
        className="absolute inset-0 bg-[#0C100E] transition-opacity duration-300"
        style={{ opacity: Math.min(0.9, Math.max(0.1, dimLevel)) }}
      />
    </div>
  );
};
