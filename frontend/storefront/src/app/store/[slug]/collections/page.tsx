import { getCollections } from '@/lib/api';
import Link from 'next/link';
import { notFound } from 'next/navigation';
import type { Metadata } from 'next';

interface CollectionsPageProps {
  params: Promise<{ slug: string }>;
}

export async function generateMetadata(): Promise<Metadata> {
  return {
    title: 'Collections',
  };
}

export default async function CollectionsPage({ params }: CollectionsPageProps) {
  const { slug } = await params;

  try {
    const collections = await getCollections(slug);
    const basePath = `/store/${slug}`;

    return (
      <div className="py-12 px-6 md:px-12">
        <div className="max-w-6xl mx-auto">
          {/* Header */}
          <div className="text-center mb-12">
            <h1 className="text-3xl md:text-4xl font-bold text-gray-900 mb-2">Collections</h1>
            <p className="text-lg text-gray-600">Browse our product categories</p>
          </div>

          {collections.length === 0 ? (
            <div className="text-center py-12">
              <p className="text-gray-500">No collections found</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">
              {collections.map((collection) => (
                <Link
                  key={collection.id}
                  href={`${basePath}/collections/${collection.slug}`}
                  className="group block relative overflow-hidden rounded-lg"
                >
                  <div className="aspect-[4/3] bg-gray-200">
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

                  {/* Overlay */}
                  <div className="absolute inset-0 bg-black/40 group-hover:bg-black/50 transition-colors flex items-center justify-center">
                    <div className="text-center text-white">
                      <h2 className="text-2xl md:text-3xl font-bold mb-2">{collection.name}</h2>
                      <p className="text-sm opacity-90">
                        {collection.productCount} {collection.productCount === 1 ? 'product' : 'products'}
                      </p>
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </div>
      </div>
    );
  } catch (error) {
    console.error('Failed to load collections:', error);
    notFound();
  }
}
