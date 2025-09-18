package com.gsm.repository;

import com.gsm.model.TrimVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TrimVariantRepository extends JpaRepository<TrimVariant, Long> {

    /**
     * Deletes all TrimVariant entities associated with a specific parent Trim ID.
     * This is a derived delete query that is useful for cleanup operations.
     * Requires a {@link Transactional} annotation to ensure safe execution.
     * @param trimId The ID of the parent {@link com.gsm.model.Trim}.
     */
    @Transactional
    void deleteByTrim_TrimId(Long trimId);
}