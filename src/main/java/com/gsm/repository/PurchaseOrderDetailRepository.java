package com.gsm.repository;

import com.gsm.model.OrderBOMDetail;
import com.gsm.model.PurchaseOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
    boolean existsByorderBOMDetail_OrderBOMDetailId(Long orderBOMDetailId);

}