import { getCustomPageLayout } from '@/lib/api';
import { notFound } from 'next/navigation';
import LayoutRenderer from '@/components/LayoutRenderer';
import type { Metadata } from 'next';

interface CustomPageProps {
  params: Promise<{ slug: string; handle: string }>;
}

export async function generateMetadata({
  params
}: CustomPageProps): Promise<Metadata> {
  const { slug, handle } = await params;

  try {
    const layout = await getCustomPageLayout(slug, handle);
    return {
      title: layout.name || handle.replace(/-/g, ' ').replace(/\b\w/g, c => c.toUpperCase()),
    };
  } catch {
    return {
      title: handle.replace(/-/g, ' ').replace(/\b\w/g, c => c.toUpperCase()),
    };
  }
}

export default async function CustomPage({ params }: CustomPageProps) {
  const { slug, handle } = await params;

  try {
    const layout = await getCustomPageLayout(slug, handle);

    return (
      <div className="min-h-screen">
        <LayoutRenderer
          layout={layout}
          storeSlug={slug}
          pageType="custom"
        />
      </div>
    );
  } catch (error) {
    console.error(`Failed to load custom page "${handle}":`, error);
    notFound();
  }
}
