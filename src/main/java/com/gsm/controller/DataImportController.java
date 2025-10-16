package com.gsm.controller;

import com.gsm.model.Customer;
import com.gsm.model.Supplier;
import com.gsm.model.Unit;
import com.gsm.service.CustomerService;
import com.gsm.service.SupplierService;
import com.gsm.service.UnitService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for handling data import operations via file uploads.
 * Provides endpoints for previewing and confirming data imports for master data.
 * Access to these endpoints requires appropriate permissions.
 */
@RestController
@RequestMapping("/api/import")
public class DataImportController {

    private final CustomerService customerService;
    private final SupplierService supplierService;
    private final UnitService unitService;

    public DataImportController(CustomerService customerService, SupplierService supplierService, UnitService unitService) {
        this.customerService = customerService;
        this.supplierService = supplierService;
        this.unitService = unitService;
    }

    // ========== CUSTOMER ENDPOINTS ==========

    /**
     * API endpoint to parse a Customer Excel file and return data for preview.
     * Access is restricted to users with ROLE_Admin or MASTER_DATA_EDIT authority.
     *
     * @param file The uploaded .xlsx file.
     * @param session The current HTTP session to store preview data.
     * @return A ResponseEntity containing a PreviewResponse with a cache key and parsed data.
     */
    @PostMapping("/preview/customers")
    @PreAuthorize("hasAuthority('ROLE_Admin') or hasAuthority('MASTER_DATA_EDIT')")
    public ResponseEntity<?> previewCustomers(@RequestParam("file") MultipartFile file, HttpSession session) {
        return handlePreviewRequest(file, session, "customer_preview", customerService::parseExcelForPreview);
    }

    /**
     * API endpoint to confirm and save the previewed Customer data from the session.
     * Access is restricted to users with ROLE_Admin or MASTER_DATA_EDIT authority.
     *
     * @param request The request body containing the cache key.
     * @param session The current HTTP session.
     * @return A ResponseEntity with a success or error message.
     */
    @PostMapping("/confirm/customers")
    @PreAuthorize("hasAuthority('ROLE_Admin') or hasAuthority('MASTER_DATA_EDIT')")
    public ResponseEntity<String> confirmCustomers(@RequestBody ConfirmRequest request, HttpSession session) {
        return handleConfirmRequest(request, session, customerService::saveAll);
    }

    // ========== SUPPLIER ENDPOINTS ==========

    /**
     * API endpoint to parse a Supplier Excel file for preview.
     * Access is restricted by MASTER_DATA_EDIT authority.
     */
    @PostMapping("/preview/suppliers")
    @PreAuthorize("hasAuthority('ROLE_Admin') or hasAuthority('MASTER_DATA_EDIT')")
    public ResponseEntity<?> previewSuppliers(@RequestParam("file") MultipartFile file, HttpSession session) {
        return handlePreviewRequest(file, session, "supplier_preview", supplierService::parseExcelForPreview);
    }

    /**
     * API endpoint to confirm and save previewed Supplier data.
     * Access is restricted by MASTER_DATA_EDIT authority.
     */
    @PostMapping("/confirm/suppliers")
    @PreAuthorize("hasAuthority('ROLE_Admin') or hasAuthority('MASTER_DATA_EDIT')")
    public ResponseEntity<String> confirmSuppliers(@RequestBody ConfirmRequest request, HttpSession session) {
        return handleConfirmRequest(request, session, supplierService::saveAll);
    }

    // ========== UNIT ENDPOINTS ==========

    /**
     * API endpoint to parse a Unit Excel file for preview.
     * Access is restricted by MASTER_DATA_EDIT authority.
     */
    @PostMapping("/preview/units")
    @PreAuthorize("hasAuthority('ROLE_Admin') or hasAuthority('MASTER_DATA_EDIT')")
    public ResponseEntity<?> previewUnits(@RequestParam("file") MultipartFile file, HttpSession session) {
        return handlePreviewRequest(file, session, "unit_preview", unitService::parseExcelForPreview);
    }

    /**
     * API endpoint to confirm and save previewed Unit data.
     * Access is restricted by MASTER_DATA_EDIT authority.
     */
    @PostMapping("/confirm/units")
    @PreAuthorize("hasAuthority('ROLE_Admin') or hasAuthority('MASTER_DATA_EDIT')")
    public ResponseEntity<String> confirmUnits(@RequestBody ConfirmRequest request, HttpSession session) {
        return handleConfirmRequest(request, session, unitService::saveAll);
    }


    // ========== GENERIC HELPER METHODS TO AVOID CODE DUPLICATION ==========

    private <T> ResponseEntity<?> handlePreviewRequest(MultipartFile file, HttpSession session, String prefix, ExcelParser<T> parser) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload.");
        }
        try {
            List<T> data = parser.parse(file.getInputStream());
            if (data.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty or does not contain valid data.");
            }
            String cacheKey = prefix + "_" + UUID.randomUUID().toString();
            session.setAttribute(cacheKey, data);
            return ResponseEntity.ok(new PreviewResponse<>(cacheKey, data));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error parsing file: " + e.getMessage());
        }
    }

    private <T> ResponseEntity<String> handleConfirmRequest(ConfirmRequest request, HttpSession session, DataSaver<T> saver) {
        try {
            @SuppressWarnings("unchecked")
            List<T> data = (List<T>) session.getAttribute(request.getCacheKey());
            if (data == null || data.isEmpty()) {
                return ResponseEntity.badRequest().body("No data to import or session has expired.");
            }
            saver.save(data);
            session.removeAttribute(request.getCacheKey());
            return ResponseEntity.ok("Data imported successfully.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error saving data: " + e.getMessage());
        }
    }

    // ========== FUNCTIONAL INTERFACES FOR HELPER METHODS ==========
    @FunctionalInterface
    interface ExcelParser<T> {
        List<T> parse(InputStream is) throws IOException;
    }

    @FunctionalInterface
    interface DataSaver<T> {
        void save(List<T> data);
    }


    // ========== DTO (DATA TRANSFER OBJECT) CLASSES ==========

    /**
     * DTO for sending preview data back to the client.
     */
    @Getter
    @AllArgsConstructor
    public static class PreviewResponse<T> {
        private String cacheKey;
        private List<T> data;
    }

    /**
     * DTO for receiving the confirmation request from the client.
     */
    @Getter
    @Setter
    public static class ConfirmRequest {
        private String cacheKey;
    }
}