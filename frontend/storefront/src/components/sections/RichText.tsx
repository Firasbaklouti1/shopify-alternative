import React from 'react';
import type { SectionProps } from '../LayoutRenderer';

interface RichTextSettings {
  title?: string;
  content?: string;
  text_alignment?: 'left' | 'center' | 'right';
  max_width?: 'small' | 'medium' | 'large' | 'full';
  padding?: 'small' | 'medium' | 'large';
  background_color?: string;
  text_color?: string;
}

const WIDTH_CLASSES = {
  small: 'max-w-2xl',
  medium: 'max-w-4xl',
  large: 'max-w-6xl',
  full: 'max-w-none',
};

const PADDING_CLASSES = {
  small: 'py-8',
  medium: 'py-12',
  large: 'py-20',
};

const ALIGNMENT_CLASSES = {
  left: 'text-left',
  center: 'text-center mx-auto',
  right: 'text-right ml-auto',
};

export default function RichText({ section }: SectionProps) {
  const settings = section.settings as RichTextSettings;

  const {
    title,
    content,
    text_alignment = 'center',
    max_width = 'medium',
    padding = 'medium',
    background_color,
    text_color,
  } = settings;

  const style: React.CSSProperties = {};
  if (background_color) style.backgroundColor = background_color;
  if (text_color) style.color = text_color;

  return (
    <section
      className={`${PADDING_CLASSES[padding]} px-6 md:px-12`}
      style={style}
    >
      <div className={`${WIDTH_CLASSES[max_width]} ${ALIGNMENT_CLASSES[text_alignment]}`}>
        {title && (
          <h2 className="text-3xl md:text-4xl font-bold mb-6">
            {title}
          </h2>
        )}

        {content && (
          <div
            className="prose prose-lg max-w-none"
            dangerouslySetInnerHTML={{ __html: content }}
          />
        )}
      </div>
    </section>
  );
}
