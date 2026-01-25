'use client';

import { useRouter, useSearchParams } from 'next/navigation';

interface SortDropdownProps {
  currentSort: string;
  basePath: string;
}

export default function SortDropdown({ currentSort, basePath }: SortDropdownProps) {
  const router = useRouter();
  const searchParams = useSearchParams();

  const handleSortChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const params = new URLSearchParams(searchParams.toString());
    params.set('sort', e.target.value);
    params.set('page', '0'); // Reset to first page on sort change
    router.push(`${basePath}/products?${params.toString()}`);
  };

  return (
    <select
      defaultValue={currentSort}
      onChange={handleSortChange}
      className="px-3 py-2 border border-gray-300 rounded-md text-sm"
    >
      <option value="name-asc">Name: A-Z</option>
      <option value="name-desc">Name: Z-A</option>
      <option value="price-asc">Price: Low to High</option>
      <option value="price-desc">Price: High to Low</option>
    </select>
  );
}
