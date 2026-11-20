import React, { useState } from 'react';
import { 
  Leaf, 
  Wallet, 
  Receipt, 
  PieChart, 
  CalendarClock, 
  Plus, 
  Bell, 
  User, 
  LogOut, 
  LogIn,
  ChevronDown
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export default function Navbar({ 
  activeTab, 
  setActiveTab, 
  onOpenAddModal, 
  onOpenNotifications,
  unreadCount = 0 
}) {
  const { user, isAuthenticated, logout, setAuthModalOpen } = useAuth();
  const [userMenuOpen, setUserMenuOpen] = useState(false);

  const navItems = [
    { id: 'dashboard', label: 'Overview', icon: Wallet },
    { id: 'transactions', label: 'Transactions', icon: Receipt },
    { id: 'budgets', label: 'Budgets & Alerts', icon: Leaf },
    { id: 'analytics', label: 'Analytics', icon: PieChart },
    { id: 'recurring', label: 'Recurring Bills', icon: CalendarClock },
  ];

  return (
    <header className="sticky top-0 z-40 bg-[#F8F1DC]/90 backdrop-blur-md border-b border-[#D6CAC1]/60 px-4 sm:px-8 py-3.5 transition-all">
      <div className="max-w-7xl mx-auto flex items-center justify-between">
        
        {/* Brand / Logo */}
        <div 
          onClick={() => setActiveTab('dashboard')}
          className="flex items-center gap-3 cursor-pointer group"
        >
          <div className="w-10 h-10 rounded-2xl bg-[#668B4A] flex items-center justify-center text-[#F8F1DC] shadow-md shadow-[#668B4A]/25 group-hover:scale-105 transition-transform">
            <Leaf className="w-5 h-5 transition-transform group-hover:rotate-12" />
          </div>
          <div>
            <div className="flex items-center gap-1.5">
              <span className="font-bold text-xl tracking-tight text-[#5A4636]">Matcha<span className="text-[#668B4A]">Spend</span></span>
              <span className="text-[10px] uppercase font-bold tracking-widest px-1.5 py-0.5 rounded bg-[#668B4A]/15 text-[#54753C]">v1.0</span>
            </div>
            <p className="text-[11px] text-[#897262] font-medium leading-none hidden sm:block">Mindful Personal Finance</p>
          </div>
        </div>

        {/* Navigation Tabs */}
        <nav className="hidden md:flex items-center gap-1 bg-[#EAE4DF]/70 p-1.5 rounded-2xl border border-[#D6CAC1]/50 shadow-inner">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = activeTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => setActiveTab(item.id)}
                className={`flex items-center gap-2 px-3.5 py-2 rounded-xl text-xs sm:text-sm font-semibold transition-all ${
                  isActive
                    ? 'bg-[#668B4A] text-[#F8F1DC] shadow-sm shadow-[#668B4A]/30'
                    : 'text-[#5A4636] hover:bg-[#F8F1DC]/60 hover:text-[#425C2F]'
                }`}
              >
                <Icon className={`w-4 h-4 ${isActive ? 'text-[#F8F1DC]' : 'text-[#897262]'}`} />
                <span>{item.label}</span>
              </button>
            );
          })}
        </nav>

        {/* Right Actions: Add Button, Notifications, User */}
        <div className="flex items-center gap-2.5 sm:gap-3">
          
          {/* Quick Add Button */}
          <button
            onClick={onOpenAddModal}
            className="flex items-center gap-1.5 bg-[#668B4A] hover:bg-[#54753C] text-[#F8F1DC] px-3.5 py-2 rounded-xl text-xs sm:text-sm font-semibold shadow-md shadow-[#668B4A]/20 transition-all hover:scale-[1.02] active:scale-[0.98]"
          >
            <Plus className="w-4 h-4 stroke-[2.5]" />
            <span className="hidden sm:inline">Add Entry</span>
          </button>

          {/* Notifications Bell */}
          <button
            onClick={onOpenNotifications}
            className="relative p-2.5 rounded-xl text-[#5A4636] hover:bg-[#EAE4DF] border border-[#D6CAC1]/60 transition-colors"
            title="Notifications & Alerts"
          >
            <Bell className="w-4 h-4" />
            {unreadCount > 0 && (
              <span className="absolute -top-1 -right-1 w-4 h-4 rounded-full bg-[#D35400] text-white text-[10px] font-bold flex items-center justify-center animate-pulse">
                {unreadCount}
              </span>
            )}
          </button>

          {/* User Profile / Auth */}
          <div className="relative">
            {isAuthenticated ? (
              <button
                onClick={() => setUserMenuOpen(!userMenuOpen)}
                className="flex items-center gap-2 p-1.5 pl-2.5 pr-2 rounded-xl border border-[#D6CAC1]/70 hover:bg-[#EAE4DF] transition-all bg-[#FDFCF7]"
              >
                <div className="w-6 h-6 rounded-lg bg-[#5A4636] text-[#F8F1DC] flex items-center justify-center text-xs font-bold">
                  {user?.firstName?.[0] || 'U'}
                </div>
                <span className="text-xs font-semibold text-[#5A4636] hidden lg:inline max-w-[100px] truncate">
                  {user?.firstName || 'User'}
                </span>
                <ChevronDown className="w-3.5 h-3.5 text-[#897262]" />
              </button>
            ) : (
              <button
                onClick={() => setAuthModalOpen(true)}
                className="flex items-center gap-1.5 bg-[#5A4636] hover:bg-[#49372A] text-[#F8F1DC] px-3 py-2 rounded-xl text-xs font-semibold shadow-sm transition-all"
              >
                <LogIn className="w-3.5 h-3.5" />
                <span>Sign In</span>
              </button>
            )}

            {/* User Dropdown */}
            {userMenuOpen && (
              <div 
                className="absolute right-0 mt-2 w-56 bg-[#FDFCF7] border border-[#D6CAC1] rounded-2xl shadow-xl py-2 z-50 animate-in fade-in zoom-in-95 duration-100"
                onClick={() => setUserMenuOpen(false)}
              >
                <div className="px-4 py-2.5 border-b border-[#D6CAC1]/40">
                  <p className="text-xs font-bold text-[#5A4636]">{user?.firstName} {user?.lastName}</p>
                  <p className="text-[11px] text-[#897262] truncate">{user?.email}</p>
                  <span className="inline-block mt-1 text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full bg-[#668B4A]/15 text-[#54753C]">
                    {user?.role || 'User'}
                  </span>
                </div>

                <div className="p-1">
                  <a
                    href="/swagger-ui/index.html"
                    target="_blank"
                    rel="noreferrer"
                    className="w-full flex items-center gap-2 px-3 py-2 text-xs font-semibold text-[#5A4636] hover:bg-[#EAE4DF] rounded-xl transition-colors text-left"
                  >
                    <span>📜 Swagger API Docs</span>
                  </a>
                  <button
                    onClick={() => {
                      logout();
                      setUserMenuOpen(false);
                    }}
                    className="w-full flex items-center gap-2 px-3 py-2 text-xs font-semibold text-[#D35400] hover:bg-[#D35400]/10 rounded-xl transition-colors text-left"
                  >
                    <LogOut className="w-3.5 h-3.5" />
                    <span>Sign Out</span>
                  </button>
                </div>
              </div>
            )}
          </div>

        </div>

      </div>

      {/* Mobile Tab Bar */}
      <div className="flex md:hidden items-center justify-around mt-3 pt-2 border-t border-[#D6CAC1]/40 overflow-x-auto">
        {navItems.map((item) => {
          const Icon = item.icon;
          const isActive = activeTab === item.id;
          return (
            <button
              key={item.id}
              onClick={() => setActiveTab(item.id)}
              className={`flex flex-col items-center gap-1 py-1 px-2 text-[10px] font-semibold transition-colors ${
                isActive ? 'text-[#668B4A]' : 'text-[#897262]'
              }`}
            >
              <Icon className="w-4 h-4" />
              <span>{item.label}</span>
            </button>
          );
        })}
      </div>
    </header>
  );
}
