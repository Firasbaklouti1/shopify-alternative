'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import type { StoreSettings, CartItem } from '@/lib/api';
import { getCart } from '@/lib/api';

interface StoreHeaderProps {
  settings: StoreSettings;
  storeSlug: string;
}

export default function StoreHeader({ settings, storeSlug }: StoreHeaderProps) {
  const [cartCount, setCartCount] = useState(0);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  useEffect(() => {
    // Load cart count
    const updateCartCount = () => {
      const cart = getCart();
      setCartCount(cart.reduce((sum: number, item: CartItem) => sum + item.quantity, 0));
    };

    updateCartCount();

    // Listen for cart updates
    window.addEventListener('cart-updated', updateCartCount);
    return () => window.removeEventListener('cart-updated', updateCartCount);
  }, []);

  const basePath = `/store/${storeSlug}`;

  return (
    <header className="bg-white border-b border-gray-200 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link href={basePath} className="flex items-center">
            {settings.globalStyles?.logo ? (
              <img
                src={settings.globalStyles.logo}
                alt={settings.storeName}
                className="h-8 w-auto"
              />
            ) : (
              <span className="text-xl font-bold text-gray-900">
                {settings.storeName}
              </span>
            )}
          </Link>

          {/* Desktop Navigation */}
          <nav className="hidden md:flex items-center gap-8">
            <Link
              href={`${basePath}/products`}
              className="text-sm font-medium text-gray-700 hover:text-gray-900 transition-colors"
            >
              Shop All
            </Link>
            <Link
              href={`${basePath}/collections`}
              className="text-sm font-medium text-gray-700 hover:text-gray-900 transition-colors"
            >
              Collections
            </Link>
          </nav>

          {/* Right side: Search, Account, Cart */}
          <div className="flex items-center gap-4">
            {/* Search */}
            <button
              className="p-2 text-gray-500 hover:text-gray-700 transition-colors"
              aria-label="Search"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </button>

            {/* Account */}
            {settings.checkoutMode !== 'GUEST_ONLY' && (
              <Link
                href={`${basePath}/account`}
                className="p-2 text-gray-500 hover:text-gray-700 transition-colors"
                aria-label="Account"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </Link>
            )}

            {/* Cart */}
            <Link
              href={`${basePath}/cart`}
              className="p-2 text-gray-500 hover:text-gray-700 transition-colors relative"
              aria-label="Cart"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
              </svg>
              {cartCount > 0 && (
                <span className="absolute -top-1 -right-1 bg-gray-900 text-white text-xs w-5 h-5 rounded-full flex items-center justify-center font-medium">
                  {cartCount > 99 ? '99+' : cartCount}
                </span>
              )}
            </Link>

            {/* Mobile menu button */}
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="md:hidden p-2 text-gray-500 hover:text-gray-700 transition-colors"
              aria-label="Menu"
            >
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                {mobileMenuOpen ? (
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                ) : (
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                )}
              </svg>
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Navigation */}
      {mobileMenuOpen && (
        <div className="md:hidden border-t border-gray-200 bg-white">
          <nav className="px-4 py-4 space-y-3">
            <Link
              href={`${basePath}/products`}
              className="block text-base font-medium text-gray-700 hover:text-gray-900"
              onClick={() => setMobileMenuOpen(false)}
            >
              Shop All
            </Link>
            <Link
              href={`${basePath}/collections`}
              className="block text-base font-medium text-gray-700 hover:text-gray-900"
              onClick={() => setMobileMenuOpen(false)}
            >
              Collections
            </Link>
          </nav>
        </div>
      )}
    </header>
  );
}
