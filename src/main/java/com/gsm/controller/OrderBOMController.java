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
import com.gsm.repository.MaterialGroupRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles all web requests related to the Order Bill of Materials (BOM).
 * This includes displaying the BOM list, showing the creation/edit form, and saving data.
 */
@Controller
@RequestMapping("/order-boms")
public class OrderBOMController {

    private final OrderBOMService orderBOMService;
    private final BOMTemplateService bomTemplateService;
    private final SupplierRepository supplierRepository;
    private final UnitRepository unitRepository;
    private final MaterialGroupRepository materialGroupRepository; // Thêm

    /**
     * Constructor for dependency injection of required services and repositories.
     */
    @Autowired
    public OrderBOMController(OrderBOMService orderBOMService, BOMTemplateService bomTemplateService, UnitRepository unitRepository, SupplierRepository supplierRepository, MaterialGroupRepository materialGroupRepository) {
        this.orderBOMService = orderBOMService;
        this.bomTemplateService = bomTemplateService;
        this.supplierRepository = supplierRepository;
        this.unitRepository = unitRepository;
        this.materialGroupRepository = materialGroupRepository;
    }

    /**
     * Displays the list of all Order BOMs.
     * @param model The Spring Model to pass data to the view.
     * @return The name of the Order BOM list view template.
     */
    @GetMapping
    public String showOrderBomList(Model model) {
        List<OrderBOMDto> orderBOMs = orderBOMService.findAll();
        model.addAttribute("orderBOMs", orderBOMs);
        model.addAttribute("isBomPage", true);
        return "bom/order_bom_list";
    }

    /**
     * Displays the Order BOM form for a specific Sale Order.
     * It will find an existing BOM or create a new one if it doesn't exist.
     * It also prepares all necessary data for dropdowns and serializes details to JSON for the frontend.
     * @param saleOrderId The ID of the Sale Order to create/edit the BOM for.
     * @param model The Spring Model to pass data to the view.
     * @param request The HttpServletRequest to access the CSRF token.
     * @return The name of the Order BOM form view template.
     * @throws JsonProcessingException If there is an error serializing the details list to JSON.
     */
    @GetMapping("/form")
    public String showOrderBomForm(@RequestParam Long saleOrderId, Model model, HttpServletRequest request) throws JsonProcessingException { // Thêm throws
        OrderBOMDto orderBOM = orderBOMService.findOrCreateBySaleOrderId(saleOrderId);
        List<BOMTemplateDto> bomTemplates = bomTemplateService.findAll();

        List<Unit> allUnits = unitRepository.findAll();
        List<Supplier> allSuppliers = supplierRepository.findAll();
        List<Map<String, Object>> supplierOptions = new ArrayList<>();
        List<Map<String, Object>> templateOptions = new ArrayList<>();
        Long selectedTemplateId = orderBOM.getBomTemplateId();
        for (Supplier supplier : allSuppliers) {
            Map<String, Object> option = new HashMap<>();
            option.put("supplierId", supplier.getSupplierId());
            option.put("supplierName", supplier.getSupplierName());
            option.put("currencyCode", supplier.getCurrencyCode());
            supplierOptions.add(option);
        }

        for (BOMTemplateDto template : bomTemplates) {
            Map<String, Object> option = new HashMap<>();
            option.put("bomTemplateId", template.getBomTemplateId());
            option.put("bomTemplateName", template.getBomTemplateName());

            if (selectedTemplateId != null && selectedTemplateId.equals(template.getBomTemplateId())) {
                option.put("selected", true);
            }
            templateOptions.add(option);
        }

        ObjectMapper mapper = new ObjectMapper();
        String detailsJson = "[]";
        if (orderBOM.getDetails() != null && !orderBOM.getDetails().isEmpty()) {
            detailsJson = mapper.writeValueAsString(orderBOM.getDetails());
        }

        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));
        model.addAttribute("orderBOM", orderBOM);
        model.addAttribute("bomTemplates", templateOptions);
        model.addAttribute("units", allUnits);
        model.addAttribute("suppliers", supplierOptions);
        model.addAttribute("detailsJson", detailsJson);
        model.addAttribute("isBomPage", true);
        model.addAttribute("materialGroups", materialGroupRepository.findAll());

        return "sale-order/order_bom_form";
    }


    /**
     * Handles the submission of the Order BOM form.
     * @param orderBOMDto The DTO containing the form data.
     * @param redirectAttributes Used to pass success or error messages after a redirect.
     * @return A redirect command to the Sale Order form.
     */
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
}