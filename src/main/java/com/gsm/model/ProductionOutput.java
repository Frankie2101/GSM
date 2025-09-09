package com.gsm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "ProductionOutput")
@Getter
@Setter
@NoArgsConstructor
public class ProductionOutput extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductionOutputId")
    private Long productionOutputId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SaleOrderId", nullable = false)
    private SaleOrder saleOrder;

    @Column(name = "Style", nullable = false, columnDefinition = "NVARCHAR(100)")
    private String style;

    @Column(name = "Color", nullable = false, columnDefinition = "NVARCHAR(50)")
    private String color;

    // ĐÃ XÓA CỘT SIZE

    @Column(name = "Department", columnDefinition = "NVARCHAR(100)")
    private String department;

    @Column(name = "ProductionLine", columnDefinition = "NVARCHAR(100)")
    private String productionLine;

    @Column(name = "OutputDate", nullable = false)
    private LocalDate outputDate;

    @Column(name = "OutputQuantity", nullable = false)
    private Integer outputQuantity;
}

