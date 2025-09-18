package com.gsm.repository;

import com.gsm.model.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {

    /**
     * Finds a Unit by its unique name.
     * @param unitName The name of the unit to find.
     * @return An {@link Optional} containing the found unit.
     */
    Optional<Unit> findByUnitName(String unitName);
}