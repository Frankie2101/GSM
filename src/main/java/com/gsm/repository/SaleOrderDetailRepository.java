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

    @Query("SELECT DISTINCT new map(pv.product.productName as style, pv.color as color) " +
            "FROM SaleOrderDetail sod " +
            "JOIN sod.productVariant pv " +
            "WHERE sod.saleOrder.saleOrderId = :saleOrderId")
    List<Map<String, String>> findDistinctStylesAndColorsBySaleOrderId(@Param("saleOrderId") Long saleOrderId);
}