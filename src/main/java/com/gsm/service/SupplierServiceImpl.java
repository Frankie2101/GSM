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
    public List<Supplier> parseExcelForPreview(InputStream inputStream) throws IOException {
        List<Supplier> suppliers = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                String supplierCode = getCellValueAsString(row.getCell(0));
                if (supplierCode.isEmpty()) continue; // Skip empty rows

                // Create a transient Supplier object just for preview
                Supplier supplier = new Supplier();
                supplier.setSupplierCode(supplierCode);
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
                } catch (NumberFormatException ignored) {}
                supplier.setCountryCode(getCellValueAsString(row.getCell(9)));

                suppliers.add(supplier);
            }
        }
        return suppliers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void saveAll(List<Supplier> suppliers) {
        List<Supplier> suppliersToSave = new ArrayList<>();
        for (Supplier previewSupplier : suppliers) {
            // Find existing supplier by code or create a new one
            Optional<Supplier> existingOpt = supplierRepository.findBySupplierCode(previewSupplier.getSupplierCode());
            Supplier supplier = existingOpt.orElseGet(() -> {
                Supplier newSupplier = new Supplier();
                newSupplier.setSupplierCode(previewSupplier.getSupplierCode());
                return newSupplier;
            });

            // Map properties from the preview object to the persistent object
            supplier.setSupplierName(previewSupplier.getSupplierName());
            supplier.setAddress(previewSupplier.getAddress());
            supplier.setContactPhone(previewSupplier.getContactPhone());
            supplier.setContactEmail(previewSupplier.getContactEmail());
            supplier.setDeliveryTerm(previewSupplier.getDeliveryTerm());
            supplier.setPaymentTerm(previewSupplier.getPaymentTerm());
            supplier.setCurrencyCode(previewSupplier.getCurrencyCode());
            supplier.setTaxRate(previewSupplier.getTaxRate());
            supplier.setCountryCode(previewSupplier.getCountryCode());

            suppliersToSave.add(supplier);
        }
        if (!suppliersToSave.isEmpty()) {
            supplierRepository.saveAll(suppliersToSave);
        }
    }

    /**
     * Safely reads a cell's value and returns it as a String, regardless of the cell type.
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