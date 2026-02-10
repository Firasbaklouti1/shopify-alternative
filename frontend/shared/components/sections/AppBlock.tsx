'use client';

import React, { useEffect, useRef, useState } from 'react';

interface AppBlockProps {
  app_id?: string;
  script_url?: string;
  tag_name?: string;
  puck?: { isEditing: boolean; metadata: Record<string, unknown> };
}

export default function AppBlock({
  app_id,
  script_url,
  tag_name,
  puck,
}: AppBlockProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const [loaded, setLoaded] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!script_url || !tag_name) {
      setError('Missing script_url or tag_name');
      return;
    }

    if (customElements.get(tag_name)) {
      setLoaded(true);
      return;
    }

    const existingScript = document.querySelector(`script[src="${script_url}"]`);
    if (existingScript) {
      customElements.whenDefined(tag_name)
        .then(() => setLoaded(true))
        .catch(() => setError(`Failed to define custom element: ${tag_name}`));
      return;
    }

    const script = document.createElement('script');
    script.src = script_url;
    script.async = true;
    script.setAttribute('data-app-id', app_id || '');

    script.onload = () => {
      customElements.whenDefined(tag_name)
        .then(() => setLoaded(true))
        .catch(() => setError(`Custom element "${tag_name}" was not defined by the script`));
    };

    script.onerror = () => {
      setError(`Failed to load script: ${script_url}`);
    };

    document.head.appendChild(script);
  }, [script_url, tag_name, app_id]);

  useEffect(() => {
    if (!loaded || !containerRef.current || !tag_name) return;

    containerRef.current.innerHTML = '';
    const element = document.createElement(tag_name);
    containerRef.current.appendChild(element);

    return () => {
      if (containerRef.current) {
        containerRef.current.innerHTML = '';
      }
    };
  }, [loaded, tag_name]);

  if (error) {
    if (process.env.NODE_ENV === 'development' || puck?.isEditing) {
      return (
        <div className="bg-red-50 border border-red-200 p-4 rounded-lg">
          <p className="text-red-700 text-sm">
            <strong>App Block Error:</strong> {error}
          </p>
          <p className="text-red-600 text-xs mt-1">
            App ID: {app_id || 'N/A'}
          </p>
        </div>
      );
    }
    return null;
  }

  if (!loaded) {
    return (
      <div className="animate-pulse bg-gray-100 h-16 flex items-center justify-center rounded">
        <span className="text-gray-400 text-sm">Loading app...</span>
      </div>
    );
  }

  return (
    <div
      ref={containerRef}
      data-app-block={app_id}
      className="app-block-container"
    />
  );
}
