'use client';

import React, { useState } from 'react';
import type { SectionProps } from '../LayoutRenderer';
import { addToCart } from '@/lib/api';

interface ProductMainSettings {
  gallery_position?: 'left' | 'right';
  show_vendor?: boolean;
  show_sku?: boolean;
  show_quantity_selector?: boolean;
  enable_sticky_buy?: boolean;
}

function formatPrice(price: number): string {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(price);
}

export default function ProductMain({ section, product }: SectionProps) {
  const settings = section.settings as ProductMainSettings;
  const [selectedVariantId, setSelectedVariantId] = useState<number | null>(
    product?.variants?.[0]?.id || null
  );
  const [quantity, setQuantity] = useState(1);
  const [activeImageIndex, setActiveImageIndex] = useState(0);
  const [addedToCart, setAddedToCart] = useState(false);

  const {
    gallery_position = 'left',
    show_vendor = true,
    show_sku = false,
    show_quantity_selector = true,
  } = settings;

  if (!product) {
    return (
      <div className="py-12 px-6 text-center text-gray-500">
        Product data not available
      </div>
    );
  }

  const selectedVariant = product.variants?.find((v) => v.id === selectedVariantId);
  const images = product.images?.length ? product.images : product.imageUrl ? [product.imageUrl] : [];
  const isOnSale = product.compareAtPrice && product.compareAtPrice > product.price;

  const handleAddToCart = () => {
    if (!selectedVariant) return;

    addToCart({
      productId: product.id,
      variantId: selectedVariant.id,
      name: product.name,
      variantName: selectedVariant.name,
      price: selectedVariant.price,
      quantity,
      imageUrl: images[0],
    });

    setAddedToCart(true);
    setTimeout(() => setAddedToCart(false), 2000);

    // Dispatch custom event for cart update
    window.dispatchEvent(new CustomEvent('cart-updated'));
  };

  const galleryContent = (
    <div className="space-y-4">
      {/* Main Image */}
      <div className="aspect-square bg-gray-100 rounded-lg overflow-hidden">
        {images[activeImageIndex] ? (
          <img
            src={images[activeImageIndex]}
            alt={product.name}
            className="w-full h-full object-cover"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-gray-400">
            <svg className="w-24 h-24" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
          </div>
        )}
      </div>

      {/* Thumbnail Gallery */}
      {images.length > 1 && (
        <div className="flex gap-2 overflow-x-auto pb-2">
          {images.map((img, index) => (
            <button
              key={index}
              onClick={() => setActiveImageIndex(index)}
              className={`flex-shrink-0 w-20 h-20 rounded-md overflow-hidden border-2 transition-colors ${
                index === activeImageIndex ? 'border-gray-900' : 'border-transparent'
              }`}
            >
              <img src={img} alt="" className="w-full h-full object-cover" />
            </button>
          ))}
        </div>
      )}
    </div>
  );

  const productInfo = (
    <div className="space-y-6">
      {/* Vendor */}
      {show_vendor && product.vendor && (
        <p className="text-sm text-gray-500 uppercase tracking-wide">{product.vendor}</p>
      )}

      {/* Title */}
      <h1 className="text-3xl md:text-4xl font-bold text-gray-900">{product.name}</h1>

      {/* Price */}
      <div className="flex items-center gap-3">
        <span className={`text-2xl font-semibold ${isOnSale ? 'text-red-600' : 'text-gray-900'}`}>
          {formatPrice(selectedVariant?.price || product.price)}
        </span>
        {isOnSale && product.compareAtPrice && (
          <span className="text-lg text-gray-400 line-through">
            {formatPrice(product.compareAtPrice)}
          </span>
        )}
        {isOnSale && (
          <span className="bg-red-100 text-red-700 text-sm font-medium px-2 py-0.5 rounded">
            Sale
          </span>
        )}
      </div>

      {/* SKU */}
      {show_sku && selectedVariant?.sku && (
        <p className="text-sm text-gray-500">SKU: {selectedVariant.sku}</p>
      )}

      {/* Variant Selector */}
      {product.variants && product.variants.length > 1 && (
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Variant
          </label>
          <div className="flex flex-wrap gap-2">
            {product.variants.map((variant) => (
              <button
                key={variant.id}
                onClick={() => setSelectedVariantId(variant.id)}
                disabled={!variant.inStock}
                className={`px-4 py-2 border rounded-md text-sm font-medium transition-colors ${
                  selectedVariantId === variant.id
                    ? 'border-gray-900 bg-gray-900 text-white'
                    : variant.inStock
                    ? 'border-gray-300 hover:border-gray-400'
                    : 'border-gray-200 text-gray-400 cursor-not-allowed'
                }`}
              >
                {variant.name}
                {!variant.inStock && ' (Sold out)'}
              </button>
            ))}
          </div>
        </div>
      )}

      {/* Quantity Selector */}
      {show_quantity_selector && (
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Quantity
          </label>
          <div className="flex items-center border border-gray-300 rounded-md w-32">
            <button
              onClick={() => setQuantity(Math.max(1, quantity - 1))}
              className="px-3 py-2 hover:bg-gray-100"
            >
              −
            </button>
            <input
              type="number"
              value={quantity}
              onChange={(e) => setQuantity(Math.max(1, parseInt(e.target.value) || 1))}
              className="w-full text-center border-0 focus:ring-0"
              min="1"
            />
            <button
              onClick={() => setQuantity(quantity + 1)}
              className="px-3 py-2 hover:bg-gray-100"
            >
              +
            </button>
          </div>
        </div>
      )}

      {/* Add to Cart Button */}
      <button
        onClick={handleAddToCart}
        disabled={!selectedVariant?.inStock}
        className={`w-full py-4 rounded-md font-semibold text-lg transition-colors ${
          addedToCart
            ? 'bg-green-600 text-white'
            : selectedVariant?.inStock
            ? 'bg-gray-900 text-white hover:bg-gray-800'
            : 'bg-gray-200 text-gray-500 cursor-not-allowed'
        }`}
      >
        {addedToCart
          ? '✓ Added to Cart!'
          : selectedVariant?.inStock
          ? 'Add to Cart'
          : 'Sold Out'}
      </button>

      {/* Description */}
      {product.description && (
        <div className="prose prose-gray max-w-none">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Description</h3>
          <div dangerouslySetInnerHTML={{ __html: product.description }} />
        </div>
      )}
    </div>
  );

  return (
    <section className="py-12 px-6 md:px-12">
      <div className={`grid md:grid-cols-2 gap-8 md:gap-12 ${gallery_position === 'right' ? 'md:flex-row-reverse' : ''}`}>
        {gallery_position === 'left' ? (
          <>
            {galleryContent}
            {productInfo}
          </>
        ) : (
          <>
            {productInfo}
            {galleryContent}
          </>
        )}
      </div>
    </section>
  );
}
