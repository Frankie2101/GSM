package com.gsm.repository;

import com.gsm.model.OrderBOMDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the OrderBOMDetail entity.
 * Provides standard CRUD (Create, Read, Update, Delete) operations out of the box.
 */
@Repository
public interface OrderBOMDetailRepository extends JpaRepository<OrderBOMDetail, Long> {
}