package com.gsm.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsm.dto.BOMTemplateDto;
import com.gsm.dto.OrderBOMDto;
import com.gsm.model.Supplier;
import com.gsm.model.Unit;
import com.gsm.repository.SupplierRepository;
import com.gsm.repository.UnitRepository;
import com.gsm.service.BOMTemplateService;
import com.gsm.service.OrderBOMService;
import com.gsm.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.gsm.repository.MaterialGroupRepository; // Thêm import

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/order-boms")
public class OrderBOMController {

    private final OrderBOMService orderBOMService;
    private final BOMTemplateService bomTemplateService;
    private final SupplierRepository supplierRepository;
    private final UnitRepository unitRepository;
    private final MaterialGroupRepository materialGroupRepository; // Thêm

    @Autowired
    public OrderBOMController(OrderBOMService orderBOMService, BOMTemplateService bomTemplateService, UnitRepository unitRepository, SupplierRepository supplierRepository, MaterialGroupRepository materialGroupRepository) {
        this.orderBOMService = orderBOMService;
        this.bomTemplateService = bomTemplateService;
        this.supplierRepository = supplierRepository;
        this.unitRepository = unitRepository;
        this.materialGroupRepository = materialGroupRepository;
    }

    // ===============================================
    // === ENDPOINT MỚI ĐỂ HIỂN THỊ DANH SÁCH BOM ===
    // ===============================================
    @GetMapping
    public String showOrderBomList(Model model) {
        List<OrderBOMDto> orderBOMs = orderBOMService.findAll();
        model.addAttribute("orderBOMs", orderBOMs);
        model.addAttribute("isBomPage", true); // Để highlight menu sidebar
        return "bom/order_bom_list";
    }

    @GetMapping("/form")
    public String showOrderBomForm(@RequestParam Long saleOrderId, Model model, HttpServletRequest request) throws JsonProcessingException { // Thêm throws
        OrderBOMDto orderBOM = orderBOMService.findOrCreateBySaleOrderId(saleOrderId);
        List<BOMTemplateDto> bomTemplates = bomTemplateService.findAll();

        // Lấy danh sách Unit và Supplier để truyền ra cho các dropdown
        List<Unit> allUnits = unitRepository.findAll();
        List<Supplier> allSuppliers = supplierRepository.findAll();
        List<Map<String, Object>> supplierOptions = new ArrayList<>();
        for (Supplier supplier : allSuppliers) {
            Map<String, Object> option = new HashMap<>();
            option.put("supplierId", supplier.getSupplierId());
            option.put("supplierName", supplier.getSupplierName());
            // Thêm currencyCode vào đây
            option.put("currencyCode", supplier.getCurrencyCode());
            supplierOptions.add(option);
        }

        // === THÊM MỚI: Chuyển đổi details thành JSON cho JavaScript ===
        ObjectMapper mapper = new ObjectMapper();
        String detailsJson = "[]"; // Mặc định là một mảng rỗng
        if (orderBOM.getDetails() != null && !orderBOM.getDetails().isEmpty()) {
            detailsJson = mapper.writeValueAsString(orderBOM.getDetails());
        }
        // ==========================================================

        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));
        model.addAttribute("orderBOM", orderBOM);
        model.addAttribute("bomTemplates", bomTemplates);
        model.addAttribute("units", allUnits); // Truyền danh sách Unit
        model.addAttribute("suppliers", supplierOptions);
        model.addAttribute("detailsJson", detailsJson); // TRUYỀN CHUỖI JSON RA VIEW
        model.addAttribute("isBomPage", true);
        model.addAttribute("materialGroups", materialGroupRepository.findAll()); // Thêm dòng này

        return "sale-order/order_bom_form";
    }


    @PostMapping("/save")
    public String saveOrderBom(@ModelAttribute OrderBOMDto orderBOMDto, RedirectAttributes redirectAttributes) {
        try {
            OrderBOMDto savedDto = orderBOMService.save(orderBOMDto);
            redirectAttributes.addFlashAttribute("successMessage", "Order BOM saved successfully!");
            return "redirect:/sale-orders/form?id=" + savedDto.getSaleOrderId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving Order BOM: " + e.getMessage());
            redirectAttributes.addFlashAttribute("orderBOM", orderBOMDto);
            return "redirect:/order-boms/form?saleOrderId=" + orderBOMDto.getSaleOrderId();
        }
    }

    @PostMapping("/generate-pos")
    @ResponseBody
    public ResponseEntity<?> generatePurchaseOrders(@ModelAttribute OrderBOMDto orderBOMDto) {
        try {
            // === SỬA LẠI: Gọi đến orderBOMService thay vì purchaseOrderService ===
            Map<String, Object> result = orderBOMService.saveAndGeneratePOs(orderBOMDto);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}