package com.gsm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductCategoryId", referencedColumnName = "CategoryId", nullable = false)
    private ProductCategory productCategory;

    @OneToMany(mappedBy = "bomTemplate", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BOMTemplateDetail> details = new ArrayList<>();

    public void addDetail(BOMTemplateDetail detail) {
        this.details.add(detail);
        detail.setBomTemplate(this);
    }
}
