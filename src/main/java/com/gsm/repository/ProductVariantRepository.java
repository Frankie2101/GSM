package com.gsm.repository;

import com.gsm.controller.api.SaleOrderApiController.ColorInfo;
import com.gsm.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

/**
 * Spring Data JPA repository for the {@link ProductVariant} entity.
 * Handles all database operations for product variants.
 *
 * @author ThanhDX
 * @version 1.0.0
 */
@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    /**
     * Finds all variants associated with a specific parent product ID.
     *
     * @param productId The ID of the parent {@link com.gsm.model.Product}.
     * @return A list of matching {@link ProductVariant}s.
     */
    List<ProductVariant> findByProduct_ProductId(Long productId);

    /**
     * Finds all variants for a specific product that also match a given color.
     *
     * @param productId The ID of the parent product.
     * @param color The color to filter by.
     * @return A list of matching {@link ProductVariant}s.
     */
    List<ProductVariant> findByProduct_ProductIdAndColor(Long productId, String color);

    /**
     * Retrieves a distinct list of colors available for a given product ID.
     *
     * @param productId The ID of the product to query for colors.
     * @return A {@code List<Object[]>}, where each array contains the color code (e.g., ["White"], ["Black"]).
     */
    @Query("SELECT DISTINCT v.color FROM ProductVariant v WHERE v.product.productId = :productId ORDER BY v.color")
    List<String> findDistinctColorsByProductId(@Param("productId") Long productId);

    /**
     * Deletes all variants associated with a specific parent product ID.
     *
     * @param productId The ID of the parent {@link com.gsm.model.Product} whose variants should be deleted.
     */
    @Transactional
    void deleteByProduct_ProductId(Long productId);
}
