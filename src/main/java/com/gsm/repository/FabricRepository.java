// File: src/main/java/com/gsm/repository/FabricRepository.java
package com.gsm.repository;

import com.gsm.model.Fabric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link Fabric} entity.
 * Provides standard CRUD operations and custom queries for fabrics.
 */
@Repository
public interface FabricRepository extends JpaRepository<Fabric, Long> {

    /**
     * Finds a Fabric by its unique business code.
     * This is a "derived query method". Spring Data JPA automatically generates the
     * implementation based on the method name.
     *
     * @param fabricCode The unique code of the fabric to find.
     * @return An {@link Optional} containing the found fabric, or an empty Optional if no fabric matches.
     */
    Optional<Fabric> findByFabricCode(String fabricCode);

    /**
     * Finds all fabrics belonging to a specific Material Group.
     * This is a "derived query method". Spring Data JPA automatically generates the
     * implementation by parsing the method name.
     *
     * @param materialGroupId The ID of the Material Group to filter by.
     * @return A list of matching {@link com.gsm.model.Fabric} entities, or an empty list if none are found.
     */
    List<Fabric> findByMaterialGroup_MaterialGroupId(Long materialGroupId);

    /**
     * Searches for fabrics using a keyword against the fabric's code and name.
     * The search is case-insensitive and matches if the keyword appears anywhere in the fields.
     *
     * @param keyword The search term to match. Can be null or empty.
     * @return A list of {@link Fabric} entities that match the search criteria.
     */
    @Query("SELECT f FROM Fabric f WHERE " +
            "(:keyword IS NULL OR LOWER(f.fabricCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(f.fabricName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Fabric> searchFabrics(@Param("keyword") String keyword);
}