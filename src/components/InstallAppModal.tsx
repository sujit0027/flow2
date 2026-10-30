import React, { useState, useEffect } from 'react';
import { Download, Smartphone, Copy, Check, X, Sparkles, CheckCircle2 } from 'lucide-react';

interface InstallAppModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const InstallAppModal: React.FC<InstallAppModalProps> = ({ isOpen, onClose }) => {
  const [deferredPrompt, setDeferredPrompt] = useState<any>(null);
  const [isCopied, setIsCopied] = useState(false);
  const [isInstalled, setIsInstalled] = useState(false);

  useEffect(() => {
    // Check if running in standalone mode (already installed)
    if (window.matchMedia('(display-mode: standalone)').matches || (window.navigator as any).standalone) {
      setIsInstalled(true);
    }

    const handleBeforeInstallPrompt = (e: Event) => {
      e.preventDefault();
      setDeferredPrompt(e);
    };

    window.addEventListener('beforeinstallprompt', handleBeforeInstallPrompt);

    return () => {
      window.removeEventListener('beforeinstallprompt', handleBeforeInstallPrompt);
    };
  }, []);

  if (!isOpen) return null;

  const appUrl = window.location.origin.includes('localhost')
    ? 'https://ais-pre-ovqtydmr5q2tx323j3jmls-345237109087.asia-east1.run.app'
    : window.location.origin;

  const handleInstallClick = async () => {
    if (deferredPrompt) {
      deferredPrompt.prompt();
      const choiceResult = await deferredPrompt.userChoice;
      if (choiceResult.outcome === 'accepted') {
        setIsInstalled(true);
        setDeferredPrompt(null);
      }
    }
  };

  const handleCopy = () => {
    navigator.clipboard.writeText(appUrl);
    setIsCopied(true);
    setTimeout(() => setIsCopied(false), 2000);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fade-in">
      <div className="relative w-full max-w-md rounded-2xl bg-[#131A16] border border-[#23352B] p-5 shadow-2xl space-y-4 max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-[#23352B]">
          <div className="flex items-center space-x-2.5">
            <div className="w-10 h-10 rounded-xl bg-[#10B981]/15 border border-[#10B981]/40 flex items-center justify-center text-[#10B981]">
              <Smartphone className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-[#F3F7F5] flex items-center gap-1.5">
                Install FlowStudy
                <span className="text-[10px] uppercase tracking-wider px-2 py-0.5 rounded-full bg-[#10B981]/20 text-[#34D399] font-bold">
                  Android PWA
                </span>
              </h2>
              <p className="text-xs text-[#98ABA1]">Bina Play Store / PC ke seedha phone me install karein</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-[#98ABA1] hover:text-[#F3F7F5] hover:bg-[#1E2A23] transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* 1-Click Install Button if native prompt is supported */}
        {deferredPrompt && (
          <button
            onClick={handleInstallClick}
            className="w-full py-3 px-4 rounded-xl bg-[#10B981] hover:bg-[#059669] text-black font-bold flex items-center justify-center space-x-2 shadow-lg shadow-[#10B981]/20 transition-all active:scale-[0.98]"
          >
            <Download className="w-5 h-5 text-black" />
            <span>Install on this Device Now</span>
          </button>
        )}

        {isInstalled && (
          <div className="p-3 rounded-xl bg-[#10B981]/15 border border-[#10B981]/30 flex items-center space-x-2.5 text-[#34D399]">
            <CheckCircle2 className="w-5 h-5 shrink-0" />
            <span className="text-xs font-semibold">FlowStudy is already installed on this device!</span>
          </div>
        )}

        {/* Direct Phone Install Instructions */}
        <div className="space-y-3">
          <h3 className="text-xs font-semibold uppercase tracking-wider text-[#98ABA1] flex items-center gap-1.5">
            <Sparkles className="w-3.5 h-3.5 text-[#10B981]" />
            Android Phone me 1 Minute me Install karne ka tarika:
          </h3>

          <div className="space-y-2 text-xs">
            <div className="p-3 rounded-xl bg-[#18231D] border border-[#23352B] flex items-start space-x-3">
              <span className="w-5 h-5 rounded-full bg-[#10B981]/20 text-[#10B981] font-bold flex items-center justify-center shrink-0 mt-0.5">
                1
              </span>
              <div>
                <p className="font-semibold text-[#F3F7F5]">Phone ke Chrome browser me yeh link open karein:</p>
                <div className="mt-1.5 flex items-center space-x-1.5">
                  <input
                    readOnly
                    value={appUrl}
                    className="w-full text-[11px] font-mono bg-[#0C100E] border border-[#23352B] rounded-lg px-2.5 py-1.5 text-[#98ABA1] select-all truncate"
                  />
                  <button
                    onClick={handleCopy}
                    className="px-2.5 py-1.5 rounded-lg bg-[#23352B] hover:bg-[#2F4538] text-xs font-medium text-[#F3F7F5] flex items-center space-x-1 shrink-0 transition-colors"
                  >
                    {isCopied ? <Check className="w-3.5 h-3.5 text-[#10B981]" /> : <Copy className="w-3.5 h-3.5" />}
                    <span>{isCopied ? 'Copied' : 'Copy'}</span>
                  </button>
                </div>
              </div>
            </div>

            <div className="p-3 rounded-xl bg-[#18231D] border border-[#23352B] flex items-start space-x-3">
              <span className="w-5 h-5 rounded-full bg-[#10B981]/20 text-[#10B981] font-bold flex items-center justify-center shrink-0 mt-0.5">
                2
              </span>
              <div className="text-[#98ABA1] leading-relaxed">
                <p className="font-semibold text-[#F3F7F5]">Chrome me Top-Right corner par 3 Dots (⋮) dabayein</p>
                <p className="mt-0.5">Menu me <span className="text-[#10B981] font-semibold">"Install app"</span> ya <span className="text-[#10B981] font-semibold">"Add to Home screen"</span> par tap karein.</p>
              </div>
            </div>

            <div className="p-3 rounded-xl bg-[#18231D] border border-[#23352B] flex items-start space-x-3">
              <span className="w-5 h-5 rounded-full bg-[#10B981]/20 text-[#10B981] font-bold flex items-center justify-center shrink-0 mt-0.5">
                3
              </span>
              <div className="text-[#98ABA1] leading-relaxed">
                <p className="font-semibold text-[#F3F7F5]">"Install" par tap karein — bas ho gaya!</p>
                <p className="mt-0.5">Yeh app aapke phone ke icon list (App Drawer) aur Home screen par as a standalone real Android app install ho jayegi (bina kisi PC ya USB cable ke).</p>
              </div>
            </div>
          </div>
        </div>

        {/* Why top-right button differed explanation */}
        <div className="p-3 rounded-xl bg-[#0C100E] border border-[#23352B]/60 space-y-1.5 text-[11px] text-[#98ABA1]">
          <p className="font-semibold text-[#F3F7F5]">ℹ️ Pehle wale photo me upar 'Install' button kyu tha?</p>
          <p className="leading-relaxed">
            Pehle wale screenshot me AI Studio ka built-in virtual Android emulator chal raha tha, isliye AI Studio ke top bar me emulator ka Install button tha. Ab yeh production web server par live hai, jisse aap directly apne kisi bhi Android phone me upar diye gaye tarike se install kar sakte hain!
          </p>
        </div>

        {/* Close Button */}
        <button
          onClick={onClose}
          className="w-full py-2.5 rounded-xl bg-[#1E2A23] hover:bg-[#26372D] text-[#F3F7F5] text-xs font-semibold transition-colors"
        >
          Close
        </button>
      </div>
    </div>
  );
};
