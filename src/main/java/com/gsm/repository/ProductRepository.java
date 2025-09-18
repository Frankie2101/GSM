package com.gsm.repository;

import com.gsm.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link Product} entity.
 * <p>
 * This interface provides the mechanism for all data access operations
 * related to products. By extending {@link JpaRepository}, it inherits a suite of
 * standard CRUD methods. It also defines custom query methods for specific
 * business requirements like searching and existence checks.
 *
 * @author ThanhDX
 * @version 1.0.0
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Checks if a Product with the given product code already exists.
     * This is a derived query method; Spring Data JPA implements it automatically.
     *
     * @param productCode The product code to check.
     * @return {@code true} if a product with this code exists, {@code false} otherwise.
     */
    boolean existsByProductCode(String productCode);

    /**
     * Finds a Product by its unique product code.
     * This is another derived query method.
     *
     * @param productCode The unique code of the product to find.
     * @return An {@link Optional} containing the found product, or an empty Optional if no product matches.
     */
    Optional<Product> findByProductCode(String productCode);

    /**
     * Searches for products based on a keyword and/or a category name.
     *
     * @param keyword The search term to match against product code and name (case-insensitive). Can be null.
     * @param categoryName The exact category name to filter by. Can be null.
     * @return A list of {@link Product} entities that match the search criteria.
     */
    @Query("SELECT p FROM Product p WHERE " +
            "(:keyword IS NULL OR LOWER(p.productCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:categoryName IS NULL OR p.productCategory.categoryName = :categoryName)")
    List<Product> searchProducts(@Param("keyword") String keyword, @Param("categoryName") String categoryName);
}