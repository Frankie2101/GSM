package com.gsm.model;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Represents a specific variant of a {@link Product}.
 * <p>
 * Each variant is a unique, purchasable item defined by attributes like
 * color, size, and a unique SKU. It is the "child" in the one-to-many
 * relationship with Product.
 */
@Entity
@Table(name = "ProductVariant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant extends AuditableEntity {

    /**
     * The unique identifier for the ProductVariant.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductVariantId")
    private Long productVariantId;

    /**
     * The color of this specific product variant. E.g., "White", "Navy Blue".
     */
    @Column(name = "Color", nullable = false, length = 50)
    private String color;

    /**
     * The color of this specific product variant. E.g., "White", "Navy Blue".
     */
    @Column(name = "[Size]", nullable = false, length = 20)
    private String size;

    /**
     * The Stock Keeping Unit (SKU), a unique identifier for this specific variant.
     * Enforced by a unique constraint at the database level. E.g., "POLO-WHT-S".
     */
    @Column(name = "SKU", nullable = false, unique = true, length = 100)
    private String sku;

    /**
     * The selling price of this variant.
     */
    @Column(name = "Price")
    private Double price;

    /**
     * The parent product to which this variant belongs.
     * This is the "owning" side of the many-to-one relationship.
     * FetchType.LAZY is used for performance.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductId", nullable = false)
    private Product product;

    /**
     * The currency code for the price. E.g., "VND", "USD".
     */
    @Column(name = "Currency", length = 3)
    private String currency;
}