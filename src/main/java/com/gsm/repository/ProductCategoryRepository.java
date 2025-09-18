package com.gsm.repository;

import com.gsm.model.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    /**
     * Finds a ProductCategory by its unique name.
     * @param categoryName The name of the category to find.
     * @return An {@link Optional} containing the found category.
     */
    Optional<ProductCategory> findByCategoryName(String categoryName);
}