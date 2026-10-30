import React, { useState, useMemo } from 'react';
import {
  Download,
  Trash2,
  TrendingUp,
  Zap,
  Flame,
  History,
} from 'lucide-react';
import { useFlowStudy } from '../context/FlowStudyContext';
import { CATEGORY_CONFIG } from '../types';
import { formatDurationHMS } from '../utils/storage';

export const ProgressScreen: React.FC = () => {
  const { allLogs, todayLogs, clearAllLogs, exportLogsCsv } = useFlowStudy();

  const [selectedTimeTab, setSelectedTimeTab] = useState<0 | 1>(0); // 0: Today, 1: Weekly / All-Time
  const [showClearConfirmDialog, setShowClearConfirmDialog] = useState(false);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const currentLogs = selectedTimeTab === 0 ? todayLogs : allLogs;

  const totalStudySeconds = currentLogs
    .filter((l) => l.category === 'STUDY')
    .reduce((sum, l) => sum + l.durationSeconds, 0);

  const totalExtensionSeconds = currentLogs.reduce(
    (sum, l) => sum + (l.extensionSeconds || 0),
    0
  );

  const completedSessionsCount = currentLogs.filter((l) => !l.wasInterrupted).length;
  const interruptedSessionsCount = currentLogs.filter((l) => l.wasInterrupted).length;

  // Subject breakdown
  const subjectMap = useMemo(() => {
    const map: Record<string, number> = {};
    currentLogs.forEach((log) => {
      if (log.subject) {
        map[log.subject] = (map[log.subject] || 0) + log.durationSeconds;
      }
    });
    return Object.entries(map).sort((a, b) => b[1] - a[1]);
  }, [currentLogs]);

  // Streaks calculation (days with >= 1 hour of study)
  const studyDays = useMemo(() => {
    const days = new Set<string>();
    allLogs
      .filter((l) => l.category === 'STUDY')
      .forEach((log) => {
        const d = new Date(log.completedTimestamp).toISOString().split('T')[0];
        days.add(d);
      });
    return days.size;
  }, [allLogs]);

  const handleExportCsv = () => {
    const csv = exportLogsCsv();
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', `flowstudy_analytics_${Date.now()}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    setToastMessage('CSV Exported successfully!');
    setTimeout(() => setToastMessage(null), 3000);
  };

  return (
    <div className="relative z-10 mx-auto max-w-lg px-4 pt-4 pb-28 space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-[#F3F7F5]">Study Analytics</h1>
          <p className="text-xs text-[#98ABA1]">Subject-wise & flow state insights</p>
        </div>

        <div className="flex items-center space-x-1">
          <button
            onClick={handleExportCsv}
            data-testid="progress_export_csv_button"
            className="p-2 rounded-xl text-[#98ABA1] hover:text-[#34D399] hover:bg-[#1C2420] transition-colors cursor-pointer"
            title="Export CSV"
          >
            <Download className="w-5 h-5" />
          </button>

          {allLogs.length > 0 && (
            <button
              onClick={() => setShowClearConfirmDialog(true)}
              data-testid="progress_clear_logs_button"
              className="p-2 rounded-xl text-[#64756B] hover:text-[#EF4444] hover:bg-[#1C2420] transition-colors cursor-pointer"
              title="Clear History"
            >
              <Trash2 className="w-5 h-5" />
            </button>
          )}
        </div>
      </div>

      {/* Toast Notification */}
      {toastMessage && (
        <div className="p-2.5 rounded-xl bg-[#132F23] border border-[#10B981] text-[#34D399] text-xs font-semibold text-center animate-in fade-in duration-200">
          {toastMessage}
        </div>
      )}

      {/* Today vs Weekly/All-Time Tabs */}
      <div className="flex p-1 rounded-xl bg-[#1C2420] border border-[#25332B]">
        <button
          onClick={() => setSelectedTimeTab(0)}
          className={`flex-1 py-1.5 rounded-lg text-xs font-semibold transition-all cursor-pointer ${
            selectedTimeTab === 0
              ? 'bg-[#10B981] text-black shadow-md'
              : 'text-[#98ABA1] hover:text-[#F3F7F5]'
          }`}
        >
          Today's Session
        </button>
        <button
          onClick={() => setSelectedTimeTab(1)}
          className={`flex-1 py-1.5 rounded-lg text-xs font-semibold transition-all cursor-pointer ${
            selectedTimeTab === 1
              ? 'bg-[#10B981] text-black shadow-md'
              : 'text-[#98ABA1] hover:text-[#F3F7F5]'
          }`}
        >
          Weekly / All-Time
        </button>
      </div>

      {/* Focus & Flow Overtime Metric Strip */}
      <div className="grid grid-cols-2 gap-3">
        {/* Focused Study */}
        <div className="p-3.5 rounded-2xl bg-[#131916]/90 border border-[#10B981]/40 backdrop-blur-sm">
          <div className="flex items-center space-x-1.5 text-[10px] text-[#64756B] uppercase font-bold tracking-wider">
            <TrendingUp className="w-3.5 h-3.5 text-[#34D399]" />
            <span>Focused Study</span>
          </div>
          <div className="font-mono-digits text-lg font-bold text-[#F3F7F5] mt-1.5">
            {formatDurationHMS(totalStudySeconds)}
          </div>
        </div>

        {/* Flow Overtime */}
        <div className="p-3.5 rounded-2xl bg-[#131916]/90 border border-[#25332B] backdrop-blur-sm">
          <div className="flex items-center space-x-1.5 text-[10px] text-[#64756B] uppercase font-bold tracking-wider">
            <Zap className="w-3.5 h-3.5 text-[#34D399]" />
            <span>Flow Overtime</span>
          </div>
          <div className="font-mono-digits text-lg font-bold text-[#34D399] mt-1.5">
            +{formatDurationHMS(totalExtensionSeconds)}
          </div>
        </div>
      </div>

      {/* Completion vs Interrupted Sessions & Streaks */}
      <div className="p-4 rounded-2xl bg-[#131916]/90 border border-[#25332B] backdrop-blur-sm">
        <div className="grid grid-cols-3 gap-2 text-center">
          <div>
            <div className="text-[10px] text-[#64756B] uppercase font-bold">Completed</div>
            <div className="text-sm font-bold text-[#F3F7F5] mt-0.5">
              {completedSessionsCount} done
            </div>
          </div>

          <div className="border-x border-[#25332B]">
            <div className="text-[10px] text-[#64756B] uppercase font-bold">Interrupted</div>
            <div
              className={`text-sm font-bold mt-0.5 ${
                interruptedSessionsCount > 0 ? 'text-[#FBBF24]' : 'text-[#98ABA1]'
              }`}
            >
              {interruptedSessionsCount} stopped
            </div>
          </div>

          <div>
            <div className="text-[10px] text-[#64756B] uppercase font-bold flex items-center justify-center space-x-1">
              <Flame className="w-3 h-3 text-[#FBBF24]" />
              <span>Streak</span>
            </div>
            <div className="text-sm font-bold text-[#FBBF24] mt-0.5">
              {studyDays} Days
            </div>
          </div>
        </div>
      </div>

      {/* Subject-Wise Breakdown */}
      {subjectMap.length > 0 && (
        <div className="p-4 rounded-2xl bg-[#131916]/90 border border-[#25332B] space-y-3 backdrop-blur-sm">
          <h3 className="text-[10px] font-bold text-[#64756B] tracking-wider uppercase">
            Subject-Wise Study Breakdown
          </h3>
          <div className="space-y-2.5">
            {subjectMap.map(([subjectName, seconds]) => {
              const maxSecs = subjectMap[0][1] || 1;
              const fraction = Math.min(1, seconds / maxSecs);

              return (
                <div key={subjectName} className="space-y-1">
                  <div className="flex items-center justify-between text-xs">
                    <span className="font-semibold text-[#F3F7F5]">{subjectName}</span>
                    <span className="font-mono-digits text-[#34D399]">
                      {formatDurationHMS(seconds)}
                    </span>
                  </div>
                  <div className="w-full bg-[#1C2420] h-1.5 rounded-full overflow-hidden">
                    <div
                      className="bg-[#10B981] h-full rounded-full transition-all duration-500"
                      style={{ width: `${Math.round(fraction * 100)}%` }}
                    />
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Recent Sessions Log */}
      <div className="space-y-2.5 pt-1">
        <h3 className="text-[10px] font-bold text-[#64756B] tracking-wider uppercase">
          Session History Log ({currentLogs.length})
        </h3>

        {currentLogs.length === 0 ? (
          <div className="p-8 text-center rounded-2xl bg-[#131916]/80 border border-[#25332B] space-y-2">
            <History className="w-8 h-8 text-[#64756B] mx-auto" />
            <h4 className="text-sm font-bold text-[#F3F7F5]">No Focus Sessions Recorded</h4>
            <p className="text-xs text-[#98ABA1]">
              Start your study routine to track active time, flow overtime, and milestones.
            </p>
          </div>
        ) : (
          <div className="space-y-2">
            {currentLogs.slice(0, 20).map((log) => {
              const meta = CATEGORY_CONFIG[log.category] || CATEGORY_CONFIG.STUDY;
              const dateStr = new Date(log.completedTimestamp).toLocaleTimeString([], {
                hour: '2-digit',
                minute: '2-digit',
              });

              return (
                <div
                  key={log.sessionId}
                  className="p-3.5 rounded-2xl bg-[#131916]/90 border border-[#25332B] flex items-center justify-between backdrop-blur-sm"
                >
                  <div className="flex-1 min-w-0 pr-3">
                    <div className="flex items-center space-x-1.5 mb-1 text-[10px]">
                      <span
                        className="px-1.5 py-0.2 rounded font-semibold"
                        style={{ backgroundColor: meta.containerColor, color: meta.textColor }}
                      >
                        {meta.displayName}
                      </span>
                      {log.subject && (
                        <span className="text-[#98ABA1] font-medium">• {log.subject}</span>
                      )}
                      <span className="text-[#64756B]">• {dateStr}</span>
                    </div>

                    <h4 className="text-sm font-semibold text-[#F3F7F5] truncate">
                      {log.taskTitle}
                    </h4>

                    {log.notes && (
                      <p className="text-[11px] text-[#64756B] truncate mt-0.5">{log.notes}</p>
                    )}
                  </div>

                  <div className="text-right shrink-0">
                    <div
                      className={`font-mono-digits text-sm font-bold ${
                        log.wasInterrupted ? 'text-[#FBBF24]' : 'text-[#34D399]'
                      }`}
                    >
                      {formatDurationHMS(log.durationSeconds)}
                    </div>
                    {log.wasInterrupted ? (
                      <span className="text-[10px] text-[#FBBF24] font-medium">Interrupted</span>
                    ) : log.extensionSeconds > 0 ? (
                      <span className="text-[10px] text-[#34D399] font-medium">
                        +{formatDurationHMS(log.extensionSeconds)} flow
                      </span>
                    ) : null}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Clear Confirmation Modal */}
      {showClearConfirmDialog && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="w-full max-w-sm rounded-2xl bg-[#131916] border border-[#25332B] p-5 shadow-2xl space-y-3">
            <h3 className="text-base font-bold text-[#F3F7F5]">Clear History Logs?</h3>
            <p className="text-xs text-[#98ABA1] leading-relaxed">
              This will erase recorded session logs and history. Your routines and task structures
              will remain intact.
            </p>
            <div className="flex items-center space-x-2 pt-2">
              <button
                onClick={() => setShowClearConfirmDialog(false)}
                className="flex-1 py-2 rounded-xl bg-[#1C2420] text-xs font-semibold text-[#98ABA1] cursor-pointer"
              >
                Cancel
              </button>
              <button
                onClick={() => {
                  clearAllLogs();
                  setShowClearConfirmDialog(false);
                }}
                className="flex-1 py-2 rounded-xl bg-[#EF4444] text-xs font-bold text-white cursor-pointer"
              >
                Clear History
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
