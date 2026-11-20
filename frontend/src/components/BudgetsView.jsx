import React, { useState } from 'react';
import { 
  Leaf, 
  Plus, 
  AlertTriangle, 
  CheckCircle2, 
  Trash2, 
  Calendar,
  Sparkles,
  ArrowRight
} from 'lucide-react';
import { api } from '../api/client';

export default function BudgetsView({ 
  budgets = [], 
  categories = [], 
  onRefreshBudgets, 
  onOpenAddBudgetModal 
}) {
  const [selectedMonth, setSelectedMonth] = useState(11);
  const [selectedYear, setSelectedYear] = useState(2026);
  const [isDeleting, setIsDeleting] = useState(null);

  const totalTarget = budgets.reduce((acc, b) => acc + Number(b.targetAmount || 0), 0);
  const totalSpent = budgets.reduce((acc, b) => acc + Number(b.currentSpending || 0), 0);
  const totalRemaining = Math.max(0, totalTarget - totalSpent);
  const overallPercentage = totalTarget > 0 ? Math.min(100, Math.round((totalSpent / totalTarget) * 100)) : 0;

  const handleDeleteBudget = async (id) => {
    if (!window.confirm('Are you sure you want to delete this category budget?')) return;
    setIsDeleting(id);
    try {
      await api.budgets.delete(id);
      onRefreshBudgets();
    } catch (err) {
      alert('Failed to delete budget: ' + err.message);
    } finally {
      setIsDeleting(null);
    }
  };

  return (
    <div className="space-y-8 animate-in fade-in duration-200">
      
      {/* Header & New Budget Action */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-black text-[#5A4636] tracking-tight">Category Budgets & Alerts</h2>
          <p className="text-xs sm:text-sm text-[#897262]">
            Target limits with automatic 80% warning and 100% exceeded notifications
          </p>
        </div>

        <div className="flex items-center gap-3">
          {/* Month Selector */}
          <div className="flex items-center gap-1.5 bg-[#FDFCF7] border border-[#D6CAC1] px-3 py-1.5 rounded-2xl text-xs font-bold text-[#5A4636]">
            <Calendar className="w-3.5 h-3.5 text-[#897262]" />
            <span>Nov 2026</span>
          </div>

          <button
            onClick={onOpenAddBudgetModal}
            className="flex items-center gap-1.5 bg-[#668B4A] hover:bg-[#54753C] text-[#F8F1DC] px-4 py-2 rounded-2xl text-xs sm:text-sm font-bold shadow-md shadow-[#668B4A]/25 transition-all hover:scale-105 active:scale-95"
          >
            <Plus className="w-4 h-4 stroke-[3]" />
            <span>Create Budget</span>
          </button>
        </div>
      </div>

      {/* Hero Overview Card */}
      <div className="bg-[#5A4636] text-[#F8F1DC] p-6 sm:p-8 rounded-3xl shadow-xl shadow-[#5A4636]/20 relative overflow-hidden">
        <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div className="space-y-2">
            <span className="text-xs font-bold uppercase tracking-wider text-[#F8F1DC]/70 flex items-center gap-1.5">
              <Leaf className="w-3.5 h-3.5 text-[#CFDEBF]" />
              <span>Overall Monthly Budget Health</span>
            </span>
            <div className="text-3xl sm:text-4xl font-black tracking-tight">
              ${totalSpent.toLocaleString('en-US', { minimumFractionDigits: 2 })} <span className="text-lg font-normal text-[#F8F1DC]/60">/ ${totalTarget.toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
            </div>
            <p className="text-xs text-[#F8F1DC]/80 font-medium">
              You have <strong className="text-[#CFDEBF] font-bold">${totalRemaining.toLocaleString('en-US', { minimumFractionDigits: 2 })}</strong> remaining for the rest of November.
            </p>
          </div>

          {/* Radial / Progress Visual */}
          <div className="bg-white/10 p-4 rounded-2xl border border-white/10 flex flex-col items-center justify-center min-w-[140px]">
            <span className="text-2xl font-black text-[#CFDEBF]">{overallPercentage}%</span>
            <span className="text-[10px] uppercase font-bold tracking-wider text-[#F8F1DC]/70">Cap Utilized</span>
          </div>
        </div>

        {/* Global Progress Bar */}
        <div className="w-full h-3 bg-black/20 rounded-full overflow-hidden mt-6 p-0.5">
          <div 
            className="h-full bg-[#668B4A] rounded-full transition-all duration-700"
            style={{ width: `${overallPercentage}%` }}
          />
        </div>
      </div>

      {/* Category Budgets Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        {budgets.map((b) => {
          const target = Number(b.targetAmount);
          const spent = Number(b.currentSpending || 0);
          const percent = Math.min(100, Math.round((spent / target) * 100));
          const remaining = Math.max(0, target - spent);
          const isOver = percent >= 100;
          const isWarn = percent >= 80 && percent < 100;

          return (
            <div 
              key={b.id} 
              className={`bg-[#FDFCF7] border p-5 rounded-3xl shadow-sm flex flex-col justify-between transition-all hover:shadow-md ${
                isOver 
                  ? 'border-[#D35400]/40' 
                  : isWarn 
                  ? 'border-[#F39C12]/40' 
                  : 'border-[#D6CAC1]/70'
              }`}
            >
              <div>
                {/* Header row */}
                <div className="flex items-center justify-between">
                  <span className="text-sm font-bold text-[#5A4636]">{b.categoryName || 'Overall Month'}</span>
                  <div className="flex items-center gap-1.5">
                    {isOver ? (
                      <span className="flex items-center gap-1 px-2 py-0.5 rounded-md bg-[#D35400]/15 text-[#D35400] text-[10px] font-bold">
                        <AlertTriangle className="w-3 h-3" />
                        <span>Exceeded</span>
                      </span>
                    ) : isWarn ? (
                      <span className="flex items-center gap-1 px-2 py-0.5 rounded-md bg-[#F39C12]/15 text-[#F39C12] text-[10px] font-bold">
                        <AlertTriangle className="w-3 h-3" />
                        <span>Warning (80%)</span>
                      </span>
                    ) : (
                      <span className="flex items-center gap-1 px-2 py-0.5 rounded-md bg-[#668B4A]/15 text-[#54753C] text-[10px] font-bold">
                        <CheckCircle2 className="w-3 h-3" />
                        <span>On Track</span>
                      </span>
                    )}

                    <button
                      onClick={() => handleDeleteBudget(b.id)}
                      disabled={isDeleting === b.id}
                      className="p-1.5 text-[#897262] hover:text-[#D35400] rounded-lg transition-colors"
                      title="Delete budget"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>

                {/* Amounts */}
                <div className="my-4">
                  <div className="flex items-baseline justify-between">
                    <span className="text-2xl font-black text-[#5A4636]">${spent.toFixed(2)}</span>
                    <span className="text-xs font-semibold text-[#897262]">Limit: ${target.toFixed(2)}</span>
                  </div>

                  {/* Progress Bar */}
                  <div className="w-full h-2.5 bg-[#EAE4DF] rounded-full overflow-hidden mt-2">
                    <div 
                      className={`h-full rounded-full transition-all duration-500 ${
                        isOver ? 'bg-[#D35400]' : isWarn ? 'bg-[#F39C12]' : 'bg-[#668B4A]'
                      }`}
                      style={{ width: `${percent}%` }}
                    />
                  </div>
                </div>
              </div>

              {/* Footer info */}
              <div className="pt-3 border-t border-[#D6CAC1]/40 flex items-center justify-between text-xs text-[#897262]">
                <span>{percent}% consumed</span>
                <span className="font-semibold text-[#5A4636]">
                  {isOver ? '$0.00 left' : `$${remaining.toFixed(2)} remaining`}
                </span>
              </div>
            </div>
          );
        })}
      </div>

    </div>
  );
}
