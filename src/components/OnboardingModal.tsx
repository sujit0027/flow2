import React, { useState } from 'react';
import { Hourglass, School, Check, Clock } from 'lucide-react';
import { AppThemeConfig } from '../types';

interface OnboardingModalProps {
  onFinish: (timerStyle: AppThemeConfig['timerStyle'], dailyHours: number) => void;
}

export const OnboardingModal: React.FC<OnboardingModalProps> = ({ onFinish }) => {
  const [step, setStep] = useState(0);
  const [timerStyle, setTimerStyle] = useState<AppThemeConfig['timerStyle']>('CIRCULAR');
  const [dailyHours, setDailyHours] = useState(4);

  const timerStyles: { id: AppThemeConfig['timerStyle']; label: string; desc: string }[] = [
    { id: 'CIRCULAR', label: 'Circular Progress Ring', desc: 'Dial that fills as you approach and surpass minimum target' },
    { id: 'DIGITAL', label: 'Digital Chronograph Countdown', desc: 'Large high-contrast numerical display' },
    { id: 'STOPWATCH', label: 'Elapsed Active Stopwatch', desc: 'Forward-counting pure focus chronograph' },
    { id: 'TEXT_ONLY', label: 'Ultra Minimal Text', desc: 'Distraction-free pure typography' },
  ];

  const handleNext = () => {
    if (step === 3) {
      onFinish(timerStyle, dailyHours);
    } else {
      setStep((prev) => prev + 1);
    }
  };

  return (
    <div
      data-testid="onboarding_screen"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-[#0C100E]"
    >
      <div className="w-full max-w-md flex flex-col justify-between min-h-[500px] text-center p-6 rounded-2xl bg-[#131916] border border-[#25332B] shadow-2xl">
        {/* Step Indicator */}
        <div className="flex items-center justify-center space-x-2 pt-2">
          {[0, 1, 2, 3].map((index) => (
            <div
              key={index}
              className={`h-2 rounded-full transition-all duration-300 ${
                index === step ? 'w-7 bg-[#10B981]' : 'w-2 bg-[#25332B]'
              }`}
            />
          ))}
        </div>

        {/* Step Content */}
        <div className="my-auto py-6">
          {step === 0 && (
            <div className="space-y-4 animate-in fade-in zoom-in-95 duration-200">
              <div className="w-16 h-16 mx-auto rounded-full bg-[#132F23] border border-[#10B981]/30 flex items-center justify-center text-[#34D399] shadow-lg">
                <Hourglass className="w-8 h-8" />
              </div>
              <h2 className="text-2xl font-bold text-[#F3F7F5]">Welcome to FlowStudy</h2>
              <p className="text-sm font-semibold text-[#34D399]">
                Task-Based Flexible Flow, Not Rigid Clocks
              </p>
              <p className="text-xs text-[#98ABA1] leading-relaxed max-w-sm mx-auto">
                Standard timetables fail when a single session runs 5 minutes over. In FlowStudy,
                tasks have a compulsory minimum target. Once reached, you're in Flow State: study
                longer or finish on your own terms.
              </p>
            </div>
          )}

          {step === 1 && (
            <div className="space-y-4 animate-in fade-in zoom-in-95 duration-200">
              <div className="w-16 h-16 mx-auto rounded-full bg-[#132F23] border border-[#10B981]/30 flex items-center justify-center text-[#34D399] shadow-lg">
                <School className="w-8 h-8" />
              </div>
              <h2 className="text-2xl font-bold text-[#F3F7F5]">Ready-To-Edit Routine</h2>
              <p className="text-sm font-semibold text-[#34D399]">
                Physics, Chemistry & Revision Prepopulated
              </p>
              <p className="text-xs text-[#98ABA1] leading-relaxed max-w-sm mx-auto">
                We've loaded an editable student routine including Physics Theory, Questions, CET
                MCQs, Weak Topics, and Flexible Lunch/Breaks. You can customize, reorder, or replace
                any task.
              </p>
            </div>
          )}

          {step === 2 && (
            <div className="space-y-4 animate-in fade-in zoom-in-95 duration-200 text-left">
              <div className="text-center">
                <h2 className="text-xl font-bold text-[#F3F7F5]">Choose Timer Appearance</h2>
                <p className="text-xs text-[#98ABA1] mt-1">Select your preferred visual style</p>
              </div>

              <div className="space-y-2 mt-4">
                {timerStyles.map((style) => {
                  const isSelected = timerStyle === style.id;
                  return (
                    <div
                      key={style.id}
                      onClick={() => setTimerStyle(style.id)}
                      className={`p-3 rounded-xl border transition-all cursor-pointer flex items-center justify-between ${
                        isSelected
                          ? 'bg-[#132F23] border-[#10B981]'
                          : 'bg-[#1C2420] border-[#25332B] hover:border-[#64756B]'
                      }`}
                    >
                      <div>
                        <div
                          className={`text-sm font-semibold ${
                            isSelected ? 'text-[#34D399]' : 'text-[#F3F7F5]'
                          }`}
                        >
                          {style.label}
                        </div>
                        <div className="text-[11px] text-[#98ABA1]">{style.desc}</div>
                      </div>
                      {isSelected && <Check className="w-4 h-4 text-[#10B981] shrink-0 ml-2" />}
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {step === 3 && (
            <div className="space-y-4 animate-in fade-in zoom-in-95 duration-200">
              <div className="w-16 h-16 mx-auto rounded-full bg-[#132F23] border border-[#10B981]/30 flex items-center justify-center text-[#34D399] shadow-lg">
                <Clock className="w-8 h-8" />
              </div>
              <h2 className="text-2xl font-bold text-[#F3F7F5]">Daily Study Target</h2>
              <p className="text-xs text-[#98ABA1] leading-relaxed max-w-sm mx-auto">
                How many hours of focused study do you aim for each day?
              </p>

              <div className="flex items-center justify-center space-x-3 py-3">
                {[2, 3, 4, 6, 8].map((hours) => (
                  <button
                    key={hours}
                    onClick={() => setDailyHours(hours)}
                    className={`w-12 h-12 rounded-xl font-bold text-sm border transition-all cursor-pointer ${
                      dailyHours === hours
                        ? 'bg-[#10B981] text-black border-[#10B981] shadow-lg scale-105'
                        : 'bg-[#1C2420] text-[#98ABA1] border-[#25332B] hover:text-[#F3F7F5]'
                    }`}
                  >
                    {hours}h
                  </button>
                ))}
              </div>

              <p className="text-[11px] text-[#64756B]">
                FlowStudy tracks both compulsory milestones and bonus flow overtime.
              </p>
            </div>
          )}
        </div>

        {/* Buttons */}
        <div className="flex items-center space-x-3 pt-4 border-t border-[#25332B]">
          {step > 0 && (
            <button
              onClick={() => setStep((prev) => prev - 1)}
              className="flex-1 py-3 rounded-xl bg-[#1C2420] hover:bg-[#26322C] text-[#98ABA1] font-semibold text-sm transition-colors cursor-pointer"
            >
              Back
            </button>
          )}
          <button
            onClick={handleNext}
            data-testid="onboarding_next_button"
            className="flex-1 py-3 rounded-xl bg-[#10B981] hover:bg-[#34D399] text-black font-bold text-sm shadow-lg transition-colors cursor-pointer"
          >
            {step === 3 ? 'Start FlowStudy' : 'Next'}
          </button>
        </div>
      </div>
    </div>
  );
};
