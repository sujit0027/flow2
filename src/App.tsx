import React, { useState } from 'react';
import { FlowStudyProvider, useFlowStudy } from './context/FlowStudyContext';
import { Navigation } from './components/Navigation';
import { MiniPlayer } from './components/MiniPlayer';
import { WallpaperBackground } from './components/WallpaperBackground';
import { TaskTransitionModal } from './components/TaskTransitionModal';
import { TaskEditModal } from './components/TaskEditModal';
import { PostTaskChoiceModal } from './components/PostTaskChoiceModal';
import { OnboardingModal } from './components/OnboardingModal';
import { HomeScreen } from './screens/HomeScreen';
import { RoutineScreen } from './screens/RoutineScreen';
import { ProgressScreen } from './screens/ProgressScreen';
import { SettingsScreen } from './screens/SettingsScreen';
import { ActiveTimerScreen } from './screens/ActiveTimerScreen';
import { FocusCustomizationModal } from './screens/FocusCustomizationModal';
import { WallpaperSettingsModal } from './screens/WallpaperSettingsModal';
import { AppBlockerModal } from './screens/AppBlockerModal';
import { SoundRingtoneModal } from './screens/SoundRingtoneModal';
import { AutoFlowModal } from './screens/AutoFlowModal';
import { CompulsoryCelebrationModal } from './components/CompulsoryCelebrationModal';

const AppContent: React.FC = () => {
  const {
    currentTab,
    setCurrentTab,
    themeConfig,
    completeOnboarding,
    timerSnapshot,
    isTimerScreenOpen,
    setIsTimerScreenOpen,
    isMiniPlayerHidden,
    setIsMiniPlayerHidden,
    togglePlayPause,
    wallpaperConfig,
    isEditModalOpen,
    taskBeingEdited,
    closeEditModal,
    saveTask,
    transitionCountdownState,
    onTransitionTakeBreak,
    onTransitionExtendPrevious,
    onTransitionStartCompulsory,
    onTransitionCancelPause,
    showPostTaskModal,
    setShowPostTaskModal,
  } = useFlowStudy();

  // Secondary modals open state
  const [isFocusCustomizationOpen, setIsFocusCustomizationOpen] = useState(false);
  const [isWallpaperSettingsOpen, setIsWallpaperSettingsOpen] = useState(false);
  const [isAppBlockerOpen, setIsAppBlockerOpen] = useState(false);
  const [isSoundSettingsOpen, setIsSoundSettingsOpen] = useState(false);
  const [isAutoFlowSettingsOpen, setIsAutoFlowSettingsOpen] = useState(false);
  const [isCelebrationOpen, setIsCelebrationOpen] = useState(false);

  // If first-time user, render Onboarding
  if (!themeConfig.isOnboardingCompleted) {
    return <OnboardingModal onFinish={completeOnboarding} />;
  }

  return (
    <div className="min-h-screen bg-[#0C100E] text-[#F3F7F5] selection:bg-[#10B981] selection:text-black relative overflow-x-hidden">
      {/* Background Wallpaper */}
      <WallpaperBackground
        wallpaperType={wallpaperConfig.homeWallpaperType}
        dimLevel={wallpaperConfig.dimLevel}
        customUri={wallpaperConfig.customWallpaperUri}
        useBlur={wallpaperConfig.useBlur}
      />

      {/* Main Screen Content by Tab */}
      <main className="relative z-10">
        {currentTab === 'HOME' && (
          <HomeScreen
            onOpenWallpaperSettings={() => setIsWallpaperSettingsOpen(true)}
            onOpenFocusCustomization={() => setIsFocusCustomizationOpen(true)}
            onOpenCelebrationModal={() => setIsCelebrationOpen(true)}
          />
        )}
        {currentTab === 'ROUTINE' && <RoutineScreen />}
        {currentTab === 'PROGRESS' && <ProgressScreen />}
        {currentTab === 'SETTINGS' && (
          <SettingsScreen
            onOpenFocusCustomization={() => setIsFocusCustomizationOpen(true)}
            onOpenWallpaperSettings={() => setIsWallpaperSettingsOpen(true)}
            onOpenAppBlocker={() => setIsAppBlockerOpen(true)}
            onOpenSoundSettings={() => setIsSoundSettingsOpen(true)}
            onOpenAutoFlowSettings={() => setIsAutoFlowSettingsOpen(true)}
          />
        )}
      </main>

      {/* Mini Player when active timer session exists and timer overlay is minimized */}
      {timerSnapshot.taskId !== 0 && !isTimerScreenOpen && (
        <MiniPlayer
          snapshot={timerSnapshot}
          isMiniPlayerHidden={isMiniPlayerHidden}
          onExpand={() => setIsTimerScreenOpen(true)}
          onTogglePlayPause={togglePlayPause}
          onHide={() => setIsMiniPlayerHidden(true)}
          onUnhide={() => setIsMiniPlayerHidden(false)}
        />
      )}

      {/* Bottom Navigation */}
      <Navigation currentTab={currentTab} onSelectTab={setCurrentTab} />

      {/* Active Timer Fullscreen Overlay */}
      {isTimerScreenOpen && <ActiveTimerScreen />}

      {/* Modals & Dialogs */}
      {isEditModalOpen && (
        <TaskEditModal
          taskToEdit={taskBeingEdited}
          onDismiss={closeEditModal}
          onSaveTask={saveTask}
        />
      )}

      {transitionCountdownState && (
        <TaskTransitionModal
          state={transitionCountdownState}
          onTakeBreak={onTransitionTakeBreak}
          onExtendPrevious={onTransitionExtendPrevious}
          onStartCompulsory={onTransitionStartCompulsory}
          onCancelPause={onTransitionCancelPause}
        />
      )}

      <PostTaskChoiceModal
        isOpen={showPostTaskModal}
        onClose={() => setShowPostTaskModal(false)}
      />

      <FocusCustomizationModal
        isOpen={isFocusCustomizationOpen}
        onClose={() => setIsFocusCustomizationOpen(false)}
      />

      <WallpaperSettingsModal
        isOpen={isWallpaperSettingsOpen}
        onClose={() => setIsWallpaperSettingsOpen(false)}
      />

      <AppBlockerModal
        isOpen={isAppBlockerOpen}
        onClose={() => setIsAppBlockerOpen(false)}
      />

      <SoundRingtoneModal
        isOpen={isSoundSettingsOpen}
        onClose={() => setIsSoundSettingsOpen(false)}
      />

      <AutoFlowModal
        isOpen={isAutoFlowSettingsOpen}
        onClose={() => setIsAutoFlowSettingsOpen(false)}
      />

      <CompulsoryCelebrationModal
        isOpen={isCelebrationOpen}
        onClose={() => setIsCelebrationOpen(false)}
        onOpenSettings={() => {
          setIsCelebrationOpen(false);
          setIsAutoFlowSettingsOpen(true);
        }}
      />
    </div>
  );
};

export default function App() {
  return (
    <FlowStudyProvider>
      <AppContent />
    </FlowStudyProvider>
  );
}
