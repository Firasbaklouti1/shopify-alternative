import React from 'react';
import Link from 'next/link';
import type { SectionProps } from '../LayoutRenderer';

interface ImageWithTextSettings {
  image_url?: string;
  image_position?: 'left' | 'right';
  image_width?: 'small' | 'medium' | 'large';
  title?: string;
  subtitle?: string;
  content?: string;
  cta_text?: string;
  cta_link?: string;
  background_color?: string;
  text_color?: string;
  vertical_alignment?: 'top' | 'center' | 'bottom';
}

const IMAGE_WIDTH_CLASSES = {
  small: 'md:w-1/3',
  medium: 'md:w-1/2',
  large: 'md:w-2/3',
};

const VERTICAL_ALIGN_CLASSES = {
  top: 'items-start',
  center: 'items-center',
  bottom: 'items-end',
};

export default function ImageWithText({ section }: SectionProps) {
  const settings = section.settings as ImageWithTextSettings;

  const {
    image_url,
    image_position = 'left',
    image_width = 'medium',
    title,
    subtitle,
    content,
    cta_text,
    cta_link,
    background_color,
    text_color,
    vertical_alignment = 'center',
  } = settings;

  const style: React.CSSProperties = {};
  if (background_color) style.backgroundColor = background_color;
  if (text_color) style.color = text_color;

  const imageSection = (
    <div className={`${IMAGE_WIDTH_CLASSES[image_width]} flex-shrink-0`}>
      {image_url ? (
        <img
          src={image_url}
          alt={title || ''}
          className="w-full h-auto rounded-lg"
        />
      ) : (
        <div className="aspect-[4/3] bg-gray-200 rounded-lg flex items-center justify-center text-gray-400">
          <svg className="w-16 h-16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
        </div>
      )}
    </div>
  );

  const textSection = (
    <div className="flex-1 space-y-4">
      {subtitle && (
        <p className="text-sm uppercase tracking-wider text-gray-500 font-medium">
          {subtitle}
        </p>
      )}

      {title && (
        <h2 className="text-3xl md:text-4xl font-bold">
          {title}
        </h2>
      )}

      {content && (
        <div
          className="prose prose-lg"
          dangerouslySetInnerHTML={{ __html: content }}
        />
      )}

      {cta_text && cta_link && (
        <Link
          href={cta_link}
          className="inline-block mt-4 px-6 py-3 bg-gray-900 text-white font-semibold rounded-md hover:bg-gray-800 transition-colors"
        >
          {cta_text}
        </Link>
      )}
    </div>
  );

  return (
    <section
      className="py-12 px-6 md:px-12"
      style={style}
    >
      <div className={`flex flex-col md:flex-row gap-8 md:gap-12 ${VERTICAL_ALIGN_CLASSES[vertical_alignment]}`}>
        {image_position === 'left' ? (
          <>
            {imageSection}
            {textSection}
          </>
        ) : (
          <>
            {textSection}
            {imageSection}
          </>
        )}
      </div>
    </section>
  );
}
