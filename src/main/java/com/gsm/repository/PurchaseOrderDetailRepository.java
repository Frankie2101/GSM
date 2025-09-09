package com.gsm.repository;

import com.gsm.model.OrderBOMDetail;
import com.gsm.model.PurchaseOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseOrderDetailRepository extends JpaRepository<PurchaseOrderDetail, Long> {
    boolean existsByOrderBOMDetail(OrderBOMDetail orderBOMDetail);

}