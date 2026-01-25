'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { useParams } from 'next/navigation';
import { getStoreSettings, StoreSettings } from '@/lib/api';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

interface Order {
  id: number;
  orderNumber: string;
  status: string;
  totalAmount: number;
  createdAt: string;
}

interface AuthState {
  isAuthenticated: boolean;
  token: string | null;
  email: string | null;
}

type TabType = 'login' | 'register' | 'orders';

export default function AccountPage() {
  const params = useParams();
  const slug = params.slug as string;

  const [settings, setSettings] = useState<StoreSettings | null>(null);
  const [activeTab, setActiveTab] = useState<TabType>('login');
  const [auth, setAuth] = useState<AuthState>({ isAuthenticated: false, token: null, email: null });
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [storeError, setStoreError] = useState<string | null>(null);
  const [mounted, setMounted] = useState(false);

  // Login form state
  const [loginEmail, setLoginEmail] = useState('');
  const [loginPassword, setLoginPassword] = useState('');

  // Register form state
  const [registerData, setRegisterData] = useState({
    email: '',
    password: '',
    confirmPassword: '',
    firstName: '',
    lastName: '',
    phone: '',
  });

  useEffect(() => {
    setMounted(true);
    // Load auth state from localStorage
    const savedToken = localStorage.getItem(`${slug}_customer_token`);
    const savedEmail = localStorage.getItem(`${slug}_customer_email`);
    if (savedToken && savedEmail) {
      setAuth({ isAuthenticated: true, token: savedToken, email: savedEmail });
      setActiveTab('orders');
    }

    // Fetch store settings
    getStoreSettings(slug)
      .then(setSettings)
      .catch((err) => {
        console.error('Failed to fetch store settings:', err);
        setStoreError(`Store "${slug}" not found or not published. Please make sure the store exists.`);
      });
  }, [slug]);

  useEffect(() => {
    if (auth.isAuthenticated && auth.token) {
      fetchOrders();
    }
  }, [auth.isAuthenticated, auth.token]);

  const fetchOrders = async () => {
    if (!auth.token) return;
    try {
      const res = await fetch(`${API_BASE_URL}/api/v1/orders/my`, {
        headers: {
          Authorization: `Bearer ${auth.token}`,
        },
      });
      if (res.ok) {
        const data = await res.json();
        setOrders(data);
      }
    } catch (err) {
      console.error('Failed to fetch orders:', err);
    }
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const res = await fetch(`${API_BASE_URL}/api/v1/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: loginEmail, password: loginPassword }),
      });

      if (!res.ok) {
        const data = await res.json();
        throw new Error(data.message || 'Invalid email or password');
      }

      const data = await res.json();

      // Save to localStorage
      localStorage.setItem(`${slug}_customer_token`, data.token);
      localStorage.setItem(`${slug}_customer_email`, data.email);

      setAuth({ isAuthenticated: true, token: data.token, email: data.email });
      setActiveTab('orders');
    } catch (err: any) {
      setError(err.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    if (registerData.password !== registerData.confirmPassword) {
      setError('Passwords do not match');
      setLoading(false);
      return;
    }

    try {
      const res = await fetch(`${API_BASE_URL}/api/v1/auth/customer/${slug}/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: registerData.email,
          password: registerData.password,
          firstName: registerData.firstName,
          lastName: registerData.lastName,
          phone: registerData.phone || undefined,
        }),
      });

      if (!res.ok) {
        const data = await res.json();
        throw new Error(data.message || 'Registration failed');
      }

      const data = await res.json();

      // Save to localStorage and auto-login
      localStorage.setItem(`${slug}_customer_token`, data.token);
      localStorage.setItem(`${slug}_customer_email`, data.email);

      setAuth({ isAuthenticated: true, token: data.token, email: data.email });
      setActiveTab('orders');
    } catch (err: any) {
      setError(err.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem(`${slug}_customer_token`);
    localStorage.removeItem(`${slug}_customer_email`);
    setAuth({ isAuthenticated: false, token: null, email: null });
    setOrders([]);
    setActiveTab('login');
  };

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(price);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  if (!mounted) {
    return <div className="min-h-screen flex items-center justify-center">Loading...</div>;
  }

  if (storeError) {
    return (
      <div className="min-h-screen bg-gray-50 py-12 px-4">
        <div className="max-w-md mx-auto">
          <div className="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
            <h2 className="text-xl font-semibold text-red-800 mb-2">Store Not Found</h2>
            <p className="text-red-700 mb-4">{storeError}</p>
            <p className="text-sm text-gray-600">
              Make sure to run the <code className="bg-gray-100 px-1 rounded">store-demo.http</code> test file to create and publish the demo store.
            </p>
          </div>
        </div>
      </div>
    );
  }

  // Check checkout mode for registration display
  const showRegister = settings?.checkoutMode !== 'GUEST_ONLY';
  const showLogin = settings?.checkoutMode !== 'GUEST_ONLY';

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4">
      <div className="max-w-md mx-auto">
        <h1 className="text-3xl font-bold text-center text-gray-900 mb-8">
          {auth.isAuthenticated ? 'My Account' : 'Account'}
        </h1>

        {auth.isAuthenticated ? (
          // Logged in view
          <div className="bg-white rounded-lg shadow-md p-6">
            <div className="flex justify-between items-center mb-6">
              <p className="text-gray-600">
                Logged in as <span className="font-medium">{auth.email}</span>
              </p>
              <button
                onClick={handleLogout}
                className="text-sm text-red-600 hover:text-red-700"
              >
                Logout
              </button>
            </div>

            <h2 className="text-xl font-semibold mb-4">My Orders</h2>

            {orders.length === 0 ? (
              <div className="text-center py-8">
                <p className="text-gray-500 mb-4">You haven't placed any orders yet.</p>
                <Link
                  href={`/store/${slug}/products`}
                  className="inline-block bg-gray-900 text-white px-6 py-2 rounded-md hover:bg-gray-800"
                >
                  Start Shopping
                </Link>
              </div>
            ) : (
              <div className="space-y-4">
                {orders.map((order) => (
                  <div key={order.id} className="border rounded-lg p-4">
                    <div className="flex justify-between items-start mb-2">
                      <div>
                        <p className="font-medium">Order #{order.orderNumber}</p>
                        <p className="text-sm text-gray-500">{formatDate(order.createdAt)}</p>
                      </div>
                      <span className={`px-2 py-1 text-xs rounded-full ${
                        order.status === 'DELIVERED' ? 'bg-green-100 text-green-800' :
                        order.status === 'SHIPPED' ? 'bg-blue-100 text-blue-800' :
                        order.status === 'CANCELLED' ? 'bg-red-100 text-red-800' :
                        'bg-gray-100 text-gray-800'
                      }`}>
                        {order.status}
                      </span>
                    </div>
                    <p className="font-semibold">{formatPrice(order.totalAmount)}</p>
                  </div>
                ))}
              </div>
            )}
          </div>
        ) : (
          // Login/Register view
          <div className="bg-white rounded-lg shadow-md">
            {/* Tabs */}
            {showLogin && showRegister && (
              <div className="flex border-b">
                <button
                  onClick={() => { setActiveTab('login'); setError(null); }}
                  className={`flex-1 py-4 text-center font-medium ${
                    activeTab === 'login'
                      ? 'border-b-2 border-gray-900 text-gray-900'
                      : 'text-gray-500 hover:text-gray-700'
                  }`}
                >
                  Login
                </button>
                <button
                  onClick={() => { setActiveTab('register'); setError(null); }}
                  className={`flex-1 py-4 text-center font-medium ${
                    activeTab === 'register'
                      ? 'border-b-2 border-gray-900 text-gray-900'
                      : 'text-gray-500 hover:text-gray-700'
                  }`}
                >
                  Register
                </button>
              </div>
            )}

            <div className="p-6">
              {error && (
                <div className="mb-4 p-3 bg-red-50 text-red-700 rounded-md text-sm">
                  {error}
                </div>
              )}

              {activeTab === 'login' && showLogin && (
                <form onSubmit={handleLogin} className="space-y-4">
                  <div>
                    <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">
                      Email
                    </label>
                    <input
                      type="email"
                      id="email"
                      value={loginEmail}
                      onChange={(e) => setLoginEmail(e.target.value)}
                      required
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-900"
                    />
                  </div>
                  <div>
                    <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">
                      Password
                    </label>
                    <input
                      type="password"
                      id="password"
                      value={loginPassword}
                      onChange={(e) => setLoginPassword(e.target.value)}
                      required
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-900"
                    />
                  </div>
                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full bg-gray-900 text-white py-3 rounded-md hover:bg-gray-800 disabled:bg-gray-400"
                  >
                    {loading ? 'Logging in...' : 'Login'}
                  </button>
                </form>
              )}

              {activeTab === 'register' && showRegister && (
                <form onSubmit={handleRegister} className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label htmlFor="firstName" className="block text-sm font-medium text-gray-700 mb-1">
                        First Name
                      </label>
                      <input
                        type="text"
                        id="firstName"
                        value={registerData.firstName}
                        onChange={(e) => setRegisterData({ ...registerData, firstName: e.target.value })}
                        required
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-900"
                      />
                    </div>
                    <div>
                      <label htmlFor="lastName" className="block text-sm font-medium text-gray-700 mb-1">
                        Last Name
                      </label>
                      <input
                        type="text"
                        id="lastName"
                        value={registerData.lastName}
                        onChange={(e) => setRegisterData({ ...registerData, lastName: e.target.value })}
                        required
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-900"
                      />
                    </div>
                  </div>
                  <div>
                    <label htmlFor="registerEmail" className="block text-sm font-medium text-gray-700 mb-1">
                      Email
                    </label>
                    <input
                      type="email"
                      id="registerEmail"
                      value={registerData.email}
                      onChange={(e) => setRegisterData({ ...registerData, email: e.target.value })}
                      required
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-900"
                    />
                  </div>
                  <div>
                    <label htmlFor="phone" className="block text-sm font-medium text-gray-700 mb-1">
                      Phone (optional)
                    </label>
                    <input
                      type="tel"
                      id="phone"
                      value={registerData.phone}
                      onChange={(e) => setRegisterData({ ...registerData, phone: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-900"
                    />
                  </div>
                  <div>
                    <label htmlFor="registerPassword" className="block text-sm font-medium text-gray-700 mb-1">
                      Password
                    </label>
                    <input
                      type="password"
                      id="registerPassword"
                      value={registerData.password}
                      onChange={(e) => setRegisterData({ ...registerData, password: e.target.value })}
                      required
                      minLength={6}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-900"
                    />
                  </div>
                  <div>
                    <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700 mb-1">
                      Confirm Password
                    </label>
                    <input
                      type="password"
                      id="confirmPassword"
                      value={registerData.confirmPassword}
                      onChange={(e) => setRegisterData({ ...registerData, confirmPassword: e.target.value })}
                      required
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-900"
                    />
                  </div>
                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full bg-gray-900 text-white py-3 rounded-md hover:bg-gray-800 disabled:bg-gray-400"
                  >
                    {loading ? 'Creating Account...' : 'Create Account'}
                  </button>
                </form>
              )}

              {settings?.checkoutMode === 'GUEST_ONLY' && (
                <div className="text-center py-8">
                  <p className="text-gray-600 mb-4">
                    This store uses guest checkout only. No account needed!
                  </p>
                  <Link
                    href={`/store/${slug}/products`}
                    className="inline-block bg-gray-900 text-white px-6 py-2 rounded-md hover:bg-gray-800"
                  >
                    Continue Shopping
                  </Link>
                </div>
              )}
            </div>
          </div>
        )}

        <div className="text-center mt-6">
          <Link href={`/store/${slug}`} className="text-gray-600 hover:text-gray-900">
            ← Back to Store
          </Link>
        </div>
      </div>
    </div>
  );
}
