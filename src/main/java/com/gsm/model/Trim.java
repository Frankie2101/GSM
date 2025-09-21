package com.gsm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import com.gsm.model.MaterialGroup;

@Entity
@Table(name = "Trim")
@Getter
@Setter
@NoArgsConstructor
public class Trim extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TrimId")
    private Long trimId;

    @Column(name = "TrimCode", nullable = false, unique = true, length = 50)
    private String trimCode;

    @Column(name = "TrimName", nullable = false, length = 100)
    private String trimName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UnitId", referencedColumnName = "UnitId", nullable = false)
    private Unit unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SupplierId", referencedColumnName = "SupplierId", nullable = false)
    private Supplier supplier;

    @Column(name = "TechnicalReference", length = 255)
    private String technicalReference;

    /**
     * A list of all available variants for this trim.
     */
    @OneToMany(mappedBy = "trim", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TrimVariant> variants = new ArrayList<>();

    /**
     * A Material group of trim
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaterialGroupId", referencedColumnName = "MaterialGroupId")
    private MaterialGroup materialGroup;

    // Helper method to maintain relationship consistency
    public void addVariant(TrimVariant variant) {
        this.variants.add(variant);
        variant.setTrim(this);
    }
}
