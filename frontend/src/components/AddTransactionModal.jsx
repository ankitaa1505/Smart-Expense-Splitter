import React, { useState } from 'react';
import { X, Plus, DollarSign, Calendar, Tag, CreditCard } from 'lucide-react';
import { api } from '../api/client';

export default function AddTransactionModal({ 
  isOpen, 
  onClose, 
  categories = [], 
  onSuccess 
}) {
  const [title, setTitle] = useState('');
  const [amount, setAmount] = useState('');
  const [type, setType] = useState('EXPENSE');
  const [categoryId, setCategoryId] = useState('');
  const [transactionDate, setTransactionDate] = useState(new Date().toISOString().split('T')[0]);
  const [paymentMethod, setPaymentMethod] = useState('UPI');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  if (!isOpen) return null;

  const filteredCategories = categories.filter(c => c.type === type);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!title.trim()) return setError('Please enter a transaction title');
    if (!amount || Number(amount) <= 0) return setError('Please enter a valid amount');
    if (!categoryId) return setError('Please select a category');

    setLoading(true);
    setError('');

    try {
      await api.transactions.create({
        title: title.trim(),
        amount: Number(amount),
        type,
        categoryId: Number(categoryId),
        transactionDate,
        paymentMethod,
        description: description.trim() || null,
      });
      onSuccess();
      onClose();
      // Reset
      setTitle('');
      setAmount('');
      setDescription('');
    } catch (err) {
      setError(err.message || 'Failed to record transaction');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm animate-in fade-in duration-150">
      <div 
        className="bg-[#FDFCF7] border border-[#D6CAC1] w-full max-w-lg rounded-3xl shadow-2xl p-6 sm:p-7 space-y-5 animate-in zoom-in-95 duration-150"
        onClick={(e) => e.stopPropagation()}
      >
        
        {/* Modal Header */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-xl bg-[#668B4A]/15 text-[#668B4A] flex items-center justify-center">
              <Plus className="w-4 h-4 stroke-[3]" />
            </div>
            <h3 className="text-lg font-black text-[#5A4636]">Record Transaction</h3>
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
          
          {/* Income vs Expense Toggle */}
          <div className="grid grid-cols-2 gap-2 bg-[#EAE4DF]/60 p-1 rounded-2xl border border-[#D6CAC1]/60">
            <button
              type="button"
              onClick={() => {
                setType('EXPENSE');
                setCategoryId('');
              }}
              className={`py-2 rounded-xl font-bold transition-all ${
                type === 'EXPENSE'
                  ? 'bg-[#5A4636] text-[#F8F1DC] shadow-sm'
                  : 'text-[#897262] hover:text-[#5A4636]'
              }`}
            >
              Expense (-)
            </button>
            <button
              type="button"
              onClick={() => {
                setType('INCOME');
                setCategoryId('');
              }}
              className={`py-2 rounded-xl font-bold transition-all ${
                type === 'INCOME'
                  ? 'bg-[#668B4A] text-[#F8F1DC] shadow-sm'
                  : 'text-[#897262] hover:text-[#5A4636]'
              }`}
            >
              Income (+)
            </button>
          </div>

          {/* Title */}
          <div>
            <label className="block text-[11px] font-bold text-[#5A4636] mb-1">Title / Merchant</label>
            <input
              type="text"
              placeholder="e.g. Organic Matcha, Supermarket, Monthly Salary"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="w-full bg-[#EAE4DF]/40 border border-[#D6CAC1] rounded-2xl px-3.5 py-2.5 text-xs text-[#5A4636] focus:outline-none focus:ring-2 focus:ring-[#668B4A]"
              required
            />
          </div>

          {/* Amount & Category */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-bold text-[#5A4636] mb-1">Amount ($)</label>
              <input
                type="number"
                step="0.01"
                placeholder="0.00"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                className="w-full bg-[#EAE4DF]/40 border border-[#D6CAC1] rounded-2xl px-3.5 py-2.5 text-xs text-[#5A4636] font-bold focus:outline-none focus:ring-2 focus:ring-[#668B4A]"
                required
              />
            </div>

            <div>
              <label className="block text-[11px] font-bold text-[#5A4636] mb-1">Category</label>
              <select
                value={categoryId}
                onChange={(e) => setCategoryId(e.target.value)}
                className="w-full bg-[#EAE4DF]/40 border border-[#D6CAC1] rounded-2xl px-3.5 py-2.5 text-xs text-[#5A4636] focus:outline-none focus:ring-2 focus:ring-[#668B4A]"
                required
              >
                <option value="">Select Category</option>
                {filteredCategories.map((c) => (
                  <option key={c.id} value={c.id}>{c.name}</option>
                ))}
              </select>
            </div>
          </div>

          {/* Date & Payment Method */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-bold text-[#5A4636] mb-1">Date</label>
              <input
                type="date"
                value={transactionDate}
                onChange={(e) => setTransactionDate(e.target.value)}
                className="w-full bg-[#EAE4DF]/40 border border-[#D6CAC1] rounded-2xl px-3.5 py-2.5 text-xs text-[#5A4636] focus:outline-none focus:ring-2 focus:ring-[#668B4A]"
                required
              />
            </div>

            <div>
              <label className="block text-[11px] font-bold text-[#5A4636] mb-1">Payment Method</label>
              <select
                value={paymentMethod}
                onChange={(e) => setPaymentMethod(e.target.value)}
                className="w-full bg-[#EAE4DF]/40 border border-[#D6CAC1] rounded-2xl px-3.5 py-2.5 text-xs text-[#5A4636] focus:outline-none focus:ring-2 focus:ring-[#668B4A]"
              >
                <option value="UPI">UPI / Instant Pay</option>
                <option value="CREDIT_CARD">Credit Card</option>
                <option value="DEBIT_CARD">Debit Card</option>
                <option value="CASH">Cash</option>
                <option value="BANK_TRANSFER">Bank Transfer</option>
              </select>
            </div>
          </div>

          {/* Description */}
          <div>
            <label className="block text-[11px] font-bold text-[#5A4636] mb-1">Notes / Description (Optional)</label>
            <input
              type="text"
              placeholder="e.g. Shared dinner with friends, tax deductible receipt"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="w-full bg-[#EAE4DF]/40 border border-[#D6CAC1] rounded-2xl px-3.5 py-2.5 text-xs text-[#5A4636] focus:outline-none focus:ring-2 focus:ring-[#668B4A]"
            />
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
              {loading ? 'Saving...' : 'Record Transaction'}
            </button>
          </div>

        </form>

      </div>
    </div>
  );
}
