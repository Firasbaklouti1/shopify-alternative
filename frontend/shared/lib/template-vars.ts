import type { Product, Collection } from './api';

export function replaceTemplateVariables(
  value: unknown,
  context: {
    store_name?: string;
    product?: Product;
    collection?: Collection;
  }
): unknown {
  if (typeof value !== 'string') return value;
  let result = value;
  // Handle {{dot.notation}} format
  result = result
    .replace(/\{\{store_name}}/g, context.store_name || '')
    .replace(/\{\{product\.id}}/g, String(context.product?.id || ''))
    .replace(/\{\{product\.name}}/g, context.product?.name || '')
    .replace(/\{\{product\.price}}/g, String(context.product?.price || ''))
    .replace(/\{\{collection\.name}}/g, context.collection?.name || '')
    .replace(/\{\{collection\.slug}}/g, context.collection?.slug || '')
    .replace(/\{\{collection\.description}}/g, context.collection?.description || '');
  // Handle {underscore_notation} format
  result = result
    .replace(/\{collection_name}/g, context.collection?.name || '')
    .replace(/\{collection_slug}/g, context.collection?.slug || '')
    .replace(/\{collection_description}/g, context.collection?.description || '')
    .replace(/\{product_name}/g, context.product?.name || '')
    .replace(/\{product_price}/g, String(context.product?.price || ''))
    .replace(/\{store_name}/g, context.store_name || '');
  return result;
}
