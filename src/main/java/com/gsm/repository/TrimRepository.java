// File: src/main/java/com/gsm/repository/TrimRepository.java
package com.gsm.repository;

import com.gsm.model.Trim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link Trim} entity.
 * Provides standard CRUD operations and custom queries for trims (accessories).
 */
@Repository
public interface TrimRepository extends JpaRepository<Trim, Long> {

    /**
     * Finds a Trim by its unique business code.
     * This is a derived query method automatically implemented by Spring Data JPA.
     *
     * @param trimCode The unique code of the trim to find.
     * @return An {@link Optional} containing the found trim, or empty if not found.
     */
    Optional<Trim> findByTrimCode(String trimCode);

    /**
     * Finds all trims belonging to a specific Material Group.
     * This is a "derived query method". Spring Data JPA automatically generates the
     * implementation by parsing the method name.
     *
     * @param materialGroupId The ID of the Material Group to filter by.
     * @return A list of matching {@link com.gsm.model.Trim} entities, or an empty list if none are found.
     */
    List<Trim> findByMaterialGroup_MaterialGroupId(Long materialGroupId);

    /**
     * Searches for trims using a keyword against the trim's code and name.
     * The search is case-insensitive and matches if the keyword appears anywhere.
     *
     * @param keyword The search term to match. Can be null or empty.
     * @return A list of {@link Trim} entities that match the search criteria.
     */
    @Query("SELECT t FROM Trim t WHERE " +
            "(:keyword IS NULL OR LOWER(t.trimCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.trimName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Trim> searchTrims(@Param("keyword") String keyword);
}