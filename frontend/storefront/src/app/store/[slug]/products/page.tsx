import { getProducts, getCollections } from '@/lib/api';
import Link from 'next/link';
import { notFound } from 'next/navigation';
import type { Metadata } from 'next';

interface ProductsPageProps {
  params: Promise<{ slug: string }>;
  searchParams: Promise<{
    page?: string;
    category?: string;
    sort?: string;
  }>;
}

export async function generateMetadata({
  params
}: {
  params: Promise<{ slug: string }>
}): Promise<Metadata> {
  return {
    title: 'All Products',
  };
}

function formatPrice(price: number): string {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(price);
}

export default async function ProductsPage({ params, searchParams }: ProductsPageProps) {
  const { slug } = await params;
  const { page = '0', category, sort = 'name-asc' } = await searchParams;

  const [sortBy, sortDir] = sort.split('-') as [string, 'asc' | 'desc'];

  try {
    const [productsData, collections] = await Promise.all([
      getProducts(slug, {
        page: parseInt(page),
        limit: 24,
        category,
        sortBy,
        sortDir,
      }),
      getCollections(slug),
    ]);

    const { products, totalPages, currentPage, hasNext, hasPrevious } = productsData;
    const basePath = `/store/${slug}`;

    return (
      <div className="py-8 px-6 md:px-12">
        <div className="max-w-7xl mx-auto">
          {/* Header */}
          <div className="mb-8">
            <h1 className="text-3xl md:text-4xl font-bold text-gray-900 mb-2">All Products</h1>
            <p className="text-gray-600">
              {productsData.totalProducts} {productsData.totalProducts === 1 ? 'product' : 'products'}
            </p>
          </div>

          <div className="flex flex-col lg:flex-row gap-8">
            {/* Sidebar Filters */}
            <aside className="w-full lg:w-64 flex-shrink-0">
              <div className="sticky top-24">
                <h3 className="font-semibold text-gray-900 mb-4">Collections</h3>
                <ul className="space-y-2">
                  <li>
                    <Link
                      href={`${basePath}/products`}
                      className={`block py-1 ${!category ? 'text-gray-900 font-medium' : 'text-gray-600 hover:text-gray-900'}`}
                    >
                      All Products
                    </Link>
                  </li>
                  {collections.map((col) => (
                    <li key={col.id}>
                      <Link
                        href={`${basePath}/products?category=${col.slug}`}
                        className={`block py-1 ${category === col.slug ? 'text-gray-900 font-medium' : 'text-gray-600 hover:text-gray-900'}`}
                      >
                        {col.name} ({col.productCount})
                      </Link>
                    </li>
                  ))}
                </ul>
              </div>
            </aside>

            {/* Products Grid */}
            <div className="flex-1">
              {/* Sort Controls */}
              <div className="flex justify-between items-center mb-6">
                <p className="text-sm text-gray-600">
                  Showing page {currentPage + 1} of {totalPages || 1}
                </p>
                <select
                  defaultValue={sort}
                  onChange={(e) => {
                    const url = new URL(window.location.href);
                    url.searchParams.set('sort', e.target.value);
                    window.location.href = url.toString();
                  }}
                  className="px-3 py-2 border border-gray-300 rounded-md text-sm"
                >
                  <option value="name-asc">Name: A-Z</option>
                  <option value="name-desc">Name: Z-A</option>
                  <option value="price-asc">Price: Low to High</option>
                  <option value="price-desc">Price: High to Low</option>
                </select>
              </div>

              {products.length === 0 ? (
                <div className="text-center py-12">
                  <p className="text-gray-500">No products found</p>
                </div>
              ) : (
                <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-6">
                  {products.map((product) => (
                    <Link
                      key={product.id}
                      href={`${basePath}/products/${product.slug}`}
                      className="group"
                    >
                      <div className="aspect-square bg-gray-100 rounded-lg overflow-hidden mb-3">
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
                      </div>
                      <h3 className="font-medium text-gray-900 group-hover:text-gray-600 transition-colors line-clamp-2">
                        {product.name}
                      </h3>
                      <p className="font-semibold mt-1">{formatPrice(product.price)}</p>
                    </Link>
                  ))}
                </div>
              )}

              {/* Pagination */}
              {totalPages > 1 && (
                <div className="flex justify-center gap-2 mt-12">
                  {hasPrevious && (
                    <Link
                      href={`${basePath}/products?page=${currentPage - 1}${category ? `&category=${category}` : ''}&sort=${sort}`}
                      className="px-4 py-2 border border-gray-300 rounded-md hover:bg-gray-50"
                    >
                      Previous
                    </Link>
                  )}

                  {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                    const pageNum = currentPage <= 2
                      ? i
                      : currentPage >= totalPages - 3
                        ? totalPages - 5 + i
                        : currentPage - 2 + i;

                    if (pageNum < 0 || pageNum >= totalPages) return null;

                    return (
                      <Link
                        key={pageNum}
                        href={`${basePath}/products?page=${pageNum}${category ? `&category=${category}` : ''}&sort=${sort}`}
                        className={`px-4 py-2 rounded-md ${
                          pageNum === currentPage
                            ? 'bg-gray-900 text-white'
                            : 'border border-gray-300 hover:bg-gray-50'
                        }`}
                      >
                        {pageNum + 1}
                      </Link>
                    );
                  })}

                  {hasNext && (
                    <Link
                      href={`${basePath}/products?page=${currentPage + 1}${category ? `&category=${category}` : ''}&sort=${sort}`}
                      className="px-4 py-2 border border-gray-300 rounded-md hover:bg-gray-50"
                    >
                      Next
                    </Link>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    );
  } catch (error) {
    console.error('Failed to load products:', error);
    notFound();
  }
}
