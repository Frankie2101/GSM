package com.gsm.service;

import com.gsm.model.Customer;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Service interface for managing Customer entities.
 * <p>
 * This interface defines the business operations related to customers, abstracting the
 * underlying data access and business logic from the controllers.
 */
public interface CustomerService {

    /**
     * Parses an Excel file from an input stream into a list of Customer objects
     * without persisting them to the database. This is used for data preview.
     *
     * @param inputStream The input stream of the .xlsx file to be parsed.
     * @return A {@link List} of {@link Customer} objects parsed from the file.
     * @throws IOException if an error occurs during file reading.
     */
    List<Customer> parseExcelForPreview(InputStream inputStream) throws IOException;

    /**
     * Saves a list of Customer objects to the database using an "upsert" logic.
     * If a customer with the same code exists, it will be updated. Otherwise, a new
     * customer will be created.
     *
     * @param customers The list of Customer objects to be saved.
     */
    void saveAll(List<Customer> customers);
}