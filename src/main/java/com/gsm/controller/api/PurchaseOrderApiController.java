// trong file: com/gsm/controller/api/PurchaseOrderApiController.java

package com.gsm.controller.api;

import com.gsm.dto.PurchaseOrderDto;
import com.gsm.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * REST Controller that provides a full suite of API endpoints for managing Purchase Orders.
 * This follows a RESTful design, with specific endpoints for actions and resources.
 */
@RestController
@RequestMapping("/api/purchase_orders")
public class PurchaseOrderApiController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    /**
     * API to get a list of all Purchase Orders.
     * @return A list of all POs.
     */
    @GetMapping
    public ResponseEntity<List<PurchaseOrderDto>> getAllPurchaseOrders() {
        return ResponseEntity.ok(purchaseOrderService.findAll());
    }

    /**
     * API to get the full details of a single Purchase Order by its ID.
     * @param id The ID of the PO.
     * @return The detailed PO DTO.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderDto> getPurchaseOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.findById(id));
    }

    /**
     * API to save (create a new or update an existing) Purchase Order.
     * @param poDto The PO data sent in the request body.
     * @return The saved PO DTO, or an error message.
     */
    @PostMapping
    public ResponseEntity<?> savePurchaseOrder(@RequestBody PurchaseOrderDto poDto) {
        try {
            return ResponseEntity.ok(purchaseOrderService.save(poDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePurchaseOrder(@PathVariable Long id) {
        try {
            purchaseOrderService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API for the business action of submitting a PO for approval.
     * Using a dedicated endpoint like `/submit` is a good RESTful practice for state transitions.
     * @param id The ID of the PO to submit.
     * @return A success or error message.
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submitPurchaseOrder(@PathVariable Long id) {
        try {
            purchaseOrderService.submitForApproval(id);
            return ResponseEntity.ok(Map.of("message", "PO submitted successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API to get all POs that are pending approval (status = "Submitted").
     * @return A list of pending POs.
     */
    @GetMapping("/pending")
    public ResponseEntity<List<PurchaseOrderDto>> getPendingPurchaseOrders() {
        return ResponseEntity.ok(purchaseOrderService.findPendingApproval());
    }

    /**
     * API for the business action of approving a PO.
     * @param id The ID of the PO to approve.
     * @return A success or error message.
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approvePurchaseOrder(@PathVariable Long id) {
        try {
            purchaseOrderService.approve(id);
            return ResponseEntity.ok(Map.of("message", "PO approved successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API for the business action of rejecting a PO.
     * @param id The ID of the PO to reject.
     * @return A success or error message.
     */
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