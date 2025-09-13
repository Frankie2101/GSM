package com.gsm.controller.api;

import com.gsm.dto.OrderBOMDto;
import com.gsm.service.OrderBOMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/order-boms")
public class OrderBOMApiController {

    private final OrderBOMService orderBOMService;

    @Autowired
    public OrderBOMApiController(OrderBOMService orderBOMService) {
        this.orderBOMService = orderBOMService;
    }

    /**
     * API endpoint để tải dữ liệu chi tiết BOM từ một template.
     * Được gọi bởi JavaScript khi người dùng chọn một BOM Template.
     * @param saleOrderId ID của Sale Order hiện tại.
     * @param bomTemplateId ID của BOM Template được chọn.
     * @return Dữ liệu OrderBOMDto dưới dạng JSON.
     */
    @GetMapping("/generate-preview")
    public ResponseEntity<OrderBOMDto> generateBomPreview(
            @RequestParam Long saleOrderId,
            @RequestParam Long bomTemplateId) {

        OrderBOMDto previewDto = orderBOMService.generatePreviewFromTemplate(saleOrderId, bomTemplateId);
        return ResponseEntity.ok(previewDto);
    }

    @PostMapping("/generate-pos")
    public ResponseEntity<?> generatePurchaseOrders(@ModelAttribute OrderBOMDto orderBOMDto) {
        try {
            Map<String, Object> result = orderBOMService.saveAndGeneratePOs(orderBOMDto);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // Trả về một đối tượng JSON có chứa thông báo lỗi
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}