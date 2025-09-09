package com.gsm.model;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PurchaseOrder")
@Getter
@Setter
public class PurchaseOrder extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long purchaseOrderId;

    // === THÊM MỚI: Trường để lưu số PO ===
    @Column(length = 100, nullable = false, unique = true)
    private String purchaseOrderNo;
    // ===================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SupplierId", nullable = false)
    private Supplier supplier;

    @Column(name = "PoDate", nullable = false)
    private LocalDate poDate;

    @Column(length = 3)
    private String currencyCode;

    @Column(length = 100)
    private String deliveryTerm;

    @Column(length = 100)
    private String paymentTerm;

    private LocalDate arrivalDate;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "New";

    @OneToMany(
            mappedBy = "purchaseOrder",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PurchaseOrderDetail> details = new ArrayList<>();

    public void addDetail(PurchaseOrderDetail detail) {
        details.add(detail);
        detail.setPurchaseOrder(this);
    }

    public void removeDetail(PurchaseOrderDetail detail) {
        details.remove(detail);
        detail.setPurchaseOrder(null);
    }
}
