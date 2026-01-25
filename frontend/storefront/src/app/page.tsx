import Link from 'next/link';

export default function Home() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gradient-to-br from-gray-900 to-gray-800 text-white px-6">
      <div className="max-w-2xl text-center">
        <h1 className="text-5xl md:text-6xl font-bold mb-6">
          Storefront
        </h1>
        <p className="text-xl text-gray-300 mb-8">
          A Next.js-powered storefront renderer for JSON-driven e-commerce layouts.
        </p>

        <div className="bg-gray-800/50 rounded-lg p-6 text-left mb-8">
          <h2 className="text-lg font-semibold mb-3">How it works:</h2>
          <ul className="space-y-2 text-gray-300">
            <li className="flex gap-2">
              <span className="text-green-400">→</span>
              Visit <code className="bg-gray-700 px-2 py-0.5 rounded text-sm">/store/[slug]</code> to view a store
            </li>
            <li className="flex gap-2">
              <span className="text-green-400">→</span>
              The storefront fetches JSON layouts from the Spring Boot API
            </li>
            <li className="flex gap-2">
              <span className="text-green-400">→</span>
              Layouts are rendered using React Server Components
            </li>
          </ul>
        </div>

        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <Link
            href="/store/demo"
            className="px-8 py-3 bg-white text-gray-900 font-semibold rounded-md hover:bg-gray-100 transition-colors"
          >
            View Demo Store
          </Link>
          <a
            href="http://localhost:8080/swagger-ui.html"
            target="_blank"
            rel="noopener noreferrer"
            className="px-8 py-3 border border-white/30 font-semibold rounded-md hover:bg-white/10 transition-colors"
          >
            API Documentation
          </a>
        </div>
      </div>

      <footer className="absolute bottom-6 text-gray-500 text-sm">
        Powered by Next.js + Spring Boot
      </footer>
    </div>
  );
}
