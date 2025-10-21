package com.gsm.service.impl;

import com.gsm.model.Customer;
import com.gsm.repository.CustomerRepository;
import com.gsm.service.CustomerService;
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
 * including parsing and saving data from Excel files.
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * DataFormatter is a utility from Apache POI to safely retrieve the formatted
     * string value of a cell, regardless of its underlying data type.
     */
    private final DataFormatter dataFormatter = new DataFormatter();

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Customer> parseExcelForPreview(InputStream inputStream) throws IOException {
        List<Customer> customers = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            // Iterate through each row, skipping the header (row 0)
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String customerCode = getCellValueAsString(row.getCell(0));
                if (customerCode.isEmpty()) continue; // Skip empty rows

                // Create a transient Customer object for preview purposes
                Customer customer = new Customer();
                customer.setCustomerCode(customerCode);
                customer.setCustomerName(getCellValueAsString(row.getCell(1)));
                customer.setAddress(getCellValueAsString(row.getCell(2)));
                customer.setContactPhone(getCellValueAsString(row.getCell(3)));
                customer.setContactEmail(getCellValueAsString(row.getCell(4)));
                customer.setDeliveryTerm(getCellValueAsString(row.getCell(5)));
                customer.setPaymentTerm(getCellValueAsString(row.getCell(6)));
                customer.setCurrencyCode(getCellValueAsString(row.getCell(7)));
                customer.setCountryCode(getCellValueAsString(row.getCell(8)));

                customers.add(customer);
            }
        }
        return customers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void saveAll(List<Customer> customers) {
        List<Customer> customersToSave = new ArrayList<>();
        for (Customer previewCustomer : customers) {
            // Find existing customer or create a new instance (upsert logic)
            Optional<Customer> existingOpt = customerRepository.findByCustomerCode(previewCustomer.getCustomerCode());
            Customer customer = existingOpt.orElseGet(() -> {
                Customer newCustomer = new Customer();
                newCustomer.setCustomerCode(previewCustomer.getCustomerCode());
                return newCustomer;
            });

            // Copy properties from the preview object to the persistent object
            customer.setCustomerName(previewCustomer.getCustomerName());
            customer.setAddress(previewCustomer.getAddress());
            customer.setContactPhone(previewCustomer.getContactPhone());
            customer.setContactEmail(previewCustomer.getContactEmail());
            customer.setDeliveryTerm(previewCustomer.getDeliveryTerm());
            customer.setPaymentTerm(previewCustomer.getPaymentTerm());
            customer.setCurrencyCode(previewCustomer.getCurrencyCode());
            customer.setCountryCode(previewCustomer.getCountryCode());

            customersToSave.add(customer);
        }

        if (!customersToSave.isEmpty()) {
            customerRepository.saveAll(customersToSave);
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