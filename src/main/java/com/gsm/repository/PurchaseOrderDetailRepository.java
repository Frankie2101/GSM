package com.gsm.repository;

import com.gsm.model.OrderBOMDetail;
import com.gsm.model.PurchaseOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * Spring Data JPA repository for the {@link PurchaseOrderDetail} entity.
 */
@Repository
public interface PurchaseOrderDetailRepository extends JpaRepository<PurchaseOrderDetail, Long> {
    /**
     * Checks if a PurchaseOrderDetail already exists for a given OrderBOMDetail.
     * This is a derived query method used to prevent creating duplicate PO lines
     * if the PO generation process is run multiple times.
     * @param orderBOMDetail The originating OrderBOMDetail to check against.
     * @return {@code true} if a PO detail for this BOM detail already exists, {@code false} otherwise.
     */
    boolean existsByOrderBOMDetail(OrderBOMDetail orderBOMDetail);

    /**
     * Checks if a PurchaseOrderDetail exists by the ID of its associated OrderBOMDetail.
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM PurchaseOrderDetail p " +
            "WHERE p.orderBOMDetail.orderBOMDetailId = :orderBOMDetailId")
    boolean existsByOrderBOMDetail_OrderBOMDetailId(@Param("orderBOMDetailId") Long orderBOMDetailId);

    /**
     * Finds all distinct OrderBOMDetail IDs that are used in any PurchaseOrderDetail
     * for a given parent OrderBOM.
     * @param orderBOMId The ID of the parent OrderBOM.
     * @return A Set of Longs containing the used OrderBOMDetail IDs.
     */
    @Query("SELECT pod.orderBOMDetail.orderBOMDetailId " +
            "FROM PurchaseOrderDetail pod " +
            "WHERE pod.orderBOMDetail.orderBOM.orderBOMId = :orderBOMId")
    Set<Long> findUsedOrderBOMDetailIdsByOrderBOMId(@Param("orderBOMId") Long orderBOMId);
}