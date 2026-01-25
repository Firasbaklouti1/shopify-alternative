'use client';

import React, { useState } from 'react';
import type { SectionProps } from '../LayoutRenderer';

interface NewsletterSettings {
  title?: string;
  subtitle?: string;
  placeholder?: string;
  button_text?: string;
  success_message?: string;
  background_color?: string;
  text_color?: string;
  text_alignment?: 'left' | 'center';
  layout?: 'stacked' | 'inline';
}

export default function Newsletter({ section }: SectionProps) {
  const settings = section.settings as NewsletterSettings;
  const [email, setEmail] = useState('');
  const [status, setStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle');

  const {
    title = 'Join our newsletter',
    subtitle = 'Subscribe to get special offers, free giveaways, and once-in-a-lifetime deals.',
    placeholder = 'Enter your email',
    button_text = 'Subscribe',
    success_message = 'Thanks for subscribing!',
    background_color,
    text_color,
    text_alignment = 'center',
    layout = 'inline',
  } = settings;

  const style: React.CSSProperties = {};
  if (background_color) style.backgroundColor = background_color;
  if (text_color) style.color = text_color;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email) return;

    setStatus('loading');

    // Simulate API call - in production, this would call a real endpoint
    await new Promise((resolve) => setTimeout(resolve, 1000));

    setStatus('success');
    setEmail('');
  };

  return (
    <section
      className={`py-16 px-6 md:px-12 ${!background_color ? 'bg-gray-100' : ''}`}
      style={style}
    >
      <div className={`max-w-2xl ${text_alignment === 'center' ? 'mx-auto text-center' : ''}`}>
        {title && (
          <h2 className="text-3xl md:text-4xl font-bold mb-4">
            {title}
          </h2>
        )}

        {subtitle && (
          <p className={`text-lg mb-8 ${text_color ? '' : 'text-gray-600'}`}>
            {subtitle}
          </p>
        )}

        {status === 'success' ? (
          <div className="bg-green-100 text-green-800 px-6 py-4 rounded-lg font-medium">
            {success_message}
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            <div className={`flex ${layout === 'stacked' ? 'flex-col gap-4' : 'flex-col sm:flex-row gap-3'}`}>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder={placeholder}
                required
                className={`flex-1 px-4 py-3 rounded-md border border-gray-300 focus:ring-2 focus:ring-gray-900 focus:border-transparent ${
                  layout === 'stacked' ? 'w-full' : ''
                }`}
              />
              <button
                type="submit"
                disabled={status === 'loading'}
                className={`px-8 py-3 bg-gray-900 text-white font-semibold rounded-md hover:bg-gray-800 transition-colors disabled:bg-gray-400 ${
                  layout === 'stacked' ? 'w-full' : 'flex-shrink-0'
                }`}
              >
                {status === 'loading' ? (
                  <span className="flex items-center justify-center gap-2">
                    <svg className="animate-spin h-5 w-5" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                    </svg>
                    Subscribing...
                  </span>
                ) : (
                  button_text
                )}
              </button>
            </div>
          </form>
        )}

        {status === 'error' && (
          <p className="mt-4 text-red-600">Something went wrong. Please try again.</p>
        )}
      </div>
    </section>
  );
}
