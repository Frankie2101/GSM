package com.gsm.model;

import com.gsm.enums.SaleOrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the core Sale Order entity, acting as the header for a customer's order.
 * It maps to the "SaleOrder" table and holds all general information for an order.
 */
@Entity
@Table(name = "SaleOrder")
@Getter
@Setter
@NoArgsConstructor
public class SaleOrder extends AuditableEntity {

    /**
     * The unique identifier for the Sale Order.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SaleOrderId")
    private Long saleOrderId;

    /**
     * The unique, business-logic-driven number for the sale order (e.g., CUST-00001).
     */
    @Column(name = "SaleOrderNo", nullable = false, unique = true, length = 50)
    private String saleOrderNo;

    /**
     * The customer who placed the order. Loaded lazily for performance.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerId", referencedColumnName = "CustomerId", nullable = false)
    private Customer customer;

    /**
     * The date the order was placed.
     */
    @Column(name = "OrderDate", nullable = false)
    private LocalDate orderDate;

    /**
     * The planned date for shipping the order to the customer.
     */
    @Column(name = "ShipDate")
    private LocalDate shipDate;

    /**
     * The customer's own Purchase Order number for their reference.
     */
    @Column(name = "CustomerPO", length = 100)
    private String customerPO;

    /**
     * The currency code for this order (e.g., "VND", "USD").
     */
    @Column(name = "CurrencyCode", length = 3)
    private String currencyCode;

    /**
     * The planned start date for production of this order.
     */
    @Column(name = "ProductionStartDate")
    private LocalDate productionStartDate;

    /**
     * The planned end date for production of this order.
     */
    @Column(name = "ProductionEndDate")
    private LocalDate productionEndDate;

    /**
     * The current status of the order (e.g., New, InProgress, Shipped).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private SaleOrderStatus status;

    /**
     * A list of all detail lines associated with this sale order.
     * `orphanRemoval=true` ensures that if a detail is removed from this list,
     * it is also deleted from the database.
     */
    @OneToMany(mappedBy = "saleOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SaleOrderDetail> details = new ArrayList<>();

    /**
     * Helper method to add a new detail line, ensuring the
     * bidirectional relationship is correctly maintained.
     * @param detail The {@link SaleOrderDetail} to add.
     */
    public void addDetail(SaleOrderDetail detail) {
        this.details.add(detail);
        detail.setSaleOrder(this);
    }

    /**
     * Helper method to remove a detail line and synchronize
     * the bidirectional relationship.
     * @param detail The {@link SaleOrderDetail} to remove.
     */
    public void removeDetail(SaleOrderDetail detail) {
        this.details.remove(detail);
        detail.setSaleOrder(null);
    }
}
