package com.gsm.repository;

import com.gsm.model.Customer;
import com.gsm.model.SaleOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleOrderRepository extends JpaRepository<SaleOrder, Long> {
    Optional<SaleOrder> findBySaleOrderNo(String saleOrderNo);

    // THÊM MỚI: Phương thức đếm số đơn hàng theo Customer
    long countByCustomer(Customer customer);

    @Query("SELECT so FROM SaleOrder so WHERE " +
            "(:keyword IS NULL OR LOWER(so.saleOrderNo) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(so.customer.customerName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<SaleOrder> search(@Param("keyword") String keyword);
}