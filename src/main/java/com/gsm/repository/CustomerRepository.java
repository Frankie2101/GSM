package com.gsm.repository;

import com.gsm.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Finds a Customer by its unique business code.
     * @param customerCode The unique code to search for.
     * @return An {@link Optional} containing the found customer.
     */
    Optional<Customer> findByCustomerCode(String customerCode);
}
