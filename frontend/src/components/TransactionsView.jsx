import React, { useState } from 'react';
import { 
  Search, 
  Filter, 
  Download, 
  Plus, 
  Trash2, 
  Receipt, 
  Calendar, 
  CreditCard,
  ArrowUpDown
} from 'lucide-react';
import { api } from '../api/client';

export default function TransactionsView({ 
  transactions = [], 
  categories = [], 
  onOpenAddModal, 
  onRefreshTransactions 
}) {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedType, setSelectedType] = useState('ALL');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [selectedPayment, setSelectedPayment] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [isDeleting, setIsDeleting] = useState(null);

  // Filter transactions in client for immediate responsive feel
  const filtered = transactions.filter((t) => {
    if (selectedType !== 'ALL' && t.type !== selectedType) return false;
    if (selectedCategory && String(t.categoryId) !== String(selectedCategory)) return false;
    if (selectedPayment && t.paymentMethod !== selectedPayment) return false;
    if (startDate && t.transactionDate < startDate) return false;
    if (endDate && t.transactionDate > endDate) return false;
    if (searchTerm) {
      const q = searchTerm.toLowerCase();
      const matchTitle = t.title?.toLowerCase().includes(q);
      const matchDesc = t.description?.toLowerCase().includes(q);
      const matchCat = t.categoryName?.toLowerCase().includes(q);
      if (!matchTitle && !matchDesc && !matchCat) return false;
    }
    return true;
  });

  const totalFilteredIncome = filtered.filter(t => t.type === 'INCOME').reduce((s, t) => s + Number(t.amount), 0);
  const totalFilteredExpense = filtered.filter(t => t.type === 'EXPENSE').reduce((s, t) => s + Number(t.amount), 0);

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this transaction?')) return;
    setIsDeleting(id);
    try {
      await api.transactions.delete(id);
      onRefreshTransactions();
    } catch (err) {
      alert('Failed to delete transaction: ' + err.message);
    } finally {
      setIsDeleting(null);
    }
  };

  const handleExportCsv = () => {
    // Trigger direct download
    window.open(api.transactions.exportCsvUrl(), '_blank');
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-200">
      
      {/* Header & Primary Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-black text-[#5A4636] tracking-tight">Transactions Ledger</h2>
          <p className="text-xs sm:text-sm text-[#897262]">
            Detailed record of all cash flows with dynamic search and multi-criteria filters
          </p>
        </div>

        <div className="flex items-center gap-2.5">
          <button
            onClick={handleExportCsv}
            className="flex items-center gap-1.5 bg-[#FDFCF7] hover:bg-[#EAE4DF] border border-[#D6CAC1] text-[#5A4636] px-3.5 py-2 rounded-2xl text-xs sm:text-sm font-bold shadow-sm transition-all"
            title="Export transactions as CSV spreadsheet"
          >
            <Download className="w-4 h-4 text-[#897262]" />
            <span>Export CSV</span>
          </button>

          <button
            onClick={onOpenAddModal}
            className="flex items-center gap-1.5 bg-[#668B4A] hover:bg-[#54753C] text-[#F8F1DC] px-4 py-2 rounded-2xl text-xs sm:text-sm font-bold shadow-md shadow-[#668B4A]/25 transition-all hover:scale-105 active:scale-95"
          >
            <Plus className="w-4 h-4 stroke-[3]" />
            <span>New Entry</span>
          </button>
        </div>
      </div>

      {/* Filter & Search Bar */}
      <div className="bg-[#FDFCF7] border border-[#D6CAC1]/70 p-4 sm:p-5 rounded-3xl shadow-sm space-y-3.5">
        
        {/* Top Search Input & Type Pills */}
        <div className="flex flex-col md:flex-row items-center gap-3">
          
          {/* Search Box */}
          <div className="relative w-full md:flex-1">
            <Search className="w-4 h-4 text-[#897262] absolute left-3.5 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search by title, description or category..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full bg-[#EAE4DF]/50 border border-[#D6CAC1]/80 rounded-2xl pl-10 pr-4 py-2 text-xs sm:text-sm text-[#5A4636] placeholder-[#897262] focus:outline-none focus:ring-2 focus:ring-[#668B4A]/60 transition-all"
            />
          </div>

          {/* Type Selector (All, Income, Expense) */}
          <div className="flex items-center bg-[#EAE4DF]/60 p-1 rounded-2xl border border-[#D6CAC1]/60 self-start md:self-auto">
            {['ALL', 'EXPENSE', 'INCOME'].map((type) => (
              <button
                key={type}
                onClick={() => setSelectedType(type)}
                className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all ${
                  selectedType === type
                    ? 'bg-[#668B4A] text-[#F8F1DC] shadow-sm'
                    : 'text-[#897262] hover:text-[#5A4636]'
                }`}
              >
                {type === 'ALL' ? 'All Types' : type === 'EXPENSE' ? 'Expenses' : 'Income'}
              </button>
            ))}
          </div>

        </div>

        {/* Bottom Detailed Selectors */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5 pt-2 border-t border-[#D6CAC1]/40 text-xs">
          
          {/* Category Filter */}
          <div>
            <label className="block text-[10px] font-bold uppercase tracking-wider text-[#897262] mb-1">Category</label>
            <select
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
              className="w-full bg-[#EAE4DF]/50 border border-[#D6CAC1]/80 rounded-xl px-2.5 py-1.5 text-xs text-[#5A4636] focus:outline-none focus:ring-1 focus:ring-[#668B4A]"
            >
              <option value="">All Categories</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>{c.name} ({c.type})</option>
              ))}
            </select>
          </div>

          {/* Payment Method Filter */}
          <div>
            <label className="block text-[10px] font-bold uppercase tracking-wider text-[#897262] mb-1">Payment Method</label>
            <select
              value={selectedPayment}
              onChange={(e) => setSelectedPayment(e.target.value)}
              className="w-full bg-[#EAE4DF]/50 border border-[#D6CAC1]/80 rounded-xl px-2.5 py-1.5 text-xs text-[#5A4636] focus:outline-none focus:ring-1 focus:ring-[#668B4A]"
            >
              <option value="">All Methods</option>
              <option value="UPI">UPI</option>
              <option value="CREDIT_CARD">Credit Card</option>
              <option value="DEBIT_CARD">Debit Card</option>
              <option value="CASH">Cash</option>
              <option value="BANK_TRANSFER">Bank Transfer</option>
            </select>
          </div>

          {/* Start Date */}
          <div>
            <label className="block text-[10px] font-bold uppercase tracking-wider text-[#897262] mb-1">From Date</label>
            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              className="w-full bg-[#EAE4DF]/50 border border-[#D6CAC1]/80 rounded-xl px-2.5 py-1.5 text-xs text-[#5A4636] focus:outline-none focus:ring-1 focus:ring-[#668B4A]"
            />
          </div>

          {/* End Date */}
          <div>
            <label className="block text-[10px] font-bold uppercase tracking-wider text-[#897262] mb-1">To Date</label>
            <input
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              className="w-full bg-[#EAE4DF]/50 border border-[#D6CAC1]/80 rounded-xl px-2.5 py-1.5 text-xs text-[#5A4636] focus:outline-none focus:ring-1 focus:ring-[#668B4A]"
            />
          </div>

        </div>

      </div>

      {/* Filtered Summary Pill */}
      <div className="flex flex-wrap items-center justify-between gap-3 px-3 text-xs text-[#897262] font-semibold">
        <div>
          Showing <span className="text-[#5A4636] font-bold">{filtered.length}</span> transaction{filtered.length === 1 ? '' : 's'}
        </div>
        <div className="flex items-center gap-4">
          <span>Total Income: <strong className="text-[#27AE60] font-bold">+${totalFilteredIncome.toFixed(2)}</strong></span>
          <span>Total Expenses: <strong className="text-[#5A4636] font-bold">-${totalFilteredExpense.toFixed(2)}</strong></span>
        </div>
      </div>

      {/* Transactions Table / Cards */}
      <div className="bg-[#FDFCF7] border border-[#D6CAC1]/70 rounded-3xl shadow-sm overflow-hidden">
        {filtered.length === 0 ? (
          <div className="py-16 text-center space-y-3">
            <div className="w-12 h-12 rounded-2xl bg-[#EAE4DF] text-[#897262] flex items-center justify-center mx-auto">
              <Receipt className="w-6 h-6" />
            </div>
            <p className="text-sm font-bold text-[#5A4636]">No transactions found</p>
            <p className="text-xs text-[#897262]">Try clearing filters or recording a new transaction.</p>
            <button
              onClick={onOpenAddModal}
              className="mt-2 inline-flex items-center gap-1.5 bg-[#668B4A] text-[#F8F1DC] px-4 py-2 rounded-xl text-xs font-bold shadow-sm"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>Add Transaction</span>
            </button>
          </div>
        ) : (
          <div className="divide-y divide-[#D6CAC1]/40">
            {filtered.map((tx) => {
              const isIncome = tx.type === 'INCOME';
              return (
                <div 
                  key={tx.id} 
                  className="p-4 sm:p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-3 hover:bg-[#EAE4DF]/30 transition-colors group"
                >
                  <div className="flex items-center gap-3.5">
                    {/* Category Icon Badge */}
                    <div 
                      className="w-11 h-11 rounded-2xl flex items-center justify-center text-white shrink-0 shadow-sm"
                      style={{ backgroundColor: tx.categoryColor || '#668B4A' }}
                    >
                      <Receipt className="w-5 h-5" />
                    </div>

                    <div>
                      <h4 className="text-sm font-bold text-[#5A4636] group-hover:text-[#668B4A] transition-colors">
                        {tx.title}
                      </h4>
                      {tx.description && (
                        <p className="text-xs text-[#897262] line-clamp-1">{tx.description}</p>
                      )}
                      <div className="flex flex-wrap items-center gap-2 mt-1 text-[11px] text-[#897262]">
                        <span 
                          className="font-bold px-2 py-0.5 rounded-md text-white text-[10px]"
                          style={{ backgroundColor: tx.categoryColor || '#668B4A' }}
                        >
                          {tx.categoryName || 'General'}
                        </span>
                        <span>•</span>
                        <span className="flex items-center gap-1">
                          <Calendar className="w-3 h-3" />
                          {tx.transactionDate}
                        </span>
                        <span>•</span>
                        <span className="flex items-center gap-1 px-1.5 py-0.5 rounded bg-[#EAE4DF] text-[10px] font-semibold text-[#5A4636]">
                          <CreditCard className="w-2.5 h-2.5" />
                          {tx.paymentMethod}
                        </span>
                      </div>
                    </div>
                  </div>

                  {/* Right side: Amount & Delete button */}
                  <div className="flex items-center justify-between sm:justify-end gap-4 self-end sm:self-center mt-2 sm:mt-0">
                    <div className={`text-base font-black tracking-tight ${
                      isIncome ? 'text-[#27AE60]' : 'text-[#5A4636]'
                    }`}>
                      {isIncome ? '+' : '-'}${Number(tx.amount).toFixed(2)}
                    </div>

                    <button
                      onClick={() => handleDelete(tx.id)}
                      disabled={isDeleting === tx.id}
                      className="p-2 text-[#897262] hover:text-[#D35400] hover:bg-[#D35400]/10 rounded-xl transition-colors opacity-70 group-hover:opacity-100"
                      title="Delete Transaction"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>

                </div>
              );
            })}
          </div>
        )}
      </div>

    </div>
  );
}
