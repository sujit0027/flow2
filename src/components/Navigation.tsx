import React from 'react';
import { Calendar, ListOrdered, BarChart3, Settings } from 'lucide-react';
import { NavigationTab } from '../types';

interface NavigationProps {
  currentTab: NavigationTab;
  onSelectTab: (tab: NavigationTab) => void;
}

export const Navigation: React.FC<NavigationProps> = ({ currentTab, onSelectTab }) => {
  const tabs: { id: NavigationTab; label: string; icon: React.FC<{ className?: string }> }[] = [
    { id: 'HOME', label: 'Today', icon: Calendar },
    { id: 'ROUTINE', label: 'Routine', icon: ListOrdered },
    { id: 'PROGRESS', label: 'Progress', icon: BarChart3 },
    { id: 'SETTINGS', label: 'Settings', icon: Settings },
  ];

  return (
    <nav
      data-testid="flow_bottom_navigation"
      className="fixed bottom-0 left-0 right-0 z-30 mx-auto max-w-lg border-t border-[#25332B] bg-[#131916]/95 backdrop-blur-md px-3 py-2 rounded-t-2xl shadow-2xl"
    >
      <div className="flex items-center justify-around">
        {tabs.map((tab) => {
          const Icon = tab.icon;
          const isSelected = currentTab === tab.id;

          return (
            <button
              key={tab.id}
              onClick={() => onSelectTab(tab.id)}
              data-testid={`nav_item_${tab.id.toLowerCase()}`}
              className={`flex flex-col items-center justify-center py-1 px-4 rounded-xl transition-all duration-200 cursor-pointer ${
                isSelected
                  ? 'text-[#34D399] font-bold scale-105'
                  : 'text-[#64756B] hover:text-[#98ABA1]'
              }`}
            >
              <div
                className={`p-1 rounded-lg transition-colors ${
                  isSelected ? 'bg-[#132F23]' : 'bg-transparent'
                }`}
              >
                <Icon className="w-5 h-5" />
              </div>
              <span className="text-[11px] mt-0.5 tracking-tight font-medium">
                {tab.label}
              </span>
            </button>
          );
        })}
      </div>
    </nav>
  );
};
