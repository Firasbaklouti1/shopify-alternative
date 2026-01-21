package com.firas.saas.storefront.entity;

/**
 * Defines the checkout behavior for a store.
 * Merchants can choose whether customers must create accounts,
 * can checkout as guests, or both options are available.
 */
public enum CheckoutMode {
    /**
     * Customers can only checkout as guests (email only, no account required)
     */
    GUEST_ONLY,

    /**
     * Customers must create an account or log in to checkout
     */
    ACCOUNT_ONLY,

    /**
     * Customers can choose to checkout as guest or create/use an account
     */
    BOTH
}
