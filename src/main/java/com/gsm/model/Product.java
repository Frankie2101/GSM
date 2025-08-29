package com.gsm.model;

import javax.persistence.*;

import com.gsm.enums.ProductStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductId")
    private Long productId;

    @Column(name = "ProductCode", nullable = false, unique = true, length = 50)
    private String productCode;

    @Column(name = "ProductName", nullable = false, length = 100)
    private String productName;

    // THAY ĐỔI: Sử dụng mối quan hệ Many-to-One
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CategoryId", referencedColumnName = "CategoryId", nullable = false)
    private ProductCategory productCategory;

    // THAY ĐỔI: Sử dụng mối quan hệ Many-to-One
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UnitId", referencedColumnName = "UnitId", nullable = false)
    private Unit unit;

    @Column(name = "Season", length = 50)
    private String season;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 20)
    private ProductStatus status;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductVariant> variants = new ArrayList<>();

    //Add Product Variant into Product
    public void addVariant(ProductVariant variant) {
        this.variants.add(variant);
        variant.setProduct(this);
    }
}