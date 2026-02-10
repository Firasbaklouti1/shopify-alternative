'use client';

import React, { useEffect, useState, useCallback, useRef } from 'react';
import { Puck, type Data } from '@measured/puck';
import '@measured/puck/puck.css';
import { useParams, useRouter } from 'next/navigation';
import { useAuth } from '@/lib/auth';
import { puckConfig } from '@shared/lib/puck-config';
import { convertLegacyToPuck } from '@shared/lib/puck-utils';

const EMPTY_DATA: Data = {
  content: [],
  root: { props: { title: '' } },
};

export default function EditorPage() {
  const params = useParams();
  const router = useRouter();
  const { token, storeSlug, isAuthenticated, apiUrl } = useAuth();
  const [data, setData] = useState<Data | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const saveTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  const pageType = (params.pageType as string)?.toUpperCase() || 'HOME';
  const slug = params.slug as string;

  useEffect(() => {
    if (!isAuthenticated) {
      router.replace('/login');
      return;
    }

    async function loadDraft() {
      try {
        const res = await fetch(`${apiUrl}/api/v1/stores/layouts/${pageType}/draft`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        if (res.ok) {
          const layoutData = await res.json();
          // Try to parse as Puck format, or convert from legacy { sections, order } format
          const puckData = convertLegacyToPuck(layoutData);
          if (puckData) {
            setData(puckData);
          } else {
            console.warn('Layout data not in recognized format, starting fresh:', layoutData);
            setData(EMPTY_DATA);
          }
        } else if (res.status === 404) {
          // No draft exists, start with empty
          setData(EMPTY_DATA);
        } else {
          throw new Error(`Failed to load draft: ${res.status}`);
        }
      } catch (err: unknown) {
        console.error('Failed to load draft:', err);
        setError(err instanceof Error ? err.message : 'Failed to load draft');
        setData(EMPTY_DATA);
      } finally {
        setLoading(false);
      }
    }

    loadDraft();
  }, [isAuthenticated, token, apiUrl, pageType, router]);

  const saveDraft = useCallback(
    async (puckData: Data) => {
      setSaving(true);
      try {
        const res = await fetch(`${apiUrl}/api/v1/stores/layouts/${pageType}`, {
          method: 'PUT',
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            layoutJson: puckData,
            name: `${pageType} Page`,
          }),
        });

        if (!res.ok) {
          throw new Error(`Failed to save: ${res.status}`);
        }
      } catch (err: unknown) {
        console.error('Failed to save draft:', err);
      } finally {
        setSaving(false);
      }
    },
    [apiUrl, token, pageType]
  );

  const handleChange = useCallback(
    (puckData: Data) => {
      // Debounced auto-save
      if (saveTimeoutRef.current) {
        clearTimeout(saveTimeoutRef.current);
      }
      saveTimeoutRef.current = setTimeout(() => {
        saveDraft(puckData);
      }, 2000);
    },
    [saveDraft]
  );

  const handlePublish = useCallback(
    async (puckData: Data) => {
      // Save first
      await saveDraft(puckData);

      // Then publish
      try {
        const res = await fetch(`${apiUrl}/api/v1/stores/layouts/${pageType}/publish`, {
          method: 'POST',
          headers: { Authorization: `Bearer ${token}` },
        });

        if (!res.ok) {
          throw new Error(`Failed to publish: ${res.status}`);
        }

        alert('Published successfully!');
      } catch (err: unknown) {
        console.error('Failed to publish:', err);
        alert(err instanceof Error ? err.message : 'Failed to publish');
      }
    },
    [apiUrl, token, pageType, saveDraft]
  );

  if (!isAuthenticated) return null;

  if (loading) {
    return (
      <div className="h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="animate-spin h-8 w-8 border-4 border-blue-600 border-t-transparent rounded-full mx-auto mb-4" />
          <p className="text-gray-600">Loading editor...</p>
        </div>
      </div>
    );
  }

  if (error && !data) {
    return (
      <div className="h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <p className="text-red-600 mb-4">{error}</p>
          <button
            onClick={() => router.push('/dashboard')}
            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
          >
            Back to Dashboard
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="h-screen">
      {saving && (
        <div className="fixed top-2 right-2 z-50 bg-blue-100 text-blue-800 px-3 py-1 rounded text-sm">
          Saving...
        </div>
      )}
      <Puck
        config={puckConfig}
        data={data || EMPTY_DATA}
        metadata={{
          storeSlug: slug || storeSlug,
          apiUrl,
        }}
        onChange={handleChange}
        onPublish={handlePublish}
        headerTitle={`${pageType.charAt(0) + pageType.slice(1).toLowerCase()} Page`}
        headerPath={`/store/${slug || storeSlug}`}
        viewports={[
          { width: 360, height: 'auto', icon: 'Smartphone', label: 'Mobile' },
          { width: 768, height: 'auto', icon: 'Tablet', label: 'Tablet' },
          { width: 1280, height: 'auto', icon: 'Monitor', label: 'Desktop' },
        ]}
      />
    </div>
  );
}
