package com.gsm.controller.api;

import com.gsm.dto.OrderBOMDetailDto;
import com.gsm.dto.OrderBOMDto;
import com.gsm.service.OrderBOMService;
import com.gsm.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for handling API requests related to the Order BOM.
 */
@RestController
@RequestMapping("/api/order-boms")
public class OrderBOMApiController {

    private final OrderBOMService orderBOMService;
    private final PurchaseOrderService purchaseOrderService;

    @Autowired
    public OrderBOMApiController(OrderBOMService orderBOMService , PurchaseOrderService purchaseOrderService) {
        this.orderBOMService = orderBOMService;
        this.purchaseOrderService = purchaseOrderService;
    }

    /**
     * API endpoint to generate a preview of BOM details from a selected template.
     * This is called by JavaScript when a user selects a BOM Template to populate the form.
     * @param saleOrderId The ID of the current Sale Order.
     * @param bomTemplateId The ID of the selected BOM Template.
     * @return An OrderBOMDto containing the preview data in JSON format.
     */
    @GetMapping("/generate-preview")
    public ResponseEntity<OrderBOMDto> generateBomPreview(
            @RequestParam Long saleOrderId,
            @RequestParam Long bomTemplateId) {

        OrderBOMDto previewDto = orderBOMService.generatePreviewFromTemplate(saleOrderId, bomTemplateId);
        return ResponseEntity.ok(previewDto);
    }

    /**
     * API endpoint to save the Order BOM and generate Purchase Orders from it.
     * @param bomDtoFromForm The DTO containing the complete BOM data from the form.
     * @return A success message on completion, or an error message on failure.
     */
    @PostMapping("/generate-pos")
    public ResponseEntity<?> generatePurchaseOrders(@RequestBody OrderBOMDto bomDtoFromForm) {
        try {
            OrderBOMDto savedBomDto = orderBOMService.save(bomDtoFromForm);
            Map<Integer, Double> purchaseQtyMap = bomDtoFromForm.getDetails().stream()
                    .collect(Collectors.toMap(OrderBOMDetailDto::getSeq, OrderBOMDetailDto::getPurchaseQty));
            savedBomDto.getDetails().forEach(detail -> detail.setPurchaseQty(purchaseQtyMap.get(detail.getSeq())));
            Map<String, Object> result = purchaseOrderService.generatePOsFromOrderBOM(savedBomDto);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}