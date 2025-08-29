package com.gsm.model;

import com.gsm.enums.SaleOrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SaleOrder")
@Getter
@Setter
@NoArgsConstructor
public class SaleOrder extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SaleOrderId")
    private Long saleOrderId;

    @Column(name = "SaleOrderNo", nullable = false, unique = true, length = 50)
    private String saleOrderNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerId", referencedColumnName = "CustomerId", nullable = false)
    private Customer customer;

    @Column(name = "OrderDate", nullable = false)
    private LocalDate orderDate;

    @Column(name = "ShipDate")
    private LocalDate shipDate;

    // --- THÊM CÁC TRƯỜNG CÒN THIẾU ---
    @Column(name = "CustomerPO", length = 100)
    private String customerPO;

    @Column(name = "CurrencyCode", length = 3)
    private String currencyCode;

    @Column(name = "ProductionStartDate")
    private LocalDate productionStartDate;

    @Column(name = "ProductionEndDate")
    private LocalDate productionEndDate;
    // ------------------------------------

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private SaleOrderStatus status;

    @OneToMany(mappedBy = "saleOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SaleOrderDetail> details = new ArrayList<>();

    public void addDetail(SaleOrderDetail detail) {
        this.details.add(detail);
        detail.setSaleOrder(this);
    }

    /**
     * Phương thức tiện ích để xóa một chi tiết đơn hàng
     * và tự động đồng bộ hóa mối quan hệ hai chiều.
     */
    public void removeDetail(SaleOrderDetail detail) {
        this.details.remove(detail);
        detail.setSaleOrder(null);
    }
}
