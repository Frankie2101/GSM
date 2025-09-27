package com.gsm.repository;

import com.gsm.model.OrderBOMDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * Spring Data JPA repository for the OrderBOMDetail entity.
 * Provides standard CRUD (Create, Read, Update, Delete) operations out of the box.
 */
@Repository
public interface OrderBOMDetailRepository extends JpaRepository<OrderBOMDetail, Long> {
    boolean existsByFabric_FabricId(Long fabricId);

    @Query("SELECT DISTINCT obd.fabric.fabricId FROM OrderBOMDetail obd WHERE obd.fabric IS NOT NULL")
    Set<Long> findDistinctFabricIdsInUse();

    @Query("SELECT CASE WHEN COUNT(obd) > 0 THEN true ELSE false END " +
            "FROM OrderBOMDetail obd " +
            "WHERE obd.fabric.fabricId = :fabricId AND obd.colorCode = :colorCode")
    boolean existsByFabricIdAndColorCode(@Param("fabricId") Long fabricId, @Param("colorCode") String colorCode);

    boolean existsByTrim_TrimId(Long trimId);

    @Query("SELECT DISTINCT obd.trim.trimId FROM OrderBOMDetail obd WHERE obd.trim IS NOT NULL")
    Set<Long> findDistinctTrimIdsInUse();

    @Query("SELECT CASE WHEN COUNT(obd) > 0 THEN true ELSE false END " +
            "FROM OrderBOMDetail obd " +
            "WHERE obd.trim.trimId = :trimId AND obd.colorCode = :colorCode AND obd.size = :sizeCode")
    boolean existsByTrimIdAndColorCodeAndSizeCode(@Param("trimId") Long trimId, @Param("colorCode") String colorCode, @Param("sizeCode") String sizeCode);
}