package com.gsm.model; // Giả sử package là model

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;

@Entity
@Table(name = "OrderBOMDetail")
@Getter
@Setter
public class OrderBOMDetail extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderBOMDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderBOMId", nullable = false)
    private OrderBOM orderBOM;

    @Column(nullable = false)
    private Integer seq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaterialGroupId")
    private MaterialGroup materialGroup;

    // THÊM CÁC TRƯỜNG MỚI
    @Column(length = 50)
    private String colorCode;

    @Column(length = 100)
    private String colorName;

    @Column(length = 50)
    private String size;

    @Column(name = "MaterialType", nullable = false, length = 2) // "FA" for Fabric, "TR" for Trim
    private String materialType;

    @Column(length = 100)
    private String materialCode;

    @Column(length = 255)
    private String materialName;

    @Column(length = 50)
    private String uom;

    @Column(nullable = false)
    private Double usageValue;

    @Column(nullable = false)
    private Double waste;

    @Column
    private Double demandQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FabricId", nullable = true)
    private Fabric fabric;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TrimId", nullable = true)
    private Trim trim;

    @Column(length = 255)
    private String supplier;

    private Double price;

    @Column(length = 3)
    private String currency;
}