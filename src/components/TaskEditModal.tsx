import React, { useState } from 'react';
import { X, Plus, Minus } from 'lucide-react';
import { RoutineTask, TaskCategoryType, CATEGORY_CONFIG } from '../types';
import { formatMinutesDetailed, formatMinutesHuman } from '../utils/storage';

interface TaskEditModalProps {
  taskToEdit: RoutineTask | null;
  onDismiss: () => void;
  onSaveTask: (task: RoutineTask) => void;
}

export const TaskEditModal: React.FC<TaskEditModalProps> = ({
  taskToEdit,
  onDismiss,
  onSaveTask,
}) => {
  const [title, setTitle] = useState(taskToEdit?.title || '');
  const [category, setCategory] = useState<TaskCategoryType>(taskToEdit?.category || 'STUDY');
  const [customCategoryName, setCustomCategoryName] = useState(taskToEdit?.customCategoryName || '');
  const [subject, setSubject] = useState(taskToEdit?.subject || '');
  const [minDuration, setMinDuration] = useState<number>(taskToEdit?.minDurationMinutes || 40);
  const [extensionTarget, setExtensionTarget] = useState<number>(taskToEdit?.extensionTargetMinutes ?? 15);
  const [isExtensionAllowed, setIsExtensionAllowed] = useState(taskToEdit?.isExtensionAllowed ?? true);
  const [isCompulsory, setIsCompulsory] = useState(
    taskToEdit ? taskToEdit.isCompulsory : category === 'STUDY'
  );
  const [isOptional, setIsOptional] = useState(
    taskToEdit ? taskToEdit.isOptional : category !== 'STUDY'
  );
  const [canBeSkipped, setCanBeSkipped] = useState(
    taskToEdit ? taskToEdit.canBeSkipped : category !== 'STUDY'
  );
  const [mealType, setMealType] = useState(taskToEdit?.mealType || '');
  const [notes, setNotes] = useState(taskToEdit?.notes || '');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const currentHours = Math.floor(minDuration / 60);
  const currentMins = minDuration % 60;

  const handleCategoryChange = (cat: TaskCategoryType) => {
    setCategory(cat);
    if (!taskToEdit) {
      const meta = CATEGORY_CONFIG[cat];
      setIsCompulsory(meta.defaultIsCompulsory);
      setIsOptional(meta.defaultIsOptional);
      setCanBeSkipped(meta.defaultIsOptional);
      setMinDuration(meta.defaultDurationMinutes);
    }
  };

  const handleSave = () => {
    if (!title.trim()) {
      setErrorMessage('Please enter a task title');
      return;
    }

    const newTask: RoutineTask = {
      id: taskToEdit?.id || Date.now(),
      routineId: taskToEdit?.routineId || 1,
      title: title.trim(),
      category,
      customCategoryName: customCategoryName.trim(),
      subject: subject.trim(),
      minDurationMinutes: Math.max(5, minDuration),
      extensionTargetMinutes: Math.max(0, extensionTarget),
      isExtensionAllowed,
      isCompulsory,
      isOptional,
      canBeSkipped,
      mealType: mealType.trim().toLowerCase(),
      notes: notes.trim(),
      taskOrder: taskToEdit?.taskOrder || 1,
      isCompleted: taskToEdit?.isCompleted || false,
      accumulatedDurationSeconds: taskToEdit?.accumulatedDurationSeconds || 0,
      createdAt: taskToEdit?.createdAt || Date.now(),
      completedAt: taskToEdit?.completedAt || null,
    };

    onSaveTask(newTask);
    onDismiss();
  };

  const categories: TaskCategoryType[] = ['STUDY', 'BREAK', 'MEAL', 'EXERCISE', 'PERSONAL', 'CUSTOM'];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-black/80 backdrop-blur-md">
      <div className="relative w-full max-w-lg max-h-[92vh] overflow-y-auto rounded-2xl bg-[#131916] border border-[#25332B] p-5 sm:p-6 shadow-2xl flex flex-col text-left animate-in fade-in zoom-in-95 duration-200">
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-[#25332B]">
          <h2 className="text-lg font-bold text-[#F3F7F5]">
            {taskToEdit ? 'Edit Routine Task' : 'New Routine Task'}
          </h2>
          <button
            onClick={onDismiss}
            data-testid="close_task_sheet_button"
            className="p-1.5 rounded-lg text-[#98ABA1] hover:text-[#F3F7F5] hover:bg-[#1C2420] transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="space-y-4 pt-4">
          {/* Title input */}
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-[#98ABA1] mb-1.5">
              Task Title *
            </label>
            <input
              type="text"
              value={title}
              onChange={(e) => {
                setTitle(e.target.value);
                if (e.target.value.trim()) setErrorMessage(null);
              }}
              placeholder="e.g. Wave Optics Theory, Lunch, CET MCQs"
              data-testid="task_title_input"
              className="w-full px-3.5 py-2.5 rounded-xl bg-[#1C2420] border border-[#25332B] text-[#F3F7F5] placeholder-[#64756B] focus:outline-none focus:border-[#10B981] transition-colors text-sm"
            />
            {errorMessage && (
              <p className="text-xs text-[#EF4444] mt-1">{errorMessage}</p>
            )}
          </div>

          {/* Subject / Topic input */}
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-[#98ABA1] mb-1.5">
              Subject or Topic (Optional)
            </label>
            <input
              type="text"
              value={subject}
              onChange={(e) => setSubject(e.target.value)}
              placeholder="e.g. Physics, Chemistry, Test Prep, Revision"
              data-testid="task_subject_input"
              className="w-full px-3.5 py-2.5 rounded-xl bg-[#1C2420] border border-[#25332B] text-[#F3F7F5] placeholder-[#64756B] focus:outline-none focus:border-[#10B981] transition-colors text-sm"
            />
          </div>

          {/* Category Selector Chips */}
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-[#98ABA1] mb-2">
              Category
            </label>
            <div className="flex flex-wrap gap-2">
              {categories.map((cat) => {
                const isSelected = category === cat;
                const meta = CATEGORY_CONFIG[cat];
                return (
                  <button
                    key={cat}
                    type="button"
                    onClick={() => handleCategoryChange(cat)}
                    data-testid={`select_category_${cat}`}
                    className={`flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold border transition-all cursor-pointer ${
                      isSelected
                        ? 'border-transparent'
                        : 'bg-[#1C2420] border-[#25332B] text-[#98ABA1] hover:border-[#64756B]'
                    }`}
                    style={
                      isSelected
                        ? {
                            backgroundColor: meta.containerColor,
                            color: meta.textColor,
                            borderColor: meta.accentColor,
                          }
                        : undefined
                    }
                  >
                    <span
                      className="w-2 h-2 rounded-full"
                      style={{ backgroundColor: meta.accentColor }}
                    />
                    <span>{meta.displayName}</span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Custom Category input */}
          {category === 'CUSTOM' && (
            <div>
              <input
                type="text"
                value={customCategoryName}
                onChange={(e) => setCustomCategoryName(e.target.value)}
                placeholder="Enter custom category name (e.g. Coding, Reading)"
                className="w-full px-3.5 py-2 rounded-xl bg-[#1C2420] border border-[#25332B] text-[#F3F7F5] placeholder-[#64756B] focus:outline-none focus:border-[#10B981] transition-colors text-xs"
              />
            </div>
          )}

          {/* Meal Recurrence Selector if MEAL */}
          {category === 'MEAL' && (
            <div>
              <label className="block text-[11px] font-bold uppercase tracking-wider text-[#98ABA1] mb-1.5">
                Meal Recurrence Type
              </label>
              <div className="grid grid-cols-4 gap-2">
                {['breakfast', 'lunch', 'dinner', 'snack'].map((typeKey) => {
                  const isSelected = mealType.toLowerCase() === typeKey;
                  return (
                    <button
                      key={typeKey}
                      type="button"
                      onClick={() => setMealType(typeKey)}
                      className={`py-1.5 rounded-lg text-xs font-medium capitalize border transition-all cursor-pointer ${
                        isSelected
                          ? 'bg-[#26322C] border-[#34D399] text-[#34D399]'
                          : 'bg-[#1C2420] border-[#25332B] text-[#98ABA1] hover:text-[#F3F7F5]'
                      }`}
                    >
                      {typeKey}
                    </button>
                  );
                })}
              </div>
            </div>
          )}

          {/* Compulsory Minimum Duration Section */}
          <div className="p-3.5 rounded-xl bg-[#1C2420] border border-[#25332B] space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-bold uppercase tracking-wider text-[#98ABA1]">
                Compulsory Minimum Duration
              </span>
              <span className="font-mono-digits text-sm font-bold text-[#34D399]">
                {formatMinutesDetailed(minDuration)}
              </span>
            </div>

            {/* Quick Presets */}
            <div className="flex flex-wrap gap-1.5">
              {[1, 5, 15, 25, 30, 45, 60, 90, 120, 150, 180].map((mins) => {
                const isSelected = minDuration === mins;
                return (
                  <button
                    key={mins}
                    type="button"
                    onClick={() => setMinDuration(mins)}
                    data-testid={`min_duration_${mins}_chip`}
                    className={`px-2.5 py-1 rounded-md text-xs font-medium border transition-colors cursor-pointer ${
                      isSelected
                        ? 'bg-[#10B981]/20 border-[#10B981] text-[#34D399]'
                        : 'bg-[#131916] border-[#25332B] text-[#98ABA1] hover:text-[#F3F7F5]'
                    }`}
                  >
                    {mins >= 60 ? `${Math.floor(mins / 60)}h${mins % 60 ? ` ${mins % 60}m` : ''}` : `${mins}m`}
                  </button>
                );
              })}
            </div>

            {/* Interactive Hours & Minutes Stepper */}
            <div className="grid grid-cols-2 gap-2 pt-1">
              {/* Hours Stepper */}
              <div className="flex flex-col items-center p-2 rounded-lg bg-[#0C100E] border border-[#25332B]">
                <span className="text-[10px] text-[#64756B] font-bold uppercase">Hours</span>
                <div className="flex items-center justify-between w-full mt-1 px-1">
                  <button
                    type="button"
                    onClick={() => {
                      if (currentHours > 0) {
                        setMinDuration((currentHours - 1) * 60 + currentMins);
                      }
                    }}
                    disabled={currentHours <= 0}
                    data-testid="decrement_hours_button"
                    className="p-1 rounded bg-[#1C2420] text-[#98ABA1] hover:text-[#F3F7F5] disabled:opacity-30 cursor-pointer"
                  >
                    <Minus className="w-3.5 h-3.5" />
                  </button>
                  <span className="text-xs font-bold text-[#34D399]">
                    {currentHours} hr{currentHours !== 1 ? 's' : ''}
                  </span>
                  <button
                    type="button"
                    onClick={() => {
                      if (currentHours < 12) {
                        setMinDuration((currentHours + 1) * 60 + currentMins);
                      }
                    }}
                    data-testid="increment_hours_button"
                    className="p-1 rounded bg-[#1C2420] text-[#98ABA1] hover:text-[#F3F7F5] cursor-pointer"
                  >
                    <Plus className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>

              {/* Minutes Stepper */}
              <div className="flex flex-col items-center p-2 rounded-lg bg-[#0C100E] border border-[#25332B]">
                <span className="text-[10px] text-[#64756B] font-bold uppercase">Minutes</span>
                <div className="flex items-center justify-between w-full mt-1 px-1">
                  <button
                    type="button"
                    onClick={() => {
                      const newMins = Math.max(0, currentMins - 1);
                      setMinDuration(Math.max(1, currentHours * 60 + newMins));
                    }}
                    data-testid="decrement_minutes_button"
                    className="p-1 rounded bg-[#1C2420] text-[#98ABA1] hover:text-[#F3F7F5] cursor-pointer"
                  >
                    <Minus className="w-3.5 h-3.5" />
                  </button>
                  <span className="text-xs font-bold text-[#34D399]">{currentMins} min</span>
                  <button
                    type="button"
                    onClick={() => {
                      const newMins = (currentMins + 1) % 60;
                      const addHours = currentMins + 1 >= 60 ? 1 : 0;
                      setMinDuration((currentHours + addHours) * 60 + newMins);
                    }}
                    data-testid="increment_minutes_button"
                    className="p-1 rounded bg-[#1C2420] text-[#98ABA1] hover:text-[#F3F7F5] cursor-pointer"
                  >
                    <Plus className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            </div>

            {/* Quick Additive Pills */}
            <div className="grid grid-cols-4 gap-1.5 pt-1">
              {[15, 30, 60, 120].map((addMins) => (
                <button
                  key={addMins}
                  type="button"
                  onClick={() => setMinDuration((prev) => Math.min(720, prev + addMins))}
                  className="py-1 rounded bg-[#26322C] border border-[#25332B] text-[11px] text-[#98ABA1] hover:text-[#34D399] transition-colors cursor-pointer"
                >
                  +{addMins >= 60 ? `${addMins / 60}h` : `${addMins}m`}
                </button>
              ))}
            </div>
          </div>

          {/* Optional Flow Extension Target */}
          <div>
            <div className="flex items-center justify-between mb-1.5">
              <span className="text-[11px] font-bold uppercase tracking-wider text-[#98ABA1]">
                Optional Extension Target (Flow)
              </span>
              <span className="text-xs text-[#34D399] font-medium">
                {extensionTarget > 0 ? `+${formatMinutesHuman(extensionTarget)}` : 'Open-ended'}
              </span>
            </div>
            <div className="flex flex-wrap gap-1.5">
              {[0, 15, 30, 45, 60, 90, 120].map((ext) => {
                const isSelected = extensionTarget === ext;
                return (
                  <button
                    key={ext}
                    type="button"
                    onClick={() => setExtensionTarget(ext)}
                    data-testid={`extension_target_${ext}_chip`}
                    className={`px-2.5 py-1 rounded-md text-xs font-medium border transition-colors cursor-pointer ${
                      isSelected
                        ? 'bg-[#26322C] border-[#34D399] text-[#34D399]'
                        : 'bg-[#1C2420] border-[#25332B] text-[#98ABA1] hover:text-[#F3F7F5]'
                    }`}
                  >
                    {ext === 0 ? 'None' : `+${ext >= 60 ? `${ext / 60}h` : `${ext}m`}`}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Configuration Switches */}
          <div className="p-3.5 rounded-xl bg-[#1C2420] border border-[#25332B] space-y-3">
            {/* Compulsory Switch */}
            <div className="flex items-center justify-between">
              <div>
                <div className="text-xs font-semibold text-[#F3F7F5]">Compulsory Minimum</div>
                <div className="text-[11px] text-[#98ABA1]">
                  Mandatory before task is marked completed
                </div>
              </div>
              <input
                type="checkbox"
                checked={isCompulsory}
                onChange={(e) => setIsCompulsory(e.target.checked)}
                data-testid="compulsory_switch"
                className="w-4 h-4 accent-[#10B981] rounded cursor-pointer"
              />
            </div>

            <div className="h-px bg-[#25332B]" />

            {/* Allow Extension Switch */}
            <div className="flex items-center justify-between">
              <div>
                <div className="text-xs font-semibold text-[#F3F7F5]">Allow Flow Extension</div>
                <div className="text-[11px] text-[#98ABA1]">
                  Allows continuing focus into overtime
                </div>
              </div>
              <input
                type="checkbox"
                checked={isExtensionAllowed}
                onChange={(e) => setIsExtensionAllowed(e.target.checked)}
                className="w-4 h-4 accent-[#10B981] rounded cursor-pointer"
              />
            </div>

            <div className="h-px bg-[#25332B]" />

            {/* Can Be Skipped Switch */}
            <div className="flex items-center justify-between">
              <div>
                <div className="text-xs font-semibold text-[#F3F7F5]">Can Be Skipped</div>
                <div className="text-[11px] text-[#98ABA1]">
                  Ideal for optional breaks and flexible meals
                </div>
              </div>
              <input
                type="checkbox"
                checked={canBeSkipped}
                onChange={(e) => {
                  setCanBeSkipped(e.target.checked);
                  setIsOptional(e.target.checked);
                }}
                className="w-4 h-4 accent-[#10B981] rounded cursor-pointer"
              />
            </div>
          </div>

          {/* Notes / Intent */}
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-[#98ABA1] mb-1.5">
              Notes & Study Intent (Optional)
            </label>
            <textarea
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="e.g. Complete chapter exercises 1 to 15, formulas revision"
              data-testid="task_notes_input"
              rows={2}
              className="w-full px-3.5 py-2 rounded-xl bg-[#1C2420] border border-[#25332B] text-[#F3F7F5] placeholder-[#64756B] focus:outline-none focus:border-[#10B981] transition-colors text-xs resize-none"
            />
          </div>

          {/* Action buttons */}
          <div className="pt-2">
            <button
              type="button"
              onClick={handleSave}
              data-testid="save_task_submit_button"
              className="w-full py-3 rounded-xl bg-[#10B981] hover:bg-[#34D399] text-black font-bold text-sm shadow-lg transition-colors cursor-pointer"
            >
              {taskToEdit ? 'Save Changes' : 'Add to Routine'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
