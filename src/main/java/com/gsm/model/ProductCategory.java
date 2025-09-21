package com.gsm.model;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a category for products (e.g., T-Shirt, Jacket).
 */
@Entity
@Table(name = "ProductCategory")
@Getter
@Setter
public class ProductCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @Column(name = "CategoryCode", nullable = false, unique = true, length = 50)
    private String categoryCode;

    @Column(name = "CategoryName", length = 100)
    private String categoryName;
}