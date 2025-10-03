package com.gsm.service;

import java.io.IOException;
import java.io.InputStream;

/**
 * Service interface for managing Supplier entities.
 */
public interface SupplierService {

    /**
     * Imports supplier data from an Excel spreadsheet via an input stream.
     * Performs an "upsert" operation based on the unique supplierCode.
     *
     * @param inputStream The input stream of the .xlsx file.
     * @throws IOException if an error occurs while reading the file.
     */
    void importFromExcel(InputStream inputStream) throws IOException;
}