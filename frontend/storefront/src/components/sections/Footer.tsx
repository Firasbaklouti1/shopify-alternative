import React from 'react';
import Link from 'next/link';
import type { SectionProps } from '../LayoutRenderer';

interface FooterLink {
  label: string;
  url: string;
}

interface FooterColumn {
  title: string;
  links: FooterLink[];
}

interface FooterSettings {
  logo_url?: string;
  tagline?: string;
  columns?: FooterColumn[];
  show_social?: boolean;
  social_facebook?: string;
  social_instagram?: string;
  social_twitter?: string;
  social_youtube?: string;
  show_payment_icons?: boolean;
  copyright_text?: string;
  background_color?: string;
  text_color?: string;
}

function SocialIcon({ platform, url }: { platform: string; url: string }) {
  const icons: Record<string, React.ReactNode> = {
    facebook: (
      <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
        <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/>
      </svg>
    ),
    instagram: (
      <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
        <path d="M12.315 2c2.43 0 2.784.013 3.808.06 1.064.049 1.791.218 2.427.465a4.902 4.902 0 011.772 1.153 4.902 4.902 0 011.153 1.772c.247.636.416 1.363.465 2.427.048 1.067.06 1.407.06 4.123v.08c0 2.643-.012 2.987-.06 4.043-.049 1.064-.218 1.791-.465 2.427a4.902 4.902 0 01-1.153 1.772 4.902 4.902 0 01-1.772 1.153c-.636.247-1.363.416-2.427.465-1.067.048-1.407.06-4.123.06h-.08c-2.643 0-2.987-.012-4.043-.06-1.064-.049-1.791-.218-2.427-.465a4.902 4.902 0 01-1.772-1.153 4.902 4.902 0 01-1.153-1.772c-.247-.636-.416-1.363-.465-2.427-.047-1.024-.06-1.379-.06-3.808v-.63c0-2.43.013-2.784.06-3.808.049-1.064.218-1.791.465-2.427a4.902 4.902 0 011.153-1.772A4.902 4.902 0 015.45 2.525c.636-.247 1.363-.416 2.427-.465C8.901 2.013 9.256 2 11.685 2h.63zm-.081 1.802h-.468c-2.456 0-2.784.011-3.807.058-.975.045-1.504.207-1.857.344-.467.182-.8.398-1.15.748-.35.35-.566.683-.748 1.15-.137.353-.3.882-.344 1.857-.047 1.023-.058 1.351-.058 3.807v.468c0 2.456.011 2.784.058 3.807.045.975.207 1.504.344 1.857.182.466.399.8.748 1.15.35.35.683.566 1.15.748.353.137.882.3 1.857.344 1.054.048 1.37.058 4.041.058h.08c2.597 0 2.917-.01 3.96-.058.976-.045 1.505-.207 1.858-.344.466-.182.8-.398 1.15-.748.35-.35.566-.683.748-1.15.137-.353.3-.882.344-1.857.048-1.055.058-1.37.058-4.041v-.08c0-2.597-.01-2.917-.058-3.96-.045-.976-.207-1.505-.344-1.858a3.097 3.097 0 00-.748-1.15 3.098 3.098 0 00-1.15-.748c-.353-.137-.882-.3-1.857-.344-1.023-.047-1.351-.058-3.807-.058zM12 6.865a5.135 5.135 0 110 10.27 5.135 5.135 0 010-10.27zm0 1.802a3.333 3.333 0 100 6.666 3.333 3.333 0 000-6.666zm5.338-3.205a1.2 1.2 0 110 2.4 1.2 1.2 0 010-2.4z"/>
      </svg>
    ),
    twitter: (
      <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
        <path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z"/>
      </svg>
    ),
    youtube: (
      <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
        <path d="M23.498 6.186a3.016 3.016 0 00-2.122-2.136C19.505 3.545 12 3.545 12 3.545s-7.505 0-9.377.505A3.017 3.017 0 00.502 6.186C0 8.07 0 12 0 12s0 3.93.502 5.814a3.016 3.016 0 002.122 2.136c1.871.505 9.376.505 9.376.505s7.505 0 9.377-.505a3.015 3.015 0 002.122-2.136C24 15.93 24 12 24 12s0-3.93-.502-5.814zM9.545 15.568V8.432L15.818 12l-6.273 3.568z"/>
      </svg>
    ),
  };

  return (
    <a
      href={url}
      target="_blank"
      rel="noopener noreferrer"
      className="hover:opacity-70 transition-opacity"
    >
      {icons[platform]}
    </a>
  );
}

export default function Footer({ section }: SectionProps) {
  const settings = section.settings as FooterSettings;

  const {
    logo_url,
    tagline,
    columns = [],
    show_social = true,
    social_facebook,
    social_instagram,
    social_twitter,
    social_youtube,
    show_payment_icons = true,
    copyright_text,
    background_color = '#111827',
    text_color = '#ffffff',
  } = settings;

  const defaultColumns: FooterColumn[] = columns.length > 0 ? columns : [
    {
      title: 'Shop',
      links: [
        { label: 'All Products', url: '/products' },
        { label: 'Collections', url: '/collections' },
        { label: 'Sale', url: '/collections/sale' },
      ],
    },
    {
      title: 'Help',
      links: [
        { label: 'Contact Us', url: '/pages/contact' },
        { label: 'Shipping', url: '/pages/shipping' },
        { label: 'Returns', url: '/pages/returns' },
      ],
    },
    {
      title: 'About',
      links: [
        { label: 'Our Story', url: '/pages/about' },
        { label: 'Blog', url: '/blog' },
        { label: 'Careers', url: '/pages/careers' },
      ],
    },
  ];

  const socialLinks = [
    { platform: 'facebook', url: social_facebook },
    { platform: 'instagram', url: social_instagram },
    { platform: 'twitter', url: social_twitter },
    { platform: 'youtube', url: social_youtube },
  ].filter((s) => s.url);

  const currentYear = new Date().getFullYear();
  const copyrightDisplay = copyright_text || `© ${currentYear} Store. All rights reserved.`;

  return (
    <footer
      className="py-12 px-6 md:px-12"
      style={{ backgroundColor: background_color, color: text_color }}
    >
      <div className="max-w-6xl mx-auto">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-8 mb-12">
          {/* Brand Column */}
          <div className="col-span-2 md:col-span-1">
            {logo_url ? (
              <img src={logo_url} alt="Logo" className="h-8 mb-4" />
            ) : (
              <div className="text-xl font-bold mb-4">Store</div>
            )}
            {tagline && (
              <p className="text-sm opacity-80">{tagline}</p>
            )}
          </div>

          {/* Link Columns */}
          {defaultColumns.map((column, index) => (
            <div key={index}>
              <h4 className="font-semibold mb-4">{column.title}</h4>
              <ul className="space-y-2">
                {column.links.map((link, linkIndex) => (
                  <li key={linkIndex}>
                    <Link
                      href={link.url}
                      className="text-sm opacity-80 hover:opacity-100 transition-opacity"
                    >
                      {link.label}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        {/* Bottom Section */}
        <div className="border-t border-white/20 pt-8 flex flex-col md:flex-row justify-between items-center gap-4">
          <p className="text-sm opacity-80">{copyrightDisplay}</p>

          {show_social && socialLinks.length > 0 && (
            <div className="flex gap-4">
              {socialLinks.map((social) => (
                <SocialIcon
                  key={social.platform}
                  platform={social.platform}
                  url={social.url!}
                />
              ))}
            </div>
          )}

          {show_payment_icons && (
            <div className="flex gap-2">
              <span className="text-xs px-2 py-1 bg-white/10 rounded">Visa</span>
              <span className="text-xs px-2 py-1 bg-white/10 rounded">Mastercard</span>
              <span className="text-xs px-2 py-1 bg-white/10 rounded">PayPal</span>
              <span className="text-xs px-2 py-1 bg-white/10 rounded">Apple Pay</span>
            </div>
          )}
        </div>
      </div>
    </footer>
  );
}
