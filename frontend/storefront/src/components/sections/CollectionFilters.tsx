import React from 'react';
import type { SectionProps } from '../LayoutRenderer';

interface CollectionFiltersSettings {
  show_sort?: boolean;
  show_filter?: boolean;
  filter_type?: 'sidebar' | 'dropdown';
}

/**
 * Collection filters section - allows sorting and filtering products.
 * Currently a placeholder that shows the sort dropdown.
 */
export default function CollectionFilters({ section, storeSlug }: SectionProps) {
  const settings = section.settings as CollectionFiltersSettings;

  const {
    show_sort = true,
    show_filter = false,
    filter_type = 'dropdown',
  } = settings;

  // If no filters to show, render nothing
  if (!show_sort && !show_filter) {
    return null;
  }

  return (
    <div className="py-4 px-6 md:px-12 border-b border-gray-200">
      <div className="flex items-center justify-between flex-wrap gap-4">
        {/* Filter placeholder */}
        {show_filter && (
          <div className="flex items-center gap-2">
            <span className="text-sm text-gray-600">Filter:</span>
            <select className="border border-gray-300 rounded-md px-3 py-1.5 text-sm">
              <option value="">All Products</option>
            </select>
          </div>
        )}

        {/* Sort dropdown */}
        {show_sort && (
          <div className="flex items-center gap-2 ml-auto">
            <span className="text-sm text-gray-600">Sort by:</span>
            <select className="border border-gray-300 rounded-md px-3 py-1.5 text-sm">
              <option value="name-asc">Name (A-Z)</option>
              <option value="name-desc">Name (Z-A)</option>
              <option value="price-asc">Price (Low to High)</option>
              <option value="price-desc">Price (High to Low)</option>
            </select>
          </div>
        )}
      </div>
    </div>
  );
}
