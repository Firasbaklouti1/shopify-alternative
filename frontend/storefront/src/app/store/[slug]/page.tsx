import { getPageLayout, getProducts, getCollections } from '@/lib/api';
import PuckRenderer from '@/components/PuckRenderer';
import { notFound } from 'next/navigation';

interface HomePageProps {
  params: Promise<{ slug: string }>;
}

export default async function HomePage({ params }: HomePageProps) {
  const { slug } = await params;

  try {
    const [layout, productsData, collections] = await Promise.all([
      getPageLayout(slug, 'home'),
      getProducts(slug, { limit: 24 }),
      getCollections(slug),
    ]);

    return (
      <PuckRenderer
        data={layout}
        storeSlug={slug}
        products={productsData.products}
        collections={collections}
      />
    );
  } catch (error) {
    console.error('Failed to load home page:', error);
    notFound();
  }
}
