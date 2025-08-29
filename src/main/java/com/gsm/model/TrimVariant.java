package com.gsm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "TrimVariant")
@Getter
@Setter
@NoArgsConstructor
public class TrimVariant extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TrimVariantId")
    private Long trimVariantId;

    @Column(name = "ColorCode", nullable = false, length = 50)
    private String colorCode;

    @Column(name = "ColorName", length = 100)
    private String colorName;

    @Column(name = "SizeCode", nullable = false, length = 50)
    private String sizeCode;

    @Column(name = "NetPrice")
    private Double netPrice;

    @Column(name = "TaxRate")
    private Double taxRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TrimId", nullable = false)
    private Trim trim;
}
