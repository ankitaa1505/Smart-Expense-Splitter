import React, { useState } from 'react';
import { 
  PieChart, 
  TrendingUp, 
  Sparkles, 
  AlertCircle, 
  CheckCircle, 
  Lightbulb, 
  Calendar,
  Layers
} from 'lucide-react';

export default function AnalyticsView({ 
  summary, 
  categoryBreakdown = [], 
  monthlyTrends = [], 
  insights = [] 
}) {
  const [selectedYear, setSelectedYear] = useState(2026);

  const totalExpense = categoryBreakdown.reduce((s, c) => s + Number(c.totalAmount || 0), 0);
  const maxTrendAmount = Math.max(...monthlyTrends.map(m => Math.max(m.totalIncome, m.totalExpense)), 5000);

  return (
    <div className="space-y-8 animate-in fade-in duration-200">
      
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-black text-[#5A4636] tracking-tight">Financial Intelligence & Analytics</h2>
          <p className="text-xs sm:text-sm text-[#897262]">
            Rule-based spending insights, category breakdowns, and multi-month cash flow trends
          </p>
        </div>

        <div className="flex items-center gap-2 bg-[#FDFCF7] border border-[#D6CAC1] px-3 py-1.5 rounded-2xl text-xs font-bold text-[#5A4636]">
          <Calendar className="w-3.5 h-3.5 text-[#897262]" />
          <span>Year 2026</span>
        </div>
      </div>

      {/* Row 1: Smart Insights Section */}
      <div className="space-y-4">
        <div className="flex items-center gap-2">
          <Sparkles className="w-4 h-4 text-[#668B4A]" />
          <h3 className="font-extrabold text-base text-[#5A4636]">Automated Financial Health Engine</h3>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {insights.map((insight, idx) => {
            const isWarning = insight.level === 'WARNING';
            const isSuccess = insight.level === 'SUCCESS';
            return (
              <div 
                key={idx}
                className={`p-5 rounded-3xl border bg-[#FDFCF7] shadow-sm flex flex-col justify-between space-y-3 ${
                  isWarning ? 'border-[#D35400]/40' : isSuccess ? 'border-[#668B4A]/40' : 'border-[#3498DB]/40'
                }`}
              >
                <div>
                  <div className="flex items-center justify-between mb-2">
                    <span className={`text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-md ${
                      isWarning 
                        ? 'bg-[#D35400]/15 text-[#D35400]' 
                        : isSuccess 
                        ? 'bg-[#668B4A]/15 text-[#54753C]' 
                        : 'bg-[#3498DB]/15 text-[#3498DB]'
                    }`}>
                      {insight.type}
                    </span>
                    {isWarning ? (
                      <AlertCircle className="w-4 h-4 text-[#D35400]" />
                    ) : isSuccess ? (
                      <CheckCircle className="w-4 h-4 text-[#668B4A]" />
                    ) : (
                      <Lightbulb className="w-4 h-4 text-[#3498DB]" />
                    )}
                  </div>
                  <h4 className="text-sm font-bold text-[#5A4636]">{insight.title}</h4>
                  <p className="text-xs text-[#897262] mt-1.5 leading-relaxed">{insight.description}</p>
                </div>

                <div className="pt-3 border-t border-[#D6CAC1]/40 text-xs text-[#5A4636] font-semibold flex items-start gap-1.5">
                  <span>💡</span>
                  <span className="text-[11px] text-[#54753C]">{insight.suggestion}</span>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Row 2: Charts Grid (Category Breakdown + Monthly Trends) */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        
        {/* Category Breakdown */}
        <div className="bg-[#FDFCF7] border border-[#D6CAC1]/70 p-6 rounded-3xl shadow-sm space-y-5">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="font-bold text-base text-[#5A4636]">Category Spending Breakdown</h3>
              <p className="text-xs text-[#897262]">Total expenditures: ${totalExpense.toFixed(2)}</p>
            </div>
            <div className="w-8 h-8 rounded-xl bg-[#668B4A]/15 text-[#668B4A] flex items-center justify-center">
              <Layers className="w-4 h-4" />
            </div>
          </div>

          {/* Visual Stacked Progress Bar */}
          <div className="w-full h-4 bg-[#EAE4DF] rounded-full overflow-hidden flex">
            {categoryBreakdown.map((cat, i) => (
              <div 
                key={i}
                className="h-full transition-all duration-500 hover:opacity-85"
                style={{ 
                  width: `${cat.percentage}%`, 
                  backgroundColor: cat.colorCode || '#668B4A' 
                }}
                title={`${cat.categoryName}: ${cat.percentage}% ($${cat.totalAmount})`}
              />
            ))}
          </div>

          {/* Category List */}
          <div className="space-y-3 pt-2">
            {categoryBreakdown.map((cat, i) => (
              <div key={i} className="flex items-center justify-between text-xs font-semibold">
                <div className="flex items-center gap-2.5">
                  <span 
                    className="w-3 h-3 rounded-full shrink-0" 
                    style={{ backgroundColor: cat.colorCode || '#668B4A' }}
                  />
                  <span className="text-[#5A4636]">{cat.categoryName}</span>
                </div>
                <div className="flex items-center gap-3">
                  <span className="text-[#897262] font-medium">{cat.percentage}%</span>
                  <span className="text-[#5A4636] font-bold min-w-[70px] text-right">
                    ${Number(cat.totalAmount).toFixed(2)}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* 6-Month Income vs Expense Trend (Clean SVG Bar Chart) */}
        <div className="bg-[#FDFCF7] border border-[#D6CAC1]/70 p-6 rounded-3xl shadow-sm space-y-5 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-1">
              <h3 className="font-bold text-base text-[#5A4636]">6-Month Cash Flow Trajectory</h3>
              <div className="flex items-center gap-3 text-xs font-bold">
                <span className="flex items-center gap-1.5 text-[#27AE60]">
                  <span className="w-2.5 h-2.5 rounded bg-[#27AE60]" /> Income
                </span>
                <span className="flex items-center gap-1.5 text-[#5A4636]">
                  <span className="w-2.5 h-2.5 rounded bg-[#5A4636]" /> Expenses
                </span>
              </div>
            </div>
            <p className="text-xs text-[#897262]">Monthly comparison between income and expenses</p>
          </div>

          {/* SVG Bar Chart */}
          <div className="pt-4 pb-2">
            <div className="h-48 flex items-end justify-between gap-2 sm:gap-4 px-2">
              {monthlyTrends.map((trend, i) => {
                const incomeHeight = Math.round((trend.totalIncome / maxTrendAmount) * 160);
                const expenseHeight = Math.round((trend.totalExpense / maxTrendAmount) * 160);
                return (
                  <div key={i} className="flex-1 flex flex-col items-center gap-2 group">
                    <div className="w-full flex items-end justify-center gap-1.5 h-44">
                      {/* Income Bar */}
                      <div 
                        className="w-3 sm:w-4 bg-[#668B4A] rounded-t-lg transition-all duration-500 group-hover:brightness-110"
                        style={{ height: `${incomeHeight}px` }}
                        title={`${trend.monthName} Income: $${trend.totalIncome}`}
                      />
                      {/* Expense Bar */}
                      <div 
                        className="w-3 sm:w-4 bg-[#5A4636] rounded-t-lg transition-all duration-500 group-hover:brightness-110"
                        style={{ height: `${expenseHeight}px` }}
                        title={`${trend.monthName} Expenses: $${trend.totalExpense}`}
                      />
                    </div>
                    <span className="text-[11px] font-bold text-[#897262] group-hover:text-[#5A4636]">
                      {trend.monthName}
                    </span>
                  </div>
                );
              })}
            </div>
          </div>

          <div className="pt-3 border-t border-[#D6CAC1]/40 flex justify-between text-xs text-[#897262]">
            <span>Average monthly net savings: <strong>+$3,130.00</strong></span>
            <span className="text-[#668B4A] font-bold">Consistent Positive Savings</span>
          </div>
        </div>

      </div>

    </div>
  );
}
