import React, { useState } from 'react';
import { 
  CalendarClock, 
  Plus, 
  Trash2, 
  CreditCard, 
  CheckCircle2, 
  AlertCircle,
  Clock,
  Sparkles
} from 'lucide-react';
import { api } from '../api/client';

export default function RecurringView({ 
  recurringList = [], 
  categories = [], 
  onRefreshRecurring, 
  onOpenAddRecurringModal 
}) {
  const [isDeleting, setIsDeleting] = useState(null);

  const totalMonthlyRecurring = recurringList.reduce((sum, r) => sum + Number(r.amount || 0), 0);

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this recurring subscription/bill?')) return;
    setIsDeleting(id);
    try {
      await api.recurring.delete(id);
      onRefreshRecurring();
    } catch (err) {
      alert('Failed to delete recurring transaction: ' + err.message);
    } finally {
      setIsDeleting(null);
    }
  };

  return (
    <div className="space-y-8 animate-in fade-in duration-200">
      
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-black text-[#5A4636] tracking-tight">Recurring Bills & Subscriptions</h2>
          <p className="text-xs sm:text-sm text-[#897262]">
            Automated scheduler processes due items daily at 01:00 AM UTC
          </p>
        </div>

        <button
          onClick={onOpenAddRecurringModal}
          className="flex items-center gap-1.5 bg-[#668B4A] hover:bg-[#54753C] text-[#F8F1DC] px-4 py-2 rounded-2xl text-xs sm:text-sm font-bold shadow-md shadow-[#668B4A]/25 transition-all hover:scale-105 active:scale-95"
        >
          <Plus className="w-4 h-4 stroke-[3]" />
          <span>New Subscription</span>
        </button>
      </div>

      {/* Summary Card */}
      <div className="bg-[#FDFCF7] border border-[#D6CAC1]/70 p-6 rounded-3xl shadow-sm flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3.5">
          <div className="w-12 h-12 rounded-2xl bg-[#668B4A]/15 text-[#668B4A] flex items-center justify-center shrink-0">
            <Clock className="w-6 h-6" />
          </div>
          <div>
            <span className="text-xs font-bold uppercase tracking-wider text-[#897262]">Monthly Recurring Overhead</span>
            <div className="text-2xl font-black text-[#5A4636] tracking-tight">
              ${totalMonthlyRecurring.toFixed(2)} <span className="text-xs font-medium text-[#897262]">across {recurringList.length} subscriptions</span>
            </div>
          </div>
        </div>

        <div className="bg-[#668B4A]/10 px-3.5 py-2 rounded-xl border border-[#668B4A]/20 flex items-center gap-2 text-xs font-semibold text-[#54753C]">
          <CheckCircle2 className="w-4 h-4" />
          <span>Automated Daily Cron Active</span>
        </div>
      </div>

      {/* List */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        {recurringList.map((item) => (
          <div 
            key={item.id} 
            className="bg-[#FDFCF7] border border-[#D6CAC1]/70 p-5 rounded-3xl shadow-sm flex flex-col justify-between hover:border-[#668B4A]/60 transition-all group"
          >
            <div>
              <div className="flex items-center justify-between mb-3">
                <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-md bg-[#668B4A]/15 text-[#54753C]">
                  {item.frequency}
                </span>
                <button
                  onClick={() => handleDelete(item.id)}
                  disabled={isDeleting === item.id}
                  className="p-1.5 text-[#897262] hover:text-[#D35400] rounded-lg transition-colors opacity-70 group-hover:opacity-100"
                  title="Delete subscription"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>

              <h4 className="text-base font-bold text-[#5A4636] group-hover:text-[#668B4A] transition-colors">
                {item.title}
              </h4>
              <p className="text-xs text-[#897262] mt-0.5">{item.categoryName || 'Subscription'}</p>

              <div className="my-4">
                <div className="text-2xl font-black text-[#5A4636]">
                  ${Number(item.amount).toFixed(2)}
                  <span className="text-xs font-semibold text-[#897262]"> / {item.frequency.toLowerCase()}</span>
                </div>
              </div>
            </div>

            <div className="pt-3 border-t border-[#D6CAC1]/40 flex items-center justify-between text-xs text-[#897262]">
              <span className="flex items-center gap-1">
                <CalendarClock className="w-3.5 h-3.5" />
                <span>Next: {item.nextRunDate}</span>
              </span>
              <span className="px-2 py-0.5 rounded bg-[#EAE4DF] text-[10px] font-bold text-[#5A4636]">
                {item.paymentMethod}
              </span>
            </div>
          </div>
        ))}
      </div>

    </div>
  );
}
