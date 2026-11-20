import React, { createContext, useContext, useState, useEffect } from 'react';
import { api, getStoredUser, getStoredToken, setStoredToken, setStoredUser } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => getStoredUser() || {
    id: 1,
    email: 'ankita@example.com',
    firstName: 'Ankita',
    lastName: 'Sharma',
    role: 'ROLE_USER',
  });
  const [token, setToken] = useState(() => getStoredToken() || 'demo-token');
  const [loading, setLoading] = useState(false);
  const [authModalOpen, setAuthModalOpen] = useState(false);

  useEffect(() => {
    const handleExpired = () => {
      setUser(null);
      setToken(null);
      setAuthModalOpen(true);
    };
    window.addEventListener('auth:expired', handleExpired);
    return () => window.removeEventListener('auth:expired', handleExpired);
  }, []);

  const login = async (email, password) => {
    setLoading(true);
    try {
      const res = await api.auth.login(email, password);
      const loggedUser = res.data?.user || { email, firstName: 'User', role: 'ROLE_USER' };
      const authToken = res.data?.accessToken || 'token';
      setUser(loggedUser);
      setToken(authToken);
      setAuthModalOpen(false);
      return { success: true };
    } catch (err) {
      return { success: false, message: err.message };
    } finally {
      setLoading(false);
    }
  };

  const register = async (userData) => {
    setLoading(true);
    try {
      const res = await api.auth.register(userData);
      // Auto login after register
      await login(userData.email, userData.password);
      return { success: true };
    } catch (err) {
      return { success: false, message: err.message };
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    setStoredToken(null);
    setStoredUser(null);
    setUser(null);
    setToken(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token,
        loading,
        login,
        register,
        logout,
        authModalOpen,
        setAuthModalOpen,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
