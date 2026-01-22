package com.firas.saas.storefront.entity;

/**
 * Defines the types of pages that can be customized in a storefront.
 * Each page type has a different default layout and component options.
 */
public enum PageType {
    /**
     * The store's home/landing page
     */
    HOME,

    /**
     * Individual product detail page
     */
    PRODUCT,

    /**
     * Collection/category listing page
     */
    COLLECTION,

    /**
     * Shopping cart page
     */
    CART,

    /**
     * Checkout page (handles payment and shipping)
     */
    CHECKOUT,

    /**
     * Custom pages (e.g., About Us, Contact, FAQ)
     */
    CUSTOM
}
