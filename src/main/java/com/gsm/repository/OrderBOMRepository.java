package com.gsm.repository;

import com.gsm.model.OrderBOM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface OrderBOMRepository extends JpaRepository<OrderBOM, Long> {

    /**
     * Finds an OrderBOM associated with a specific SaleOrder ID.
     * This is used to check if a BOM has already been created for a sale order.
     * @param saleOrderId The ID of the parent {@link com.gsm.model.SaleOrder}.
     * @return An {@link Optional} containing the found OrderBOM.
     */
    Optional<OrderBOM> findBySaleOrder_SaleOrderId(Long saleOrderId);

    /**
     * Checks if a BOMTemplate is used in any OrderBOM.
     * @param bomTemplateId The ID of the BOMTemplate to check.
     * @return true if it is in use, false otherwise.
     */
    boolean existsByBomTemplate_BomTemplateId(Long bomTemplateId);

    /**
     * Finds a distinct set of BOMTemplate IDs that are currently in use by OrderBOMs.
     * @return A Set of BOMTemplate IDs.
     */
    @Query("SELECT DISTINCT ob.bomTemplate.bomTemplateId FROM OrderBOM ob WHERE ob.bomTemplate IS NOT NULL")
    Set<Long> findDistinctBomTemplateIdsInUse();

    /**
     * Checks if any OrderBOM record is linked to a specific SaleOrder ID.
     * This is an optimized query to quickly check for usage before allowing a delete operation.
     *
     * @param saleOrderId The ID of the SaleOrder to check.
     * @return true if the SaleOrder is in use, false otherwise.
     */
    boolean existsBySaleOrder_SaleOrderId(Long saleOrderId);

    /**
     * Finds a distinct set of all SaleOrder IDs that are currently referenced in the OrderBOM table.
     * This is used to efficiently determine the lock status for a list of sale orders,
     * preventing the N+1 query problem.
     *
     * @return A Set containing the unique IDs of all used SaleOrders.
     */
    @Query("SELECT DISTINCT o.saleOrder.saleOrderId FROM OrderBOM o WHERE o.saleOrder IS NOT NULL")
    Set<Long> findDistinctSaleOrderIdsInUse();
}