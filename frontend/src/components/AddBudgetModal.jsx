import React, { useState } from 'react';
import { X, Leaf, DollarSign } from 'lucide-react';
import { api } from '../api/client';

export default function AddBudgetModal({ 
  isOpen, 
  onClose, 
  categories = [], 
  onSuccess 
}) {
  const [categoryId, setCategoryId] = useState('');
  const [targetAmount, setTargetAmount] = useState('');
  const [budgetMonth, setBudgetMonth] = useState(11);
  const [budgetYear, setBudgetYear] = useState(2026);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  if (!isOpen) return null;

  const expenseCategories = categories.filter(c => c.type === 'EXPENSE');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!targetAmount || Number(targetAmount) <= 0) return setError('Please enter a target budget amount');

    setLoading(true);
    setError('');

    try {
      await api.budgets.create({
        categoryId: categoryId ? Number(categoryId) : null,
        targetAmount: Number(targetAmount),
        budgetMonth: Number(budgetMonth),
        budgetYear: Number(budgetYear),
      });
      onSuccess();
      onClose();
      setTargetAmount('');
      setCategoryId('');
    } catch (err) {
      setError(err.message || 'Failed to create budget');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm animate-in fade-in duration-150">
      <div 
        className="bg-[#FDFCF7] border border-[#D6CAC1] w-full max-w-md rounded-3xl shadow-2xl p-6 sm:p-7 space-y-5 animate-in zoom-in-95 duration-150"
        onClick={(e) => e.stopPropagation()}
      >
        
        {/* Header */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-xl bg-[#668B4A]/15 text-[#668B4A] flex items-center justify-center">
              <Leaf className="w-4 h-4" />
            </div>
            <h3 className="text-lg font-black text-[#5A4636]">Set Category Budget</h3>
          </div>
          <button 
            onClick={onClose}
            className="p-1.5 rounded-xl text-[#897262] hover:text-[#5A4636] hover:bg-[#EAE4DF] transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {error && (
          <div className="p-3 rounded-2xl bg-[#D35400]/10 border border-[#D35400]/20 text-xs font-semibold text-[#D35400]">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4 text-xs">
          
          {/* Target Amount */}
          <div>
            <label className="block text-[11px] font-bold text-[#5A4636] mb-1">Target Monthly Limit ($)</label>
            <input
              type="number"
              step="0.01"
              placeholder="e.g. 400.00"
              value={targetAmount}
              onChange={(e) => setTargetAmount(e.target.value)}
              className="w-full bg-[#EAE4DF]/40 border border-[#D6CAC1] rounded-2xl px-3.5 py-2.5 text-xs text-[#5A4636] font-bold focus:outline-none focus:ring-2 focus:ring-[#668B4A]"
              required
            />
          </div>

          {/* Category */}
          <div>
            <label className="block text-[11px] font-bold text-[#5A4636] mb-1">Target Category</label>
            <select
              value={categoryId}
              onChange={(e) => setCategoryId(e.target.value)}
              className="w-full bg-[#EAE4DF]/40 border border-[#D6CAC1] rounded-2xl px-3.5 py-2.5 text-xs text-[#5A4636] focus:outline-none focus:ring-2 focus:ring-[#668B4A]"
            >
              <option value="">Overall Monthly Budget (All categories)</option>
              {expenseCategories.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
            <p className="text-[10px] text-[#897262] mt-1">Leaving blank will track your total overall monthly expenses.</p>
          </div>

          {/* Month & Year */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-bold text-[#5A4636] mb-1">Month</label>
              <select
                value={budgetMonth}
                onChange={(e) => setBudgetMonth(e.target.value)}
                className="w-full bg-[#EAE4DF]/40 border border-[#D6CAC1] rounded-2xl px-3.5 py-2.5 text-xs text-[#5A4636] focus:outline-none focus:ring-2 focus:ring-[#668B4A]"
              >
                {Array.from({ length: 12 }, (_, i) => i + 1).map((m) => (
                  <option key={m} value={m}>
                    {new Date(2026, m - 1).toLocaleString('default', { month: 'long' })}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-[11px] font-bold text-[#5A4636] mb-1">Year</label>
              <input
                type="number"
                value={budgetYear}
                onChange={(e) => setBudgetYear(e.target.value)}
                className="w-full bg-[#EAE4DF]/40 border border-[#D6CAC1] rounded-2xl px-3.5 py-2.5 text-xs text-[#5A4636] focus:outline-none focus:ring-2 focus:ring-[#668B4A]"
              />
            </div>
          </div>

          {/* Actions */}
          <div className="pt-3 flex items-center justify-end gap-2.5 border-t border-[#D6CAC1]/40">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2.5 rounded-2xl bg-[#EAE4DF] hover:bg-[#D6CAC1]/60 text-[#5A4636] font-bold text-xs transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="px-5 py-2.5 rounded-2xl bg-[#668B4A] hover:bg-[#54753C] text-[#F8F1DC] font-bold text-xs shadow-md shadow-[#668B4A]/25 transition-all hover:scale-105 active:scale-95 disabled:opacity-50"
            >
              {loading ? 'Creating...' : 'Set Budget'}
            </button>
          </div>

        </form>

      </div>
    </div>
  );
}
