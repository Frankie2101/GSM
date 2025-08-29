package com.gsm.repository;

import com.gsm.model.OrderBOMDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderBOMDetailRepository extends JpaRepository<OrderBOMDetail, Long> {
}