package com.gsm.repository;

import com.gsm.model.SaleOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

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
}