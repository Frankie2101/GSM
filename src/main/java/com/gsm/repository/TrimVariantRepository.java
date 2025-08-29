package com.gsm.repository;

import com.gsm.model.TrimVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TrimVariantRepository extends JpaRepository<TrimVariant, Long> {
    @Transactional
    void deleteByTrim_TrimId(Long trimId);
}