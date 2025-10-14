package com.gsm.controller;

import com.gsm.service.CustomerService;
import com.gsm.service.SupplierService;
import com.gsm.service.UnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller for handling data import operations.
 * This MUST be annotated with @RestController for Spring to recognize it as an API endpoint provider.
 */
@RestController
@RequestMapping("/api/import")
@PreAuthorize("hasAuthority('ROLE_Admin') or hasAuthority('MASTER_DATA_EDIT')")
public class DataImportController {

    private final CustomerService customerService;
    private final SupplierService supplierService;
    private final UnitService unitService;

    @Autowired
    public DataImportController(CustomerService customerService, SupplierService supplierService, UnitService unitService) {
        this.customerService = customerService;
        this.supplierService = supplierService;
        this.unitService = unitService;
    }

    /**
     * Handles the file upload for importing customer data.
     * Mapped to POST /api/import/customers
     */
    @PostMapping("/customers")
    public ResponseEntity<String> importCustomers(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload.");
        }
        try {
            customerService.importFromExcel(file.getInputStream());
            return ResponseEntity.ok("Successfully imported customer data.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to import data: " + e.getMessage());
        }
    }

    /**
     * Handles the file upload for importing supplier data.
     * Mapped to POST /api/import/suppliers
     */
    @PostMapping("/suppliers")
    public ResponseEntity<String> importSuppliers(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload.");
        }
        try {
            supplierService.importFromExcel(file.getInputStream());
            return ResponseEntity.ok("Successfully imported supplier data.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to import data: " + e.getMessage());
        }
    }

    /**
     * Handles the file upload for importing unit data.
     * Mapped to POST /api/import/units
     */
    @PostMapping("/units")
    public ResponseEntity<String> importUnits(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload.");
        }
        try {
            unitService.importFromExcel(file.getInputStream());
            return ResponseEntity.ok("Successfully imported unit data.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to import data: " + e.getMessage());
        }
    }
}