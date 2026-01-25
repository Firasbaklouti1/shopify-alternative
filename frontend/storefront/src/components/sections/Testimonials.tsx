'use client';

import React, { useState } from 'react';
import type { SectionProps } from '../LayoutRenderer';

interface Testimonial {
  author: string;
  role?: string;
  content: string;
  avatar?: string;
  rating?: number;
}

interface TestimonialsSettings {
  title?: string;
  subtitle?: string;
  testimonials?: Testimonial[];
  layout?: 'carousel' | 'grid';
  columns?: 2 | 3;
  show_rating?: boolean;
  background_color?: string;
}

const COLUMN_CLASSES = {
  2: 'grid-cols-1 md:grid-cols-2',
  3: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3',
};

function StarRating({ rating }: { rating: number }) {
  return (
    <div className="flex gap-1">
      {[1, 2, 3, 4, 5].map((star) => (
        <svg
          key={star}
          className={`w-5 h-5 ${star <= rating ? 'text-yellow-400' : 'text-gray-300'}`}
          fill="currentColor"
          viewBox="0 0 20 20"
        >
          <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
        </svg>
      ))}
    </div>
  );
}

function TestimonialCard({
  testimonial,
  showRating
}: {
  testimonial: Testimonial;
  showRating: boolean;
}) {
  return (
    <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100">
      {showRating && testimonial.rating && (
        <div className="mb-4">
          <StarRating rating={testimonial.rating} />
        </div>
      )}

      <blockquote className="text-gray-700 mb-4 italic">
        &ldquo;{testimonial.content}&rdquo;
      </blockquote>

      <div className="flex items-center gap-3">
        {testimonial.avatar ? (
          <img
            src={testimonial.avatar}
            alt={testimonial.author}
            className="w-10 h-10 rounded-full object-cover"
          />
        ) : (
          <div className="w-10 h-10 rounded-full bg-gray-200 flex items-center justify-center text-gray-500 font-semibold">
            {testimonial.author.charAt(0).toUpperCase()}
          </div>
        )}

        <div>
          <p className="font-semibold text-gray-900">{testimonial.author}</p>
          {testimonial.role && (
            <p className="text-sm text-gray-500">{testimonial.role}</p>
          )}
        </div>
      </div>
    </div>
  );
}

export default function Testimonials({ section }: SectionProps) {
  const settings = section.settings as TestimonialsSettings;
  const [activeIndex, setActiveIndex] = useState(0);

  const {
    title = 'What Our Customers Say',
    subtitle,
    testimonials = [],
    layout = 'grid',
    columns = 3,
    show_rating = true,
    background_color,
  } = settings;

  // Default testimonials if none provided
  const displayTestimonials = testimonials.length > 0 ? testimonials : [
    {
      author: 'Sarah J.',
      role: 'Verified Buyer',
      content: 'Amazing quality and fast shipping! Will definitely order again.',
      rating: 5,
    },
    {
      author: 'Michael T.',
      role: 'Verified Buyer',
      content: 'Best purchase I\'ve made this year. Highly recommended.',
      rating: 5,
    },
    {
      author: 'Emma R.',
      role: 'Verified Buyer',
      content: 'Great customer service and the product exceeded my expectations.',
      rating: 4,
    },
  ];

  const style: React.CSSProperties = {};
  if (background_color) style.backgroundColor = background_color;

  if (layout === 'carousel') {
    return (
      <section
        className={`py-16 px-6 md:px-12 ${!background_color ? 'bg-gray-50' : ''}`}
        style={style}
      >
        <div className="max-w-4xl mx-auto text-center">
          {title && (
            <h2 className="text-3xl md:text-4xl font-bold text-gray-900 mb-2">
              {title}
            </h2>
          )}
          {subtitle && (
            <p className="text-lg text-gray-600 mb-10">{subtitle}</p>
          )}

          <div className="relative">
            <div className="bg-white p-8 rounded-lg shadow-sm border border-gray-100">
              {show_rating && displayTestimonials[activeIndex].rating && (
                <div className="flex justify-center mb-4">
                  <StarRating rating={displayTestimonials[activeIndex].rating!} />
                </div>
              )}

              <blockquote className="text-xl text-gray-700 mb-6 italic">
                &ldquo;{displayTestimonials[activeIndex].content}&rdquo;
              </blockquote>

              <div className="flex items-center justify-center gap-3">
                {displayTestimonials[activeIndex].avatar ? (
                  <img
                    src={displayTestimonials[activeIndex].avatar}
                    alt={displayTestimonials[activeIndex].author}
                    className="w-12 h-12 rounded-full object-cover"
                  />
                ) : (
                  <div className="w-12 h-12 rounded-full bg-gray-200 flex items-center justify-center text-gray-500 font-semibold">
                    {displayTestimonials[activeIndex].author.charAt(0).toUpperCase()}
                  </div>
                )}

                <div className="text-left">
                  <p className="font-semibold text-gray-900">
                    {displayTestimonials[activeIndex].author}
                  </p>
                  {displayTestimonials[activeIndex].role && (
                    <p className="text-sm text-gray-500">
                      {displayTestimonials[activeIndex].role}
                    </p>
                  )}
                </div>
              </div>
            </div>

            {/* Navigation dots */}
            <div className="flex justify-center gap-2 mt-6">
              {displayTestimonials.map((_, index) => (
                <button
                  key={index}
                  onClick={() => setActiveIndex(index)}
                  className={`w-3 h-3 rounded-full transition-colors ${
                    index === activeIndex ? 'bg-gray-900' : 'bg-gray-300 hover:bg-gray-400'
                  }`}
                  aria-label={`Go to testimonial ${index + 1}`}
                />
              ))}
            </div>
          </div>
        </div>
      </section>
    );
  }

  return (
    <section
      className={`py-16 px-6 md:px-12 ${!background_color ? 'bg-gray-50' : ''}`}
      style={style}
    >
      <div className="max-w-6xl mx-auto">
        {(title || subtitle) && (
          <div className="text-center mb-10">
            {title && (
              <h2 className="text-3xl md:text-4xl font-bold text-gray-900 mb-2">
                {title}
              </h2>
            )}
            {subtitle && (
              <p className="text-lg text-gray-600">{subtitle}</p>
            )}
          </div>
        )}

        <div className={`grid ${COLUMN_CLASSES[columns]} gap-6`}>
          {displayTestimonials.map((testimonial, index) => (
            <TestimonialCard
              key={index}
              testimonial={testimonial}
              showRating={show_rating}
            />
          ))}
        </div>
      </div>
    </section>
  );
}
