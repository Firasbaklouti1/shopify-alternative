'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { useParams, useRouter } from 'next/navigation';
import {
  getCart,
  getStoreSettings,
  clearCart,
  CartItem,
  StoreSettings
} from '@/lib/api';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

function formatPrice(price: number): string {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(price);
}

type CheckoutStep = 'information' | 'shipping' | 'payment';

interface CustomerInfo {
  email: string;
  firstName: string;
  lastName: string;
  address: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
  phone: string;
}

interface OrderResult {
  id: number;
  orderNumber: string;
  customerEmail: string;
  status: string;
  totalPrice: number;
}

export default function CheckoutPage() {
  const params = useParams();
  const router = useRouter();
  const slug = params.slug as string;

  const [cart, setCart] = useState<CartItem[]>([]);
  const [settings, setSettings] = useState<StoreSettings | null>(null);
  const [step, setStep] = useState<CheckoutStep>('information');
  const [isGuest, setIsGuest] = useState(true);
  const [mounted, setMounted] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
  const [orderComplete, setOrderComplete] = useState(false);
  const [orderResult, setOrderResult] = useState<OrderResult | null>(null);
  const [error, setError] = useState<string | null>(null);

  // Check if user is logged in
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [customerToken, setCustomerToken] = useState<string | null>(null);
  const [customerEmail, setCustomerEmail] = useState<string | null>(null);

  const [customerInfo, setCustomerInfo] = useState<CustomerInfo>({
    email: '',
    firstName: '',
    lastName: '',
    address: '',
    city: '',
    state: '',
    zipCode: '',
    country: 'US',
    phone: '',
  });

  useEffect(() => {
    setMounted(true);
    setCart(getCart());

    // Check if user is logged in
    const token = localStorage.getItem(`${slug}_customer_token`);
    const email = localStorage.getItem(`${slug}_customer_email`);
    if (token && email) {
      setIsLoggedIn(true);
      setCustomerToken(token);
      setCustomerEmail(email);
      setIsGuest(false);
      setCustomerInfo(prev => ({ ...prev, email }));
    }

    // Fetch store settings for checkout mode
    getStoreSettings(slug).then(setSettings).catch(console.error);
  }, [slug]);

  const subtotal = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);
  const shipping = 9.99;
  const total = subtotal + shipping;

  const handleInfoChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setCustomerInfo(prev => ({
      ...prev,
      [e.target.name]: e.target.value,
    }));
  };

  const processOrder = async () => {
    setIsProcessing(true);
    setError(null);

    try {
      if (isLoggedIn && customerToken) {
        // Authenticated checkout - first add items to server cart, then checkout
        // For logged-in users, we need to sync cart with server then call checkout
        for (const item of cart) {
          await fetch(`${API_BASE_URL}/api/v1/orders/cart/add`, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              'Authorization': `Bearer ${customerToken}`,
            },
            body: JSON.stringify({
              variantId: item.variantId,
              quantity: item.quantity,
            }),
          });
        }

        // Now call checkout
        const res = await fetch(`${API_BASE_URL}/api/v1/orders/checkout`, {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${customerToken}`,
          },
        });

        if (!res.ok) {
          const data = await res.json();
          throw new Error(data.message || 'Checkout failed');
        }

        const order = await res.json();
        setOrderResult(order);
      } else {
        // Guest checkout - call public storefront API
        const res = await fetch(`${API_BASE_URL}/api/v1/storefront/${slug}/checkout`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            email: customerInfo.email,
            firstName: customerInfo.firstName,
            lastName: customerInfo.lastName,
            phone: customerInfo.phone || undefined,
            address: customerInfo.address,
            city: customerInfo.city,
            state: customerInfo.state,
            zipCode: customerInfo.zipCode,
            country: customerInfo.country,
            items: cart.map(item => ({
              productId: item.productId,
              variantId: item.variantId,
              quantity: item.quantity,
            })),
          }),
        });

        if (!res.ok) {
          const data = await res.json();
          throw new Error(data.message || 'Checkout failed');
        }

        const order = await res.json();
        setOrderResult(order);
      }

      // Clear cart and show success
      clearCart();
      window.dispatchEvent(new CustomEvent('cart-updated'));
      setOrderComplete(true);
    } catch (err: any) {
      setError(err.message || 'Something went wrong during checkout');
    } finally {
      setIsProcessing(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (step === 'information') {
      setStep('shipping');
    } else if (step === 'shipping') {
      setStep('payment');
    } else {
      // Process payment and create order
      await processOrder();
    }
  };

  if (!mounted || !settings) {
    return (
      <div className="max-w-6xl mx-auto px-6 py-12">
        <div className="animate-pulse">
          <div className="h-8 bg-gray-200 rounded w-48 mb-8" />
          <div className="grid lg:grid-cols-2 gap-12">
            <div className="space-y-4">
              {[1, 2, 3, 4, 5].map((i) => (
                <div key={i} className="h-12 bg-gray-200 rounded" />
              ))}
            </div>
            <div className="h-64 bg-gray-200 rounded" />
          </div>
        </div>
      </div>
    );
  }

  if (cart.length === 0 && !orderComplete) {
    return (
      <div className="max-w-4xl mx-auto px-6 py-12 text-center">
        <h1 className="text-2xl font-bold text-gray-900 mb-2">Your cart is empty</h1>
        <p className="text-gray-600 mb-8">Add some products before checking out.</p>
        <Link
          href={`/store/${slug}/products`}
          className="inline-block px-8 py-3 bg-gray-900 text-white font-semibold rounded-md hover:bg-gray-800 transition-colors"
        >
          Continue Shopping
        </Link>
      </div>
    );
  }

  if (orderComplete) {
    return (
      <div className="max-w-2xl mx-auto px-6 py-12 text-center">
        <div className="mb-6">
          <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto">
            <svg className="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            </svg>
          </div>
        </div>
        <h1 className="text-3xl font-bold text-gray-900 mb-2">Order Confirmed!</h1>
        {orderResult && (
          <div className="bg-gray-50 rounded-lg p-4 mb-6 inline-block text-left">
            <p className="text-sm text-gray-600">Order Number</p>
            <p className="font-bold text-lg">{orderResult.orderNumber}</p>
            <p className="text-sm text-gray-600 mt-2">Total</p>
            <p className="font-semibold">{formatPrice(orderResult.totalPrice)}</p>
          </div>
        )}
        <p className="text-gray-600 mb-8">
          Thank you for your order. We'll send you a confirmation email to {orderResult?.customerEmail || customerInfo.email}.
        </p>
        <div className="flex gap-4 justify-center">
          <Link
            href={`/store/${slug}`}
            className="inline-block px-8 py-3 bg-gray-900 text-white font-semibold rounded-md hover:bg-gray-800 transition-colors"
          >
            Continue Shopping
          </Link>
          {isLoggedIn && (
            <Link
              href={`/store/${slug}/account`}
              className="inline-block px-8 py-3 border border-gray-300 font-semibold rounded-md hover:bg-gray-50 transition-colors"
            >
              View Orders
            </Link>
          )}
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto px-6 py-12">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">Checkout</h1>

      {/* Checkout Mode Notice */}
      {settings.checkoutMode === 'ACCOUNT_ONLY' && !isLoggedIn && (
        <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-8">
          <p className="text-yellow-800">
            This store requires an account to complete checkout.{' '}
            <Link href={`/store/${slug}/account`} className="underline font-medium">
              Log in or create an account
            </Link>
          </p>
        </div>
      )}

      {/* Show logged-in status */}
      {isLoggedIn && customerEmail && (
        <div className="bg-green-50 border border-green-200 rounded-lg p-4 mb-8">
          <p className="text-green-800">
            ✓ Logged in as <strong>{customerEmail}</strong>
          </p>
        </div>
      )}

      {/* Guest/Account Toggle (only for BOTH mode and not logged in) */}
      {settings.checkoutMode === 'BOTH' && !isLoggedIn && (
        <div className="flex gap-4 mb-8">
          <button
            onClick={() => setIsGuest(true)}
            className={`px-4 py-2 rounded-md font-medium transition-colors ${
              isGuest 
                ? 'bg-gray-900 text-white' 
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            }`}
          >
            Checkout as Guest
          </button>
          <Link
            href={`/store/${slug}/account`}
            className="px-4 py-2 rounded-md font-medium transition-colors bg-gray-100 text-gray-700 hover:bg-gray-200"
          >
            Log In
          </Link>
        </div>
      )}

      {/* Error Display */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-8">
          <p className="text-red-800">{error}</p>
        </div>
      )}

      {/* Progress Steps */}
      <div className="flex items-center gap-4 mb-8">
        {(['information', 'shipping', 'payment'] as CheckoutStep[]).map((s, index) => (
          <React.Fragment key={s}>
            <button
              onClick={() => {
                if (index < ['information', 'shipping', 'payment'].indexOf(step)) {
                  setStep(s);
                }
              }}
              className={`flex items-center gap-2 ${
                step === s
                  ? 'text-gray-900 font-semibold'
                  : index < ['information', 'shipping', 'payment'].indexOf(step)
                  ? 'text-gray-600 cursor-pointer hover:text-gray-900'
                  : 'text-gray-400'
              }`}
            >
              <span className={`w-6 h-6 rounded-full flex items-center justify-center text-sm ${
                step === s
                  ? 'bg-gray-900 text-white'
                  : index < ['information', 'shipping', 'payment'].indexOf(step)
                  ? 'bg-green-600 text-white'
                  : 'bg-gray-200'
              }`}>
                {index < ['information', 'shipping', 'payment'].indexOf(step) ? '✓' : index + 1}
              </span>
              <span className="hidden sm:inline capitalize">{s}</span>
            </button>
            {index < 2 && (
              <div className="flex-1 h-px bg-gray-200" />
            )}
          </React.Fragment>
        ))}
      </div>

      <div className="grid lg:grid-cols-2 gap-12">
        {/* Form */}
        <form onSubmit={handleSubmit}>
          {step === 'information' && (
            <div className="space-y-6">
              <h2 className="text-xl font-semibold mb-4">Contact Information</h2>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Email
                </label>
                <input
                  type="email"
                  name="email"
                  value={customerInfo.email}
                  onChange={handleInfoChange}
                  required
                  className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-gray-900 focus:border-transparent"
                />
              </div>

              <h2 className="text-xl font-semibold pt-4">Shipping Address</h2>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    First Name
                  </label>
                  <input
                    type="text"
                    name="firstName"
                    value={customerInfo.firstName}
                    onChange={handleInfoChange}
                    required
                    className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-gray-900 focus:border-transparent"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Last Name
                  </label>
                  <input
                    type="text"
                    name="lastName"
                    value={customerInfo.lastName}
                    onChange={handleInfoChange}
                    required
                    className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-gray-900 focus:border-transparent"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Address
                </label>
                <input
                  type="text"
                  name="address"
                  value={customerInfo.address}
                  onChange={handleInfoChange}
                  required
                  className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-gray-900 focus:border-transparent"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    City
                  </label>
                  <input
                    type="text"
                    name="city"
                    value={customerInfo.city}
                    onChange={handleInfoChange}
                    required
                    className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-gray-900 focus:border-transparent"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    State
                  </label>
                  <input
                    type="text"
                    name="state"
                    value={customerInfo.state}
                    onChange={handleInfoChange}
                    required
                    className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-gray-900 focus:border-transparent"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    ZIP Code
                  </label>
                  <input
                    type="text"
                    name="zipCode"
                    value={customerInfo.zipCode}
                    onChange={handleInfoChange}
                    required
                    className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-gray-900 focus:border-transparent"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Phone
                  </label>
                  <input
                    type="tel"
                    name="phone"
                    value={customerInfo.phone}
                    onChange={handleInfoChange}
                    className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-gray-900 focus:border-transparent"
                  />
                </div>
              </div>
            </div>
          )}

          {step === 'shipping' && (
            <div className="space-y-6">
              <h2 className="text-xl font-semibold mb-4">Shipping Method</h2>

              <div className="space-y-3">
                <label className="flex items-center justify-between p-4 border border-gray-300 rounded-lg cursor-pointer hover:border-gray-400">
                  <div className="flex items-center gap-3">
                    <input
                      type="radio"
                      name="shipping"
                      value="standard"
                      defaultChecked
                      className="w-4 h-4"
                    />
                    <div>
                      <p className="font-medium">Standard Shipping</p>
                      <p className="text-sm text-gray-500">5-7 business days</p>
                    </div>
                  </div>
                  <span className="font-semibold">$9.99</span>
                </label>

                <label className="flex items-center justify-between p-4 border border-gray-300 rounded-lg cursor-pointer hover:border-gray-400">
                  <div className="flex items-center gap-3">
                    <input
                      type="radio"
                      name="shipping"
                      value="express"
                      className="w-4 h-4"
                    />
                    <div>
                      <p className="font-medium">Express Shipping</p>
                      <p className="text-sm text-gray-500">2-3 business days</p>
                    </div>
                  </div>
                  <span className="font-semibold">$19.99</span>
                </label>
              </div>
            </div>
          )}

          {step === 'payment' && (
            <div className="space-y-6">
              <h2 className="text-xl font-semibold mb-4">Payment</h2>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Card Number
                </label>
                <input
                  type="text"
                  placeholder="1234 5678 9012 3456"
                  className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-gray-900 focus:border-transparent"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Expiry Date
                  </label>
                  <input
                    type="text"
                    placeholder="MM/YY"
                    className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-gray-900 focus:border-transparent"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    CVV
                  </label>
                  <input
                    type="text"
                    placeholder="123"
                    className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-gray-900 focus:border-transparent"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Name on Card
                </label>
                <input
                  type="text"
                  className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-gray-900 focus:border-transparent"
                />
              </div>
            </div>
          )}

          <div className="mt-8 flex gap-4">
            {step !== 'information' && (
              <button
                type="button"
                onClick={() => {
                  if (step === 'shipping') setStep('information');
                  if (step === 'payment') setStep('shipping');
                }}
                className="px-6 py-3 border border-gray-300 rounded-md font-medium hover:bg-gray-50"
              >
                Back
              </button>
            )}
            <button
              type="submit"
              disabled={isProcessing}
              className="flex-1 px-6 py-3 bg-gray-900 text-white font-semibold rounded-md hover:bg-gray-800 disabled:bg-gray-400 transition-colors"
            >
              {isProcessing ? (
                <span className="flex items-center justify-center gap-2">
                  <svg className="animate-spin h-5 w-5" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                  </svg>
                  Processing...
                </span>
              ) : step === 'payment' ? (
                `Pay ${formatPrice(total)}`
              ) : (
                'Continue'
              )}
            </button>
          </div>
        </form>

        {/* Order Summary */}
        <div className="lg:order-last">
          <div className="bg-gray-50 rounded-lg p-6 sticky top-24">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Order Summary</h2>

            <div className="space-y-4 mb-6 max-h-64 overflow-y-auto">
              {cart.map((item) => (
                <div key={`${item.productId}-${item.variantId}`} className="flex gap-3">
                  <div className="w-16 h-16 bg-gray-200 rounded flex-shrink-0 overflow-hidden relative">
                    {item.imageUrl ? (
                      <img src={item.imageUrl} alt={item.name} className="w-full h-full object-cover" />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center text-gray-400">
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                      </div>
                    )}
                    <span className="absolute -top-1 -right-1 bg-gray-500 text-white text-xs w-5 h-5 rounded-full flex items-center justify-center">
                      {item.quantity}
                    </span>
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="font-medium text-sm truncate">{item.name}</p>
                    <p className="text-xs text-gray-500">{item.variantName}</p>
                  </div>
                  <p className="font-medium text-sm">{formatPrice(item.price * item.quantity)}</p>
                </div>
              ))}
            </div>

            <div className="border-t border-gray-200 pt-4 space-y-2">
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">Subtotal</span>
                <span className="font-medium">{formatPrice(subtotal)}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">Shipping</span>
                <span className="font-medium">{formatPrice(shipping)}</span>
              </div>
              <div className="border-t border-gray-200 pt-2 mt-2">
                <div className="flex justify-between">
                  <span className="font-semibold">Total</span>
                  <span className="font-bold text-lg">{formatPrice(total)}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
