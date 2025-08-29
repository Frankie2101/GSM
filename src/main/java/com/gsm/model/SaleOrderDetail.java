package com.gsm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "SaleOrderDetail")
@Getter
@Setter
@NoArgsConstructor
public class SaleOrderDetail extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SaleOrderDetailId")
    private Long saleOrderDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SaleOrderId", nullable = false)
    private SaleOrder saleOrder;

    // SỬA LẠI: Liên kết trực tiếp đến ProductVariant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductVariantId", nullable = false)
    private ProductVariant productVariant;

    @Column(name = "OrderQuantity", nullable = false)
    private Integer orderQuantity;

    @Column(name = "ShipQuantity") // Tên cột trong DB của bạn
    private Integer shipQuantity;

    @Column(name = "Price", nullable = false)
    private Double price;
}
