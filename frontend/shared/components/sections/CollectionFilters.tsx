import React from 'react';

interface CollectionFiltersProps {
  show_sort?: boolean;
  show_filter?: boolean;
  filter_type?: 'sidebar' | 'dropdown';
  puck?: { isEditing: boolean; metadata: Record<string, unknown> };
}

export default function CollectionFilters({
  show_sort = true,
  show_filter = false,
}: CollectionFiltersProps) {
  if (!show_sort && !show_filter) {
    return null;
  }

  return (
    <div className="py-4 px-6 md:px-12 border-b border-gray-200">
      <div className="flex items-center justify-between flex-wrap gap-4">
        {show_filter && (
          <div className="flex items-center gap-2">
            <span className="text-sm text-gray-600">Filter:</span>
            <select className="border border-gray-300 rounded-md px-3 py-1.5 text-sm">
              <option value="">All Products</option>
            </select>
          </div>
        )}

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
