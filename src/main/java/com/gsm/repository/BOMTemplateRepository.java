package com.gsm.repository;

import com.gsm.model.BOMTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BOMTemplateRepository extends JpaRepository<BOMTemplate, Long> {
    Optional<BOMTemplate> findByBomTemplateCode(String code);

    @Query("SELECT b FROM BOMTemplate b WHERE " +
            "(:keyword IS NULL OR LOWER(b.bomTemplateCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.bomTemplateName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<BOMTemplate> search(@Param("keyword") String keyword);
}