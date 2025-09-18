package com.gsm.controller;

import com.gsm.dto.TrimDto;
import com.gsm.exception.DuplicateResourceException;
import com.gsm.model.Supplier;
import com.gsm.model.Unit;
import com.gsm.repository.SupplierRepository;
import com.gsm.repository.UnitRepository;
import com.gsm.service.TrimService;
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

/**
 * Controller for handling all HTTP requests for the Trim management feature.
 */
@Controller
@RequestMapping("/trims")
public class TrimController {

    @Autowired private TrimService trimService;
    @Autowired private UnitRepository unitRepository;
    @Autowired private SupplierRepository supplierRepository;

    /**
     * Displays the list of all trims, with an optional keyword search.
     * <p><b>Use Case:</b> The main landing page for trim management.
     */
    @GetMapping
    public String showTrimList(@RequestParam(required = false) String keyword, Model model, HttpServletRequest request) {
        List<TrimDto> trims;
        if (keyword != null && !keyword.isEmpty()) {
            trims = trimService.search(keyword);
        } else {
            trims = trimService.findAll();
        }
        model.addAttribute("trims", trims);
        model.addAttribute("isTrimPage", true);
        model.addAttribute("keyword", keyword);
        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));
        return "trim/trim_list";
    }

    /**
     * Displays the form for creating a new trim or editing an existing one.
     * <p><b>Use Case:</b> Called when a user clicks "Create" or "Edit".
     */
    @GetMapping("/form")
    public String showTrimForm(@RequestParam(required = false) Long id, Model model, HttpServletRequest request) {
        TrimDto trim;
        if (id != null) {
            trim = trimService.findById(id);
        } else {
            trim = new TrimDto();
            trim.setVariants(new ArrayList<>()); // Khởi tạo list rỗng
        }

        // Logic for preparing dropdown data...
        List<Unit> allUnits = unitRepository.findAll();
        List<Map<String, Object>> unitOptions = new ArrayList<>();
        for (Unit unit : allUnits) {
            Map<String, Object> option = new HashMap<>();
            option.put("id", unit.getUnitId());
            option.put("name", unit.getUnitName());
            if (trim.getUnitId() != null && trim.getUnitId().equals(unit.getUnitId())) {
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
            if (trim.getSupplierId() != null && trim.getSupplierId().equals(supplier.getSupplierId())) {
                option.put("selected", true);
            }
            supplierOptions.add(option);
        }

        model.addAttribute("trim", trim);
        model.addAttribute("isTrimPage", true);
        model.addAttribute("units", unitOptions);
        model.addAttribute("suppliers", supplierOptions);
        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));

        return "trim/trim_form";
    }

    /**
     * Processes the submission of the trim form.
     */
    @PostMapping("/save")
    public String saveTrim(@ModelAttribute TrimDto trimDto, RedirectAttributes redirectAttributes) {
        try {
            TrimDto savedTrim = trimService.save(trimDto);
            redirectAttributes.addFlashAttribute("successMessage", "Saved Successfully!");
            return "redirect:/trims/form?id=" + savedTrim.getTrimId();
        } catch (DuplicateResourceException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("trim", trimDto);
            if (trimDto.getTrimId() == null) {
                return "redirect:/trims/form";
            }
            return "redirect:/trims/form?id=" + trimDto.getTrimId();
        }
    }

    /**
     * Deletes one or more trims based on a list of selected IDs.
     * <p><b>Use Case:</b> Called when the user clicks "Delete" on the list page.
     */
    @PostMapping("/delete")
    public String deleteTrims(@RequestParam(value = "selectedIds", required = false) List<Long> ids, RedirectAttributes redirectAttributes) {
        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select at least one trim to delete.");
        } else {
            trimService.deleteByIds(ids);
            redirectAttributes.addFlashAttribute("successMessage", "Successfully deleted " + ids.size() + " trim(s).");
        }
        return "redirect:/trims";
    }
}
