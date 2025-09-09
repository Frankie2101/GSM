package com.gsm.model;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;

@Entity
@Table(name = "PurchaseOrderDetail")
@Getter
@Setter
public class PurchaseOrderDetail extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long purchaseOrderDetailId;

    // Mối quan hệ nhiều-một với PurchaseOrder (bảng cha)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PurchaseOrderId", nullable = false)
    private PurchaseOrder purchaseOrder;

    // Mối quan hệ một-một (hoặc nhiều-một) để truy vết lại dòng BOM gốc đã tạo ra nó
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderBOMDetailId", nullable = false)
    private OrderBOMDetail orderBOMDetail;

    @Column(nullable = false)
    private Double purchaseQuantity;

    @Column(name = "ReceivedQuantity")
    private Double receivedQuantity = 0.0; // Mặc định là 0 khi mới tạo

    @Column(name = "NetPrice", nullable = false)
    private Double netPrice;

    @Column(name = "TaxRate")
    private Double taxRate = 0.0; // Mặc định là 0 nếu không có thuế
}