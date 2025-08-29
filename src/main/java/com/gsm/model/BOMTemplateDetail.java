package com.gsm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BOMTemplateId", nullable = false)
    private BOMTemplate bomTemplate;

    @Column(name = "Seq", nullable = false)
    private Integer seq;

    @Column(name = "RMType", nullable = false, length = 2) // "FA" for Fabric, "TR" for Trim
    private String rmType;

    @Column(name = "Usage", nullable = false)
    private Double usageValue; // Renamed to avoid SQL keyword conflict

    @Column(name = "Waste", nullable = false)
    private Double waste;

    // Polymorphic relationship: Only one of these will be non-null
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FabricId", nullable = true)
    private Fabric fabric;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TrimId", nullable = true)
    private Trim trim;
}
