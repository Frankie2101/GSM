package com.gsm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the BOM (Bill of Materials) Template entity.
 * This acts as a reusable master data for the standard list of materials
 * required to produce a product within a specific category.
 */
@Entity
@Table(name = "BOMTemplate")
@Getter
@Setter
@NoArgsConstructor
public class BOMTemplate extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BOMTemplateId")
    private Long bomTemplateId;

    @Column(name = "BOMTemplateCode", nullable = false, unique = true, length = 50)
    private String bomTemplateCode;

    @Column(name = "BOMTemplateName", nullable = false, length = 100)
    private String bomTemplateName;

    /**
     * The product category to which this BOM template applies.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductCategoryId", referencedColumnName = "CategoryId", nullable = false)
    private ProductCategory productCategory;


    /**
     * A list of all detail lines associated with this BOM template.
     */
    @OneToMany(mappedBy = "bomTemplate", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BOMTemplateDetail> details = new ArrayList<>();

    /**
     * Helper method to add a new detail line and maintain the bidirectional relationship.
     * @param detail The {@link BOMTemplateDetail} to add.
     */
    public void addDetail(BOMTemplateDetail detail) {
        this.details.add(detail);
        detail.setBomTemplate(this);
    }
}
