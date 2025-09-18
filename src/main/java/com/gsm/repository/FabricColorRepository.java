package com.gsm.repository;

import com.gsm.model.FabricColor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA repository for the {@link FabricColor} entity.
 */
@Repository
public interface FabricColorRepository extends JpaRepository<FabricColor, Long> {

    /**
     * Deletes all FabricColor entities associated with a specific parent Fabric ID.
     * This is a derived delete query. Spring Data JPA interprets the method name
     * to generate the appropriate DELETE statement.
     * Requires a {@link Transactional} annotation to ensure the delete operation is executed safely.
     *
     * @param fabricId The ID of the parent {@link com.gsm.model.Fabric} whose colors should be deleted.
     */
    @Transactional
    void deleteByFabric_FabricId(Long fabricId);
}
