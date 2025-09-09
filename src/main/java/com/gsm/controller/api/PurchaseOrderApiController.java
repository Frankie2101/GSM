// trong file: com/gsm/controller/api/PurchaseOrderApiController.java

package com.gsm.controller.api;

import com.gsm.dto.PurchaseOrderDto;
import com.gsm.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/purchase_orders")
public class PurchaseOrderApiController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    // Lấy danh sách tất cả PO
    @GetMapping
    public ResponseEntity<List<PurchaseOrderDto>> getAllPurchaseOrders() {
        return ResponseEntity.ok(purchaseOrderService.findAll());
    }

    // THÊM MỚI: API để lấy thông tin chi tiết của 1 PO
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderDto> getPurchaseOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.findById(id));
    }

    // Lưu (tạo mới/cập nhật) PO
    @PostMapping
    public ResponseEntity<?> savePurchaseOrder(@RequestBody PurchaseOrderDto poDto) {
        try {
            return ResponseEntity.ok(purchaseOrderService.save(poDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Xóa PO
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePurchaseOrder(@PathVariable Long id) {
        try {
            purchaseOrderService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Trình duyệt PO
    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submitPurchaseOrder(@PathVariable Long id) {
        try {
            purchaseOrderService.submitForApproval(id);
            return ResponseEntity.ok(Map.of("message", "PO submitted successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Lấy danh sách PO đang chờ duyệt (status = Submitted)
    @GetMapping("/pending")
    public ResponseEntity<List<PurchaseOrderDto>> getPendingPurchaseOrders() {
        return ResponseEntity.ok(purchaseOrderService.findPendingApproval());
    }

    // Duyệt PO
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approvePurchaseOrder(@PathVariable Long id) {
        try {
            purchaseOrderService.approve(id);
            return ResponseEntity.ok(Map.of("message", "PO approved successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Từ chối PO
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectPurchaseOrder(@PathVariable Long id) {
        try {
            purchaseOrderService.reject(id);
            return ResponseEntity.ok(Map.of("message", "PO rejected successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}