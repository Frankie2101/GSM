package com.gsm.model; // Giả sử package là model

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;

/**
 * Represents a single line item (a material) within an OrderBOM.
 */
@Entity
@Table(name = "OrderBOMDetail")
@Getter
@Setter
public class OrderBOMDetail extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderBOMDetailId;

    /**
     * The many-to-one relationship back to the parent OrderBOM.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderBOMId", nullable = false)
    private OrderBOM orderBOM;

    /**
     * The sequence number for ordering the details.
     */
    @Column(nullable = false)
    private Integer seq;

    /**
     * The relationship with the material group for classification.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaterialGroupId")
    private MaterialGroup materialGroup;

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
    private String uom; // Unit of Measure

    @Column(nullable = false)
    private Double usageValue; // Consumption rate

    @Column(nullable = false)
    private Double waste;  // Waste percentage

    @Column
    private Double demandQuantity;

    /**
     * The link to the Fabric entity if this material is a fabric.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FabricId", nullable = true)
    private Fabric fabric;

    /**
     * The link to the Trim entity if this material is a trim.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TrimId", nullable = true)
    private Trim trim;

    @Column(length = 255)
    private String supplier;

    private Double price;

    @Column(length = 3)
    private String currency;
}