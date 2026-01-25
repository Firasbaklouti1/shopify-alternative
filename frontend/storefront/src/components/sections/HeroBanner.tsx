import React from 'react';
import Link from 'next/link';
import type { SectionProps } from '../LayoutRenderer';

interface HeroBannerSettings {
  title?: string;
  subtitle?: string;
  bg_image?: string;
  bg_video?: string;
  cta_text?: string;
  cta_link?: string;
  secondary_cta_text?: string;
  secondary_cta_link?: string;
  overlay_opacity?: number;
  text_color?: 'light' | 'dark';
  text_alignment?: 'left' | 'center' | 'right';
  height?: 'small' | 'medium' | 'large' | 'full';
}

const HEIGHT_CLASSES = {
  small: 'min-h-[300px] md:min-h-[400px]',
  medium: 'min-h-[400px] md:min-h-[500px]',
  large: 'min-h-[500px] md:min-h-[600px]',
  full: 'min-h-screen',
};

const ALIGNMENT_CLASSES = {
  left: 'items-start text-left',
  center: 'items-center text-center',
  right: 'items-end text-right',
};

export default function HeroBanner({ section }: SectionProps) {
  const settings = section.settings as HeroBannerSettings;

  const {
    title = 'Welcome to Our Store',
    subtitle,
    bg_image,
    bg_video,
    cta_text = 'Shop Now',
    cta_link = '/products',
    secondary_cta_text,
    secondary_cta_link,
    overlay_opacity = 0.4,
    text_color = 'light',
    text_alignment = 'center',
    height = 'large',
  } = settings;

  const textColorClasses = text_color === 'light'
    ? 'text-white'
    : 'text-gray-900';

  return (
    <div
      className={`relative ${HEIGHT_CLASSES[height]} flex flex-col justify-center ${ALIGNMENT_CLASSES[text_alignment]} px-6 md:px-12 lg:px-24`}
    >
      {/* Background Image/Video */}
      {bg_video ? (
        <video
          autoPlay
          muted
          loop
          playsInline
          className="absolute inset-0 w-full h-full object-cover"
        >
          <source src={bg_video} type="video/mp4" />
        </video>
      ) : bg_image ? (
        <div
          className="absolute inset-0 bg-cover bg-center"
          style={{ backgroundImage: `url(${bg_image})` }}
        />
      ) : (
        <div className="absolute inset-0 bg-gradient-to-br from-gray-800 to-gray-900" />
      )}

      {/* Overlay */}
      <div
        className="absolute inset-0 bg-black"
        style={{ opacity: overlay_opacity }}
      />

      {/* Content */}
      <div className={`relative z-10 max-w-4xl ${text_alignment === 'center' ? 'mx-auto' : ''}`}>
        <h1 className={`text-4xl md:text-5xl lg:text-6xl font-bold mb-4 ${textColorClasses}`}>
          {title}
        </h1>

        {subtitle && (
          <p className={`text-lg md:text-xl lg:text-2xl mb-8 opacity-90 ${textColorClasses}`}>
            {subtitle}
          </p>
        )}

        <div className={`flex gap-4 ${text_alignment === 'center' ? 'justify-center' : ''} flex-wrap`}>
          {cta_text && (
            <Link
              href={cta_link}
              className="inline-block px-8 py-3 bg-white text-gray-900 font-semibold rounded-md hover:bg-gray-100 transition-colors"
            >
              {cta_text}
            </Link>
          )}

          {secondary_cta_text && secondary_cta_link && (
            <Link
              href={secondary_cta_link}
              className={`inline-block px-8 py-3 border-2 border-white font-semibold rounded-md hover:bg-white hover:text-gray-900 transition-colors ${textColorClasses}`}
            >
              {secondary_cta_text}
            </Link>
          )}
        </div>
      </div>
    </div>
  );
}
