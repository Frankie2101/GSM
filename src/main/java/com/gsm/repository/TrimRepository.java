// File: src/main/java/com/gsm/repository/TrimRepository.java
package com.gsm.repository;

import com.gsm.model.Trim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrimRepository extends JpaRepository<Trim, Long> {
    Optional<Trim> findByTrimCode(String trimCode);

    @Query("SELECT t FROM Trim t WHERE " +
            "(:keyword IS NULL OR LOWER(t.trimCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.trimName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Trim> searchTrims(@Param("keyword") String keyword);
}