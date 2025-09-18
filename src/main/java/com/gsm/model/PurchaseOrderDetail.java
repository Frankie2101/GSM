package com.gsm.model;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;

/**
 * Represents a single line item within a {@link PurchaseOrder}.
 * Each line corresponds to a specific raw material being purchased.
 */
@Entity
@Table(name = "PurchaseOrderDetail")
@Getter
@Setter
public class PurchaseOrderDetail extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long purchaseOrderDetailId;

    /**
     * The parent PurchaseOrder to which this detail line belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PurchaseOrderId", nullable = false)
    private PurchaseOrder purchaseOrder;

    /**
     * A direct link back to the specific OrderBOMDetail line that generated this purchase requirement.
     * This provides excellent traceability from procurement back to the original demand.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderBOMDetailId", nullable = false)
    private OrderBOMDetail orderBOMDetail;

    /**
     * The quantity of the material being purchased.
     */
    @Column(nullable = false)
    private Double purchaseQuantity;

    /**
     * The quantity of the material that has been physically received into inventory.
     * Defaults to 0 when the line is created.
     */
    @Column(name = "ReceivedQuantity")
    private Double receivedQuantity = 0.0;

    /**
     * The price per unit for the material in this specific PO.
     */
    @Column(name = "NetPrice", nullable = false)
    private Double netPrice;

    /**
     * The tax rate (e.g., VAT) applied to this line item, as a percentage.
     * Defaults to 0 if no tax is applied.
     */
    @Column(name = "TaxRate")
    private Double taxRate = 0.0;
}