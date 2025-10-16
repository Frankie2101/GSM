package com.gsm.service;

import com.gsm.model.Supplier;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Service interface for managing Supplier entities.
 * <p>
 * This interface defines the business operations for parsing and persisting
 * Supplier data, abstracting the implementation details from the controller.
 */
public interface SupplierService {

    /**
     * Parses an Excel file from an input stream into a list of Supplier objects
     * for data preview, without saving them to the database.
     *
     * @param inputStream The input stream of the .xlsx file.
     * @return A {@link List} of {@link Supplier} objects parsed from the file.
     * @throws IOException if an error occurs during file reading.
     */
    List<Supplier> parseExcelForPreview(InputStream inputStream) throws IOException;

    /**
     * Saves a list of Supplier objects to the database using an "upsert" logic.
     * If a supplier with a matching code exists, it is updated; otherwise, a new
     * supplier is created.
     *
     * @param suppliers The list of Supplier objects to be saved.
     */
    void saveAll(List<Supplier> suppliers);
}