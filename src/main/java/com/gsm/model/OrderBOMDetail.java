package com.gsm.model;

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
    @Column(name = "OrderBOMDetailId")
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
    @Column(name = "Seq", nullable = false)
    private Integer seq;

    /**
     * The relationship with the material group for classification.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaterialGroupId")
    private MaterialGroup materialGroup;

    @Column(name = "ColorCode", length = 50)
    private String colorCode;

    @Column(name = "Size", length = 50)
    private String size;

    @Column(name = "MaterialType", nullable = false, length = 2) // "FA" for Fabric, "TR" for Trim
    private String materialType;

    @Column(name = "UOM", length = 50)
    private String uom; // Unit of Measure

    @Column(name = "UsageValue", nullable = false)
    private Double usageValue; // Consumption rate

    @Column(name = "Waste", nullable = false)
    private Double waste;  // Waste percentage

    @Column(name = "DemandQuantity")
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

    @Column(name = "Supplier",length = 255)
    private String supplier;

    @Column(name = "Price")
    private Double price;

    @Column(name = "Currency", length = 3)
    private String currency;
}