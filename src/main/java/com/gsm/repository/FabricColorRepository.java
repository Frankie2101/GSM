package com.gsm.repository;

import com.gsm.model.FabricColor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface FabricColorRepository extends JpaRepository<FabricColor, Long> {
    @Transactional
    void deleteByFabric_FabricId(Long fabricId);
}
