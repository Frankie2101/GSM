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

@RestController
@RequestMapping("/api/production-outputs") // Đường dẫn gốc cho tất cả API của chức năng này
public class ProductionOutputApiController {

    @Autowired
    private ProductionOutputService productionOutputService;

    /**
     * API để tìm kiếm và lấy danh sách sản lượng.
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
     * API để lấy chi tiết một sản lượng theo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductionOutputDto> findById(@PathVariable Long id) {
        ProductionOutputDto dto = productionOutputService.findById(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * API để lưu (tạo mới hoặc cập nhật) một sản lượng.
     */
    @PostMapping("/save")
    public ResponseEntity<?> saveProductionOutput(@RequestBody ProductionOutputDto dto) {
        try {
            ProductionOutputDto savedDto = productionOutputService.save(dto);
            return ResponseEntity.ok(savedDto);
        } catch (Exception e) {
            // Trả về lỗi 400 Bad Request kèm thông báo lỗi
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * API để xóa nhiều sản lượng.
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