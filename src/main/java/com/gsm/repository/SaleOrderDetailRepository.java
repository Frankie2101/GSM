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
     * Finds a distinct list of styles (product names) and colors for a given sale order.
     * This query uses a JPQL "constructor expression" (`new map(...)`) to directly create a
     * list of maps from the query result, which is more efficient than returning Object[] arrays.
     *
     * @param saleOrderId The ID of the sale order to query.
     * @return A list of Maps, where each map has two keys: "style" and "color".
     */
    @Query("SELECT DISTINCT new map(pv.product.productName as style, pv.color as color) " +
            "FROM SaleOrderDetail sod " +
            "JOIN sod.productVariant pv " +
            "WHERE sod.saleOrder.saleOrderId = :saleOrderId")
    List<Map<String, String>> findDistinctStylesAndColorsBySaleOrderId(@Param("saleOrderId") Long saleOrderId);
}