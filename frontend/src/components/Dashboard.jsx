import React from 'react';
import { 
  TrendingUp, 
  TrendingDown, 
  PiggyBank, 
  Sparkles, 
  ArrowUpRight, 
  ArrowDownRight, 
  ArrowRight,
  ShieldCheck, 
  AlertTriangle,
  Receipt,
  Plus
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export default function Dashboard({ 
  summary, 
  transactions = [], 
  budgets = [], 
  insights = [],
  onOpenAddModal, 
  setActiveTab 
}) {
  const { user } = useAuth();

  const totalIncome = summary?.totalIncome || 0;
  const totalExpense = summary?.totalExpense || 0;
  const netSavings = summary?.netSavings || (totalIncome - totalExpense);
  const savingsRate = summary?.savingsRate || (totalIncome > 0 ? ((netSavings / totalIncome) * 100).toFixed(1) : 0);

  // Calculate overall monthly budget usage
  const totalBudgeted = budgets.reduce((acc, b) => acc + Number(b.targetAmount || 0), 0) || 2000;
  const budgetPercentage = totalBudgeted > 0 ? Math.min(100, Math.round((totalExpense / totalBudgeted) * 100)) : 0;
  const isBudgetWarning = budgetPercentage >= 80 && budgetPercentage < 100;
  const isBudgetExceeded = budgetPercentage >= 100;

  const topInsight = insights?.[0];

  return (
    <div className="space-y-8 animate-in fade-in duration-200">
      
      {/* Welcome Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-gradient-to-r from-[#668B4A]/15 via-[#F8F1DC] to-[#5A4636]/10 p-6 rounded-3xl border border-[#D6CAC1]/60">
        <div>
          <div className="flex items-center gap-2">
            <span className="text-xl">🍵</span>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-[#5A4636] tracking-tight">
              Welcome back, {user?.firstName || 'Friend'}!
            </h1>
          </div>
          <p className="text-sm text-[#897262] mt-1">
            Here is your financial wellness summary for November 2026.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={onOpenAddModal}
            className="flex items-center gap-2 bg-[#668B4A] hover:bg-[#54753C] text-[#F8F1DC] px-4 py-2.5 rounded-2xl text-sm font-bold shadow-md shadow-[#668B4A]/25 transition-all hover:scale-105 active:scale-95"
          >
            <Plus className="w-4 h-4 stroke-[3]" />
            <span>Record Transaction</span>
          </button>
        </div>
      </div>

      {/* 4 Financial Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-5">
        
        {/* Card 1: Net Savings (Garden Matcha Hero) */}
        <div className="bg-[#668B4A] text-[#F8F1DC] p-5 rounded-3xl shadow-lg shadow-[#668B4A]/20 flex flex-col justify-between relative overflow-hidden group">
          <div className="absolute -right-4 -bottom-4 w-24 h-24 bg-white/10 rounded-full blur-xl pointer-events-none group-hover:scale-125 transition-transform" />
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-[#F8F1DC]/80">Net Savings</span>
            <div className="w-8 h-8 rounded-xl bg-white/15 flex items-center justify-center">
              <PiggyBank className="w-4 h-4 text-[#F8F1DC]" />
            </div>
          </div>
          <div className="my-3">
            <div className="text-3xl font-black tracking-tight">
              ${Number(netSavings).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </div>
            <p className="text-xs text-[#F8F1DC]/80 mt-1 font-medium flex items-center gap-1">
              <TrendingUp className="w-3.5 h-3.5 text-[#CFDEBF]" />
              <span>+{savingsRate}% velocity this month</span>
            </p>
          </div>
          <div className="text-[11px] text-[#F8F1DC]/70 font-medium">
            Saved from ${totalIncome.toLocaleString()} earned
          </div>
        </div>

        {/* Card 2: Total Income */}
        <div className="bg-[#FDFCF7] border border-[#D6CAC1]/70 p-5 rounded-3xl shadow-sm flex flex-col justify-between hover:border-[#668B4A]/50 transition-colors">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-[#897262]">Monthly Income</span>
            <div className="w-8 h-8 rounded-xl bg-[#27AE60]/15 flex items-center justify-center text-[#27AE60]">
              <ArrowDownRight className="w-4 h-4" />
            </div>
          </div>
          <div className="my-3">
            <div className="text-3xl font-black text-[#5A4636] tracking-tight">
              ${Number(totalIncome).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </div>
            <p className="text-xs text-[#27AE60] mt-1 font-medium flex items-center gap-1">
              <TrendingUp className="w-3.5 h-3.5" />
              <span>Stable cash inflow</span>
            </p>
          </div>
          <div className="text-[11px] text-[#897262]">Salary & freelance payments</div>
        </div>

        {/* Card 3: Total Expenses (Mocha Accent) */}
        <div className="bg-[#FDFCF7] border border-[#D6CAC1]/70 p-5 rounded-3xl shadow-sm flex flex-col justify-between hover:border-[#5A4636]/50 transition-colors">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-[#897262]">Monthly Spend</span>
            <div className="w-8 h-8 rounded-xl bg-[#D35400]/15 flex items-center justify-center text-[#D35400]">
              <ArrowUpRight className="w-4 h-4" />
            </div>
          </div>
          <div className="my-3">
            <div className="text-3xl font-black text-[#5A4636] tracking-tight">
              ${Number(totalExpense).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </div>
            <p className="text-xs text-[#897262] mt-1 font-medium flex items-center gap-1">
              <span>{transactions.length} total transactions</span>
            </p>
          </div>
          <div className="text-[11px] text-[#897262]">Housing, groceries & living</div>
        </div>

        {/* Card 4: Savings Rate */}
        <div className="bg-[#FDFCF7] border border-[#D6CAC1]/70 p-5 rounded-3xl shadow-sm flex flex-col justify-between hover:border-[#668B4A]/50 transition-colors">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-[#897262]">Savings Rate</span>
            <div className="w-8 h-8 rounded-xl bg-[#668B4A]/15 flex items-center justify-center text-[#668B4A]">
              <ShieldCheck className="w-4 h-4" />
            </div>
          </div>
          <div className="my-3">
            <div className="text-3xl font-black text-[#5A4636] tracking-tight">
              {savingsRate}%
            </div>
            {/* Mini Progress */}
            <div className="w-full h-2 bg-[#EAE4DF] rounded-full overflow-hidden mt-2">
              <div 
                className="h-full bg-[#668B4A] rounded-full transition-all duration-500"
                style={{ width: `${Math.min(100, Math.max(0, savingsRate))}%` }}
              />
            </div>
          </div>
          <div className="text-[11px] text-[#668B4A] font-semibold">Exceeds 20% healthy benchmark</div>
        </div>

      </div>

      {/* Smart Insight Callout Banner */}
      {topInsight && (
        <div className="bg-[#FDFCF7] border-l-4 border-[#668B4A] border-y border-r border-[#D6CAC1]/60 p-4 sm:p-5 rounded-2xl shadow-sm flex items-start gap-3.5">
          <div className="w-9 h-9 rounded-xl bg-[#668B4A]/15 flex items-center justify-center text-[#54753C] shrink-0 mt-0.5">
            <Sparkles className="w-5 h-5" />
          </div>
          <div className="flex-1">
            <div className="flex items-center gap-2">
              <span className="text-xs font-bold uppercase tracking-wider text-[#54753C]">{topInsight.type} Insight</span>
              <span className="text-xs text-[#897262]">• Automated Rule Engine</span>
            </div>
            <h4 className="text-sm font-bold text-[#5A4636] mt-0.5">{topInsight.title}</h4>
            <p className="text-xs text-[#897262] mt-1">{topInsight.description}</p>
            <p className="text-xs text-[#54753C] font-semibold mt-1">💡 Suggestion: {topInsight.suggestion}</p>
          </div>
        </div>
      )}

      {/* Monthly Budget Spending Progress Widget */}
      <div className="bg-[#FDFCF7] border border-[#D6CAC1]/70 p-6 rounded-3xl shadow-sm">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-4">
          <div>
            <h3 className="font-bold text-base text-[#5A4636]">Monthly Target Budget Utilization</h3>
            <p className="text-xs text-[#897262]">
              Overall limit: ${totalBudgeted.toLocaleString()} • Spent: ${totalExpense.toLocaleString()}
            </p>
          </div>
          <div className="flex items-center gap-2">
            <span className={`text-xs font-bold px-2.5 py-1 rounded-full ${
              isBudgetExceeded 
                ? 'bg-[#D35400]/15 text-[#D35400]'
                : isBudgetWarning 
                ? 'bg-[#F39C12]/15 text-[#F39C12]'
                : 'bg-[#668B4A]/15 text-[#54753C]'
            }`}>
              {budgetPercentage}% Consumed
            </span>
            <button
              onClick={() => setActiveTab('budgets')}
              className="text-xs font-semibold text-[#668B4A] hover:underline flex items-center gap-1"
            >
              <span>Manage Budgets</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>

        {/* Progress Bar with 80% Marker */}
        <div className="relative w-full h-4 bg-[#EAE4DF] rounded-full overflow-hidden p-0.5">
          <div 
            className={`h-full rounded-full transition-all duration-700 ${
              isBudgetExceeded 
                ? 'bg-[#D35400]'
                : isBudgetWarning 
                ? 'bg-[#F39C12]'
                : 'bg-[#668B4A]'
            }`}
            style={{ width: `${budgetPercentage}%` }}
          />
        </div>
        <div className="flex justify-between items-center text-[11px] text-[#897262] mt-2 font-medium">
          <span>$0 (Start)</span>
          <span className="text-[#F39C12] font-semibold">80% Warning Limit</span>
          <span>${totalBudgeted.toLocaleString()} (Target)</span>
        </div>
      </div>

      {/* Two Columns: Recent Transactions & Category Breakdown Preview */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Left Column (2 spans): Recent Transactions */}
        <div className="lg:col-span-2 bg-[#FDFCF7] border border-[#D6CAC1]/70 p-6 rounded-3xl shadow-sm space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="font-bold text-base text-[#5A4636]">Recent Transactions</h3>
              <p className="text-xs text-[#897262]">Your latest recorded income and expenses</p>
            </div>
            <button
              onClick={() => setActiveTab('transactions')}
              className="text-xs font-bold text-[#668B4A] hover:text-[#54753C] flex items-center gap-1 transition-colors"
            >
              <span>View All</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </button>
          </div>

          <div className="divide-y divide-[#D6CAC1]/40">
            {transactions.slice(0, 5).map((tx) => {
              const isIncome = tx.type === 'INCOME';
              return (
                <div key={tx.id} className="py-3.5 flex items-center justify-between gap-4 group">
                  <div className="flex items-center gap-3.5">
                    <div 
                      className="w-10 h-10 rounded-2xl flex items-center justify-center text-white shrink-0 shadow-sm"
                      style={{ backgroundColor: tx.categoryColor || '#668B4A' }}
                    >
                      <Receipt className="w-4 h-4" />
                    </div>
                    <div>
                      <p className="text-sm font-bold text-[#5A4636] group-hover:text-[#668B4A] transition-colors">
                        {tx.title}
                      </p>
                      <div className="flex items-center gap-2 text-xs text-[#897262]">
                        <span className="font-medium text-[#5A4636]/80">{tx.categoryName || 'General'}</span>
                        <span>•</span>
                        <span>{tx.transactionDate}</span>
                        <span>•</span>
                        <span className="px-1.5 py-0.2 rounded bg-[#EAE4DF] text-[10px] font-semibold text-[#5A4636]">
                          {tx.paymentMethod}
                        </span>
                      </div>
                    </div>
                  </div>

                  <div className={`text-sm font-extrabold tracking-tight shrink-0 ${
                    isIncome ? 'text-[#27AE60]' : 'text-[#5A4636]'
                  }`}>
                    {isIncome ? '+' : '-'}${Number(tx.amount).toFixed(2)}
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Right Column (1 span): Active Budgets Snapshot */}
        <div className="bg-[#FDFCF7] border border-[#D6CAC1]/70 p-6 rounded-3xl shadow-sm space-y-4 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-3">
              <h3 className="font-bold text-base text-[#5A4636]">Active Budgets</h3>
              <button
                onClick={() => setActiveTab('budgets')}
                className="text-xs font-bold text-[#668B4A] hover:underline"
              >
                Details
              </button>
            </div>
            <p className="text-xs text-[#897262] mb-4">Category limits set for this period</p>

            <div className="space-y-4">
              {budgets.slice(0, 4).map((b) => {
                const percent = Math.min(100, Math.round(b.percentageUsed || 0));
                const isOver = percent >= 100;
                const isWarn = percent >= 80 && percent < 100;
                return (
                  <div key={b.id} className="space-y-1.5">
                    <div className="flex justify-between text-xs font-semibold">
                      <span className="text-[#5A4636]">{b.categoryName || 'General'}</span>
                      <span className={isOver ? 'text-[#D35400]' : isWarn ? 'text-[#F39C12]' : 'text-[#668B4A]'}>
                        ${b.currentSpending} / ${b.targetAmount} ({percent}%)
                      </span>
                    </div>
                    <div className="w-full h-2 bg-[#EAE4DF] rounded-full overflow-hidden">
                      <div 
                        className={`h-full rounded-full ${
                          isOver ? 'bg-[#D35400]' : isWarn ? 'bg-[#F39C12]' : 'bg-[#668B4A]'
                        }`}
                        style={{ width: `${percent}%` }}
                      />
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          <div className="pt-4 border-t border-[#D6CAC1]/50">
            <button
              onClick={() => setActiveTab('budgets')}
              className="w-full py-2.5 rounded-2xl bg-[#EAE4DF] hover:bg-[#D6CAC1]/60 text-[#5A4636] font-bold text-xs flex items-center justify-center gap-1.5 transition-colors"
            >
              <span>Set New Category Budget</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>

      </div>

    </div>
  );
}
