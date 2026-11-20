import React from 'react';
import { X, Bell, CheckCheck, AlertTriangle, AlertOctagon, Info } from 'lucide-react';
import { api } from '../api/client';

export default function NotificationsModal({ 
  isOpen, 
  onClose, 
  notifications = [], 
  onRefresh 
}) {
  if (!isOpen) return null;

  const handleMarkAsRead = async (id) => {
    try {
      await api.notifications.markAsRead(id);
      onRefresh();
    } catch (err) {
      console.warn(err);
    }
  };

  const handleMarkAll = async () => {
    try {
      await api.notifications.markAllAsRead();
      onRefresh();
    } catch (err) {
      console.warn(err);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm animate-in fade-in duration-150">
      <div 
        className="bg-[#FDFCF7] border border-[#D6CAC1] w-full max-w-lg rounded-3xl shadow-2xl p-6 sm:p-7 space-y-4 animate-in zoom-in-95 duration-150"
        onClick={(e) => e.stopPropagation()}
      >
        
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-[#D6CAC1]/50">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-xl bg-[#668B4A]/15 text-[#668B4A] flex items-center justify-center">
              <Bell className="w-4 h-4" />
            </div>
            <div>
              <h3 className="text-base font-black text-[#5A4636]">Budget Alerts & Notifications</h3>
              <p className="text-[11px] text-[#897262]">Automated financial warnings & alerts</p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={handleMarkAll}
              className="text-xs font-semibold text-[#668B4A] hover:underline flex items-center gap-1"
            >
              <CheckCheck className="w-3.5 h-3.5" />
              <span>Mark all read</span>
            </button>
            <button 
              onClick={onClose}
              className="p-1 rounded-lg text-[#897262] hover:text-[#5A4636] hover:bg-[#EAE4DF] transition-colors"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* List */}
        <div className="max-h-96 overflow-y-auto space-y-2.5 pr-1">
          {notifications.length === 0 ? (
            <div className="py-12 text-center text-xs text-[#897262]">
              No alerts at this moment. You are well within all budget limits! 🍵
            </div>
          ) : (
            notifications.map((notif) => {
              const isExceeded = notif.notificationType === 'BUDGET_EXCEEDED';
              return (
                <div 
                  key={notif.id}
                  onClick={() => !notif.isRead && handleMarkAsRead(notif.id)}
                  className={`p-4 rounded-2xl border transition-all cursor-pointer ${
                    notif.isRead 
                      ? 'bg-[#EAE4DF]/30 border-[#D6CAC1]/40 opacity-70' 
                      : 'bg-[#FDFCF7] border-[#668B4A]/50 shadow-sm'
                  }`}
                >
                  <div className="flex items-start gap-3">
                    <div className={`w-8 h-8 rounded-xl flex items-center justify-center shrink-0 ${
                      isExceeded ? 'bg-[#D35400]/15 text-[#D35400]' : 'bg-[#F39C12]/15 text-[#F39C12]'
                    }`}>
                      {isExceeded ? <AlertOctagon className="w-4 h-4" /> : <AlertTriangle className="w-4 h-4" />}
                    </div>

                    <div className="flex-1">
                      <div className="flex items-center justify-between">
                        <h4 className="text-xs font-bold text-[#5A4636]">{notif.title}</h4>
                        {!notif.isRead && (
                          <span className="w-2 h-2 rounded-full bg-[#668B4A]" title="Unread" />
                        )}
                      </div>
                      <p className="text-xs text-[#897262] mt-1">{notif.message}</p>
                      <span className="text-[10px] text-[#897262]/70 mt-1 block">
                        {new Date(notif.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>
                  </div>
                </div>
              );
            })
          )}
        </div>

      </div>
    </div>
  );
}
