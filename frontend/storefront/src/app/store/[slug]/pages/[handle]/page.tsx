import { getCustomPageLayout } from '@/lib/api';
import { notFound } from 'next/navigation';
import PuckRenderer from '@/components/PuckRenderer';
import type { Metadata } from 'next';

interface CustomPageProps {
  params: Promise<{ slug: string; handle: string }>;
}

export async function generateMetadata({
  params
}: CustomPageProps): Promise<Metadata> {
  const { handle } = await params;

  return {
    title: handle.replace(/-/g, ' ').replace(/\b\w/g, c => c.toUpperCase()),
  };
}

export default async function CustomPage({ params }: CustomPageProps) {
  const { slug, handle } = await params;

  try {
    const layout = await getCustomPageLayout(slug, handle);

    return (
      <div className="min-h-screen">
        <PuckRenderer
          data={layout}
          storeSlug={slug}
        />
      </div>
    );
  } catch (error) {
    console.error(`Failed to load custom page "${handle}":`, error);
    notFound();
  }
}
