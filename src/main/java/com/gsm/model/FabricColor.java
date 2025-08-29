package com.gsm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

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

    @Column(name = "Color", nullable = false, length = 50)
    private String color;

    @Column(name = "ColorName", length = 100)
    private String colorName;

    @Column(name = "Width", length = 50)
    private String width;

    @Column(name = "NetPrice")
    private Double netPrice;

    @Column(name = "TaxPercent")
    private Double taxPercent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FabricId", nullable = false)
    private Fabric fabric;
}