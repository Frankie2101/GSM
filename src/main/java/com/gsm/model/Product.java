package com.gsm.model;

import javax.persistence.*;

import com.gsm.enums.ProductStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the core Product entity in the system.
 * <p>
 * A Product is a general template for a sellable item (e.g., "Polo Shirt")
 * and acts as a parent for multiple {@link ProductVariant}s, which define
 * specific attributes like color and size.
 */
@Entity
@Table(name = "Product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product extends AuditableEntity {

    /**
     * The unique identifier for the Product.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductId")
    private Long productId;

    /**
     * The unique business code for the product, used for identification and integration.
     * E.g., "P-001".
     */
    @Column(name = "ProductCode", nullable = false, unique = true, length = 50)
    private String productCode;

    /**
     * The descriptive name of the product. E.g., "Men's Cotton Polo Shirt".
     */
    @Column(name = "ProductName", nullable = false, length = 100)
    private String productName;

    /**
     * The category this product belongs to.
     * FetchType.LAZY is used for performance optimization, preventing the category
     * from being loaded from the database until it's explicitly accessed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CategoryId", referencedColumnName = "CategoryId", nullable = false)
    private ProductCategory productCategory;

    /**
     * The standard unit of measure for this product (e.g., "Pcs", "Set").
     * FetchType.LAZY improves performance by delaying the loading of this entity.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UnitId", referencedColumnName = "UnitId", nullable = false)
    private Unit unit;

    /**
     * The season or collection this product is associated with. E.g., "Spring/Summer 2025".
     */
    @Column(name = "Season", length = 50)
    private String season;

    /**
     * The current lifecycle status of the product.
     * Stored as a String in the database for readability and to prevent issues
     * if the enum order changes in the future.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 20)
    private ProductStatus status;

    /**
     * A list of all specific variants (e.g., by color/size) for this product.
     * - CascadeType.ALL: Operations (save, update, delete) on a Product will cascade to its variants.
     * - orphanRemoval=true: If a variant is removed from this list, it will be deleted from the database.
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductVariant> variants = new ArrayList<>();

    /**
     * Helper method to add a new variant to this product, ensuring the
     * bidirectional relationship is correctly maintained on both sides.
     *
     * @param variant The {@link ProductVariant} to add.
     */
    public void addVariant(ProductVariant variant) {
        this.variants.add(variant);
        variant.setProduct(this);
    }
}