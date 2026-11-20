import React, { useState, useEffect, useCallback } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import Navbar from './components/Navbar';
import Dashboard from './components/Dashboard';
import TransactionsView from './components/TransactionsView';
import BudgetsView from './components/BudgetsView';
import AnalyticsView from './components/AnalyticsView';
import RecurringView from './components/RecurringView';
import AddTransactionModal from './components/AddTransactionModal';
import AddBudgetModal from './components/AddBudgetModal';
import AddRecurringModal from './components/AddRecurringModal';
import NotificationsModal from './components/NotificationsModal';
import AuthModal from './components/AuthModal';
import { api } from './api/client';
import { Leaf, ShieldCheck, Heart, Code2 } from 'lucide-react';

function MainApp() {
  const { authModalOpen, setAuthModalOpen } = useAuth();
  
  const [activeTab, setActiveTab] = useState('dashboard');
  const [transactions, setTransactions] = useState([]);
  const [categories, setCategories] = useState([]);
  const [budgets, setBudgets] = useState([]);
  const [summary, setSummary] = useState(null);
  const [categoryBreakdown, setCategoryBreakdown] = useState([]);
  const [monthlyTrends, setMonthlyTrends] = useState([]);
  const [recurringList, setRecurringList] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [insights, setInsights] = useState([]);

  // Modals
  const [addTxOpen, setAddTxOpen] = useState(false);
  const [addBudgetOpen, setAddBudgetOpen] = useState(false);
  const [addRecurringOpen, setAddRecurringOpen] = useState(false);
  const [notifOpen, setNotifOpen] = useState(false);
  const [loading, setLoading] = useState(true);

  // Load All Data
  const loadAllData = useCallback(async () => {
    setLoading(true);
    try {
      const [
        cats,
        txPage,
        bList,
        sum,
        breakdown,
        trends,
        recList,
        notifs,
        insList
      ] = await Promise.all([
        api.categories.getAll(),
        api.transactions.getAll({ size: 50 }),
        api.budgets.getAll(11, 2026),
        api.analytics.getSummary(11, 2026),
        api.analytics.getCategoryBreakdown(11, 2026),
        api.analytics.getMonthlyTrends(2026),
        api.recurring.getAll(),
        api.notifications.getAll(),
        api.insights.getAll(),
      ]);

      setCategories(cats || []);
      setTransactions(txPage?.content || []);
      setBudgets(bList || []);
      setSummary(sum);
      setCategoryBreakdown(breakdown || []);
      setMonthlyTrends(trends || []);
      setRecurringList(recList || []);
      setNotifications(notifs || []);
      setInsights(insList || []);
    } catch (err) {
      console.warn('Data load error:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadAllData();
  }, [loadAllData]);

  const unreadNotifsCount = notifications.filter(n => !n.isRead).length;

  return (
    <div className="min-h-screen flex flex-col bg-[#F8F1DC] text-[#5A4636]">
      
      {/* Navigation Bar */}
      <Navbar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        onOpenAddModal={() => setAddTxOpen(true)}
        onOpenNotifications={() => setNotifOpen(true)}
        unreadCount={unreadNotifsCount}
      />

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-8 py-6 sm:py-8">
        {activeTab === 'dashboard' && (
          <Dashboard
            summary={summary}
            transactions={transactions}
            budgets={budgets}
            insights={insights}
            onOpenAddModal={() => setAddTxOpen(true)}
            setActiveTab={setActiveTab}
          />
        )}

        {activeTab === 'transactions' && (
          <TransactionsView
            transactions={transactions}
            categories={categories}
            onOpenAddModal={() => setAddTxOpen(true)}
            onRefreshTransactions={loadAllData}
          />
        )}

        {activeTab === 'budgets' && (
          <BudgetsView
            budgets={budgets}
            categories={categories}
            onRefreshBudgets={loadAllData}
            onOpenAddBudgetModal={() => setAddBudgetOpen(true)}
          />
        )}

        {activeTab === 'analytics' && (
          <AnalyticsView
            summary={summary}
            categoryBreakdown={categoryBreakdown}
            monthlyTrends={monthlyTrends}
            insights={insights}
          />
        )}

        {activeTab === 'recurring' && (
          <RecurringView
            recurringList={recurringList}
            categories={categories}
            onRefreshRecurring={loadAllData}
            onOpenAddRecurringModal={() => setAddRecurringOpen(true)}
          />
        )}
      </main>

      {/* Footer */}
      <footer className="border-t border-[#D6CAC1]/60 bg-[#FDFCF7]/60 py-6 px-4 sm:px-8 text-xs text-[#897262] mt-12">
        <div className="max-w-7xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-2">
            <div className="w-6 h-6 rounded-lg bg-[#668B4A] text-[#F8F1DC] flex items-center justify-center">
              <Leaf className="w-3.5 h-3.5" />
            </div>
            <span className="font-bold text-[#5A4636]">MatchaSpend • Smart Expense Tracker</span>
            <span>• Java 21 LTS + Spring Boot 3 + React</span>
          </div>

          <div className="flex items-center gap-4">
            <span className="flex items-center gap-1.5 text-[#54753C] font-semibold">
              <ShieldCheck className="w-4 h-4" />
              <span>Multi-Tenant IDOR Protected</span>
            </span>
            <a
              href="https://github.com/ankitaa1505/Smart-Expense-Splitter"
              target="_blank"
              rel="noreferrer"
              className="flex items-center gap-1.5 font-bold text-[#5A4636] hover:text-[#668B4A] transition-colors"
            >
              <Code2 className="w-4 h-4" />
              <span>GitHub</span>
            </a>
          </div>
        </div>
      </footer>

      {/* Modals */}
      <AddTransactionModal
        isOpen={addTxOpen}
        onClose={() => setAddTxOpen(false)}
        categories={categories}
        onSuccess={loadAllData}
      />

      <AddBudgetModal
        isOpen={addBudgetOpen}
        onClose={() => setAddBudgetOpen(false)}
        categories={categories}
        onSuccess={loadAllData}
      />

      <AddRecurringModal
        isOpen={addRecurringOpen}
        onClose={() => setAddRecurringOpen(false)}
        categories={categories}
        onSuccess={loadAllData}
      />

      <NotificationsModal
        isOpen={notifOpen}
        onClose={() => setNotifOpen(false)}
        notifications={notifications}
        onRefresh={loadAllData}
      />

      <AuthModal
        isOpen={authModalOpen}
        onClose={() => setAuthModalOpen(false)}
      />

    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <MainApp />
    </AuthProvider>
  );
}
