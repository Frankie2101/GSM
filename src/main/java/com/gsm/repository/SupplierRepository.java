package com.gsm.repository;

import com.gsm.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    /**
     * Finds a Supplier by its unique business code.
     * @param supplierCode The code of the supplier to find.
     * @return An {@link Optional} containing the found supplier.
     */
    Optional<Supplier> findBySupplierCode(String supplierCode);

    /**
     * Finds a Supplier by its unique name.
     * @param supplierName The name of the supplier to find.
     * @return An {@link Optional} containing the found supplier.
     */
    Optional<Supplier> findBySupplierName(String supplierName);

    List<Supplier> findBySupplierNameIn(List<String> names);
}
