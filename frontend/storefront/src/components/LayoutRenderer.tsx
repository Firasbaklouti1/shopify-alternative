'use client';

import React, { Suspense } from 'react';
import type { PageLayout, Section, Product, Collection } from '@/lib/api';

// Section components
import HeroBanner from './sections/HeroBanner';
import ProductGrid from './sections/ProductGrid';
import ProductMain from './sections/ProductMain';
import CollectionList from './sections/CollectionList';
import RichText from './sections/RichText';
import ImageWithText from './sections/ImageWithText';
import Newsletter from './sections/Newsletter';
import Testimonials from './sections/Testimonials';
import AnnouncementBar from './sections/AnnouncementBar';
import Footer from './sections/Footer';
import AppBlockLoader from './AppBlockLoader';

// Section component mapping
const SECTION_COMPONENTS: Record<string, React.ComponentType<SectionProps>> = {
  'hero-banner': HeroBanner,
  'product-grid': ProductGrid,
  'product-main': ProductMain,
  'collection-list': CollectionList,
  'rich-text': RichText,
  'image-with-text': ImageWithText,
  'newsletter': Newsletter,
  'testimonials': Testimonials,
  'announcement-bar': AnnouncementBar,
  'footer': Footer,
  'app-block': AppBlockLoader,
};

export interface SectionProps {
  id: string;
  section: Section;
  storeSlug: string;
  isSelected?: boolean;
  onSectionClick?: (sectionId: string) => void;
  // Context data for dynamic sections
  product?: Product;
  products?: Product[];
  collection?: Collection;
  collections?: Collection[];
}

interface LayoutRendererProps {
  layout: PageLayout;
  storeSlug: string;
  selectedSectionId?: string | null;
  onSectionClick?: (sectionId: string) => void;
  // Context data
  product?: Product;
  products?: Product[];
  collection?: Collection;
  collections?: Collection[];
}

function SectionWrapper({
  id,
  children,
  isSelected,
  onSectionClick,
}: {
  id: string;
  children: React.ReactNode;
  isSelected?: boolean;
  onSectionClick?: (sectionId: string) => void;
}) {
  const handleClick = (e: React.MouseEvent) => {
    // Only notify if we have a click handler (editor mode)
    if (onSectionClick) {
      e.stopPropagation();
      onSectionClick(id);
    }
  };

  return (
    <section
      id={`section-${id}`}
      data-section-id={id}
      className={`storefront-section ${isSelected ? 'ring-2 ring-blue-500 ring-offset-2' : ''}`}
      onClick={handleClick}
    >
      {children}
    </section>
  );
}

function SectionFallback() {
  return (
    <div className="animate-pulse bg-gray-100 h-48 flex items-center justify-center">
      <span className="text-gray-400">Loading section...</span>
    </div>
  );
}

function UnknownSection({ type }: { type: string }) {
  return (
    <div className="bg-yellow-50 border border-yellow-200 p-4 text-center">
      <p className="text-yellow-700">
        Unknown section type: <code className="bg-yellow-100 px-1">{type}</code>
      </p>
    </div>
  );
}

export default function LayoutRenderer({
  layout,
  storeSlug,
  selectedSectionId,
  onSectionClick,
  product,
  products,
  collection,
  collections,
}: LayoutRendererProps) {
  if (!layout || !layout.sections || !layout.order) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p className="text-gray-500">No layout configuration found</p>
      </div>
    );
  }

  return (
    <div className="layout-renderer">
      {layout.order.map((sectionId) => {
        const section = layout.sections[sectionId];

        if (!section) {
          return null;
        }

        const Component = SECTION_COMPONENTS[section.type];
        const isSelected = selectedSectionId === sectionId;

        if (!Component) {
          return (
            <SectionWrapper
              key={sectionId}
              id={sectionId}
              isSelected={isSelected}
              onSectionClick={onSectionClick}
            >
              <UnknownSection type={section.type} />
            </SectionWrapper>
          );
        }

        return (
          <SectionWrapper
            key={sectionId}
            id={sectionId}
            isSelected={isSelected}
            onSectionClick={onSectionClick}
          >
            <Suspense fallback={<SectionFallback />}>
              <Component
                id={sectionId}
                section={section}
                storeSlug={storeSlug}
                isSelected={isSelected}
                onSectionClick={onSectionClick}
                product={product}
                products={products}
                collection={collection}
                collections={collections}
              />
            </Suspense>
          </SectionWrapper>
        );
      })}
    </div>
  );
}

// Utility to replace template variables in settings
export function replaceTemplateVariables(
  value: unknown,
  context: {
    store_name?: string;
    product?: Product;
    collection?: Collection;
  }
): unknown {
  if (typeof value !== 'string') return value;

  return value
    .replace(/\{\{store_name}}/g, context.store_name || '')
    .replace(/\{\{product\.id}}/g, String(context.product?.id || ''))
    .replace(/\{\{product\.name}}/g, context.product?.name || '')
    .replace(/\{\{product\.price}}/g, String(context.product?.price || ''))
    .replace(/\{\{collection\.name}}/g, context.collection?.name || '')
    .replace(/\{\{collection\.slug}}/g, context.collection?.slug || '');
}
