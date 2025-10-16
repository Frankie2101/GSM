package com.gsm.service;

import com.gsm.model.Unit;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Service interface for managing Unit entities.
 */
public interface UnitService {

    /**
     * Parses a Unit Excel file into a list of objects for preview.
     *
     * @param inputStream The input stream of the .xlsx file.
     * @return A {@link List} of {@link Unit} objects parsed from the file.
     * @throws IOException if an error occurs during file reading.
     */
    List<Unit> parseExcelForPreview(InputStream inputStream) throws IOException;

    /**
     * Saves a list of Unit objects to the database using an "upsert" logic
     * based on the unique unit code.
     *
     * @param units The list of Unit objects to be saved.
     */
    void saveAll(List<Unit> units);
}