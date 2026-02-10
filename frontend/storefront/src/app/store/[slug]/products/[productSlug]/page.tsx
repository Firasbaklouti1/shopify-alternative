import { getPageLayout, getProduct } from '@/lib/api';
import PuckRenderer from '@/components/PuckRenderer';
import { notFound } from 'next/navigation';
import type { Metadata } from 'next';

interface ProductPageProps {
  params: Promise<{ slug: string; productSlug: string }>;
}

export async function generateMetadata({
  params
}: ProductPageProps): Promise<Metadata> {
  const { slug, productSlug } = await params;

  try {
    const product = await getProduct(slug, productSlug);

    return {
      title: product.name,
      description: product.description?.slice(0, 160) || `Buy ${product.name}`,
      openGraph: {
        images: product.imageUrl ? [product.imageUrl] : [],
      },
    };
  } catch {
    return {
      title: 'Product Not Found',
    };
  }
}

export default async function ProductPage({ params }: ProductPageProps) {
  const { slug, productSlug } = await params;

  try {
    const [layout, product] = await Promise.all([
      getPageLayout(slug, 'product'),
      getProduct(slug, productSlug),
    ]);

    return (
      <PuckRenderer
        data={layout}
        storeSlug={slug}
        product={product}
      />
    );
  } catch (error) {
    console.error('Failed to load product page:', error);
    notFound();
  }
}
