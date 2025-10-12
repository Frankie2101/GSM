package com.gsm.repository;

import com.gsm.model.BOMTemplateDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * Spring Data JPA repository for the {@link BOMTemplateDetail} entity.
 * Standard CRUD operations inherited from JpaRepository are sufficient for now.
 */
@Repository
public interface BOMTemplateDetailRepository extends JpaRepository<BOMTemplateDetail, Long> {
    boolean existsByFabric_FabricId(Long fabricId);

    @Query("SELECT DISTINCT btd.fabric.fabricId FROM BOMTemplateDetail btd WHERE btd.fabric IS NOT NULL")
    Set<Long> findDistinctFabricIdsInUse();

    boolean existsByTrim_TrimId(Long trimId);

    @Query("SELECT DISTINCT btd.trim.trimId FROM BOMTemplateDetail btd WHERE btd.trim IS NOT NULL")
    Set<Long> findDistinctTrimIdsInUse();
}