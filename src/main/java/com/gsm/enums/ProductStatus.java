package com.gsm.enums;

/**
 * Defines the lifecycle statuses for a {@link com.gsm.model.Product}.
 * This enum ensures type safety and restricts the status to a predefined set of values.
 */
public enum ProductStatus {

    /**
     * The product is currently being produced and is available for sale.
     */
    Active,

    /**
     * The product is no longer in production, but remaining stock may still be sold.
     * It should not be included in new sale orders.
     */
    Discontinued,

    /**
     * The product is outdated and no longer used or sold.
     * It is kept for historical data purposes only.
     */
    Obsolete
}