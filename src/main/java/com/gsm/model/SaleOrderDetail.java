package com.gsm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * Represents a single line item within a {@link SaleOrder}.
 * This entity uses a normalized structure, where each unique size of a
 * product variant is stored as a separate row.
 */
@Entity
@Table(name = "SaleOrderDetail")
@Getter
@Setter
@NoArgsConstructor
public class SaleOrderDetail extends AuditableEntity {

    /**
     * The unique identifier for the Sale Order Detail.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SaleOrderDetailId")
    private Long saleOrderDetailId;

    /**
     * The parent SaleOrder to which this detail line belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SaleOrderId", nullable = false)
    private SaleOrder saleOrder;

    /**
     * The specific product variant (product + color + size) for this detail line.
     * This establishes the link to the exact item being ordered.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductVariantId", nullable = false)
    private ProductVariant productVariant;

    /**
     * The quantity of this specific variant ordered by the customer.
     */
    @Column(name = "OrderQuantity", nullable = false)
    private Integer orderQuantity;

    /**
     * The quantity of this specific variant that has been shipped.
     */
    @Column(name = "ShipQuantity")
    private Integer shipQuantity;

    /**
     * The price per unit for this specific variant in this order.
     */
    @Column(name = "Price", nullable = false)
    private Double price;
}
