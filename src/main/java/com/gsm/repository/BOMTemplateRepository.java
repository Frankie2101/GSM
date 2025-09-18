package com.gsm.repository;

import com.gsm.model.BOMTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link BOMTemplate} entity.
 */
@Repository
public interface BOMTemplateRepository extends JpaRepository<BOMTemplate, Long> {

    /**
     * Finds a BOMTemplate by its unique business code.
     * @param code The template code to search for.
     * @return An {@link Optional} containing the found template.
     */
    Optional<BOMTemplate> findByBomTemplateCode(String code);

    /**
     * Searches for BOM templates using a keyword against the template's code and name.
     * @param keyword The search term to match.
     * @return A list of matching {@link BOMTemplate}s.
     */
    @Query("SELECT b FROM BOMTemplate b WHERE " +
            "(:keyword IS NULL OR LOWER(b.bomTemplateCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.bomTemplateName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<BOMTemplate> search(@Param("keyword") String keyword);
}