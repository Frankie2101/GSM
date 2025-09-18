package com.gsm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * Represents a specific color variant of a {@link Fabric}.
 * This is the "child" side of the one-to-many relationship with Fabric.
 */
@Entity
@Table(name = "FabricColor")
@Getter
@Setter
@NoArgsConstructor
public class FabricColor extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FabricColorId")
    private Long fabricColorId;

    /**
     * The code for the color, e.g., "BLK", "WHT". Must not be null.
     */
    @Column(name = "Color", nullable = false, length = 50)
    private String color;

    /**
     * The descriptive name of the color, e.g., "Black", "Optic White".
     */
    @Column(name = "ColorName", length = 100)
    private String colorName;

    @Column(name = "Width", length = 50)
    private String width;

    @Column(name = "NetPrice")
    private Double netPrice;

    @Column(name = "TaxPercent")
    private Double taxPercent;

    /**
     * The parent Fabric to which this color belongs. This is the owning side of the relationship.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FabricId", nullable = false)
    private Fabric fabric;
}