import React, { useState } from 'react';
import {
  Plus,
  ChevronUp,
  ChevronDown,
  Edit2,
  Copy,
  Trash2,
  ListOrdered,
} from 'lucide-react';
import { useFlowStudy } from '../context/FlowStudyContext';
import { CATEGORY_CONFIG, RoutineTask } from '../types';
import { formatMinutesHuman } from '../utils/storage';

export const RoutineScreen: React.FC = () => {
  const {
    tasks,
    openCreateTask,
    openEditTask,
    duplicateTask,
    deleteTask,
    moveTaskUp,
    moveTaskDown,
    loadPreset,
  } = useFlowStudy();

  const [taskToDelete, setTaskToDelete] = useState<RoutineTask | null>(null);
  const [presetToLoad, setPresetToLoad] = useState<string | null>(null);

  const presets = ['Balanced Flow', 'Deep Work Focus', 'Fast Exam Sprint'];

  return (
    <div className="relative z-10 mx-auto max-w-lg px-4 pt-4 pb-28 space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-[#F3F7F5]">Routine Sequence</h1>
          <p className="text-xs text-[#98ABA1]">Custom task order & durations</p>
        </div>

        <button
          onClick={openCreateTask}
          data-testid="routine_header_add_button"
          className="flex items-center space-x-1.5 px-3 py-1.5 rounded-xl bg-[#132F23] border border-[#10B981]/40 text-[#34D399] font-bold text-xs hover:bg-[#10B981] hover:text-black transition-colors cursor-pointer"
        >
          <Plus className="w-4 h-4" />
          <span>New</span>
        </button>
      </div>

      {/* Preset Quick Switcher */}
      <div className="space-y-1.5">
        <span className="text-[10px] text-[#64756B] uppercase font-bold tracking-wider">
          Preset Templates
        </span>
        <div className="flex items-center space-x-2 overflow-x-auto pb-1 scrollbar-none">
          {presets.map((preset) => (
            <button
              key={preset}
              onClick={() => setPresetToLoad(preset)}
              data-testid={`preset_${preset.replace(/ /g, '_')}`}
              className="px-3 py-1.5 rounded-xl bg-[#1C2420] border border-[#25332B] text-xs text-[#98ABA1] hover:text-[#34D399] hover:border-[#10B981]/50 shrink-0 transition-colors cursor-pointer font-medium"
            >
              {preset}
            </button>
          ))}
        </div>
      </div>

      {/* Tasks List */}
      {tasks.length === 0 ? (
        <div className="p-8 text-center rounded-2xl bg-[#131916]/80 border border-[#25332B] space-y-3">
          <ListOrdered className="w-10 h-10 text-[#64756B] mx-auto" />
          <h4 className="text-sm font-bold text-[#F3F7F5]">No Tasks in Routine</h4>
          <p className="text-xs text-[#98ABA1]">
            Add tasks to craft your personal daily study flow, or load a preset.
          </p>
          <button
            onClick={openCreateTask}
            className="px-4 py-2 rounded-xl bg-[#10B981] text-black font-bold text-xs cursor-pointer"
          >
            Add First Task
          </button>
        </div>
      ) : (
        <div className="space-y-2.5">
          {tasks.map((task, index) => {
            const meta = CATEGORY_CONFIG[task.category] || CATEGORY_CONFIG.STUDY;
            const isFirst = index === 0;
            const isLast = index === tasks.length - 1;

            return (
              <div
                key={task.id}
                data-testid={`routine_management_card_${task.id}`}
                className="p-3.5 rounded-2xl bg-[#131916]/95 border border-[#25332B] flex items-center justify-between backdrop-blur-sm"
              >
                {/* Reorder Buttons Column */}
                <div className="flex flex-col items-center justify-center mr-3 shrink-0">
                  <button
                    onClick={() => moveTaskUp(task)}
                    disabled={isFirst}
                    data-testid={`move_up_task_${task.id}`}
                    className="p-1 rounded text-[#98ABA1] hover:text-[#34D399] disabled:opacity-20 cursor-pointer"
                    title="Move Up"
                  >
                    <ChevronUp className="w-4 h-4" />
                  </button>
                  <span className="font-mono-digits text-[11px] text-[#64756B] font-bold my-0.5">
                    #{index + 1}
                  </span>
                  <button
                    onClick={() => moveTaskDown(task)}
                    disabled={isLast}
                    data-testid={`move_down_task_${task.id}`}
                    className="p-1 rounded text-[#98ABA1] hover:text-[#34D399] disabled:opacity-20 cursor-pointer"
                    title="Move Down"
                  >
                    <ChevronDown className="w-4 h-4" />
                  </button>
                </div>

                {/* Task Details */}
                <div
                  className="flex-1 min-w-0 pr-2 cursor-pointer"
                  onClick={() => openEditTask(task)}
                >
                  <div className="flex items-center space-x-1.5 mb-1">
                    <span
                      className="px-1.5 py-0.2 rounded text-[10px] font-semibold"
                      style={{ backgroundColor: meta.containerColor, color: meta.textColor }}
                    >
                      {meta.displayName}
                    </span>
                    <span
                      className={`px-1.5 py-0.2 rounded text-[9px] font-bold uppercase tracking-wider ${
                        task.isCompulsory
                          ? 'bg-[#3A2111] text-[#FDBA74]'
                          : 'bg-[#1C2420] text-[#7DD3FC]'
                      }`}
                    >
                      {task.isCompulsory ? 'Compulsory' : 'Flexible'}
                    </span>
                  </div>

                  <h3 className="text-sm font-semibold text-[#F3F7F5] truncate">{task.title}</h3>

                  <div className="flex items-center space-x-2 text-[11px] text-[#64756B] mt-0.5">
                    <span className="font-mono-digits text-[#34D399]">
                      Min: {formatMinutesHuman(task.minDurationMinutes)}
                    </span>
                    {task.extensionTargetMinutes > 0 && (
                      <span>• Flow: +{formatMinutesHuman(task.extensionTargetMinutes)}</span>
                    )}
                  </div>
                </div>

                {/* Actions: Edit, Duplicate, Delete */}
                <div className="flex items-center space-x-1 shrink-0">
                  <button
                    onClick={() => openEditTask(task)}
                    data-testid={`edit_task_button_${task.id}`}
                    className="p-2 rounded-lg text-[#98ABA1] hover:text-[#34D399] hover:bg-[#1C2420] transition-colors cursor-pointer"
                    title="Edit Task"
                  >
                    <Edit2 className="w-4 h-4" />
                  </button>
                  <button
                    onClick={() => duplicateTask(task)}
                    data-testid={`duplicate_task_button_${task.id}`}
                    className="p-2 rounded-lg text-[#98ABA1] hover:text-[#34D399] hover:bg-[#1C2420] transition-colors cursor-pointer"
                    title="Duplicate Task"
                  >
                    <Copy className="w-4 h-4" />
                  </button>
                  <button
                    onClick={() => setTaskToDelete(task)}
                    data-testid={`delete_task_button_${task.id}`}
                    className="p-2 rounded-lg text-[#64756B] hover:text-[#EF4444] hover:bg-[#1C2420] transition-colors cursor-pointer"
                    title="Delete Task"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Floating Action Button */}
      <button
        onClick={openCreateTask}
        data-testid="routine_fab_add_task"
        className="fixed bottom-20 right-6 z-30 p-3.5 rounded-full bg-[#10B981] hover:bg-[#34D399] text-black shadow-2xl transition-transform hover:scale-110 active:scale-95 cursor-pointer"
        title="Add Routine Task"
      >
        <Plus className="w-6 h-6 stroke-[2.5]" />
      </button>

      {/* Delete Confirmation Modal */}
      {taskToDelete && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="w-full max-w-sm rounded-2xl bg-[#131916] border border-[#25332B] p-5 shadow-2xl space-y-3">
            <h3 className="text-base font-bold text-[#F3F7F5]">Delete Task?</h3>
            <p className="text-xs text-[#98ABA1] leading-relaxed">
              Are you sure you want to remove "{taskToDelete.title}" from your routine?
            </p>
            <div className="flex items-center space-x-2 pt-2">
              <button
                onClick={() => setTaskToDelete(null)}
                className="flex-1 py-2 rounded-xl bg-[#1C2420] text-xs font-semibold text-[#98ABA1] cursor-pointer"
              >
                Cancel
              </button>
              <button
                onClick={() => {
                  deleteTask(taskToDelete);
                  setTaskToDelete(null);
                }}
                className="flex-1 py-2 rounded-xl bg-[#EF4444] text-xs font-bold text-white cursor-pointer"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Load Preset Confirmation Modal */}
      {presetToLoad && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="w-full max-w-sm rounded-2xl bg-[#131916] border border-[#25332B] p-5 shadow-2xl space-y-3">
            <h3 className="text-base font-bold text-[#F3F7F5]">Load "{presetToLoad}"?</h3>
            <p className="text-xs text-[#98ABA1] leading-relaxed">
              This will replace your current routine with the "{presetToLoad}" template.
            </p>
            <div className="flex items-center space-x-2 pt-2">
              <button
                onClick={() => setPresetToLoad(null)}
                className="flex-1 py-2 rounded-xl bg-[#1C2420] text-xs font-semibold text-[#98ABA1] cursor-pointer"
              >
                Cancel
              </button>
              <button
                onClick={() => {
                  loadPreset(presetToLoad);
                  setPresetToLoad(null);
                }}
                className="flex-1 py-2 rounded-xl bg-[#10B981] text-xs font-bold text-black cursor-pointer"
              >
                Load Preset
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
