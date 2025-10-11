package com.gsm.model;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the core Purchase Order (PO) entity.
 * This class maps to the "PurchaseOrder" table and acts as the header
 * for an order placed with a supplier for raw materials.
 */
@Entity
@Table(name = "PurchaseOrder")
@Getter
@Setter
public class PurchaseOrder extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long purchaseOrderId;

    /**
     * The unique, business-logic-driven number for the purchase order.
     */
    @Column(name = "PurchaseOrderNo", length = 100, nullable = false, unique = true)
    private String purchaseOrderNo;

    /**
     * The supplier from whom the materials are being purchased.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SupplierId", nullable = false)
    private Supplier supplier;

    /**
     * The date the purchase order was created.
     */
    @Column(name = "PoDate", nullable = false)
    private LocalDate poDate;

    /**
     * The currency code for this purchase order (e.g., "VND", "USD").
     */
    @Column(name = "CurrencyCode", length = 3)
    private String currencyCode;

    /**
     * Commercial term specifying delivery conditions (e.g., "FOB", "CIF").
     */
    @Column(name = "DeliveryTerm", length = 100)
    private String deliveryTerm;

    /**
     * Commercial term specifying payment conditions (e.g., "T/T in 30 days").
     */
    @Column(name = "PaymentTerm", length = 100)
    private String paymentTerm;

    /**
     * The expected arrival date of the materials.
     */
    @Column(name = "ArrivalDate")
    private LocalDate arrivalDate;

    /**
     * The current status of the purchase order (e.g., "New", "Submitted", "Approved").
     */
    @Column(name = "Status", nullable = false, length = 50)
    private String status = "New";

    /**
     * A list of all detail lines (individual materials) in this purchase order.
     */
    @OneToMany(
            mappedBy = "purchaseOrder",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PurchaseOrderDetail> details = new ArrayList<>();

    /**
     * Helper method to add a new detail line and maintain the bidirectional relationship.
     * @param detail The {@link PurchaseOrderDetail} to add.
     */
    public void addDetail(PurchaseOrderDetail detail) {
        details.add(detail);
        detail.setPurchaseOrder(this);
    }

    /**
     * Helper method to remove a detail line and synchronize the relationship.
     * @param detail The {@link PurchaseOrderDetail} to remove.
     */
    public void removeDetail(PurchaseOrderDetail detail) {
        details.remove(detail);
        detail.setPurchaseOrder(null);
    }
}
