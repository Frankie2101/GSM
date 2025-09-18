// File: src/main/java/com/gsm/repository/BOMTemplateDetailRepository.java
package com.gsm.repository;

import com.gsm.model.BOMTemplateDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link BOMTemplateDetail} entity.
 * Standard CRUD operations inherited from JpaRepository are sufficient for now.
 */
@Repository
public interface BOMTemplateDetailRepository extends JpaRepository<BOMTemplateDetail, Long> {
}