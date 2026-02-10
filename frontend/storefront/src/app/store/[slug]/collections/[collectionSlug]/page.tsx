import { getPageLayout, getProducts, getCollection } from '@/lib/api';
import PuckRenderer from '@/components/PuckRenderer';
import { notFound } from 'next/navigation';
import type { Metadata } from 'next';

interface CollectionPageProps {
  params: Promise<{ slug: string; collectionSlug: string }>;
}

export async function generateMetadata({
  params
}: CollectionPageProps): Promise<Metadata> {
  const { slug, collectionSlug } = await params;

  try {
    const collection = await getCollection(slug, collectionSlug);

    return {
      title: collection.name,
      description: collection.description || `Shop ${collection.name}`,
    };
  } catch {
    return {
      title: 'Collection Not Found',
    };
  }
}

export default async function CollectionPage({ params }: CollectionPageProps) {
  const { slug, collectionSlug } = await params;

  try {
    const [layout, collection, productsData] = await Promise.all([
      getPageLayout(slug, 'collection'),
      getCollection(slug, collectionSlug),
      getProducts(slug, { category: collectionSlug, limit: 24 }),
    ]);

    return (
      <div>
        <div className="bg-gray-50 py-12 px-6 md:px-12 text-center">
          <h1 className="text-4xl font-bold text-gray-900 mb-2">{collection.name}</h1>
          {collection.description && (
            <p className="text-lg text-gray-600 max-w-2xl mx-auto">{collection.description}</p>
          )}
          <p className="text-sm text-gray-500 mt-4">
            {collection.productCount} {collection.productCount === 1 ? 'product' : 'products'}
          </p>
        </div>

        <PuckRenderer
          data={layout}
          storeSlug={slug}
          collection={collection}
          products={productsData.products}
        />
      </div>
    );
  } catch (error) {
    console.error('Failed to load collection page:', error);
    notFound();
  }
}
