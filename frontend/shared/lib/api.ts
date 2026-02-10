/**
 * Spring Boot API Client for the Storefront
 * All API calls go through this module.
 */

import type { Data } from '@measured/puck';
import { convertLegacyToPuck } from './puck-utils';

export type PuckData = Data;

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export interface StoreSettings {
  storeName: string;
  storeSlug: string;
  checkoutMode: 'GUEST_ONLY' | 'ACCOUNT_ONLY' | 'BOTH';
  globalStyles: {
    primaryColor?: string;
    secondaryColor?: string;
    fontFamily?: string;
    logo?: string;
    favicon?: string;
  };
  seoDefaults: {
    titleTemplate?: string;
    defaultDescription?: string;
    ogImage?: string;
  };
  socialLinks: {
    facebook?: string;
    instagram?: string;
    twitter?: string;
  };
  contactEmail: string;
  announcementText?: string;
  announcementEnabled: boolean;
  theme?: {
    id: number;
    name: string;
    cssVariables: Record<string, string>;
  };
}

export interface ProductVariant {
  id: number;
  name: string;
  sku: string;
  price: number;
  compareAtPrice?: number;
  inStock: boolean;
  quantity: number;
  options?: { name: string; value: string }[];
}

export interface Product {
  id: number;
  name: string;
  slug: string;
  description: string;
  price: number;
  compareAtPrice?: number;
  imageUrl?: string;
  images?: string[];
  categoryName?: string;
  categorySlug?: string;
  inStock: boolean;
  variants: ProductVariant[];
  vendor?: string;
  tags?: string[];
}

export interface Collection {
  id: number;
  name: string;
  slug: string;
  description?: string;
  productCount: number;
  imageUrl?: string;
}

export interface ProductsResponse {
  products: Product[];
  currentPage: number;
  totalPages: number;
  totalProducts: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface CartItem {
  productId: number;
  variantId: number;
  name: string;
  variantName: string;
  price: number;
  quantity: number;
  imageUrl?: string;
}

// API Functions

export async function getStoreSettings(slug: string): Promise<StoreSettings> {
  const res = await fetch(`${API_BASE_URL}/api/v1/storefront/${slug}/settings`, {
    next: { revalidate: 60 }, // Cache for 60 seconds
  });

  if (!res.ok) {
    throw new Error(`Failed to fetch store settings: ${res.status}`);
  }

  return res.json();
}

export async function getPageLayout(slug: string, pageType: string = 'home'): Promise<PuckData> {
  const res = await fetch(`${API_BASE_URL}/api/v1/storefront/${slug}/layout?page=${pageType}`, {
    cache: 'no-store',
  });

  if (!res.ok) {
    throw new Error(`Failed to fetch page layout: ${res.status}`);
  }

  const data = await res.json();

  return convertLegacyToPuck(data) || { content: [], root: { props: { title: '' } }, zones: {} };
}

export async function getCustomPageLayout(slug: string, handle: string): Promise<PuckData> {
  const res = await fetch(`${API_BASE_URL}/api/v1/storefront/${slug}/pages/${handle}`, {
    cache: 'no-store',
  });

  if (!res.ok) {
    throw new Error(`Failed to fetch custom page layout: ${res.status}`);
  }

  const data = await res.json();

  return convertLegacyToPuck(data) || { content: [], root: { props: { title: '' } }, zones: {} };
}

export async function getProducts(
  slug: string,
  options: {
    page?: number;
    limit?: number;
    category?: string;
    sortBy?: string;
    sortDir?: 'asc' | 'desc';
  } = {}
): Promise<ProductsResponse> {
  const { page = 0, limit = 24, category, sortBy = 'name', sortDir = 'asc' } = options;

  const params = new URLSearchParams({
    page: String(page),
    limit: String(limit),
    sortBy,
    sortDir,
  });

  if (category) {
    params.set('category', category);
  }

  const res = await fetch(`${API_BASE_URL}/api/v1/storefront/${slug}/products?${params}`, {
    next: { revalidate: 30 },
  });

  if (!res.ok) {
    throw new Error(`Failed to fetch products: ${res.status}`);
  }

  return res.json();
}

export async function getProduct(slug: string, productSlug: string): Promise<Product> {
  const res = await fetch(`${API_BASE_URL}/api/v1/storefront/${slug}/products/${productSlug}`, {
    next: { revalidate: 30 },
  });

  if (!res.ok) {
    throw new Error(`Failed to fetch product: ${res.status}`);
  }

  return res.json();
}

export async function getCollections(slug: string): Promise<Collection[]> {
  const res = await fetch(`${API_BASE_URL}/api/v1/storefront/${slug}/collections`, {
    next: { revalidate: 60 },
  });

  if (!res.ok) {
    throw new Error(`Failed to fetch collections: ${res.status}`);
  }

  return res.json();
}

export async function getCollection(slug: string, collectionSlug: string): Promise<Collection> {
  const res = await fetch(`${API_BASE_URL}/api/v1/storefront/${slug}/collections/${collectionSlug}`, {
    next: { revalidate: 60 },
  });

  if (!res.ok) {
    throw new Error(`Failed to fetch collection: ${res.status}`);
  }

  return res.json();
}

// Cart management (client-side localStorage)
export function getCart(): CartItem[] {
  if (typeof window === 'undefined') return [];
  const cart = localStorage.getItem('storefront-cart');
  return cart ? JSON.parse(cart) : [];
}

export function saveCart(cart: CartItem[]): void {
  if (typeof window === 'undefined') return;
  localStorage.setItem('storefront-cart', JSON.stringify(cart));
}

export function addToCart(item: CartItem): CartItem[] {
  const cart = getCart();
  const existingIndex = cart.findIndex(
    (i) => i.productId === item.productId && i.variantId === item.variantId
  );

  if (existingIndex >= 0) {
    cart[existingIndex].quantity += item.quantity;
  } else {
    cart.push(item);
  }

  saveCart(cart);
  return cart;
}

export function removeFromCart(productId: number, variantId: number): CartItem[] {
  const cart = getCart().filter(
    (i) => !(i.productId === productId && i.variantId === variantId)
  );
  saveCart(cart);
  return cart;
}

export function updateCartQuantity(productId: number, variantId: number, quantity: number): CartItem[] {
  const cart = getCart();
  const item = cart.find(
    (i) => i.productId === productId && i.variantId === variantId
  );

  if (item) {
    item.quantity = quantity;
  }

  saveCart(cart);
  return cart;
}

export function clearCart(): void {
  if (typeof window === 'undefined') return;
  localStorage.removeItem('storefront-cart');
}
