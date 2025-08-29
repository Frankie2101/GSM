package com.gsm.repository;

import com.gsm.model.SaleOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleOrderDetailRepository extends JpaRepository<SaleOrderDetail, Long> {
    // JpaRepository is sufficient for now
}