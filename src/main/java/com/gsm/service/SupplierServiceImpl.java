package com.gsm.service;

import com.gsm.model.Supplier;
import com.gsm.repository.SupplierRepository;
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
 * The concrete implementation of the {@link SupplierService} interface.
 */
@Service
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final DataFormatter dataFormatter = new DataFormatter();

    @Autowired
    public SupplierServiceImpl(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void importFromExcel(InputStream inputStream) throws IOException {
        List<Supplier> suppliersToSave = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String supplierCode = getCellValueAsString(row.getCell(0));
                if (supplierCode.isEmpty()) continue;

                Optional<Supplier> existingOpt = supplierRepository.findBySupplierCode(supplierCode);
                Supplier supplier = existingOpt.orElseGet(() -> {
                    Supplier newSupplier = new Supplier();
                    newSupplier.setSupplierCode(supplierCode);
                    return newSupplier;
                });

                supplier.setSupplierName(getCellValueAsString(row.getCell(1)));
                supplier.setAddress(getCellValueAsString(row.getCell(2)));
                supplier.setContactPhone(getCellValueAsString(row.getCell(3)));
                supplier.setContactEmail(getCellValueAsString(row.getCell(4)));
                supplier.setDeliveryTerm(getCellValueAsString(row.getCell(5)));
                supplier.setPaymentTerm(getCellValueAsString(row.getCell(6)));
                supplier.setCurrencyCode(getCellValueAsString(row.getCell(7)));

                try {
                    String taxRateStr = getCellValueAsString(row.getCell(8));
                    if (!taxRateStr.isEmpty()) {
                        supplier.setTaxRate(Double.parseDouble(taxRateStr));
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Invalid TaxRate format for supplier code: " + supplierCode);
                }

                supplier.setCountryCode(getCellValueAsString(row.getCell(9)));

                suppliersToSave.add(supplier);
            }
            if (!suppliersToSave.isEmpty()) {
                supplierRepository.saveAll(suppliersToSave);
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