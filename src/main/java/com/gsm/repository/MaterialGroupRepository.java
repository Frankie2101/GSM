package com.gsm.repository;

import com.gsm.model.MaterialGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialGroupRepository extends JpaRepository<MaterialGroup, Long> {

    /**
     * Finds all MaterialGroups of a specific type.
     * @param materialType The type to filter by (e.g., "FA" for Fabric, "TR" for Trim).
     * @return A list of matching {@link MaterialGroup}s.
     */
    List<MaterialGroup> findByMaterialType(String materialType);
}