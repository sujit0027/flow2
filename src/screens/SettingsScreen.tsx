import React, { useState } from 'react';
import {
  Sliders,
  Palette,
  Shield,
  Bell,
  Clock,
  BatteryCharging,
  Download,
  Upload,
  RotateCcw,
  Trash2,
  CheckCircle2,
  Lock,
  Vibrate,
  Eye,
  Info,
} from 'lucide-react';
import { useFlowStudy } from '../context/FlowStudyContext';

interface SettingsScreenProps {
  onOpenFocusCustomization: () => void;
  onOpenWallpaperSettings: () => void;
  onOpenAppBlocker: () => void;
  onOpenSoundSettings: () => void;
  onOpenAutoFlowSettings: () => void;
}

export const SettingsScreen: React.FC<SettingsScreenProps> = ({
  onOpenFocusCustomization,
  onOpenWallpaperSettings,
  onOpenAppBlocker,
  onOpenSoundSettings,
  onOpenAutoFlowSettings,
}) => {
  const {
    settings,
    updateSettings,
    exportBackupJson,
    restoreBackupJson,
    restoreSampleRoutine,
    resetAllData,
  } = useFlowStudy();

  const [showExportModal, setShowExportModal] = useState(false);
  const [showImportModal, setShowImportModal] = useState(false);
  const [showChecklistModal, setShowChecklistModal] = useState(false);
  const [showResetRoutineDialog, setShowResetRoutineDialog] = useState(false);
  const [showClearAllDialog, setShowClearAllDialog] = useState(false);

  const [exportJson, setExportJson] = useState('');
  const [importJson, setImportJson] = useState('');
  const [importError, setImportError] = useState<string | null>(null);
  const [copyFeedback, setCopyFeedback] = useState(false);

  const handleOpenExport = () => {
    const json = exportBackupJson();
    setExportJson(json);
    setShowExportModal(true);
  };

  const handleCopyExport = () => {
    navigator.clipboard.writeText(exportJson);
    setCopyFeedback(true);
    setTimeout(() => setCopyFeedback(false), 2000);
  };

  const handlePerformRestore = () => {
    const success = restoreBackupJson(importJson);
    if (success) {
      setShowImportModal(false);
      setImportJson('');
      setImportError(null);
    } else {
      setImportError('Invalid FlowStudy JSON backup. Please check formatting.');
    }
  };

  return (
    <div className="relative z-10 mx-auto max-w-lg px-4 pt-4 pb-28 space-y-5">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-[#F3F7F5]">
          Settings & Customization
        </h1>
        <p className="text-xs text-[#98ABA1]">App blocker, ringtones, task flow & themes</p>
      </div>

      {/* Row 1: Focus Screen & Wallpapers */}
      <div className="grid grid-cols-2 gap-3">
        <button
          onClick={onOpenFocusCustomization}
          className="p-3.5 rounded-2xl bg-[#131916]/90 border border-[#10B981]/30 hover:border-[#10B981] transition-all text-left backdrop-blur-sm cursor-pointer group"
        >
          <Sliders className="w-5 h-5 text-[#34D399] mb-2 group-hover:scale-110 transition-transform" />
          <h3 className="text-sm font-bold text-[#F3F7F5]">Focus Screen</h3>
          <p className="text-[11px] text-[#98ABA1] mt-0.5">Hide elements / Ultra Minimal</p>
        </button>

        <button
          onClick={onOpenWallpaperSettings}
          className="p-3.5 rounded-2xl bg-[#131916]/90 border border-[#10B981]/30 hover:border-[#10B981] transition-all text-left backdrop-blur-sm cursor-pointer group"
        >
          <Palette className="w-5 h-5 text-[#34D399] mb-2 group-hover:scale-110 transition-transform" />
          <h3 className="text-sm font-bold text-[#F3F7F5]">Wallpapers</h3>
          <p className="text-[11px] text-[#98ABA1] mt-0.5">Nature, AMOLED & Scrims</p>
        </button>
      </div>

      {/* Row 2: App Blocker & Custom Ringtones */}
      <div className="grid grid-cols-2 gap-3">
        <button
          onClick={onOpenAppBlocker}
          className="p-3.5 rounded-2xl bg-[#131916]/90 border border-[#10B981]/40 hover:border-[#10B981] transition-all text-left backdrop-blur-sm cursor-pointer group"
        >
          <Shield className="w-5 h-5 text-[#34D399] mb-2 group-hover:scale-110 transition-transform" />
          <h3 className="text-sm font-bold text-[#F3F7F5]">App Blocker</h3>
          <p className="text-[11px] text-[#98ABA1] mt-0.5">Shield & block distracting apps</p>
        </button>

        <button
          onClick={onOpenSoundSettings}
          className="p-3.5 rounded-2xl bg-[#131916]/90 border border-[#10B981]/40 hover:border-[#10B981] transition-all text-left backdrop-blur-sm cursor-pointer group"
        >
          <Bell className="w-5 h-5 text-[#34D399] mb-2 group-hover:scale-110 transition-transform" />
          <h3 className="text-sm font-bold text-[#F3F7F5]">Ringtones</h3>
          <p className="text-[11px] text-[#98ABA1] mt-0.5">Custom ringtones & audio alerts</p>
        </button>
      </div>

      {/* Row 3: Auto-Flow & Next Task Alarms */}
      <button
        onClick={onOpenAutoFlowSettings}
        className="w-full p-3.5 rounded-2xl bg-[#131916]/90 border border-[#10B981]/35 hover:border-[#10B981] transition-all flex items-center justify-between text-left backdrop-blur-sm cursor-pointer group"
      >
        <div className="flex items-center space-x-3">
          <div className="w-9 h-9 rounded-xl bg-[#132F23] text-[#34D399] flex items-center justify-center shrink-0">
            <Clock className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-sm font-bold text-[#F3F7F5]">Auto-Start & Next Task Alarms</h3>
            <p className="text-[11px] text-[#98ABA1]">
              Automatic routine flow & wake alerts
            </p>
          </div>
        </div>
        <span className="text-xs font-bold text-[#34D399] shrink-0 ml-2">Configure</span>
      </button>

      {/* Focus & Timer Rules Section */}
      <div className="space-y-2.5">
        <h3 className="text-[10px] font-bold text-[#64756B] tracking-wider uppercase">
          Timer & Focus Engine
        </h3>

        <div className="p-4 rounded-2xl bg-[#131916]/90 border border-[#25332B] space-y-3.5 backdrop-blur-sm">
          {/* Compulsory Warning */}
          <div className="flex items-center justify-between">
            <div className="flex items-start space-x-3 pr-2">
              <Lock className="w-4 h-4 text-[#98ABA1] mt-0.5 shrink-0" />
              <div>
                <h4 className="text-xs font-semibold text-[#F3F7F5]">
                  Compulsory Minimum Warning
                </h4>
                <p className="text-[11px] text-[#98ABA1]">
                  Alerts when attempting to leave compulsory task before minimum target
                </p>
              </div>
            </div>
            <input
              type="checkbox"
              checked={settings.enforceCompulsoryWarning}
              onChange={(e) =>
                updateSettings({ ...settings, enforceCompulsoryWarning: e.target.checked })
              }
              data-testid="setting_compulsory_warning_switch"
              className="w-4 h-4 accent-[#10B981] rounded cursor-pointer"
            />
          </div>

          <div className="h-px bg-[#25332B]" />

          {/* Vibrate on Complete */}
          <div className="flex items-center justify-between">
            <div className="flex items-start space-x-3 pr-2">
              <Vibrate className="w-4 h-4 text-[#98ABA1] mt-0.5 shrink-0" />
              <div>
                <h4 className="text-xs font-semibold text-[#F3F7F5]">
                  Vibrate on Minimum Target
                </h4>
                <p className="text-[11px] text-[#98ABA1]">
                  Tactile pulse when reaching compulsory milestone & entering Flow State
                </p>
              </div>
            </div>
            <input
              type="checkbox"
              checked={settings.vibrateOnComplete}
              onChange={(e) =>
                updateSettings({ ...settings, vibrateOnComplete: e.target.checked })
              }
              data-testid="setting_vibrate_switch"
              className="w-4 h-4 accent-[#10B981] rounded cursor-pointer"
            />
          </div>

          <div className="h-px bg-[#25332B]" />

          {/* Keep Screen On */}
          <div className="flex items-center justify-between">
            <div className="flex items-start space-x-3 pr-2">
              <Eye className="w-4 h-4 text-[#98ABA1] mt-0.5 shrink-0" />
              <div>
                <h4 className="text-xs font-semibold text-[#F3F7F5]">Keep Screen Awake</h4>
                <p className="text-[11px] text-[#98ABA1]">
                  Prevents display sleep during active focus timer session
                </p>
              </div>
            </div>
            <input
              type="checkbox"
              checked={settings.keepScreenOn}
              onChange={(e) => updateSettings({ ...settings, keepScreenOn: e.target.checked })}
              data-testid="setting_screen_on_switch"
              className="w-4 h-4 accent-[#10B981] rounded cursor-pointer"
            />
          </div>

          <div className="h-px bg-[#25332B]" />

          {/* Breaks Optional by Default */}
          <div className="flex items-center justify-between">
            <div className="flex items-start space-x-3 pr-2">
              <Info className="w-4 h-4 text-[#98ABA1] mt-0.5 shrink-0" />
              <div>
                <h4 className="text-xs font-semibold text-[#F3F7F5]">
                  Breaks Optional by Default
                </h4>
                <p className="text-[11px] text-[#98ABA1]">
                  Newly created break & meal tasks can be skipped
                </p>
              </div>
            </div>
            <input
              type="checkbox"
              checked={settings.breaksOptionalDefault}
              onChange={(e) =>
                updateSettings({ ...settings, breaksOptionalDefault: e.target.checked })
              }
              data-testid="setting_breaks_optional_switch"
              className="w-4 h-4 accent-[#10B981] rounded cursor-pointer"
            />
          </div>
        </div>
      </div>

      {/* System Restrictions & Notifications Card */}
      <div className="space-y-2.5">
        <h3 className="text-[10px] font-bold text-[#64756B] tracking-wider uppercase">
          System Restrictions & Offline Execution
        </h3>

        <div className="p-4 rounded-2xl bg-[#131916]/90 border border-[#25332B] space-y-2.5 backdrop-blur-sm">
          <div className="flex items-center space-x-2 text-[#FBBF24]">
            <BatteryCharging className="w-4 h-4" />
            <h4 className="text-xs font-bold text-[#F3F7F5]">
              Background Execution & Offline Guarantee
            </h4>
          </div>
          <p className="text-xs text-[#98ABA1] leading-relaxed">
            FlowStudy works 100% offline with zero cloud latency. Active study timers maintain
            monotonic accuracy even if minimized, browser tab is backgrounded, or device sleeps.
          </p>
        </div>
      </div>

      {/* Local Data Backup & Export Section */}
      <div className="space-y-2.5">
        <h3 className="text-[10px] font-bold text-[#64756B] tracking-wider uppercase">
          Local Data Backup & Export
        </h3>

        <div className="p-4 rounded-2xl bg-[#131916]/90 border border-[#25332B] space-y-2 backdrop-blur-sm">
          <div className="grid grid-cols-2 gap-2">
            <button
              onClick={handleOpenExport}
              className="py-2.5 px-3 rounded-xl bg-[#1C2420] hover:bg-[#26322C] text-[#F3F7F5] font-semibold text-xs border border-[#25332B] flex items-center justify-center space-x-1.5 transition-colors cursor-pointer"
            >
              <Download className="w-3.5 h-3.5 text-[#34D399]" />
              <span>Export (JSON)</span>
            </button>

            <button
              onClick={() => setShowImportModal(true)}
              className="py-2.5 px-3 rounded-xl bg-[#1C2420] hover:bg-[#26322C] text-[#F3F7F5] font-semibold text-xs border border-[#25332B] flex items-center justify-center space-x-1.5 transition-colors cursor-pointer"
            >
              <Upload className="w-3.5 h-3.5 text-[#34D399]" />
              <span>Restore Backup</span>
            </button>
          </div>

          <button
            onClick={() => setShowChecklistModal(true)}
            className="w-full py-2.5 px-3 rounded-xl bg-[#1C2420] hover:bg-[#26322C] text-[#98ABA1] hover:text-[#F3F7F5] font-semibold text-xs border border-[#25332B] flex items-center justify-center space-x-1.5 transition-colors cursor-pointer"
          >
            <CheckCircle2 className="w-3.5 h-3.5 text-[#10B981]" />
            <span>View QA Manual Test Checklist</span>
          </button>

          <button
            onClick={() => setShowResetRoutineDialog(true)}
            className="w-full py-2.5 px-3 rounded-xl bg-[#1C2420] hover:bg-[#26322C] text-[#98ABA1] hover:text-[#F3F7F5] font-semibold text-xs border border-[#25332B] flex items-center justify-center space-x-1.5 transition-colors cursor-pointer"
          >
            <RotateCcw className="w-3.5 h-3.5 text-[#38BDF8]" />
            <span>Restore Prepopulated Student Routine</span>
          </button>

          <button
            onClick={() => setShowClearAllDialog(true)}
            className="w-full py-2.5 px-3 rounded-xl bg-[#1C2420] hover:bg-[#26322C] text-[#EF4444] font-semibold text-xs border border-[#25332B] flex items-center justify-center space-x-1.5 transition-colors cursor-pointer"
          >
            <Trash2 className="w-3.5 h-3.5" />
            <span>Delete All Data & Start Fresh</span>
          </button>
        </div>
      </div>

      {/* Export JSON Modal */}
      {showExportModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="w-full max-w-md rounded-2xl bg-[#131916] border border-[#25332B] p-5 shadow-2xl space-y-3">
            <h3 className="text-base font-bold text-[#F3F7F5]">Backup Data (JSON)</h3>
            <p className="text-xs text-[#98ABA1]">
              Your complete routines, tasks, and session logs in portable JSON format:
            </p>
            <textarea
              readOnly
              value={exportJson}
              className="w-full h-44 p-3 rounded-xl bg-[#1C2420] border border-[#25332B] font-mono-digits text-xs text-[#34D399] resize-none focus:outline-none"
            />
            <div className="flex items-center space-x-2 pt-1">
              <button
                onClick={() => setShowExportModal(false)}
                className="flex-1 py-2 rounded-xl bg-[#1C2420] text-xs font-semibold text-[#98ABA1] cursor-pointer"
              >
                Close
              </button>
              <button
                onClick={handleCopyExport}
                className="flex-1 py-2 rounded-xl bg-[#10B981] text-xs font-bold text-black cursor-pointer"
              >
                {copyFeedback ? 'Copied!' : 'Copy to Clipboard'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Import JSON Modal */}
      {showImportModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="w-full max-w-md rounded-2xl bg-[#131916] border border-[#25332B] p-5 shadow-2xl space-y-3">
            <h3 className="text-base font-bold text-[#F3F7F5]">Restore From JSON Backup</h3>
            <p className="text-xs text-[#98ABA1]">
              Paste previously exported FlowStudy JSON backup below:
            </p>
            <textarea
              value={importJson}
              onChange={(e) => {
                setImportJson(e.target.value);
                setImportError(null);
              }}
              placeholder="Paste JSON here..."
              className="w-full h-44 p-3 rounded-xl bg-[#1C2420] border border-[#25332B] font-mono-digits text-xs text-[#F3F7F5] placeholder-[#64756B] resize-none focus:outline-none focus:border-[#10B981]"
            />
            {importError && <p className="text-xs text-[#EF4444]">{importError}</p>}
            <div className="flex items-center space-x-2 pt-1">
              <button
                onClick={() => setShowImportModal(false)}
                className="flex-1 py-2 rounded-xl bg-[#1C2420] text-xs font-semibold text-[#98ABA1] cursor-pointer"
              >
                Cancel
              </button>
              <button
                onClick={handlePerformRestore}
                className="flex-1 py-2 rounded-xl bg-[#10B981] text-xs font-bold text-black cursor-pointer"
              >
                Restore Data
              </button>
            </div>
          </div>
        </div>
      )}

      {/* QA Test Checklist Modal */}
      {showChecklistModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="w-full max-w-md max-h-[85vh] overflow-y-auto rounded-2xl bg-[#131916] border border-[#25332B] p-5 shadow-2xl space-y-3">
            <h3 className="text-base font-bold text-[#F3F7F5]">
              Manual Test Checklist (Requirement 16)
            </h3>
            <div className="space-y-2 text-xs text-[#98ABA1] leading-relaxed">
              <p>1. Screen Off: Start focus timer, lock screen, verify lockscreen keeps live elapsed time.</p>
              <p>2. App Minimized: Minimize app, verify active mini floating player with Pause/Resume/Finish actions.</p>
              <p>3. Refresh / Process Restart: Reload browser tab, verify monotonic state is restored without lost active seconds.</p>
              <p>4. 30s Auto-Flow Alarm: Finish a task, observe 30-second countdown with ticking circle and waking alarm tone.</p>
              <p>5. Picture-in-Picture / Mini Floating Widget: Click PiP button or minimize, compact timer layout renders cleanly.</p>
              <p>6. Custom Wallpapers: Select Forest, Mountain, Sky, AMOLED or enter custom URL; dimming scrim adjusts smoothly.</p>
              <p>7. Offline Guarantee: 100% functional without internet, no remote telemetry or cloud dependencies.</p>
            </div>
            <button
              onClick={() => setShowChecklistModal(false)}
              className="w-full mt-3 py-2.5 rounded-xl bg-[#10B981] text-xs font-bold text-black cursor-pointer"
            >
              Got It
            </button>
          </div>
        </div>
      )}

      {/* Reset Routine Dialog */}
      {showResetRoutineDialog && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="w-full max-w-sm rounded-2xl bg-[#131916] border border-[#25332B] p-5 shadow-2xl space-y-3">
            <h3 className="text-base font-bold text-[#F3F7F5]">Restore Prepopulated Routine?</h3>
            <p className="text-xs text-[#98ABA1] leading-relaxed">
              This will reload the competitive student routine (Physics Theory, Questions,
              Chemistry, CET MCQs & Lunch). Your session history logs will not be touched.
            </p>
            <div className="flex items-center space-x-2 pt-2">
              <button
                onClick={() => setShowResetRoutineDialog(false)}
                className="flex-1 py-2 rounded-xl bg-[#1C2420] text-xs font-semibold text-[#98ABA1] cursor-pointer"
              >
                Cancel
              </button>
              <button
                onClick={() => {
                  restoreSampleRoutine();
                  setShowResetRoutineDialog(false);
                }}
                className="flex-1 py-2 rounded-xl bg-[#10B981] text-xs font-bold text-black cursor-pointer"
              >
                Restore Routine
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Clear All Dialog */}
      {showClearAllDialog && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="w-full max-w-sm rounded-2xl bg-[#131916] border border-[#25332B] p-5 shadow-2xl space-y-3">
            <h3 className="text-base font-bold text-[#F3F7F5]">Delete All Data?</h3>
            <p className="text-xs text-[#98ABA1] leading-relaxed">
              This will erase all routines, session logs, and reset preferences. This action cannot
              be undone.
            </p>
            <div className="flex items-center space-x-2 pt-2">
              <button
                onClick={() => setShowClearAllDialog(false)}
                className="flex-1 py-2 rounded-xl bg-[#1C2420] text-xs font-semibold text-[#98ABA1] cursor-pointer"
              >
                Cancel
              </button>
              <button
                onClick={() => {
                  resetAllData();
                  setShowClearAllDialog(false);
                }}
                className="flex-1 py-2 rounded-xl bg-[#EF4444] text-xs font-bold text-white cursor-pointer"
              >
                Delete Everything
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
