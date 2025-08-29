// File: src/main/java/com/gsm/repository/FabricRepository.java
package com.gsm.repository;

import com.gsm.model.Fabric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FabricRepository extends JpaRepository<Fabric, Long> {
    Optional<Fabric> findByFabricCode(String fabricCode);

    @Query("SELECT f FROM Fabric f WHERE " +
            "(:keyword IS NULL OR LOWER(f.fabricCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(f.fabricName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Fabric> searchFabrics(@Param("keyword") String keyword);
}