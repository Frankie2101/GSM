package com.gsm.repository;

import com.gsm.enums.SaleOrderStatus;
import com.gsm.model.Customer;
import com.gsm.model.SaleOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link SaleOrder} entity.
 */
@Repository
public interface SaleOrderRepository extends JpaRepository<SaleOrder, Long> {

    /**
     * Finds a SaleOrder by its unique, business-logic-driven saleOrderNo.
     * @param saleOrderNo The sale order number to search for.
     * @return An {@link Optional} containing the found sale order.
     */
    Optional<SaleOrder> findBySaleOrderNo(String saleOrderNo);

    /**
     * Counts the total number of existing sale orders for a given customer.
     * Used to generate the next sequential order number.
     * @param customer The customer entity to count orders for.
     * @return The total count of orders for that customer.
     */
    long countByCustomer(Customer customer);

    /**
     * Searches for sale orders using a keyword against the order number and customer name.
     * @param keyword The term to search for.
     * @return A list of matching {@link SaleOrder}s.
     */
    @Query("SELECT so FROM SaleOrder so WHERE " +
            "(:keyword IS NULL OR LOWER(so.saleOrderNo) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(so.customer.customerName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<SaleOrder> search(@Param("keyword") String keyword);

    /**
     * Finds all sale orders that have a specific status.
     * @param status The status to filter by.
     * @return A list of matching {@link SaleOrder}s.
     */
    List<SaleOrder> findByStatus(SaleOrderStatus status);
}