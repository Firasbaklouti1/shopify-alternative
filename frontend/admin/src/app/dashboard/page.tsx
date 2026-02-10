'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/auth';

interface LayoutInfo {
  id: number;
  pageType: string;
  handle: string | null;
  name: string;
  published: boolean;
  hasDraft: boolean;
  version: number;
  updatedAt: string;
}

const PAGE_TYPES = ['HOME', 'PRODUCT', 'COLLECTION', 'CART', 'CHECKOUT'];

export default function DashboardPage() {
  const router = useRouter();
  const { token, storeSlug, isAuthenticated, logout, apiUrl } = useAuth();
  const [layouts, setLayouts] = useState<LayoutInfo[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!isAuthenticated) {
      router.replace('/login');
      return;
    }

    async function loadLayouts() {
      try {
        const res = await fetch(`${apiUrl}/api/v1/stores/layouts`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        if (!res.ok) throw new Error(`Failed to load layouts: ${res.status}`);
        const data = await res.json();
        setLayouts(data);
      } catch (err: unknown) {
        setError(err instanceof Error ? err.message : 'Failed to load layouts');
      } finally {
        setLoading(false);
      }
    }

    loadLayouts();
  }, [isAuthenticated, token, apiUrl, router]);

  if (!isAuthenticated) return null;

  const standardLayouts = layouts.filter((l) => l.pageType !== 'CUSTOM');
  const customPages = layouts.filter((l) => l.pageType === 'CUSTOM');

  function getLayoutForType(type: string): LayoutInfo | undefined {
    return standardLayouts.find((l) => l.pageType === type);
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-6xl mx-auto px-6 py-4 flex justify-between items-center">
          <div>
            <h1 className="text-xl font-bold text-gray-900">Store Admin</h1>
            <p className="text-sm text-gray-500">Store: {storeSlug}</p>
          </div>
          <button
            onClick={logout}
            className="text-sm text-gray-600 hover:text-gray-900"
          >
            Sign Out
          </button>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-6 py-8">
        {error && (
          <div className="mb-6 p-4 bg-red-50 border border-red-200 text-red-700 rounded">
            {error}
          </div>
        )}

        <section className="mb-10">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Page Layouts</h2>
          {loading ? (
            <div className="text-gray-500">Loading layouts...</div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {PAGE_TYPES.map((type) => {
                const layout = getLayoutForType(type);
                return (
                  <div
                    key={type}
                    className="bg-white rounded-lg border p-5 hover:shadow-md transition-shadow"
                  >
                    <div className="flex justify-between items-start mb-3">
                      <div>
                        <h3 className="font-medium text-gray-900">
                          {type.charAt(0) + type.slice(1).toLowerCase()} Page
                        </h3>
                        {layout && (
                          <p className="text-xs text-gray-500 mt-1">
                            v{layout.version}
                            {layout.hasDraft && (
                              <span className="ml-2 text-amber-600">Has draft</span>
                            )}
                          </p>
                        )}
                      </div>
                      <span
                        className={`text-xs px-2 py-1 rounded ${
                          layout?.published
                            ? 'bg-green-100 text-green-700'
                            : 'bg-gray-100 text-gray-600'
                        }`}
                      >
                        {layout?.published ? 'Published' : 'Draft'}
                      </span>
                    </div>
                    <button
                      onClick={() =>
                        router.push(`/editor/${storeSlug}/${type.toLowerCase()}`)
                      }
                      className="w-full py-2 px-4 bg-blue-600 text-white text-sm font-medium rounded hover:bg-blue-700 transition-colors"
                    >
                      Edit Layout
                    </button>
                  </div>
                );
              })}
            </div>
          )}
        </section>

        <section>
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-semibold text-gray-900">Custom Pages</h2>
          </div>
          {customPages.length === 0 ? (
            <p className="text-gray-500 text-sm">No custom pages yet.</p>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {customPages.map((page) => (
                <div
                  key={page.handle}
                  className="bg-white rounded-lg border p-5 hover:shadow-md transition-shadow"
                >
                  <h3 className="font-medium text-gray-900 mb-1">{page.name}</h3>
                  <p className="text-xs text-gray-500 mb-3">/{page.handle}</p>
                  <button
                    onClick={() =>
                      router.push(`/editor/${storeSlug}/custom?handle=${page.handle}`)
                    }
                    className="w-full py-2 px-4 bg-blue-600 text-white text-sm font-medium rounded hover:bg-blue-700 transition-colors"
                  >
                    Edit Page
                  </button>
                </div>
              ))}
            </div>
          )}
        </section>

        <section className="mt-10">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Quick Links</h2>
          <div className="flex gap-3">
            <a
              href={`http://localhost:3000/store/${storeSlug}`}
              target="_blank"
              rel="noopener noreferrer"
              className="px-4 py-2 bg-gray-100 text-gray-700 text-sm rounded hover:bg-gray-200 transition-colors"
            >
              View Storefront
            </a>
          </div>
        </section>
      </main>
    </div>
  );
}
