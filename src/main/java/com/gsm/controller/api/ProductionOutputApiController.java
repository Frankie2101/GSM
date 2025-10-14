// In file: src/main/java/com/gsm/controller/api/ProductionOutputApiController.java

package com.gsm.controller.api;

import com.gsm.dto.ProductionOutputDto;
import com.gsm.service.ProductionOutputService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // MAKE SURE THIS IMPORT EXISTS
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST controller for handling all API requests related to Production Outputs.
 */
@RestController
@RequestMapping("/api/production-outputs")
public class ProductionOutputApiController {

    @Autowired
    private ProductionOutputService productionOutputService;

    /**
     * API endpoint to search for production outputs.
     * Requires PRODUCTION_OUTPUT_VIEW permission.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_Admin') or hasAuthority('PRODUCTION_OUTPUT_VIEW')")
    public ResponseEntity<List<ProductionOutputDto>> searchOutputs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate outputDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate outputDateTo,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String productionLine) {
        List<ProductionOutputDto> outputs = productionOutputService.search(keyword, outputDateFrom, outputDateTo, department, productionLine);
        return ResponseEntity.ok(outputs);
    }

    /**
     * API endpoint to retrieve a single production output.
     * Requires PRODUCTION_OUTPUT_VIEW permission.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_Admin') or hasAuthority('PRODUCTION_OUTPUT_VIEW')")
    public ResponseEntity<ProductionOutputDto> findById(@PathVariable Long id) {
        ProductionOutputDto dto = productionOutputService.findById(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * API endpoint to save a production output record.
     * Requires PRODUCTION_OUTPUT_CREATE_EDIT permission.
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('ROLE_Admin') or hasAuthority('PRODUCTION_OUTPUT_CREATE_EDIT')")
    public ResponseEntity<?> saveProductionOutput(@RequestBody ProductionOutputDto dto) {
        try {
            ProductionOutputDto savedDto = productionOutputService.save(dto);
            return ResponseEntity.ok(savedDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * API endpoint for bulk deletion of production outputs.
     * Requires PRODUCTION_OUTPUT_DELETE permission.
     */
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('ROLE_Admin') or hasAuthority('PRODUCTION_OUTPUT_DELETE')")
    public ResponseEntity<?> deleteProductionOutputs(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Please select at least one item to delete."));
        }
        try {
            productionOutputService.deleteByIds(ids);
            return ResponseEntity.ok(Map.of("message", "Successfully deleted " + ids.size() + " item(s)."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "An error occurred during deletion."));
        }
    }
}