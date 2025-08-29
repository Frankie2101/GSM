package com.gsm.repository;

import com.gsm.model.OrderBOM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderBOMRepository extends JpaRepository<OrderBOM, Long> {
    // Tìm OrderBOM dựa trên SaleOrderId để kiểm tra đã tồn tại hay chưa
    Optional<OrderBOM> findBySaleOrder_SaleOrderId(Long saleOrderId);
}