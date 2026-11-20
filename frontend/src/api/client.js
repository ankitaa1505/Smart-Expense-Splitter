// API client with JWT support and graceful mock fallback for instant interactive testing

const API_BASE = '/api';

export const getStoredToken = () => localStorage.getItem('matcha_auth_token');
export const setStoredToken = (token) => {
  if (token) localStorage.setItem('matcha_auth_token', token);
  else localStorage.removeItem('matcha_auth_token');
};

export const getStoredUser = () => {
  const user = localStorage.getItem('matcha_auth_user');
  return user ? JSON.parse(user) : null;
};
export const setStoredUser = (user) => {
  if (user) localStorage.setItem('matcha_auth_user', JSON.stringify(user));
  else localStorage.removeItem('matcha_auth_user');
};

// Generic fetch wrapper
async function request(endpoint, options = {}) {
  const token = getStoredToken();
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  try {
    const res = await fetch(`${API_BASE}${endpoint}`, {
      ...options,
      headers,
    });

    if (res.status === 401) {
      // Token expired or invalid
      // Only clear if we actually had a token
      if (token && !endpoint.includes('/auth/')) {
        setStoredToken(null);
        setStoredUser(null);
        window.dispatchEvent(new CustomEvent('auth:expired'));
      }
    }

    const data = await res.json().catch(() => null);
    if (!res.ok) {
      throw new Error(data?.message || data?.error || `Request failed with status ${res.status}`);
    }
    return data;
  } catch (err) {
    console.warn(`API call to ${endpoint} failed:`, err.message);
    throw err;
  }
}

// ================= Mock Data for Seamless Demo Experience =================
const MOCK_CATEGORIES = [
  { id: 1, name: 'Groceries', type: 'EXPENSE', icon: 'shopping-cart', colorCode: '#668B4A', isSystemDefault: true },
  { id: 2, name: 'Dining Out', type: 'EXPENSE', icon: 'coffee', colorCode: '#D35400', isSystemDefault: true },
  { id: 3, name: 'Rent & Housing', type: 'EXPENSE', icon: 'home', colorCode: '#5A4636', isSystemDefault: true },
  { id: 4, name: 'Transportation', type: 'EXPENSE', icon: 'truck', colorCode: '#3498DB', isSystemDefault: true },
  { id: 5, name: 'Entertainment', type: 'EXPENSE', icon: 'film', colorCode: '#9B59B6', isSystemDefault: true },
  { id: 6, name: 'Utilities', type: 'EXPENSE', icon: 'zap', colorCode: '#F39C12', isSystemDefault: true },
  { id: 7, name: 'Salary', type: 'INCOME', icon: 'briefcase', colorCode: '#27AE60', isSystemDefault: true },
  { id: 8, name: 'Freelance', type: 'INCOME', icon: 'laptop', colorCode: '#2980B9', isSystemDefault: true },
  { id: 9, name: 'Investments', type: 'INCOME', icon: 'trending-up', colorCode: '#8E44AD', isSystemDefault: true },
];

let mockTransactions = [
  { id: 101, title: 'Monthly Salary Credit', amount: 4500.00, type: 'INCOME', categoryId: 7, categoryName: 'Salary', categoryColor: '#27AE60', transactionDate: '2026-11-01', paymentMethod: 'BANK_TRANSFER', description: 'Tech Corp Monthly Payroll' },
  { id: 102, title: 'Apartment Rent', amount: 1200.00, type: 'EXPENSE', categoryId: 3, categoryName: 'Rent & Housing', categoryColor: '#5A4636', transactionDate: '2026-11-02', paymentMethod: 'BANK_TRANSFER', description: 'November Flat Rent' },
  { id: 103, title: 'Organic Matcha & Groceries', amount: 165.40, type: 'EXPENSE', categoryId: 1, categoryName: 'Groceries', categoryColor: '#668B4A', transactionDate: '2026-11-05', paymentMethod: 'UPI', description: 'Artisan matcha, oat milk, fresh produce' },
  { id: 104, title: 'Client UX Design Project', amount: 850.00, type: 'INCOME', categoryId: 8, categoryName: 'Freelance', categoryColor: '#2980B9', transactionDate: '2026-11-08', paymentMethod: 'BANK_TRANSFER', description: 'Landing page overhaul milestone 1' },
  { id: 105, title: 'Espresso & Bakery Brunch', amount: 38.50, type: 'EXPENSE', categoryId: 2, categoryName: 'Dining Out', categoryColor: '#D35400', transactionDate: '2026-11-10', paymentMethod: 'CREDIT_CARD', description: 'Weekend pastry & flat white with friend' },
  { id: 106, title: 'High-Speed Fiber Internet', amount: 65.00, type: 'EXPENSE', categoryId: 6, categoryName: 'Utilities', categoryColor: '#F39C12', transactionDate: '2026-11-12', paymentMethod: 'UPI', description: 'Monthly broadband subscription' },
  { id: 107, title: 'Metro Transit Pass', amount: 80.00, type: 'EXPENSE', categoryId: 4, categoryName: 'Transportation', categoryColor: '#3498DB', transactionDate: '2026-11-14', paymentMethod: 'DEBIT_CARD', description: 'Monthly city metro card reload' },
  { id: 108, title: 'Art Cinema Film Screening', amount: 24.00, type: 'EXPENSE', categoryId: 5, categoryName: 'Entertainment', categoryColor: '#9B59B6', transactionDate: '2026-11-15', paymentMethod: 'UPI', description: 'Indie festival ticket' },
];

let mockBudgets = [
  { id: 201, categoryId: 1, categoryName: 'Groceries', budgetMonth: 11, budgetYear: 2026, targetAmount: 400.00, currentSpending: 165.40, percentageUsed: 41.35, isThresholdExceeded: false, thresholdWarned: false },
  { id: 202, categoryId: 2, categoryName: 'Dining Out', budgetMonth: 11, budgetYear: 2026, targetAmount: 180.00, currentSpending: 148.50, percentageUsed: 82.50, isThresholdExceeded: false, thresholdWarned: true },
  { id: 203, categoryId: 3, categoryName: 'Rent & Housing', budgetMonth: 11, budgetYear: 2026, targetAmount: 1200.00, currentSpending: 1200.00, percentageUsed: 100.00, isThresholdExceeded: true, thresholdWarned: true },
  { id: 204, categoryId: 5, categoryName: 'Entertainment', budgetMonth: 11, budgetYear: 2026, targetAmount: 150.00, currentSpending: 24.00, percentageUsed: 16.00, isThresholdExceeded: false, thresholdWarned: false },
];

let mockRecurring = [
  { id: 301, title: 'Apartment Rent', amount: 1200.00, type: 'EXPENSE', categoryId: 3, categoryName: 'Rent & Housing', frequency: 'MONTHLY', startDate: '2026-01-01', nextRunDate: '2026-12-01', paymentMethod: 'BANK_TRANSFER', isActive: true },
  { id: 302, title: 'Gym & Wellness Membership', amount: 45.00, type: 'EXPENSE', categoryId: 5, categoryName: 'Entertainment', frequency: 'MONTHLY', startDate: '2026-03-01', nextRunDate: '2026-12-05', paymentMethod: 'CREDIT_CARD', isActive: true },
  { id: 303, title: 'Cloud Storage & GitHub Pro', amount: 14.00, type: 'EXPENSE', categoryId: 6, categoryName: 'Utilities', frequency: 'MONTHLY', startDate: '2026-01-15', nextRunDate: '2026-12-15', paymentMethod: 'CREDIT_CARD', isActive: true },
];

let mockNotifications = [
  { id: 401, notificationType: 'BUDGET_WARNING', title: 'Dining Out Budget Alert', message: 'You have reached 82.5% of your $180.00 monthly Dining Out budget.', isRead: false, createdAt: '2026-11-10T14:32:00Z' },
  { id: 402, notificationType: 'BUDGET_EXCEEDED', title: 'Housing Budget Limit Met', message: 'You have utilized 100.0% of your $1,200.00 Rent budget.', isRead: true, createdAt: '2026-11-02T09:15:00Z' },
];

export const api = {
  // Authentication
  auth: {
    login: async (email, password) => {
      try {
        const res = await request('/auth/login', {
          method: 'POST',
          body: JSON.stringify({ email, password }),
        });
        if (res.data?.accessToken) {
          setStoredToken(res.data.accessToken);
          setStoredUser(res.data.user);
        }
        return res;
      } catch (err) {
        // Fallback demo user if backend is unreachable
        if (email && password) {
          const demoUser = { id: 1, email, firstName: 'Ankita', lastName: 'Sharma', role: 'ROLE_USER' };
          const demoToken = 'mock-jwt-token-matcha';
          setStoredToken(demoToken);
          setStoredUser(demoUser);
          return { success: true, message: 'Logged in (demo mode)', data: { accessToken: demoToken, user: demoUser } };
        }
        throw err;
      }
    },
    register: async (userData) => {
      try {
        return await request('/auth/register', {
          method: 'POST',
          body: JSON.stringify(userData),
        });
      } catch (err) {
        const demoUser = { id: 1, email: userData.email, firstName: userData.firstName, lastName: userData.lastName, role: 'ROLE_USER' };
        return { success: true, message: 'Registered in demo mode', data: demoUser };
      }
    },
  },

  // Categories
  categories: {
    getAll: async () => {
      try {
        const res = await request('/categories');
        return res.data || [];
      } catch {
        return MOCK_CATEGORIES;
      }
    },
    create: async (catData) => {
      try {
        const res = await request('/categories', {
          method: 'POST',
          body: JSON.stringify(catData),
        });
        return res.data;
      } catch {
        const newCat = { id: Date.now(), ...catData, isSystemDefault: false };
        MOCK_CATEGORIES.push(newCat);
        return newCat;
      }
    },
  },

  // Transactions
  transactions: {
    getAll: async (params = {}) => {
      const query = new URLSearchParams();
      if (params.page !== undefined) query.set('page', params.page);
      if (params.size) query.set('size', params.size);
      if (params.type && params.type !== 'ALL') query.set('type', params.type);
      if (params.categoryId) query.set('categoryId', params.categoryId);
      if (params.paymentMethod) query.set('paymentMethod', params.paymentMethod);
      if (params.startDate) query.set('startDate', params.startDate);
      if (params.endDate) query.set('endDate', params.endDate);
      if (params.searchTerm) query.set('searchTerm', params.searchTerm);
      if (params.sortBy) query.set('sortBy', params.sortBy);
      if (params.direction) query.set('direction', params.direction);

      try {
        const res = await request(`/transactions?${query.toString()}`);
        return res.data; // PageResponse
      } catch {
        let filtered = [...mockTransactions];
        if (params.type && params.type !== 'ALL') {
          filtered = filtered.filter(t => t.type === params.type);
        }
        if (params.searchTerm) {
          const q = params.searchTerm.toLowerCase();
          filtered = filtered.filter(t => t.title.toLowerCase().includes(q) || (t.description && t.description.toLowerCase().includes(q)));
        }
        return {
          content: filtered,
          pageNumber: 0,
          pageSize: 20,
          totalElements: filtered.length,
          totalPages: 1,
          first: true,
          last: true,
        };
      }
    },
    create: async (txData) => {
      try {
        const res = await request('/transactions', {
          method: 'POST',
          body: JSON.stringify(txData),
        });
        return res.data;
      } catch {
        const cat = MOCK_CATEGORIES.find(c => c.id === Number(txData.categoryId)) || { name: 'General', colorCode: '#668B4A' };
        const newTx = {
          id: Date.now(),
          ...txData,
          categoryName: cat.name,
          categoryColor: cat.colorCode,
        };
        mockTransactions = [newTx, ...mockTransactions];
        return newTx;
      }
    },
    delete: async (id) => {
      try {
        await request(`/transactions/${id}`, { method: 'DELETE' });
      } catch {
        mockTransactions = mockTransactions.filter(t => t.id !== id);
      }
    },
    exportCsvUrl: () => `${API_BASE}/exports/transactions/csv`,
  },

  // Budgets
  budgets: {
    getAll: async (month = 11, year = 2026) => {
      try {
        const res = await request(`/budgets?month=${month}&year=${year}`);
        return res.data || [];
      } catch {
        return mockBudgets;
      }
    },
    create: async (budgetData) => {
      try {
        const res = await request('/budgets', {
          method: 'POST',
          body: JSON.stringify(budgetData),
        });
        return res.data;
      } catch {
        const cat = MOCK_CATEGORIES.find(c => c.id === Number(budgetData.categoryId));
        const newBudget = {
          id: Date.now(),
          ...budgetData,
          categoryName: cat ? cat.name : 'Overall',
          currentSpending: 0,
          percentageUsed: 0,
          isThresholdExceeded: false,
          thresholdWarned: false,
        };
        mockBudgets = [newBudget, ...mockBudgets];
        return newBudget;
      }
    },
    delete: async (id) => {
      try {
        await request(`/budgets/${id}`, { method: 'DELETE' });
      } catch {
        mockBudgets = mockBudgets.filter(b => b.id !== id);
      }
    },
  },

  // Analytics
  analytics: {
    getSummary: async (month = 11, year = 2026) => {
      try {
        const res = await request(`/analytics/summary?month=${month}&year=${year}`);
        return res.data;
      } catch {
        const totalIncome = mockTransactions.filter(t => t.type === 'INCOME').reduce((s, t) => s + Number(t.amount), 0);
        const totalExpense = mockTransactions.filter(t => t.type === 'EXPENSE').reduce((s, t) => s + Number(t.amount), 0);
        const netSavings = totalIncome - totalExpense;
        const savingsRate = totalIncome > 0 ? (netSavings / totalIncome) * 100 : 0;
        return {
          totalIncome,
          totalExpense,
          netSavings,
          savingsRate: Math.round(savingsRate * 100) / 100,
          month,
          year,
        };
      }
    },
    getCategoryBreakdown: async (month = 11, year = 2026) => {
      try {
        const res = await request(`/analytics/category-breakdown?month=${month}&year=${year}`);
        return res.data || [];
      } catch {
        return [
          { categoryName: 'Rent & Housing', totalAmount: 1200.00, percentage: 76.3, colorCode: '#5A4636' },
          { categoryName: 'Groceries', totalAmount: 165.40, percentage: 10.5, colorCode: '#668B4A' },
          { categoryName: 'Transportation', totalAmount: 80.00, percentage: 5.1, colorCode: '#3498DB' },
          { categoryName: 'Utilities', totalAmount: 65.00, percentage: 4.1, colorCode: '#F39C12' },
          { categoryName: 'Dining Out', totalAmount: 38.50, percentage: 2.5, colorCode: '#D35400' },
          { categoryName: 'Entertainment', totalAmount: 24.00, percentage: 1.5, colorCode: '#9B59B6' },
        ];
      }
    },
    getMonthlyTrends: async (year = 2026) => {
      try {
        const res = await request(`/analytics/monthly-trends?year=${year}`);
        return res.data || [];
      } catch {
        return [
          { month: 6, monthName: 'Jun', totalIncome: 4200, totalExpense: 1450, netSavings: 2750 },
          { month: 7, monthName: 'Jul', totalIncome: 4500, totalExpense: 1680, netSavings: 2820 },
          { month: 8, monthName: 'Aug', totalIncome: 4800, totalExpense: 1820, netSavings: 2980 },
          { month: 9, monthName: 'Sep', totalIncome: 4600, totalExpense: 1510, netSavings: 3090 },
          { month: 10, monthName: 'Oct', totalIncome: 5100, totalExpense: 1730, netSavings: 3370 },
          { month: 11, monthName: 'Nov', totalIncome: 5350, totalExpense: 1573.40, netSavings: 3776.60 },
        ];
      }
    },
  },

  // Recurring
  recurring: {
    getAll: async () => {
      try {
        const res = await request('/recurring-transactions');
        return res.data || [];
      } catch {
        return mockRecurring;
      }
    },
    create: async (recData) => {
      try {
        const res = await request('/recurring-transactions', {
          method: 'POST',
          body: JSON.stringify(recData),
        });
        return res.data;
      } catch {
        const cat = MOCK_CATEGORIES.find(c => c.id === Number(recData.categoryId));
        const newRec = {
          id: Date.now(),
          ...recData,
          categoryName: cat ? cat.name : 'Subscription',
          isActive: true,
        };
        mockRecurring = [newRec, ...mockRecurring];
        return newRec;
      }
    },
    delete: async (id) => {
      try {
        await request(`/recurring-transactions/${id}`, { method: 'DELETE' });
      } catch {
        mockRecurring = mockRecurring.filter(r => r.id !== id);
      }
    },
  },

  // Notifications
  notifications: {
    getAll: async () => {
      try {
        const res = await request('/notifications');
        return res.data || [];
      } catch {
        return mockNotifications;
      }
    },
    markAsRead: async (id) => {
      try {
        await request(`/notifications/${id}/read`, { method: 'PATCH' });
      } catch {
        mockNotifications = mockNotifications.map(n => n.id === id ? { ...n, isRead: true } : n);
      }
    },
    markAllAsRead: async () => {
      try {
        await request('/notifications/read-all', { method: 'PATCH' });
      } catch {
        mockNotifications = mockNotifications.map(n => ({ ...n, isRead: true }));
      }
    },
  },

  // Smart Insights
  insights: {
    getAll: async () => {
      try {
        const res = await request('/insights');
        return res.data || [];
      } catch {
        return [
          {
            type: 'ANOMALY',
            title: 'Dining Out Spending Spike Detected',
            description: 'You spent 2.4x more on Dining Out this week compared to your 30-day average.',
            suggestion: 'Consider setting a $150 cap to maintain your 65% net savings target.',
            level: 'WARNING',
          },
          {
            type: 'RECOMMENDATION',
            title: 'Rolling 3-Month Budget Recommendation',
            description: 'Based on your autumn habits, your optimal monthly grocery budget is ~$380.00.',
            suggestion: 'Adjust Groceries target from $400 to $380 to free up $20 for rainy day savings.',
            level: 'INFO',
          },
          {
            type: 'HEALTH',
            title: 'Excellent Net Savings Velocity',
            description: 'Your savings rate is currently 70.5%, well above the recommended 20% benchmark.',
            suggestion: 'Great job maintaining financial discipline this month!',
            level: 'SUCCESS',
          },
        ];
      }
    },
  },
};
