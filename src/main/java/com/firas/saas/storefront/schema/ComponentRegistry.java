package com.firas.saas.storefront.schema;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Registry of available section and block components.
 * This defines what components merchants can use in their storefronts.
 *
 * Used for:
 * 1. Validation of layout JSON
 * 2. Populating the drag-and-drop editor
 * 3. AI generation prompts (future)
 */
@Component
public class ComponentRegistry {

    private final Map<String, SectionSchema> sections = new LinkedHashMap<>();

    public ComponentRegistry() {
        registerCoreComponents();
    }

    /**
     * Get all registered section schemas
     */
    public List<SectionSchema> getAllSections() {
        return new ArrayList<>(sections.values());
    }

    /**
     * Get section schema by type
     */
    public Optional<SectionSchema> getSection(String type) {
        return Optional.ofNullable(sections.get(type));
    }

    /**
     * Check if section type exists
     */
    public boolean hasSection(String type) {
        return sections.containsKey(type);
    }

    /**
     * Get sections available for a specific page type
     */
    public List<SectionSchema> getSectionsForPageType(String pageType) {
        return sections.values().stream()
                .filter(s -> s.getAllowedPageTypes() == null ||
                             s.getAllowedPageTypes().isEmpty() ||
                             s.getAllowedPageTypes().contains(pageType) ||
                             s.getAllowedPageTypes().contains("*"))
                .toList();
    }

    /**
     * Register core platform components
     */
    private void registerCoreComponents() {
        // Hero Banner
        sections.put("hero-banner", SectionSchema.builder()
                .type("hero-banner")
                .name("Hero Banner")
                .description("Large banner with image/video background and call-to-action")
                .icon("image")
                .allowedPageTypes(List.of("*"))
                .maxPerPage(0)
                .settings(List.of(
                        setting("title", "text", "Title", "Main heading text", "Welcome to our store", true),
                        setting("subtitle", "textarea", "Subtitle", "Supporting text below title", "", false),
                        setting("bg_image", "image", "Background Image", "Hero background image URL", "", true),
                        setting("bg_video", "url", "Background Video", "Optional video URL (overrides image)", "", false),
                        setting("overlay_opacity", "range", "Overlay Opacity", "Dark overlay strength", 0.4, false,
                                Map.of("min", 0, "max", 1, "step", 0.1)),
                        setting("cta_text", "text", "Button Text", "Call-to-action button text", "Shop Now", false),
                        setting("cta_link", "url", "Button Link", "Button destination URL", "/collections/all", false),
                        setting("text_alignment", "select", "Text Alignment", "Horizontal alignment", "center", false,
                                Map.of("options", List.of("left", "center", "right"))),
                        setting("height", "select", "Section Height", "Hero section height", "large", false,
                                Map.of("options", List.of("small", "medium", "large", "fullscreen")))
                ))
                .build());

        // Product Grid
        sections.put("product-grid", SectionSchema.builder()
                .type("product-grid")
                .name("Product Grid")
                .description("Grid of products from a collection or featured products")
                .icon("grid")
                .allowedPageTypes(List.of("*"))
                .maxPerPage(0)
                .settings(List.of(
                        setting("title", "text", "Section Title", "Heading above the grid", "Featured Products", false),
                        setting("collection_handle", "text", "Collection Handle", "Slug of collection to display (leave empty for featured)", "", false),
                        setting("limit", "number", "Product Limit", "Maximum products to show", 8, false,
                                Map.of("min", 1, "max", 24)),
                        setting("columns", "select", "Columns", "Number of columns on desktop", 4, false,
                                Map.of("options", List.of(2, 3, 4, 5))),
                        setting("show_price", "checkbox", "Show Price", "Display product prices", true, false, null),
                        setting("show_vendor", "checkbox", "Show Vendor", "Display product vendor", false, false, null),
                        setting("show_rating", "checkbox", "Show Rating", "Display product ratings", false, false, null)
                ))
                .build());

        // Product Main (for product detail pages)
        sections.put("product-main", SectionSchema.builder()
                .type("product-main")
                .name("Product Details")
                .description("Main product information with gallery, price, and add to cart")
                .icon("shopping-bag")
                .allowedPageTypes(List.of("PRODUCT"))
                .maxPerPage(1)
                .settings(List.of(
                        setting("gallery_position", "select", "Gallery Position", "Image gallery placement", "left", false,
                                Map.of("options", List.of("left", "right"))),
                        setting("enable_zoom", "checkbox", "Enable Zoom", "Allow image zoom on hover", true, false, null),
                        setting("show_vendor", "checkbox", "Show Vendor", "Display vendor name", true, false, null)
                ))
                .blocks(List.of(
                        block("title", "Product Title", "type", List.of()),
                        block("price", "Price", "tag", List.of(
                                setting("show_compare_price", "checkbox", "Show Compare Price", "Show original price if on sale", true, false, null)
                        )),
                        block("variant_selector", "Variant Selector", "layers", List.of(
                                setting("picker_type", "select", "Picker Type", "How variants are displayed", "dropdown", false,
                                        Map.of("options", List.of("dropdown", "buttons", "swatches")))
                        )),
                        block("quantity_selector", "Quantity Selector", "plus-circle", List.of()),
                        block("buy_buttons", "Buy Buttons", "shopping-cart", List.of(
                                setting("show_buy_now", "checkbox", "Show Buy Now", "Direct checkout button", true, false, null)
                        )),
                        block("description", "Description", "file-text", List.of(
                                setting("collapsible", "checkbox", "Collapsible", "Show as expandable accordion", false, false, null)
                        )),
                        block("share", "Share Buttons", "share-2", List.of())
                ))
                .build());

        // Collection List
        sections.put("collection-list", SectionSchema.builder()
                .type("collection-list")
                .name("Collection List")
                .description("Grid of collection/category cards")
                .icon("folder")
                .allowedPageTypes(List.of("HOME", "COLLECTION", "CUSTOM"))
                .maxPerPage(0)
                .settings(List.of(
                        setting("title", "text", "Section Title", "Heading above collections", "Shop by Category", false),
                        setting("collection_handles", "text", "Collection Handles", "Comma-separated list of collection slugs", "", false),
                        setting("columns", "select", "Columns", "Grid columns on desktop", 3, false,
                                Map.of("options", List.of(2, 3, 4))),
                        setting("show_count", "checkbox", "Show Product Count", "Display number of products", true, false, null)
                ))
                .build());

        // Rich Text
        sections.put("rich-text", SectionSchema.builder()
                .type("rich-text")
                .name("Rich Text")
                .description("Customizable text content block")
                .icon("type")
                .allowedPageTypes(List.of("*"))
                .maxPerPage(0)
                .settings(List.of(
                        setting("heading", "text", "Heading", "Section heading (optional)", "", false),
                        setting("content", "textarea", "Content", "Rich text content (supports Markdown)", "", true),
                        setting("text_alignment", "select", "Text Alignment", "Content alignment", "center", false,
                                Map.of("options", List.of("left", "center", "right"))),
                        setting("narrow_width", "checkbox", "Narrow Width", "Limit content width for readability", true, false, null)
                ))
                .build());

        // Image with Text
        sections.put("image-with-text", SectionSchema.builder()
                .type("image-with-text")
                .name("Image with Text")
                .description("Side-by-side image and text content")
                .icon("layout")
                .allowedPageTypes(List.of("*"))
                .maxPerPage(0)
                .settings(List.of(
                        setting("image", "image", "Image", "Featured image URL", "", true),
                        setting("image_position", "select", "Image Position", "Image placement", "left", false,
                                Map.of("options", List.of("left", "right"))),
                        setting("heading", "text", "Heading", "Section heading", "", false),
                        setting("content", "textarea", "Content", "Text content", "", true),
                        setting("button_text", "text", "Button Text", "Optional button text", "", false),
                        setting("button_link", "url", "Button Link", "Button destination", "", false),
                        setting("image_ratio", "select", "Image Ratio", "Image aspect ratio", "adapt", false,
                                Map.of("options", List.of("adapt", "square", "portrait", "landscape")))
                ))
                .build());

        // Newsletter
        sections.put("newsletter", SectionSchema.builder()
                .type("newsletter")
                .name("Newsletter Signup")
                .description("Email subscription form")
                .icon("mail")
                .allowedPageTypes(List.of("*"))
                .maxPerPage(1)
                .settings(List.of(
                        setting("heading", "text", "Heading", "Section heading", "Subscribe to our newsletter", false),
                        setting("subheading", "textarea", "Subheading", "Supporting text", "Get the latest updates and offers", false),
                        setting("button_text", "text", "Button Text", "Submit button text", "Subscribe", false),
                        setting("success_message", "text", "Success Message", "Shown after signup", "Thank you for subscribing!", false),
                        setting("background_color", "color", "Background Color", "Section background", "#f5f5f5", false)
                ))
                .build());

        // Testimonials
        sections.put("testimonials", SectionSchema.builder()
                .type("testimonials")
                .name("Testimonials")
                .description("Customer testimonials carousel")
                .icon("message-circle")
                .allowedPageTypes(List.of("*"))
                .maxPerPage(0)
                .settings(List.of(
                        setting("heading", "text", "Heading", "Section heading", "What Our Customers Say", false),
                        setting("auto_rotate", "checkbox", "Auto Rotate", "Automatically cycle through testimonials", true, false, null),
                        setting("rotation_speed", "number", "Rotation Speed", "Seconds between slides", 5, false,
                                Map.of("min", 2, "max", 10))
                ))
                .blocks(List.of(
                        block("testimonial", "Testimonial", "message-square", List.of(
                                setting("quote", "textarea", "Quote", "Customer testimonial text", "", true),
                                setting("author", "text", "Author", "Customer name", "", true),
                                setting("role", "text", "Role/Title", "Customer role or title", "", false),
                                setting("avatar", "image", "Avatar", "Customer photo URL", "", false),
                                setting("rating", "number", "Rating", "Star rating (1-5)", 5, false,
                                        Map.of("min", 1, "max", 5))
                        ))
                ))
                .build());

        // Announcement Bar
        sections.put("announcement-bar", SectionSchema.builder()
                .type("announcement-bar")
                .name("Announcement Bar")
                .description("Top banner for promotions and announcements")
                .icon("bell")
                .allowedPageTypes(List.of("*"))
                .maxPerPage(1)
                .settings(List.of(
                        setting("text", "text", "Announcement Text", "Message to display", "Free shipping on orders over $50!", true),
                        setting("link", "url", "Link", "Optional link URL", "", false),
                        setting("background_color", "color", "Background Color", "Bar background color", "#000000", false),
                        setting("text_color", "color", "Text Color", "Text color", "#ffffff", false),
                        setting("dismissible", "checkbox", "Dismissible", "Allow visitors to dismiss", false, false, null)
                ))
                .build());

        // Footer
        sections.put("footer", SectionSchema.builder()
                .type("footer")
                .name("Footer")
                .description("Site footer with links and information")
                .icon("layout")
                .allowedPageTypes(List.of("*"))
                .maxPerPage(1)
                .settings(List.of(
                        setting("show_social", "checkbox", "Show Social Links", "Display social media icons", true, false, null),
                        setting("show_payment_icons", "checkbox", "Show Payment Icons", "Display accepted payment methods", true, false, null),
                        setting("copyright_text", "text", "Copyright Text", "Copyright notice", "© {{year}} {{store_name}}. All rights reserved.", false)
                ))
                .blocks(List.of(
                        block("link_column", "Link Column", "list", List.of(
                                setting("heading", "text", "Column Heading", "Column title", "", true),
                                setting("links", "textarea", "Links", "One link per line: Label|URL", "", true)
                        ))
                ))
                .build());

        // App Block (for third-party app integrations)
        sections.put("app-block", SectionSchema.builder()
                .type("app-block")
                .name("App Block")
                .description("Third-party app component")
                .icon("puzzle")
                .allowedPageTypes(List.of("*"))
                .maxPerPage(0)
                .settings(List.of(
                        setting("app_id", "text", "App ID", "Installed app identifier", "", true),
                        setting("script_url", "url", "Script URL", "App script URL (auto-filled from app)", "", true),
                        setting("tag_name", "text", "Tag Name", "Web Component tag name", "", true),
                        setting("props", "textarea", "Properties", "JSON properties to pass to component", "{}", false)
                ))
                .build());
    }

    /**
     * Helper to create a setting schema
     */
    private SectionSchema.SettingSchema setting(String id, String type, String label, String description,
                                                  Object defaultValue, boolean required) {
        return setting(id, type, label, description, defaultValue, required, null);
    }

    private SectionSchema.SettingSchema setting(String id, String type, String label, String description,
                                                  Object defaultValue, boolean required, Map<String, Object> validation) {
        return SectionSchema.SettingSchema.builder()
                .id(id)
                .type(type)
                .label(label)
                .description(description)
                .defaultValue(defaultValue)
                .required(required)
                .validation(validation)
                .build();
    }

    /**
     * Helper to create a block schema
     */
    private SectionSchema.BlockSchema block(String type, String name, String icon,
                                             List<SectionSchema.SettingSchema> settings) {
        return SectionSchema.BlockSchema.builder()
                .type(type)
                .name(name)
                .icon(icon)
                .settings(settings)
                .maxPerSection(0)
                .build();
    }
}
