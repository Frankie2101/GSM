package com.gsm.service;

import com.gsm.model.Customer;
import com.gsm.repository.CustomerRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The concrete implementation of the {@link CustomerService} interface.
 * This class orchestrates all business logic for the Customer feature,
 * including the import functionality from Excel files.
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    /**
     * DataFormatter is a utility from Apache POI to safely retrieve the formatted
     * string value of a cell, regardless of its underlying data type (e.g., numeric, string, date).
     */
    private final DataFormatter dataFormatter = new DataFormatter();

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * {@inheritDoc}
     * This implementation uses Apache POI to stream and process the Excel file.
     * It iterates through each row, performs an upsert logic, and saves all
     * changes in a single transaction for efficiency.
     */
    @Override
    @Transactional
    public void importFromExcel(InputStream inputStream) throws IOException {
        List<Customer> customersToSave = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                // Skip the header row (first row, index 0)
                if (row.getRowNum() == 0) continue;

                // Read the unique business key (customerCode) from the first column.
                String customerCode = getCellValueAsString(row.getCell(0));

                // Skip rows that do not have a customer code to avoid validation errors.
                if (customerCode.isEmpty()) continue;

                // --- UPSERT LOGIC ---
                // Attempt to find an existing customer by their unique code.
                Optional<Customer> existingOpt = customerRepository.findByCustomerCode(customerCode);

                // If a customer is found, use that instance; otherwise, create a new one.
                Customer customer = existingOpt.orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setCustomerCode(customerCode);
                    return newCustomer;
                });

                // Update the entity's fields with data from the other columns.
                customer.setCustomerName(getCellValueAsString(row.getCell(1)));
                customer.setAddress(getCellValueAsString(row.getCell(2)));
                customer.setContactPhone(getCellValueAsString(row.getCell(3)));
                customer.setContactEmail(getCellValueAsString(row.getCell(4)));

                customersToSave.add(customer);
            }

            // Save all new or updated entities in a single batch operation for better performance.
            if (!customersToSave.isEmpty()) {
                customerRepository.saveAll(customersToSave);
            }
        }
    }

    /**
     * Safely reads a cell's value and returns it as a String, regardless of the cell type.
     * This prevents "Cannot get a STRING value from a NUMERIC cell" errors.
     *
     * @param cell The Excel cell to read from. Can be null.
     * @return The cell's content as a trimmed String. Returns an empty string if the cell is null.
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return dataFormatter.formatCellValue(cell).trim();
    }
}