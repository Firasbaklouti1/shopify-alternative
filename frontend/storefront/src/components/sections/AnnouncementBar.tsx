'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import type { SectionProps } from '../LayoutRenderer';

interface AnnouncementBarSettings {
  text?: string;
  link?: string;
  link_text?: string;
  background_color?: string;
  text_color?: string;
  dismissible?: boolean;
}

export default function AnnouncementBar({ section }: SectionProps) {
  const settings = section.settings as AnnouncementBarSettings;
  const [dismissed, setDismissed] = useState(false);
  const [mounted, setMounted] = useState(false);

  const {
    text = 'Free shipping on orders over $50!',
    link,
    link_text,
    background_color = '#000000',
    text_color = '#ffffff',
    dismissible = true,
  } = settings;

  useEffect(() => {
    setMounted(true);
    // Check if previously dismissed (within this session)
    const wasDismissed = sessionStorage.getItem('announcement-dismissed');
    if (wasDismissed) {
      setDismissed(true);
    }
  }, []);

  const handleDismiss = () => {
    setDismissed(true);
    sessionStorage.setItem('announcement-dismissed', 'true');
  };

  // Don't render during SSR to avoid hydration mismatch
  if (!mounted || dismissed) {
    return null;
  }

  return (
    <div
      className="relative py-2 px-6 text-center text-sm font-medium"
      style={{ backgroundColor: background_color, color: text_color }}
    >
      <p className="inline">
        {text}
        {link && link_text && (
          <>
            {' '}
            <Link
              href={link}
              className="underline hover:no-underline font-semibold"
              style={{ color: text_color }}
            >
              {link_text}
            </Link>
          </>
        )}
      </p>

      {dismissible && (
        <button
          onClick={handleDismiss}
          className="absolute right-4 top-1/2 -translate-y-1/2 p-1 hover:opacity-70 transition-opacity"
          aria-label="Dismiss announcement"
          style={{ color: text_color }}
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      )}
    </div>
  );
}
