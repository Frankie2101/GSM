package com.gsm.repository;

import com.gsm.model.OrderBOM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderBOMRepository extends JpaRepository<OrderBOM, Long> {

    /**
     * Finds an OrderBOM associated with a specific SaleOrder ID.
     * This is used to check if a BOM has already been created for a sale order.
     * @param saleOrderId The ID of the parent {@link com.gsm.model.SaleOrder}.
     * @return An {@link Optional} containing the found OrderBOM.
     */
    Optional<OrderBOM> findBySaleOrder_SaleOrderId(Long saleOrderId);
}