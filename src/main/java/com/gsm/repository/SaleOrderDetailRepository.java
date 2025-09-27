package com.gsm.repository;

import com.gsm.model.SaleOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public interface SaleOrderDetailRepository extends JpaRepository<SaleOrderDetail, Long> {

    /**
     * Finds the distinct combinations of product code (as style) and color for a given sale order.
     * This is used by the Zalo Mini App to populate the selection dropdowns.
     *
     * @param saleOrderId The ID of the sale order to search within.
     * @return A list of maps, where each map contains a 'style' (productCode) and 'color'.
     */
    @Query("SELECT DISTINCT new map(pv.product.productCode as style, pv.color as color) " +
            "FROM SaleOrderDetail sod " +
            "JOIN sod.productVariant pv " +
            "WHERE sod.saleOrder.saleOrderId = :saleOrderId")
    List<Map<String, String>> findDistinctStylesAndColorsBySaleOrderId(@Param("saleOrderId") Long saleOrderId);

    /**
     * Checks if any SaleOrderDetail line is linked to a specific Product ID.
     * @param productId The ID of the product to check.
     * @return true if the product is used in any sale order, false otherwise.
     */
    boolean existsByProductVariant_Product_ProductId(Long productId);

    /**
     * Checks if any SaleOrderDetail line is linked to a specific ProductVariant ID.
     * @param productVariantId The ID of the product variant to check.
     * @return true if the variant is used in any sale order, false otherwise.
     */
    boolean existsByProductVariant_ProductVariantId(Long productVariantId);

    /**
     * Finds a set of unique Product IDs that are currently used in any SaleOrderDetail.
     * This is used to determine which products cannot be deleted.
     * @return A Set of product IDs.
     */
    @Query("SELECT DISTINCT sod.productVariant.product.productId FROM SaleOrderDetail sod")
    Set<Long> findDistinctProductIdsInUse();
}