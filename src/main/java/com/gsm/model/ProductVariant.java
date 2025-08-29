package com.gsm.model;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "ProductVariant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductVariantId")
    private Long productVariantId;

    @Column(name = "Color", nullable = false, length = 50)
    private String color;

    @Column(name = "[Size]", nullable = false, length = 20) // "Size" là từ khóa của SQL, nên đặt trong ngoặc vuông
    private String size;

    @Column(name = "SKU", nullable = false, unique = true, length = 100)
    private String sku;

    @Column(name = "Price")
    private Double price;

    // Many-to-one relationship with the Product model.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductId", nullable = false)
    private Product product;

    // Thêm vào dưới thuộc tính price
    @Column(name = "Currency", length = 3)
    private String currency;
}