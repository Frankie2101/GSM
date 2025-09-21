package com.gsm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * Represents a single line item within a {@link BOMTemplate}.
 * <p>
 * This entity features a polymorphic relationship to raw materials: a single detail line
 * can link to either a {@link Fabric} OR a {@link Trim}, but not both. The {@code rmType}
 * field acts as a discriminator to indicate which relationship is active.
 */
@Entity
@Table(name = "BOMTemplateDetail")
@Getter
@Setter
@NoArgsConstructor
public class BOMTemplateDetail extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BOMTemplateDetailId")
    private Long bomTemplateDetailId;

    /**
     * The parent BOMTemplate to which this detail line belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BOMTemplateId", nullable = false)
    private BOMTemplate bomTemplate;

    /**
     * Link to a MaterialGroup entity.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaterialGroupId", nullable = true)
    private MaterialGroup materialGroup;

    /**
     * The sequence number for ordering the details in the list.
     */
    @Column(name = "Seq", nullable = false)
    private Integer seq;

    /**
     * The type of raw material. Acts as a discriminator.
     * "FA" for Fabric, "TR" for Trim.
     */
    @Column(name = "RMType", nullable = false, length = 2) // "FA" for Fabric, "TR" for Trim
    private String rmType;

    /**
     * The standard quantity of material needed to produce one finished good.
     */
    @Column(name = "Usage", nullable = false)
    private Double usageValue; // Renamed to avoid SQL keyword conflict

    /**
     * The expected wastage percentage for this material.
     */
    @Column(name = "Waste", nullable = false)
    private Double waste;

    /**
     * Polymorphic Relationship: Link to a Fabric entity.
     * This will be non-null only if rmType is "FA".
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FabricId", nullable = true)
    private Fabric fabric;

    /**
     * Polymorphic Relationship: Link to a Trim entity.
     * This will be non-null only if rmType is "TR".
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TrimId", nullable = true)
    private Trim trim;
}
