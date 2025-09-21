package com.gsm.controller;

import com.gsm.dto.FabricDto;
import com.gsm.exception.DuplicateResourceException;
import com.gsm.model.MaterialGroup;
import com.gsm.model.Supplier;
import com.gsm.model.Unit;
import com.gsm.repository.SupplierRepository;
import com.gsm.repository.UnitRepository;
import com.gsm.service.FabricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.gsm.model.MaterialGroup;
import com.gsm.repository.MaterialGroupRepository;

/**
 * Controller for handling all HTTP requests for the Fabric management feature.
 * It is responsible for displaying views and processing user form submissions.
 */
@Controller
@RequestMapping("/fabrics")
public class FabricController {

    @Autowired private FabricService fabricService;
    @Autowired private UnitRepository unitRepository;
    @Autowired private SupplierRepository supplierRepository;
    @Autowired private MaterialGroupRepository materialGroupRepository;

    /**
     * Displays the list of all fabrics, with an optional keyword search.
     * <p><b>Use Case:</b> The main landing page for fabric management.
     *
     * @param keyword Optional search term.
     * @param model   The Spring Model for passing data to the view.
     * @param request The HttpServletRequest for retrieving the CSRF token.
     * @return The path to the fabric list view.
     */
    @GetMapping
    public String showFabricList(@RequestParam(required = false) String keyword, Model model, HttpServletRequest request) {
        List<FabricDto> fabrics;
        if (keyword != null && !keyword.isEmpty()) {
            fabrics = fabricService.search(keyword);
        } else {
            fabrics = fabricService.findAll();
        }
        model.addAttribute("fabrics", fabrics);
        model.addAttribute("isFabricPage", true);
        model.addAttribute("keyword", keyword);
        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));
        return "fabric/fabric_list";
    }

    /**
     * Displays the form for creating a new fabric or editing an existing one.
     * <p><b>Use Case:</b> Called when a user clicks "Create" or "Edit".
     *
     * @param id      The ID of the fabric to edit (null for creation).
     * @param model   The Spring Model.
     * @param request The HttpServletRequest.
     * @return The path to the fabric form view.
     */
    @GetMapping("/form")
    public String showFabricForm(@RequestParam(required = false) Long id, Model model, HttpServletRequest request) {
        FabricDto fabric;
        if (id != null) {
            fabric = fabricService.findById(id);
        } else {
            fabric = new FabricDto();
            fabric.setFabricColors(new ArrayList<>());
        }

        List<Unit> allUnits = unitRepository.findAll();
        List<Map<String, Object>> unitOptions = new ArrayList<>();
        for (Unit unit : allUnits) {
            Map<String, Object> option = new HashMap<>();
            option.put("id", unit.getUnitId());
            option.put("name", unit.getUnitName());
            if (fabric.getUnitId() != null && fabric.getUnitId().equals(unit.getUnitId())) {
                option.put("selected", true);
            }
            unitOptions.add(option);
        }

        List<Supplier> allSuppliers = supplierRepository.findAll();
        List<Map<String, Object>> supplierOptions = new ArrayList<>();
        for (Supplier supplier : allSuppliers) {
            Map<String, Object> option = new HashMap<>();
            option.put("id", supplier.getSupplierId());
            option.put("name", supplier.getSupplierName());
            if (fabric.getSupplierId() != null && fabric.getSupplierId().equals(supplier.getSupplierId())) {
                option.put("selected", true);
            }
            supplierOptions.add(option);
        }

        List<MaterialGroup> allGroups = materialGroupRepository.findByMaterialType("FA");
        List<Map<String, Object>> groupOptions = new ArrayList<>();
        for (MaterialGroup group : allGroups) {
            Map<String, Object> option = new HashMap<>();
            option.put("id", group.getMaterialGroupId());
            option.put("name", group.getMaterialGroupName());
            if (fabric.getMaterialGroupId() != null && fabric.getMaterialGroupId().equals(group.getMaterialGroupId())) {
                option.put("selected", true);
            }
            groupOptions.add(option);
        }

        model.addAttribute("fabric", fabric);
        model.addAttribute("isFabricPage", true);
        model.addAttribute("units", unitOptions);
        model.addAttribute("suppliers", supplierOptions);
        model.addAttribute("materialGroups", groupOptions);
        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));

        return "fabric/fabric_form";
    }

    /**
     * Processes the submission of the fabric form.
     * <p><b>Use Case:</b> Called via POST when the user clicks "Save" on the form.
     *
     * @param fabricDto          The DTO populated with form data.
     * @param redirectAttributes For passing flash messages after a redirect.
     * @return A redirect string to the appropriate page.
     */
    @PostMapping("/save")
    public String saveFabric(@ModelAttribute FabricDto fabricDto, RedirectAttributes redirectAttributes) {
        try {
            FabricDto savedFabric = fabricService.save(fabricDto);
            redirectAttributes.addFlashAttribute("successMessage", "Saved Successfully!");
            return "redirect:/fabrics/form?id=" + savedFabric.getFabricId();
        } catch (DuplicateResourceException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("fabric", fabricDto);
            if (fabricDto.getFabricId() == null) {
                return "redirect:/fabrics/form";
            }
            return "redirect:/fabrics/form?id=" + fabricDto.getFabricId();
        }
    }

    /**
     * Deletes one or more fabrics based on a list of selected IDs.
     * <p><b>Use Case:</b> Called when the user clicks "Delete" on the list page.
     *
     * @param ids                The list of fabric IDs to delete.
     * @param redirectAttributes For passing flash messages.
     * @return A redirect string back to the fabric list.
     */
    @PostMapping("/delete")
    public String deleteFabrics(@RequestParam(value = "selectedIds", required = false) List<Long> ids, RedirectAttributes redirectAttributes) {
        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select at least one fabric to delete.");
        } else {
            fabricService.deleteByIds(ids);
            redirectAttributes.addFlashAttribute("successMessage", "Successfully deleted " + ids.size() + " fabric(s).");
        }
        return "redirect:/fabrics";
    }
}
