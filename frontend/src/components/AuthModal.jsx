import React, { useState } from 'react';
import { X, LogIn, UserPlus, Leaf } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export default function AuthModal({ isOpen, onClose }) {
  const { login, register } = useAuth();
  const [tab, setTab] = useState('login'); // 'login' | 'register'
  const [email, setEmail] = useState('demo@example.com');
  const [password, setPassword] = useState('Password123!');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    if (tab === 'login') {
      const res = await login(email, password);
      if (res.success) {
        onClose();
      } else {
        setError(res.message || 'Login failed. Please check your credentials.');
      }
    } else {
      if (!firstName || !lastName) {
        setError('Please enter your full name');
        setLoading(false);
        return;
      }
      const res = await register({
        email,
        password,
        firstName,
        lastName,
      });
      if (res.success) {
        onClose();
      } else {
        setError(res.message || 'Registration failed');
      }
    }
    setLoading(false);
  };

  const fillDemoCredentials = () => {
    setEmail('demo@example.com');
    setPassword('Password123!');
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm animate-in fade-in duration-150">
      <div 
        className="bg-[#FDFCF7] border border-[#D6CAC1] w-full max-w-md rounded-3xl shadow-2xl p-6 sm:p-8 space-y-5 animate-in zoom-in-95 duration-150"
        onClick={(e) => e.stopPropagation()}
      >
        
        {/* Header */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="w-10 h-10 rounded-2xl bg-[#668B4A] text-[#F8F1DC] flex items-center justify-center shadow-md shadow-[#668B4A]/25">
              <Leaf className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-lg font-black text-[#5A4636]">
                {tab === 'login' ? 'Welcome Back' : 'Create Account'}
              </h3>
              <p className="text-xs text-[#897262]">MatchaSpend • Secure JWT Auth</p>
            </div>
          </div>
          <button 
            onClick={onClose}
            className="p-1.5 rounded-xl text-[#897262] hover:text-[#5A4636] hover:bg-[#EAE4DF] transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Tab switch */}
        <div className="grid grid-cols-2 gap-1 bg-[#EAE4DF]/60 p-1 rounded-2xl border border-[#D6CAC1]/60 text-xs font-bold">
          <button
            type="button"
            onClick={() => { setTab('login'); setError(''); }}
            className={`py-2 rounded-xl transition-all ${
              tab === 'login'
                ? 'bg-[#668B4A] text-[#F8F1DC] shadow-sm'
                : 'text-[#897262] hover:text-[#5A4636]'
            }`}
          >
            Sign In
          </button>
          <button
            type="button"
            onClick={() => { setTab('register'); setError(''); }}
            className={`py-2 rounded-xl transition-all ${
              tab === 'register'
                ? 'bg-[#668B4A] text-[#F8F1DC] shadow-sm'
                : 'text-[#897262] hover:text-[#5A4636]'
            }`}
          >
            Register
          </button>
        </div>

        {error && (
          <div className="p-3 rounded-2xl bg-[#D35400]/10 border border-[#D35400]/20 text-xs font-semibold text-[#D35400]">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-3.5 text-xs">
          
          {tab === 'register' && (
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-[11px] font-bold text-[#5A4636] mb-1">First Name</label>
                <input
                  type="text"
                  placeholder="Ankita"
                  value={firstName}
                  onChange={(e) => setFirstName(e.target.value)}
                  className="w-full bg-[#EAE4DF]/40 border border-[#D6CAC1] rounded-2xl px-3.5 py-2.5 text-xs text-[#5A4636] focus:outline-none focus:ring-2 focus:ring-[#668B4A]"
                  required
                />
              </div>
              <div>
                <label className="block text-[11px] font-bold text-[#5A4636] mb-1">Last Name</label>
                <input
                  type="text"
                  placeholder="Sharma"
                  value={lastName}
                  onChange={(e) => setLastName(e.target.value)}
                  className="w-full bg-[#EAE4DF]/40 border border-[#D6CAC1] rounded-2xl px-3.5 py-2.5 text-xs text-[#5A4636] focus:outline-none focus:ring-2 focus:ring-[#668B4A]"
                  required
                />
              </div>
            </div>
          )}

          <div>
            <label className="block text-[11px] font-bold text-[#5A4636] mb-1">Email Address</label>
            <input
              type="email"
              placeholder="name@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full bg-[#EAE4DF]/40 border border-[#D6CAC1] rounded-2xl px-3.5 py-2.5 text-xs text-[#5A4636] focus:outline-none focus:ring-2 focus:ring-[#668B4A]"
              required
            />
          </div>

          <div>
            <label className="block text-[11px] font-bold text-[#5A4636] mb-1">Password</label>
            <input
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full bg-[#EAE4DF]/40 border border-[#D6CAC1] rounded-2xl px-3.5 py-2.5 text-xs text-[#5A4636] focus:outline-none focus:ring-2 focus:ring-[#668B4A]"
              required
            />
          </div>

          {tab === 'login' && (
            <div className="flex items-center justify-between text-[11px]">
              <button
                type="button"
                onClick={fillDemoCredentials}
                className="text-[#668B4A] hover:underline font-semibold"
              >
                Auto-fill Demo Account
              </button>
            </div>
          )}

          <div className="pt-2">
            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 rounded-2xl bg-[#668B4A] hover:bg-[#54753C] text-[#F8F1DC] font-bold text-xs shadow-md shadow-[#668B4A]/25 transition-all hover:scale-[1.02] active:scale-[0.98] disabled:opacity-50"
            >
              {loading ? 'Please wait...' : tab === 'login' ? 'Sign In to Dashboard' : 'Create My Account'}
            </button>
          </div>

        </form>

      </div>
    </div>
  );
}
