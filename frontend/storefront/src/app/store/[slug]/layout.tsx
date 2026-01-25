import { getStoreSettings } from '@/lib/api';
import StoreHeader from '@/components/StoreHeader';
import { notFound } from 'next/navigation';
import type { Metadata } from 'next';

interface StoreLayoutProps {
  children: React.ReactNode;
  params: Promise<{ slug: string }>;
}

export async function generateMetadata({
  params
}: {
  params: Promise<{ slug: string }>
}): Promise<Metadata> {
  const { slug } = await params;

  try {
    const settings = await getStoreSettings(slug);

    return {
      title: {
        template: settings.seoDefaults?.titleTemplate || `%s | ${settings.storeName}`,
        default: settings.storeName,
      },
      description: settings.seoDefaults?.defaultDescription || `Welcome to ${settings.storeName}`,
      openGraph: {
        images: settings.seoDefaults?.ogImage ? [settings.seoDefaults.ogImage] : [],
      },
    };
  } catch {
    return {
      title: 'Store Not Found',
    };
  }
}

export default async function StoreLayout({
  children,
  params
}: StoreLayoutProps) {
  const { slug } = await params;

  let settings;
  try {
    settings = await getStoreSettings(slug);
  } catch {
    notFound();
  }

  // Apply theme CSS variables
  const cssVariables = settings.theme?.cssVariables || {};
  const globalStyles = settings.globalStyles || {};

  const style: Record<string, string> = {
    '--primary-color': globalStyles.primaryColor || '#000000',
    '--secondary-color': globalStyles.secondaryColor || '#4B5563',
    '--font-family': globalStyles.fontFamily || 'inherit',
    ...Object.fromEntries(
      Object.entries(cssVariables).map(([key, value]) => [`--theme-${key}`, value])
    ),
  };

  return (
    <div className="store-wrapper min-h-screen flex flex-col" style={style as React.CSSProperties}>
      <StoreHeader settings={settings} storeSlug={slug} />
      <main className="flex-1">
        {children}
      </main>
    </div>
  );
}
