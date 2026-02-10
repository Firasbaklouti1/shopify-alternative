import type { Config } from '@measured/puck';

import HeroBanner from '../components/sections/HeroBanner';
import ProductGrid from '../components/sections/ProductGrid';
import ProductMain from '../components/sections/ProductMain';
import CollectionList from '../components/sections/CollectionList';
import CollectionFilters from '../components/sections/CollectionFilters';
import RichText from '../components/sections/RichText';
import ImageWithText from '../components/sections/ImageWithText';
import Newsletter from '../components/sections/Newsletter';
import Testimonials from '../components/sections/Testimonials';
import AnnouncementBar from '../components/sections/AnnouncementBar';
import Footer from '../components/sections/Footer';
import AppBlock from '../components/sections/AppBlock';

/* eslint-disable @typescript-eslint/no-explicit-any */

export const puckConfig: Config = {
  categories: {
    hero: {
      title: 'Hero',
      components: ['HeroBanner', 'AnnouncementBar'],
    },
    content: {
      title: 'Content',
      components: ['RichText', 'ImageWithText', 'Newsletter'],
    },
    commerce: {
      title: 'Commerce',
      components: ['ProductGrid', 'ProductMain', 'CollectionList', 'CollectionFilters'],
    },
    socialProof: {
      title: 'Social Proof',
      components: ['Testimonials'],
    },
    layout: {
      title: 'Layout',
      components: ['Footer'],
    },
    integrations: {
      title: 'Integrations',
      components: ['AppBlock'],
      defaultExpanded: false,
    },
  },
  components: {
    HeroBanner: {
      label: 'Hero Banner',
      fields: {
        title: { type: 'text', label: 'Title' },
        subtitle: { type: 'textarea', label: 'Subtitle' },
        bg_image: { type: 'text', label: 'Background Image URL' },
        bg_video: { type: 'text', label: 'Background Video URL' },
        cta_text: { type: 'text', label: 'Button Text' },
        cta_link: { type: 'text', label: 'Button Link' },
        secondary_cta_text: { type: 'text', label: 'Secondary Button Text' },
        secondary_cta_link: { type: 'text', label: 'Secondary Button Link' },
        overlay_opacity: { type: 'number', label: 'Overlay Opacity', min: 0, max: 1 },
        text_color: {
          type: 'select',
          label: 'Text Color',
          options: [
            { label: 'Light', value: 'light' },
            { label: 'Dark', value: 'dark' },
          ],
        },
        text_alignment: {
          type: 'select',
          label: 'Text Alignment',
          options: [
            { label: 'Left', value: 'left' },
            { label: 'Center', value: 'center' },
            { label: 'Right', value: 'right' },
          ],
        },
        height: {
          type: 'select',
          label: 'Height',
          options: [
            { label: 'Small', value: 'small' },
            { label: 'Medium', value: 'medium' },
            { label: 'Large', value: 'large' },
            { label: 'Full Screen', value: 'full' },
          ],
        },
      },
      defaultProps: {
        title: 'Welcome to Our Store',
        cta_text: 'Shop Now',
        cta_link: '/products',
        overlay_opacity: 0.4,
        text_color: 'light',
        text_alignment: 'center',
        height: 'large',
      },
      render: HeroBanner as any,
    },

    AnnouncementBar: {
      label: 'Announcement Bar',
      fields: {
        text: { type: 'text', label: 'Text' },
        link: { type: 'text', label: 'Link URL' },
        link_text: { type: 'text', label: 'Link Text' },
        background_color: { type: 'text', label: 'Background Color' },
        text_color: { type: 'text', label: 'Text Color' },
        dismissible: {
          type: 'radio',
          label: 'Dismissible',
          options: [
            { label: 'Yes', value: true },
            { label: 'No', value: false },
          ],
        },
      },
      defaultProps: {
        text: 'Free shipping on orders over $50!',
        background_color: '#000000',
        text_color: '#ffffff',
        dismissible: true,
      },
      render: AnnouncementBar as any,
    },

    ProductGrid: {
      label: 'Product Grid',
      fields: {
        title: { type: 'text', label: 'Section Title' },
        subtitle: { type: 'text', label: 'Subtitle' },
        collection_handle: { type: 'text', label: 'Collection Handle (leave empty for all)' },
        limit: { type: 'number', label: 'Product Limit', min: 1, max: 24 },
        columns: {
          type: 'select',
          label: 'Columns',
          options: [
            { label: '2', value: 2 },
            { label: '3', value: 3 },
            { label: '4', value: 4 },
            { label: '5', value: 5 },
          ],
        },
        show_price: {
          type: 'radio',
          label: 'Show Price',
          options: [
            { label: 'Yes', value: true },
            { label: 'No', value: false },
          ],
        },
        show_vendor: {
          type: 'radio',
          label: 'Show Vendor',
          options: [
            { label: 'Yes', value: true },
            { label: 'No', value: false },
          ],
        },
        show_sale_badge: {
          type: 'radio',
          label: 'Show Sale Badge',
          options: [
            { label: 'Yes', value: true },
            { label: 'No', value: false },
          ],
        },
        image_ratio: {
          type: 'select',
          label: 'Image Ratio',
          options: [
            { label: 'Square', value: 'square' },
            { label: 'Portrait', value: 'portrait' },
            { label: 'Landscape', value: 'landscape' },
          ],
        },
      },
      defaultProps: {
        title: 'Featured Products',
        limit: 8,
        columns: 4,
        show_price: true,
        show_vendor: false,
        show_sale_badge: true,
        image_ratio: 'square',
      },
      render: ProductGrid as any,
    },

    ProductMain: {
      label: 'Product Detail',
      fields: {
        gallery_position: {
          type: 'select',
          label: 'Gallery Position',
          options: [
            { label: 'Left', value: 'left' },
            { label: 'Right', value: 'right' },
          ],
        },
        show_vendor: {
          type: 'radio',
          label: 'Show Vendor',
          options: [
            { label: 'Yes', value: true },
            { label: 'No', value: false },
          ],
        },
        show_sku: {
          type: 'radio',
          label: 'Show SKU',
          options: [
            { label: 'Yes', value: true },
            { label: 'No', value: false },
          ],
        },
        show_quantity_selector: {
          type: 'radio',
          label: 'Show Quantity Selector',
          options: [
            { label: 'Yes', value: true },
            { label: 'No', value: false },
          ],
        },
      },
      defaultProps: {
        gallery_position: 'left',
        show_vendor: true,
        show_sku: false,
        show_quantity_selector: true,
      },
      render: ProductMain as any,
    },

    CollectionList: {
      label: 'Collection List',
      fields: {
        title: { type: 'text', label: 'Section Title' },
        subtitle: { type: 'text', label: 'Subtitle' },
        columns: {
          type: 'select',
          label: 'Columns',
          options: [
            { label: '2', value: 2 },
            { label: '3', value: 3 },
            { label: '4', value: 4 },
          ],
        },
        limit: { type: 'number', label: 'Collection Limit', min: 1, max: 12 },
        show_product_count: {
          type: 'radio',
          label: 'Show Product Count',
          options: [
            { label: 'Yes', value: true },
            { label: 'No', value: false },
          ],
        },
        image_ratio: {
          type: 'select',
          label: 'Image Ratio',
          options: [
            { label: 'Square', value: 'square' },
            { label: 'Portrait', value: 'portrait' },
            { label: 'Landscape', value: 'landscape' },
          ],
        },
        card_style: {
          type: 'select',
          label: 'Card Style',
          options: [
            { label: 'Overlay', value: 'overlay' },
            { label: 'Below', value: 'below' },
          ],
        },
      },
      defaultProps: {
        title: 'Shop by Collection',
        columns: 3,
        limit: 6,
        show_product_count: true,
        image_ratio: 'square',
        card_style: 'overlay',
      },
      render: CollectionList as any,
    },

    CollectionFilters: {
      label: 'Collection Filters',
      fields: {
        show_sort: {
          type: 'radio',
          label: 'Show Sort',
          options: [
            { label: 'Yes', value: true },
            { label: 'No', value: false },
          ],
        },
        show_filter: {
          type: 'radio',
          label: 'Show Filter',
          options: [
            { label: 'Yes', value: true },
            { label: 'No', value: false },
          ],
        },
        filter_type: {
          type: 'select',
          label: 'Filter Type',
          options: [
            { label: 'Sidebar', value: 'sidebar' },
            { label: 'Dropdown', value: 'dropdown' },
          ],
        },
      },
      defaultProps: {
        show_sort: true,
        show_filter: false,
        filter_type: 'dropdown',
      },
      render: CollectionFilters as any,
    },

    RichText: {
      label: 'Rich Text',
      fields: {
        title: { type: 'text', label: 'Title' },
        heading: { type: 'text', label: 'Heading (alternative)' },
        content: { type: 'textarea', label: 'Content (HTML)' },
        text_alignment: {
          type: 'select',
          label: 'Text Alignment',
          options: [
            { label: 'Left', value: 'left' },
            { label: 'Center', value: 'center' },
            { label: 'Right', value: 'right' },
          ],
        },
        max_width: {
          type: 'select',
          label: 'Max Width',
          options: [
            { label: 'Small', value: 'small' },
            { label: 'Medium', value: 'medium' },
            { label: 'Large', value: 'large' },
            { label: 'Full', value: 'full' },
          ],
        },
        padding: {
          type: 'select',
          label: 'Padding',
          options: [
            { label: 'Small', value: 'small' },
            { label: 'Medium', value: 'medium' },
            { label: 'Large', value: 'large' },
          ],
        },
        background_color: { type: 'text', label: 'Background Color' },
        text_color: { type: 'text', label: 'Text Color' },
      },
      defaultProps: {
        text_alignment: 'center',
        max_width: 'medium',
        padding: 'medium',
      },
      render: RichText as any,
    },

    ImageWithText: {
      label: 'Image with Text',
      fields: {
        image_url: { type: 'text', label: 'Image URL' },
        image_position: {
          type: 'select',
          label: 'Image Position',
          options: [
            { label: 'Left', value: 'left' },
            { label: 'Right', value: 'right' },
          ],
        },
        image_width: {
          type: 'select',
          label: 'Image Width',
          options: [
            { label: 'Small (1/3)', value: 'small' },
            { label: 'Medium (1/2)', value: 'medium' },
            { label: 'Large (2/3)', value: 'large' },
          ],
        },
        title: { type: 'text', label: 'Title' },
        subtitle: { type: 'text', label: 'Subtitle' },
        content: { type: 'textarea', label: 'Content (HTML)' },
        cta_text: { type: 'text', label: 'Button Text' },
        cta_link: { type: 'text', label: 'Button Link' },
        background_color: { type: 'text', label: 'Background Color' },
        text_color: { type: 'text', label: 'Text Color' },
        vertical_alignment: {
          type: 'select',
          label: 'Vertical Alignment',
          options: [
            { label: 'Top', value: 'top' },
            { label: 'Center', value: 'center' },
            { label: 'Bottom', value: 'bottom' },
          ],
        },
      },
      defaultProps: {
        image_position: 'left',
        image_width: 'medium',
        vertical_alignment: 'center',
      },
      render: ImageWithText as any,
    },

    Newsletter: {
      label: 'Newsletter',
      fields: {
        title: { type: 'text', label: 'Title' },
        subtitle: { type: 'textarea', label: 'Subtitle' },
        placeholder: { type: 'text', label: 'Email Placeholder' },
        button_text: { type: 'text', label: 'Button Text' },
        success_message: { type: 'text', label: 'Success Message' },
        background_color: { type: 'text', label: 'Background Color' },
        text_color: { type: 'text', label: 'Text Color' },
        text_alignment: {
          type: 'select',
          label: 'Text Alignment',
          options: [
            { label: 'Left', value: 'left' },
            { label: 'Center', value: 'center' },
          ],
        },
        layout: {
          type: 'select',
          label: 'Layout',
          options: [
            { label: 'Inline', value: 'inline' },
            { label: 'Stacked', value: 'stacked' },
          ],
        },
      },
      defaultProps: {
        title: 'Join our newsletter',
        subtitle: 'Subscribe to get special offers, free giveaways, and once-in-a-lifetime deals.',
        placeholder: 'Enter your email',
        button_text: 'Subscribe',
        success_message: 'Thanks for subscribing!',
        text_alignment: 'center',
        layout: 'inline',
      },
      render: Newsletter as any,
    },

    Testimonials: {
      label: 'Testimonials',
      fields: {
        title: { type: 'text', label: 'Title' },
        subtitle: { type: 'text', label: 'Subtitle' },
        layout: {
          type: 'select',
          label: 'Layout',
          options: [
            { label: 'Grid', value: 'grid' },
            { label: 'Carousel', value: 'carousel' },
          ],
        },
        columns: {
          type: 'select',
          label: 'Columns',
          options: [
            { label: '2', value: 2 },
            { label: '3', value: 3 },
          ],
        },
        show_rating: {
          type: 'radio',
          label: 'Show Rating',
          options: [
            { label: 'Yes', value: true },
            { label: 'No', value: false },
          ],
        },
        background_color: { type: 'text', label: 'Background Color' },
        testimonials: {
          type: 'array',
          label: 'Testimonials',
          arrayFields: {
            author: { type: 'text', label: 'Author' },
            role: { type: 'text', label: 'Role' },
            content: { type: 'textarea', label: 'Quote' },
            avatar: { type: 'text', label: 'Avatar URL' },
            rating: { type: 'number', label: 'Rating (1-5)', min: 1, max: 5 },
          },
          getItemSummary: (item: any) => item.author || 'Testimonial',
          defaultItemProps: { author: 'Customer', content: 'Great product!', rating: 5 },
        },
      },
      defaultProps: {
        title: 'What Our Customers Say',
        layout: 'grid',
        columns: 3,
        show_rating: true,
      },
      render: Testimonials as any,
    },

    Footer: {
      label: 'Footer',
      fields: {
        logo_url: { type: 'text', label: 'Logo URL' },
        tagline: { type: 'text', label: 'Tagline' },
        show_social: {
          type: 'radio',
          label: 'Show Social Links',
          options: [
            { label: 'Yes', value: true },
            { label: 'No', value: false },
          ],
        },
        social_facebook: { type: 'text', label: 'Facebook URL' },
        social_instagram: { type: 'text', label: 'Instagram URL' },
        social_twitter: { type: 'text', label: 'Twitter/X URL' },
        social_youtube: { type: 'text', label: 'YouTube URL' },
        show_payment_icons: {
          type: 'radio',
          label: 'Show Payment Icons',
          options: [
            { label: 'Yes', value: true },
            { label: 'No', value: false },
          ],
        },
        copyright_text: { type: 'text', label: 'Copyright Text' },
        background_color: { type: 'text', label: 'Background Color' },
        text_color: { type: 'text', label: 'Text Color' },
        columns: {
          type: 'array',
          label: 'Link Columns',
          arrayFields: {
            title: { type: 'text', label: 'Column Title' },
            links: {
              type: 'array',
              label: 'Links',
              arrayFields: {
                label: { type: 'text', label: 'Label' },
                url: { type: 'text', label: 'URL' },
              },
              getItemSummary: (item: any) => item.label || 'Link',
            },
          },
          getItemSummary: (item: any) => item.title || 'Column',
        },
      },
      defaultProps: {
        show_social: true,
        show_payment_icons: true,
        background_color: '#111827',
        text_color: '#ffffff',
      },
      render: Footer as any,
    },

    AppBlock: {
      label: 'App Block',
      fields: {
        app_id: { type: 'text', label: 'App ID' },
        script_url: { type: 'text', label: 'Script URL' },
        tag_name: { type: 'text', label: 'Custom Element Tag Name' },
      },
      defaultProps: {},
      render: AppBlock as any,
    },
  },

  root: {
    fields: {
      title: { type: 'text', label: 'Page Title' },
    },
    render: ({ children }: { children: React.ReactNode }) => {
      return <div className="layout-renderer">{children}</div>;
    },
  },
};
