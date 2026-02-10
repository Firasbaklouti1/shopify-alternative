'use client';

import React, { createContext, useContext, useState, useCallback, useEffect } from 'react';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

interface AuthState {
  token: string | null;
  tenantId: number | null;
  storeSlug: string | null;
  email: string | null;
}

interface AuthContextType extends AuthState {
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
  apiUrl: string;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [authState, setAuthState] = useState<AuthState>({
    token: null,
    tenantId: null,
    storeSlug: null,
    email: null,
  });
  const [hydrated, setHydrated] = useState(false);

  useEffect(() => {
    const stored = localStorage.getItem('admin-auth');
    if (stored) {
      try {
        setAuthState(JSON.parse(stored));
      } catch {
        localStorage.removeItem('admin-auth');
      }
    }
    setHydrated(true);
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    const res = await fetch(`${API_BASE_URL}/api/v1/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    });

    if (!res.ok) {
      const error = await res.json().catch(() => ({ message: 'Login failed' }));
      throw new Error(error.message || `Login failed: ${res.status}`);
    }

    const data = await res.json();

    // Fetch tenant info to get storeSlug
    const tenantRes = await fetch(`${API_BASE_URL}/api/v1/tenants/my`, {
      headers: { Authorization: `Bearer ${data.token}` },
    });

    let storeSlug = '';
    if (tenantRes.ok) {
      const tenant = await tenantRes.json();
      storeSlug = tenant.slug;
    }

    const newState: AuthState = {
      token: data.token,
      tenantId: data.tenantId,
      storeSlug,
      email,
    };

    setAuthState(newState);
    localStorage.setItem('admin-auth', JSON.stringify(newState));
  }, []);

  const logout = useCallback(() => {
    setAuthState({ token: null, tenantId: null, storeSlug: null, email: null });
    localStorage.removeItem('admin-auth');
  }, []);

  if (!hydrated) {
    return null;
  }

  return (
    <AuthContext.Provider
      value={{
        ...authState,
        login,
        logout,
        isAuthenticated: !!authState.token,
        apiUrl: API_BASE_URL,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextType {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
