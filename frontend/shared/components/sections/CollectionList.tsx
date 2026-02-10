'use client';

import React, { useEffect, useState } from 'react';
import Link from 'next/link';

interface Collection {
  id: number;
  name: string;
  slug: string;
  description?: string;
  productCount: number;
  imageUrl?: string;
}

interface CollectionListProps {
  title?: string;
  subtitle?: string;
  columns?: 2 | 3 | 4;
  limit?: number;
  show_product_count?: boolean;
  image_ratio?: 'square' | 'portrait' | 'landscape';
  card_style?: 'overlay' | 'below';
  puck?: { isEditing: boolean; metadata: Record<string, unknown> };
}

const COLUMN_CLASSES: Record<number, string> = {
  2: 'grid-cols-1 sm:grid-cols-2',
  3: 'grid-cols-1 sm:grid-cols-2 lg:grid-cols-3',
  4: 'grid-cols-2 sm:grid-cols-3 lg:grid-cols-4',
};

const RATIO_CLASSES: Record<string, string> = {
  square: 'aspect-square',
  portrait: 'aspect-[3/4]',
  landscape: 'aspect-[4/3]',
};

function CollectionCard({
  collection,
  storeSlug,
  showProductCount,
  imageRatio,
  cardStyle,
}: {
  collection: Collection;
  storeSlug: string;
  showProductCount: boolean;
  imageRatio: 'square' | 'portrait' | 'landscape';
  cardStyle: 'overlay' | 'below';
}) {
  if (cardStyle === 'overlay') {
    return (
      <Link
        href={`/store/${storeSlug}/collections/${collection.slug}`}
        className="group block relative overflow-hidden rounded-lg"
      >
        <div className={`${RATIO_CLASSES[imageRatio]} bg-gray-200`}>
          {collection.imageUrl ? (
            <img
              src={collection.imageUrl}
              alt={collection.name}
              className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
            />
          ) : (
            <div className="w-full h-full bg-gradient-to-br from-gray-700 to-gray-900" />
          )}
        </div>
        <div className="absolute inset-0 bg-black/40 group-hover:bg-black/50 transition-colors flex items-center justify-center">
          <div className="text-center text-white">
            <h3 className="text-xl md:text-2xl font-bold mb-1">{collection.name}</h3>
            {showProductCount && (
              <p className="text-sm opacity-90">
                {collection.productCount} {collection.productCount === 1 ? 'product' : 'products'}
              </p>
            )}
          </div>
        </div>
      </Link>
    );
  }

  return (
    <Link
      href={`/store/${storeSlug}/collections/${collection.slug}`}
      className="group block"
    >
      <div className={`${RATIO_CLASSES[imageRatio]} bg-gray-100 rounded-lg overflow-hidden mb-3`}>
        {collection.imageUrl ? (
          <img
            src={collection.imageUrl}
            alt={collection.name}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-gray-400">
            <svg className="w-12 h-12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
            </svg>
          </div>
        )}
      </div>
      <h3 className="font-semibold text-gray-900 group-hover:text-gray-600 transition-colors">
        {collection.name}
      </h3>
      {showProductCount && (
        <p className="text-sm text-gray-500">
          {collection.productCount} {collection.productCount === 1 ? 'product' : 'products'}
        </p>
      )}
    </Link>
  );
}

export default function CollectionList({
  title,
  subtitle,
  columns = 3,
  limit = 6,
  show_product_count = true,
  image_ratio = 'square',
  card_style = 'overlay',
  puck,
}: CollectionListProps) {
  const storeSlug = (puck?.metadata?.storeSlug as string) || '';
  const apiUrl = (puck?.metadata?.apiUrl as string) || 'http://localhost:8080';
  const providedCollections = puck?.metadata?.collections as Collection[] | undefined;
  const [collections, setCollections] = useState<Collection[]>(providedCollections || []);
  const [loading, setLoading] = useState(!providedCollections);

  useEffect(() => {
    if (providedCollections) return;
    if (!storeSlug) {
      setLoading(false);
      return;
    }

    async function loadCollections() {
      try {
        const res = await fetch(`${apiUrl}/api/v1/storefront/${storeSlug}/collections`);
        if (res.ok) {
          const data = await res.json();
          setCollections(data);
        }
      } catch (error) {
        console.error('Failed to load collections:', error);
      } finally {
        setLoading(false);
      }
    }

    loadCollections();
  }, [storeSlug, apiUrl, providedCollections]);

  if (loading) {
    return (
      <div className="py-12 px-6 md:px-12">
        {title && <div className="h-8 bg-gray-200 rounded w-48 mx-auto mb-8 animate-pulse" />}
        <div className={`grid ${COLUMN_CLASSES[columns]} gap-6`}>
          {Array.from({ length: limit }).map((_, i) => (
            <div key={i} className="animate-pulse">
              <div className={`${RATIO_CLASSES[image_ratio]} bg-gray-200 rounded-lg mb-3`} />
              <div className="h-4 bg-gray-200 rounded w-1/2" />
            </div>
          ))}
        </div>
      </div>
    );
  }

  if (collections.length === 0) {
    if (puck?.isEditing) {
      return (
        <section className="py-12 px-6 md:px-12 text-center text-gray-400">
          <p>Collection List (no collections available)</p>
        </section>
      );
    }
    return null;
  }

  return (
    <section className="py-12 px-6 md:px-12">
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

      <div className={`grid ${COLUMN_CLASSES[columns]} gap-6 md:gap-8`}>
        {collections.slice(0, limit).map((collection) => (
          <CollectionCard
            key={collection.id}
            collection={collection}
            storeSlug={storeSlug}
            showProductCount={show_product_count}
            imageRatio={image_ratio}
            cardStyle={card_style}
          />
        ))}
      </div>
    </section>
  );
}
