'use client';

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import type { SectionProps } from '../LayoutRenderer';
import { getProducts, Product } from '@/lib/api';

interface ProductGridSettings {
  title?: string;
  subtitle?: string;
  collection_handle?: string;
  columns?: 2 | 3 | 4 | 5;
  limit?: number;
  show_price?: boolean;
  show_vendor?: boolean;
  show_sale_badge?: boolean;
  image_ratio?: 'square' | 'portrait' | 'landscape';
}

const COLUMN_CLASSES = {
  2: 'grid-cols-1 sm:grid-cols-2',
  3: 'grid-cols-1 sm:grid-cols-2 lg:grid-cols-3',
  4: 'grid-cols-2 sm:grid-cols-3 lg:grid-cols-4',
  5: 'grid-cols-2 sm:grid-cols-3 lg:grid-cols-5',
};

const RATIO_CLASSES = {
  square: 'aspect-square',
  portrait: 'aspect-[3/4]',
  landscape: 'aspect-[4/3]',
};

function formatPrice(price: number): string {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(price);
}

function ProductCard({
  product,
  storeSlug,
  showPrice,
  showVendor,
  showSaleBadge,
  imageRatio,
}: {
  product: Product;
  storeSlug: string;
  showPrice: boolean;
  showVendor: boolean;
  showSaleBadge: boolean;
  imageRatio: 'square' | 'portrait' | 'landscape';
}) {
  const isOnSale = product.compareAtPrice && product.compareAtPrice > product.price;

  return (
    <Link
      href={`/store/${storeSlug}/products/${product.slug}`}
      className="group block"
    >
      <div className={`relative ${RATIO_CLASSES[imageRatio]} bg-gray-100 rounded-lg overflow-hidden mb-3`}>
        {product.imageUrl ? (
          <img
            src={product.imageUrl}
            alt={product.name}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-gray-400">
            <svg className="w-12 h-12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
          </div>
        )}

        {showSaleBadge && isOnSale && (
          <span className="absolute top-2 left-2 bg-red-500 text-white text-xs font-semibold px-2 py-1 rounded">
            Sale
          </span>
        )}

        {!product.inStock && (
          <span className="absolute top-2 right-2 bg-gray-800 text-white text-xs font-semibold px-2 py-1 rounded">
            Sold Out
          </span>
        )}
      </div>

      {showVendor && product.vendor && (
        <p className="text-xs text-gray-500 mb-1">{product.vendor}</p>
      )}

      <h3 className="font-medium text-gray-900 group-hover:text-gray-600 transition-colors line-clamp-2">
        {product.name}
      </h3>

      {showPrice && (
        <div className="mt-1 flex items-center gap-2">
          <span className={`font-semibold ${isOnSale ? 'text-red-600' : 'text-gray-900'}`}>
            {formatPrice(product.price)}
          </span>
          {isOnSale && product.compareAtPrice && (
            <span className="text-sm text-gray-400 line-through">
              {formatPrice(product.compareAtPrice)}
            </span>
          )}
        </div>
      )}
    </Link>
  );
}

export default function ProductGrid({ section, storeSlug, products: providedProducts }: SectionProps) {
  const settings = section.settings as ProductGridSettings;
  const [products, setProducts] = useState<Product[]>(providedProducts || []);
  const [loading, setLoading] = useState(!providedProducts);

  const {
    title,
    subtitle,
    collection_handle,
    columns = 4,
    limit = 8,
    show_price = true,
    show_vendor = false,
    show_sale_badge = true,
    image_ratio = 'square',
  } = settings;

  useEffect(() => {
    if (providedProducts) return;

    async function loadProducts() {
      try {
        const response = await getProducts(storeSlug, {
          category: collection_handle,
          limit,
        });
        setProducts(response.products);
      } catch (error) {
        console.error('Failed to load products:', error);
      } finally {
        setLoading(false);
      }
    }

    loadProducts();
  }, [storeSlug, collection_handle, limit, providedProducts]);

  if (loading) {
    return (
      <div className="py-12 px-6 md:px-12">
        {title && <div className="h-8 bg-gray-200 rounded w-48 mx-auto mb-8 animate-pulse" />}
        <div className={`grid ${COLUMN_CLASSES[columns]} gap-6`}>
          {Array.from({ length: limit }).map((_, i) => (
            <div key={i} className="animate-pulse">
              <div className={`${RATIO_CLASSES[image_ratio]} bg-gray-200 rounded-lg mb-3`} />
              <div className="h-4 bg-gray-200 rounded w-3/4 mb-2" />
              <div className="h-4 bg-gray-200 rounded w-1/4" />
            </div>
          ))}
        </div>
      </div>
    );
  }

  if (products.length === 0) {
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
        {products.slice(0, limit).map((product) => (
          <ProductCard
            key={product.id}
            product={product}
            storeSlug={storeSlug}
            showPrice={show_price}
            showVendor={show_vendor}
            showSaleBadge={show_sale_badge}
            imageRatio={image_ratio}
          />
        ))}
      </div>
    </section>
  );
}
