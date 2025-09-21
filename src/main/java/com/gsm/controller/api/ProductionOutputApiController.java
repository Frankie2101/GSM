package com.gsm.controller.api;

import com.gsm.dto.ProductionOutputDto;
import com.gsm.service.ProductionOutputService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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
     * API endpoint to search for and retrieve a list of production outputs based on various criteria.
     * @param keyword General search term.
     * @param outputDateFrom Start date for the search range.
     * @param outputDateTo End date for the search range.
     * @param department Department to filter by.
     * @param productionLine Production line to filter by.
     * @return A ResponseEntity containing a list of ProductionOutputDto.
     */
    @GetMapping
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
     * API endpoint to retrieve the details of a single production output by its ID.
     * @param id The ID of the production output.
     * @return A ResponseEntity containing the ProductionOutputDto.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductionOutputDto> findById(@PathVariable Long id) {
        ProductionOutputDto dto = productionOutputService.findById(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * API endpoint to save (create or update) a production output record.
     * @param dto The DTO containing the production output data.
     * @return The saved DTO on success, or a JSON error message on failure.
     */
    @PostMapping("/save")
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
     * @param ids A list of IDs to be deleted.
     * @return A success or error message in a JSON object.
     */
    @PostMapping("/delete")
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